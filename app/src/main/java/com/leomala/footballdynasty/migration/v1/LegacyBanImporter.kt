package com.leomala.footballdynasty.migration.v1

import com.leomala.footballdynasty.data.LegacyDataGateway
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.ImportScopeCounts
import com.leomala.footballdynasty.data.local.RoomV1DataStore
import com.leomala.footballdynasty.data.local.entity.LegacyImportManifestEntity
import com.leomala.footballdynasty.data.local.entity.LegacyImportStateEntity
import com.leomala.footballdynasty.foundation.error.DuplicateStableIdentityException
import com.leomala.footballdynasty.foundation.error.IntegrityMismatchException
import com.leomala.footballdynasty.foundation.error.LegacyFormatException
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.concurrent.CancellationException
import java.util.logging.Level
import java.util.logging.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

const val LEGACY_BAN_IMPORT_SCOPE: String = "legacy-ban-corpus-v1"
const val LEGACY_BAN_ADAPTER_VERSION: Int = 1

class LegacyBanImporter(
    private val database: FootballDynastyDatabase,
    private val gateway: LegacyDataGateway = LegacyDataGateway(),
    private val adapter: LegacyBanToV1Adapter = LegacyBanToV1Adapter(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val logger: LegacyImportLogger = JulLegacyImportLogger,
    private val nanoTime: () -> Long = System::nanoTime,
    private val clockMillis: () -> Long = System::currentTimeMillis,
) {
    private val store = RoomV1DataStore(database)
    private val writeMutex = Mutex()

    suspend fun import(
        sources: List<LegacyBanSource>,
        scope: String = LEGACY_BAN_IMPORT_SCOPE,
    ): LegacyBanImportReport = writeMutex.withLock {
        importLocked(sources, scope)
    }

    suspend fun status(scope: String = LEGACY_BAN_IMPORT_SCOPE): LegacyImportStatus {
        val stored = database.legacyImportDao().state(scope) ?: return LegacyImportStatus.NOT_IMPORTED
        return try {
            LegacyImportStatus.valueOf(stored.status)
        } catch (error: IllegalArgumentException) {
            throw IntegrityMismatchException("Unknown import status '${stored.status}' for scope $scope")
        }
    }

    suspend fun verify(scope: String = LEGACY_BAN_IMPORT_SCOPE): LegacyBanImportReport {
        val manifest = database.legacyImportDao().manifest(scope)
            ?: throw IntegrityMismatchException("No import manifest exists for scope $scope")
        val state = database.legacyImportDao().state(scope)
            ?: throw IntegrityMismatchException("No import state exists for scope $scope")
        if (state.status != LegacyImportStatus.COMPLETE.name) {
            throw IntegrityMismatchException(
                "Import scope $scope is ${state.status}, expected ${LegacyImportStatus.COMPLETE.name}"
            )
        }

        val report = LegacyBanImportReport(
            outcome = LegacyBanImportOutcome.ALREADY_CURRENT,
            scope = scope,
            sourceCount = manifest.sourceCount,
            clubCount = manifest.clubCount,
            seniorCount = manifest.seniorCount,
            juniorCount = manifest.juniorCount,
            sourceManifestSha256 = manifest.sourceManifestSha256,
            semanticFingerprint = manifest.semanticFingerprint,
            timing = LegacyBanImportTiming.ZERO,
        )
        verifyPersisted(report)
        return report
    }

    suspend fun reset(scope: String = LEGACY_BAN_IMPORT_SCOPE) = writeMutex.withLock {
        store.resetImportScope(scope)
        logger.info("legacy-ban reset scope=$scope")
    }

    private suspend fun importLocked(
        sources: List<LegacyBanSource>,
        scope: String,
    ): LegacyBanImportReport {
        require(scope.isNotBlank()) { "Import scope must not be blank" }
        if (sources.isEmpty()) {
            throw LegacyFormatException("Legacy .ban import requires at least one source")
        }

        val totalStart = nanoTime()
        logger.info(
            "legacy-ban start scope=$scope adapter=$LEGACY_BAN_ADAPTER_VERSION " +
                "schema=$DATA_SCHEMA_V1 sources=${sources.size}"
        )

        val prepared = prepareSources(sources)
        val clubs = prepared.sources.map { it.club }
        validateBatch(clubs)

        val manifestStart = nanoTime()
        val sourceManifestSha256 = withContext(ioDispatcher) {
            V1Fingerprint.sourceManifest(
                prepared.sources.map { SourceManifestEntryV1(it.logicalPath, it.sourceSha256) }
            )
        }
        val manifestNanos = elapsed(manifestStart)

        val fingerprintStart = nanoTime()
        val semanticFingerprint = withContext(ioDispatcher) { V1Fingerprint.corpus(clubs) }
        val fingerprintNanos = elapsed(fingerprintStart)
        val seniorCount = clubs.sumOf { club ->
            club.players.count { it.rosterKind == RosterKindV1.SENIOR }
        }
        val juniorCount = clubs.sumOf { club ->
            club.players.count { it.rosterKind == RosterKindV1.JUNIOR }
        }

        val baseTiming = LegacyBanImportTiming(
            readDecodeNanos = prepared.readDecodeNanos,
            sourceHashNanos = prepared.sourceHashNanos,
            adapterNanos = prepared.adapterNanos,
            sourceManifestNanos = manifestNanos,
            semanticFingerprintNanos = fingerprintNanos,
            persistenceNanos = 0L,
            verificationNanos = 0L,
            totalNanos = 0L,
        )
        val expected = LegacyBanImportReport(
            outcome = LegacyBanImportOutcome.IMPORTED,
            scope = scope,
            sourceCount = prepared.sources.size,
            clubCount = clubs.size,
            seniorCount = seniorCount,
            juniorCount = juniorCount,
            sourceManifestSha256 = sourceManifestSha256,
            semanticFingerprint = semanticFingerprint,
            timing = baseTiming,
        )

        if (isAlreadyCurrent(expected)) {
            val timing = baseTiming.copy(totalNanos = elapsed(totalStart))
            logger.info(
                "legacy-ban already-current scope=$scope clubs=${clubs.size} seniors=$seniorCount " +
                    "juniors=$juniorCount fingerprint=${semanticFingerprint.take(16)} totalNanos=${timing.totalNanos}"
            )
            return expected.copy(
                outcome = LegacyBanImportOutcome.ALREADY_CURRENT,
                timing = timing,
            )
        }

        val startedAt = clockMillis()
        database.legacyImportDao().upsertState(
            LegacyImportStateEntity(
                scope = scope,
                status = LegacyImportStatus.RUNNING.name,
                adapterVersion = LEGACY_BAN_ADAPTER_VERSION,
                schemaVersion = DATA_SCHEMA_V1,
                sourceManifestSha256 = sourceManifestSha256,
                semanticFingerprint = semanticFingerprint,
                updatedAtEpochMillis = startedAt,
                lastError = null,
            )
        )

        try {
            val completedAt = clockMillis()
            val persistenceStart = nanoTime()
            store.replaceImportScope(
                scope = scope,
                clubs = clubs,
                manifest = LegacyImportManifestEntity(
                    scope = scope,
                    adapterVersion = LEGACY_BAN_ADAPTER_VERSION,
                    schemaVersion = DATA_SCHEMA_V1,
                    sourceCount = prepared.sources.size,
                    clubCount = clubs.size,
                    seniorCount = seniorCount,
                    juniorCount = juniorCount,
                    sourceManifestSha256 = sourceManifestSha256,
                    semanticFingerprint = semanticFingerprint,
                    importedAtEpochMillis = completedAt,
                ),
                completedState = LegacyImportStateEntity(
                    scope = scope,
                    status = LegacyImportStatus.COMPLETE.name,
                    adapterVersion = LEGACY_BAN_ADAPTER_VERSION,
                    schemaVersion = DATA_SCHEMA_V1,
                    sourceManifestSha256 = sourceManifestSha256,
                    semanticFingerprint = semanticFingerprint,
                    updatedAtEpochMillis = completedAt,
                    lastError = null,
                ),
            )
            val persistenceNanos = elapsed(persistenceStart)

            val verificationStart = nanoTime()
            verifyPersisted(expected)
            val verificationNanos = elapsed(verificationStart)
            val timing = baseTiming.copy(
                persistenceNanos = persistenceNanos,
                verificationNanos = verificationNanos,
                totalNanos = elapsed(totalStart),
            )
            logger.info(
                "legacy-ban complete scope=$scope clubs=${clubs.size} seniors=$seniorCount juniors=$juniorCount " +
                    "fingerprint=${semanticFingerprint.take(16)} totalNanos=${timing.totalNanos}"
            )
            return expected.copy(timing = timing)
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            try {
                database.legacyImportDao().upsertState(
                    LegacyImportStateEntity(
                        scope = scope,
                        status = LegacyImportStatus.FAILED.name,
                        adapterVersion = LEGACY_BAN_ADAPTER_VERSION,
                        schemaVersion = DATA_SCHEMA_V1,
                        sourceManifestSha256 = sourceManifestSha256,
                        semanticFingerprint = semanticFingerprint,
                        updatedAtEpochMillis = clockMillis(),
                        lastError = safeError(error),
                    )
                )
            } catch (_: Exception) {
                // Best effort only: never hide the original import failure.
            }
            logger.error("legacy-ban failed scope=$scope error=${safeError(error)}", error)
            throw error
        }
    }

    private suspend fun isAlreadyCurrent(expected: LegacyBanImportReport): Boolean {
        val state = database.legacyImportDao().state(expected.scope) ?: return false
        val manifest = database.legacyImportDao().manifest(expected.scope) ?: return false
        if (state.status != LegacyImportStatus.COMPLETE.name) return false
        if (state.adapterVersion != LEGACY_BAN_ADAPTER_VERSION) return false
        if (state.schemaVersion != DATA_SCHEMA_V1) return false
        if (manifest.adapterVersion != LEGACY_BAN_ADAPTER_VERSION) return false
        if (manifest.schemaVersion != DATA_SCHEMA_V1) return false
        if (manifest.sourceCount != expected.sourceCount) return false
        if (manifest.clubCount != expected.clubCount) return false
        if (manifest.seniorCount != expected.seniorCount) return false
        if (manifest.juniorCount != expected.juniorCount) return false
        if (manifest.sourceManifestSha256 != expected.sourceManifestSha256) return false
        if (manifest.semanticFingerprint != expected.semanticFingerprint) return false

        return try {
            verifyPersisted(expected)
            true
        } catch (_: IntegrityMismatchException) {
            false
        }
    }

    private suspend fun verifyPersisted(expected: LegacyBanImportReport) {
        val counts = store.counts(expected.scope)
        val expectedTotalPlayers = expected.seniorCount + expected.juniorCount
        val expectedCounts = ImportScopeCounts(
            clubs = expected.clubCount,
            players = expectedTotalPlayers,
            seniors = expected.seniorCount,
            juniors = expected.juniorCount,
        )
        if (counts != expectedCounts) {
            throw IntegrityMismatchException(
                "Room count mismatch for ${expected.scope}: actual=$counts expected=$expectedCounts"
            )
        }

        val roundTrip = store.readImportScope(expected.scope)
        val persistedFingerprint = withContext(ioDispatcher) { V1Fingerprint.corpus(roundTrip) }
        if (persistedFingerprint != expected.semanticFingerprint) {
            throw IntegrityMismatchException(
                "Room semantic fingerprint mismatch for ${expected.scope}: " +
                    "actual=$persistedFingerprint expected=${expected.semanticFingerprint}"
            )
        }
    }

    private suspend fun prepareSources(sources: List<LegacyBanSource>): PreparedBatch =
        withContext(ioDispatcher) {
            val sorted = sources.sortedBy { it.logicalPath }
            val paths = sorted.map { it.logicalPath }
            if (paths.any { it.isBlank() }) {
                throw LegacyFormatException("Legacy .ban source path must not be blank")
            }
            if (paths.toSet().size != paths.size) {
                throw LegacyFormatException("Duplicate logical path in legacy .ban source set")
            }
            if (paths.any { !it.lowercase().endsWith(".ban") }) {
                throw LegacyFormatException("Legacy .ban importer received a non-.ban source")
            }

            var readDecodeNanos = 0L
            var sourceHashNanos = 0L
            var adapterNanos = 0L
            val prepared = sorted.map { source ->
                val readStart = nanoTime()
                val bytes = source.openStream().use { input -> input.readBytes() }
                if (bytes.isEmpty()) {
                    throw LegacyFormatException("Legacy .ban source ${source.logicalPath} is empty")
                }
                val snapshot = try {
                    gateway.readTeamBan(ByteArrayInputStream(bytes))
                } catch (error: Exception) {
                    if (error is CancellationException) throw error
                    throw LegacyFormatException(
                        "Failed to read legacy .ban source ${source.logicalPath}",
                        error,
                    )
                }
                readDecodeNanos += elapsed(readStart)

                val hashStart = nanoTime()
                val sourceSha = V1Fingerprint.sha256(bytes)
                sourceHashNanos += elapsed(hashStart)

                val adapterStart = nanoTime()
                val club = adapter.adapt(snapshot)
                adapterNanos += elapsed(adapterStart)

                PreparedBanSource(
                    logicalPath = source.logicalPath,
                    sourceSha256 = sourceSha,
                    club = club,
                )
            }
            PreparedBatch(prepared, readDecodeNanos, sourceHashNanos, adapterNanos)
        }

    private fun validateBatch(clubs: List<ClubDataV1>) {
        val clubIds = clubs.map { it.id }
        if (clubIds.toSet().size != clubIds.size) {
            throw DuplicateStableIdentityException("Duplicate club identity across legacy .ban sources")
        }
        val fileRefs = clubs.map { it.sourceFileRef }
        if (fileRefs.toSet().size != fileRefs.size) {
            throw DuplicateStableIdentityException("Duplicate sourceFileRef across legacy .ban sources")
        }
        val playerIds = clubs.flatMap { club -> club.players.map { it.id } }
        if (playerIds.toSet().size != playerIds.size) {
            throw DuplicateStableIdentityException("Duplicate player identity across legacy .ban sources")
        }
    }

    private fun elapsed(start: Long): Long = (nanoTime() - start).coerceAtLeast(0L)

    private fun safeError(error: Throwable): String =
        (error.message ?: error.javaClass.simpleName)
            .replace('\n', ' ')
            .replace('\r', ' ')
            .take(512)
}

data class LegacyBanSource(
    val logicalPath: String,
    val openStream: () -> InputStream,
)

enum class LegacyImportStatus {
    NOT_IMPORTED,
    RUNNING,
    COMPLETE,
    FAILED,
}

enum class LegacyBanImportOutcome {
    IMPORTED,
    ALREADY_CURRENT,
}

data class LegacyBanImportTiming(
    val readDecodeNanos: Long,
    val sourceHashNanos: Long,
    val adapterNanos: Long,
    val sourceManifestNanos: Long,
    val semanticFingerprintNanos: Long,
    val persistenceNanos: Long,
    val verificationNanos: Long,
    val totalNanos: Long,
) {
    companion object {
        val ZERO = LegacyBanImportTiming(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L)
    }
}

data class LegacyBanImportReport(
    val outcome: LegacyBanImportOutcome,
    val scope: String,
    val sourceCount: Int,
    val clubCount: Int,
    val seniorCount: Int,
    val juniorCount: Int,
    val sourceManifestSha256: String,
    val semanticFingerprint: String,
    val timing: LegacyBanImportTiming,
) {
    val totalPlayerCount: Int get() = seniorCount + juniorCount
}

interface LegacyImportLogger {
    fun info(message: String)
    fun error(message: String, error: Throwable? = null)
}

object JulLegacyImportLogger : LegacyImportLogger {
    private val logger = Logger.getLogger("FootballDynasty.LegacyImport")

    override fun info(message: String) {
        logger.info(message)
    }

    override fun error(message: String, error: Throwable?) {
        if (error == null) logger.warning(message) else logger.log(Level.WARNING, message, error)
    }
}

private data class PreparedBanSource(
    val logicalPath: String,
    val sourceSha256: String,
    val club: ClubDataV1,
)

private data class PreparedBatch(
    val sources: List<PreparedBanSource>,
    val readDecodeNanos: Long,
    val sourceHashNanos: Long,
    val adapterNanos: Long,
)
