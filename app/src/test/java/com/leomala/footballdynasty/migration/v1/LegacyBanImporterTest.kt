package com.leomala.footballdynasty.migration.v1

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.RoomV1DataStore
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.repository.RoomClubRepository
import com.leomala.footballdynasty.foundation.error.IntegrityMismatchException
import com.leomala.footballdynasty.testsupport.LegacyFixtureLoader
import java.io.ByteArrayInputStream
import java.util.concurrent.Executors
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LegacyBanImporterTest {
    private lateinit var database: FootballDynastyDatabase
    private lateinit var importer: LegacyBanImporter
    private lateinit var fixtureBytes: ByteArray

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            FootballDynastyDatabase::class.java,
        ).allowMainThreadQueries().build()
        fixtureBytes = LegacyFixtureLoader.bytes(
            "/legacy/12deoctubre_par.ban.b64",
            javaClass,
        )
        var now = 1_000L
        importer = LegacyBanImporter(
            database = database,
            logger = NoOpImportLogger,
            clockMillis = { ++now },
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `initial status is explicitly not imported`() = runBlocking {
        assertEquals(LegacyImportStatus.NOT_IMPORTED, importer.status())
    }

    @Test
    fun `imports real ban fixture and round trips through Room`() = runBlocking {
        val report = importer.import(listOf(realFixtureSource()))

        assertEquals(LegacyBanImportOutcome.IMPORTED, report.outcome)
        assertEquals(LegacyImportStatus.COMPLETE, importer.status())
        assertEquals(1, report.sourceCount)
        assertEquals(1, report.clubCount)
        assertEquals(20, report.seniorCount)
        assertEquals(0, report.juniorCount)
        assertEquals(20, report.totalPlayerCount)
        assertEquals(64, report.sourceManifestSha256.length)
        assertEquals(64, report.semanticFingerprint.length)

        val verified = importer.verify()
        assertEquals(report.semanticFingerprint, verified.semanticFingerprint)
        assertEquals(report.sourceManifestSha256, verified.sourceManifestSha256)

        val repository = RoomClubRepository(database)
        val club = repository.findBySourceFileRef("12deoctubre_par")
        assertNotNull(club)
        requireNotNull(club)
        assertEquals(club, repository.findById(club.id))
        assertEquals("12 de Octubre", club.name)
        assertEquals(150, club.country)
        assertEquals(20, club.players.size)
        assertEquals("Mauro Cardozo", club.players.first().name)
        assertEquals(38, club.players.first().age)
        assertEquals(0, club.players.first().position)
    }

    @Test
    fun `reimport of unchanged source is idempotent`() = runBlocking {
        val first = importer.import(listOf(realFixtureSource()))
        val firstManifest = database.legacyImportDao().manifest(LEGACY_BAN_IMPORT_SCOPE)

        val second = importer.import(listOf(realFixtureSource()))
        val secondManifest = database.legacyImportDao().manifest(LEGACY_BAN_IMPORT_SCOPE)

        assertEquals(LegacyBanImportOutcome.IMPORTED, first.outcome)
        assertEquals(LegacyBanImportOutcome.ALREADY_CURRENT, second.outcome)
        assertEquals(first.semanticFingerprint, second.semanticFingerprint)
        assertEquals(first.sourceManifestSha256, second.sourceManifestSha256)
        assertEquals(firstManifest, secondManifest)
        assertEquals(1, database.clubDao().countForImportScope(LEGACY_BAN_IMPORT_SCOPE))
        assertEquals(20, database.playerDao().countForImportScope(LEGACY_BAN_IMPORT_SCOPE))
        assertEquals(20, database.squadMembershipDao().seniorCountForImportScope(LEGACY_BAN_IMPORT_SCOPE))
        assertEquals(0, database.squadMembershipDao().juniorCountForImportScope(LEGACY_BAN_IMPORT_SCOPE))
    }

    @Test
    fun `concurrent unchanged imports serialize without duplication`() = runBlocking {
        val reports = listOf(
            async { importer.import(listOf(realFixtureSource())) },
            async { importer.import(listOf(realFixtureSource())) },
        ).awaitAll()

        assertEquals(
            setOf(LegacyBanImportOutcome.IMPORTED, LegacyBanImportOutcome.ALREADY_CURRENT),
            reports.map { it.outcome }.toSet(),
        )
        assertEquals(1, database.clubDao().countForImportScope(LEGACY_BAN_IMPORT_SCOPE))
        assertEquals(20, database.playerDao().countForImportScope(LEGACY_BAN_IMPORT_SCOPE))
        assertEquals(LegacyImportStatus.COMPLETE, importer.status())
        importer.verify()
        Unit
    }

    @Test
    fun `reset and import concurrency ends in a consistent state`() = runBlocking {
        importer.import(listOf(realFixtureSource()))
        listOf(
            async { importer.reset(); "reset" },
            async { importer.import(listOf(realFixtureSource())); "import" },
        ).awaitAll()

        when (importer.status()) {
            LegacyImportStatus.COMPLETE -> importer.verify()
            LegacyImportStatus.NOT_IMPORTED -> {
                assertEquals(0, database.clubDao().countForImportScope(LEGACY_BAN_IMPORT_SCOPE))
                assertEquals(0, database.playerDao().countForImportScope(LEGACY_BAN_IMPORT_SCOPE))
            }
            else -> throw AssertionError("reset/import race left a non-terminal state")
        }
        Unit
    }

    @Test
    fun `verify detects persisted semantic corruption`() = runBlocking {
        importer.import(listOf(realFixtureSource()))
        val player = database.playerDao()
            .playersForClub(
                requireNotNull(
                    database.clubDao().findBySourceFileRef("12deoctubre_par")
                ).id
            )
            .first()
        database.playerDao().upsertAll(listOf(player.copy(name = player.name + " altered")))

        val error = runCatching { importer.verify() }.exceptionOrNull()
        assertTrue(error is IntegrityMismatchException)
    }

    @Test
    fun `reset then reimport preserves exact fingerprint and modern rows`() = runBlocking {
        val first = importer.import(listOf(realFixtureSource()))
        insertModernTechnicalProbe()

        importer.reset()
        assertEquals(LegacyImportStatus.NOT_IMPORTED, importer.status())
        assertEquals(0, database.clubDao().countForImportScope(LEGACY_BAN_IMPORT_SCOPE))
        assertEquals(0, database.playerDao().countForImportScope(LEGACY_BAN_IMPORT_SCOPE))
        assertNull(database.legacyImportDao().manifest(LEGACY_BAN_IMPORT_SCOPE))
        assertNull(database.legacyImportDao().state(LEGACY_BAN_IMPORT_SCOPE))
        assertNotNull(database.clubDao().findBySourceFileRef("modern-technical-probe"))

        val second = importer.import(listOf(realFixtureSource()))
        assertEquals(first.semanticFingerprint, second.semanticFingerprint)
        assertEquals(first.sourceManifestSha256, second.sourceManifestSha256)
        assertNotNull(database.clubDao().findBySourceFileRef("modern-technical-probe"))
    }

    @Test
    fun `transaction rollback restores previous scope when insert fails inside transaction`() = runBlocking {
        val initial = importer.import(listOf(realFixtureSource()))
        val previousManifest = requireNotNull(database.legacyImportDao().manifest(LEGACY_BAN_IMPORT_SCOPE))
        val previousState = requireNotNull(database.legacyImportDao().state(LEGACY_BAN_IMPORT_SCOPE))
        insertModernTechnicalProbe(sourceFileRef = "rollback-conflict-ref")

        val store = RoomV1DataStore(database)
        val current = store.readImportScope(LEGACY_BAN_IMPORT_SCOPE).single()
        val conflicting = current.copy(sourceFileRef = "rollback-conflict-ref")

        val failure = runCatching {
            store.replaceImportScope(
                scope = LEGACY_BAN_IMPORT_SCOPE,
                clubs = listOf(conflicting),
                manifest = previousManifest.copy(semanticFingerprint = "should-not-commit"),
                completedState = previousState.copy(semanticFingerprint = "should-not-commit"),
            )
        }.exceptionOrNull()

        assertNotNull(failure)
        assertNotNull(database.clubDao().findBySourceFileRef("12deoctubre_par"))
        assertEquals(1, database.clubDao().countForImportScope(LEGACY_BAN_IMPORT_SCOPE))
        assertEquals(20, database.playerDao().countForImportScope(LEGACY_BAN_IMPORT_SCOPE))
        assertEquals(previousManifest, database.legacyImportDao().manifest(LEGACY_BAN_IMPORT_SCOPE))
        assertEquals(previousState, database.legacyImportDao().state(LEGACY_BAN_IMPORT_SCOPE))
        assertEquals(initial.semanticFingerprint, importer.verify().semanticFingerprint)
    }

    @Test
    fun `source reading and adaptation use injected IO dispatcher`() = runBlocking {
        val executor = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "phase3-io-test")
        }
        val dispatcher = executor.asCoroutineDispatcher()
        var observedThread: String? = null
        try {
            val ioImporter = LegacyBanImporter(
                database = database,
                ioDispatcher = dispatcher,
                logger = NoOpImportLogger,
            )
            ioImporter.import(
                listOf(
                    LegacyBanSource("legacy/12deoctubre_par.ban") {
                        observedThread = Thread.currentThread().name
                        ByteArrayInputStream(fixtureBytes)
                    }
                )
            )
            assertTrue(observedThread?.startsWith("phase3-io-test") == true)
            assertNotEquals(Thread.currentThread().name, observedThread)
        } finally {
            dispatcher.close()
            executor.shutdownNow()
        }
    }

    @Test
    fun `prints reproducible fixture benchmark evidence`() = runBlocking {
        val report = importer.import(listOf(realFixtureSource()))
        val t = report.timing
        println(
            "PHASE3_FIXTURE_BENCHMARK " +
                "readDecodeNanos=${t.readDecodeNanos} " +
                "sourceHashNanos=${t.sourceHashNanos} " +
                "adapterNanos=${t.adapterNanos} " +
                "sourceManifestNanos=${t.sourceManifestNanos} " +
                "semanticFingerprintNanos=${t.semanticFingerprintNanos} " +
                "persistenceNanos=${t.persistenceNanos} " +
                "verificationNanos=${t.verificationNanos} " +
                "totalNanos=${t.totalNanos}"
        )
        assertTrue(t.totalNanos > 0L)
    }

    private suspend fun insertModernTechnicalProbe(
        sourceFileRef: String = "modern-technical-probe",
    ) {
        val imported = requireNotNull(database.clubDao().findBySourceFileRef("12deoctubre_par"))
        database.clubDao().upsertAll(
            listOf(
                imported.copy(
                    id = "technical-modern-${sourceFileRef}",
                    importScope = null,
                    sourceFileRef = sourceFileRef,
                    name = "Technical modern persistence probe",
                )
            )
        )
    }

    private fun realFixtureSource(): LegacyBanSource = LegacyBanSource(
        logicalPath = "legacy/12deoctubre_par.ban",
        openStream = { ByteArrayInputStream(fixtureBytes) },
    )

    private object NoOpImportLogger : LegacyImportLogger {
        override fun info(message: String) = Unit
        override fun error(message: String, error: Throwable?) = Unit
    }
}
