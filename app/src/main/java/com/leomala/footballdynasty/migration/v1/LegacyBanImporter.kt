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

const val LEGACY_BAN_IMPORT_SCOPE: String = "legacy-ban-corpus-v1"
const val LEGACY_BAN_ADAPTER_VERSION: Int = 1

class LegacyBanImporter(
    private val database: FootballDynastyDatabase,
    private val gateway: LegacyDataGateway = LegacyDataGateway(),
    private val adapter: LegacyBanToV1Adapter = LegacyBanToV1Adapter(),
    private val clockMillis: () -> Long = System::currentTimeMillis,
) {
    private val store = RoomV1DataStore(database)

    suspend fun import(
        sources: List<LegacyBanSource>,
        scope: String = LEGACY_BAN_IMPORT_SCOPE,
    ): LegacyBanImportReport {
        require(scope.isNotBlank()) { "Import scope must not be blank" }
        if (sources.isEmpty()) {
            throw LegacyFormatException("Legacy .ban import requires at least one source")
        }

        val prepared = prepareSources(sources)
        val clubs = prepared.map { it.club }
        validateBatch(clubs)

        val sourceManifestSha256 = V1Fingerprint.sourceManifest(
            prepared.map { SourceManifestEntryV1(it.logicalPath, it.sourceSha256) }
        )
        val semanticFingerprint = V1Fingerprint.corpus(clubs)
        val seniorCount = clubs.sumOf { club ->
            club.players.count { it.rosterKind == RosterKindV1.SENIOR }
        }
        val juniorCount = clubs.sumOf { club ->
            club.players.count { it.rosterKind == RosterKindV1.JUNIOR }
        }

        val expected = LegacyBanImportReport(
            outcome = LegacyBanImportOutcome.IMPORTED,
            scope = scope,
            sourceCount = prepared.size,
            clubCount = clubs.size,
            seniorCount = seniorCount,
            juniorCount = juniorCount,
            sourceManifestSha256 = sourceManifestSha256,
            semanticFingerprint = semanticFingerprint,
        )

        if (isAlreadyCurrent(expected)) {
            return expected.copy(outcome = LegacyBanImportOutcome.ALREADY_CURRENT)
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
            store.replaceImportScope(
                scope = scope,
                clubs = clubs,
                manifest = LegacyImportManifestEntity(
                    scope = scope,
                    adapterVersion = LEGACY_BAN_ADAPTER_VERSION,
                    schemaVersion = DATA_SCHEMA_V1,
                    sourceCount = prepared.size,
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
            verifyPersisted(expected)
            return expected
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
                        lastError = error.message ?: error.javaClass.name,
                    )
                )
            } catch (_: Exception) {
                // Best effort only: never hide the original import failure.
            }
            throw error
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
        )
        verifyPersisted(report)
        return report
    }

    suspend fun reset(scope: String = LEGACY_BAN_IMPORT_SCOPE) {
        store.resetImportScope(scope)
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
        val persistedFingerprint = V1Fingerprint.corpus(roundTrip)
        if (persistedFingerprint != expected.semanticFingerprint) {
            throw IntegrityMismatchException(
                "Room semantic fingerprint mismatch for ${expected.scope}: " +
                    "actual=$persistedFingerprint expected=${expected.semanticFingerprint}"
            )
        }
    }

    private fun prepareSources(sources: List<LegacyBanSource>): List<PreparedBanSource> {
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

        return sorted.map { source ->
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
            PreparedBanSource(
                logicalPath = source.logicalPath,
                sourceSha256 = V1Fingerprint.sha256(bytes),
                club = adapter.adapt(snapshot),
            )
        }
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
}

data class LegacyBanSource(
    val logicalPath: String,
    val openStream: () -> InputStream,
)

enum class LegacyImportStatus {
    RUNNING,
    COMPLETE,
    FAILED,
}

enum class LegacyBanImportOutcome {
    IMPORTED,
    ALREADY_CURRENT,
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
)

private data class PreparedBanSource(
    val logicalPath: String,
    val sourceSha256: String,
    val club: ClubDataV1,
)
