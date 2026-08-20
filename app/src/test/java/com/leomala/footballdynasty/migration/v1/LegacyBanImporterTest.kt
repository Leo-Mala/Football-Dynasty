package com.leomala.footballdynasty.migration.v1

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.repository.RoomClubRepository
import com.leomala.footballdynasty.foundation.error.IntegrityMismatchException
import com.leomala.footballdynasty.testsupport.LegacyFixtureLoader
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream

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
        importer = LegacyBanImporter(database) { ++now }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `imports real ban fixture and round trips through Room`() = runBlocking {
        val report = importer.import(listOf(realFixtureSource()))

        assertEquals(LegacyBanImportOutcome.IMPORTED, report.outcome)
        assertEquals(1, report.sourceCount)
        assertEquals(1, report.clubCount)
        assertEquals(20, report.seniorCount)
        assertEquals(0, report.juniorCount)

        val verified = importer.verify()
        assertEquals(report.semanticFingerprint, verified.semanticFingerprint)
        assertEquals(report.sourceManifestSha256, verified.sourceManifestSha256)

        val club = RoomClubRepository(database).findBySourceFileRef("12deoctubre_par")
        assertNotNull(club)
        requireNotNull(club)
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
        assertEquals(firstManifest, secondManifest)
        assertEquals(1, database.clubDao().countForImportScope(LEGACY_BAN_IMPORT_SCOPE))
        assertEquals(20, database.playerDao().countForImportScope(LEGACY_BAN_IMPORT_SCOPE))
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
        importer.verify()
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
    fun `reset removes only the legacy import scope and its metadata`() = runBlocking {
        importer.import(listOf(realFixtureSource()))
        importer.reset()

        assertEquals(0, database.clubDao().countForImportScope(LEGACY_BAN_IMPORT_SCOPE))
        assertEquals(0, database.playerDao().countForImportScope(LEGACY_BAN_IMPORT_SCOPE))
        assertNull(database.legacyImportDao().manifest(LEGACY_BAN_IMPORT_SCOPE))
        assertNull(database.legacyImportDao().state(LEGACY_BAN_IMPORT_SCOPE))
    }

    private fun realFixtureSource(): LegacyBanSource = LegacyBanSource(
        logicalPath = "legacy/12deoctubre_par.ban",
        openStream = { ByteArrayInputStream(fixtureBytes) },
    )
}
