package com.leomala.footballdynasty.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerProceduralPlayerEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.repository.RoomCareerRepository
import com.leomala.footballdynasty.data.repository.RoomCareerStateRepository
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.LegacyAnnualPlayerMovementRules
import com.leomala.footballdynasty.domain.model.Career
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerPlayerRuntimeStoreTest {
    private lateinit var database: FootballDynastyDatabase
    private lateinit var store: CareerPlayerRuntimeStore

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        database.clubDao().upsertAll(
            listOf("club-a", "club-b", "source-club", "target-club").map(::club)
        )
        store = CareerPlayerRuntimeStore(database)
        createCareer("career-a")
        createCareer("career-b")
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `procedural players are isolated by career and never enter canonical catalog`() = runBlocking {
        store.saveProceduralPlayer(
            runtime("career-a", "shared-player", overall = 61),
            procedural("career-a", "shared-player", "Generated A"),
            membership("career-a", "shared-player", "club-a", 0),
        )
        store.saveProceduralPlayer(
            runtime("career-b", "shared-player", overall = 74),
            procedural("career-b", "shared-player", "Generated B"),
            membership("career-b", "shared-player", "club-b", 0),
        )

        val a = requireNotNull(store.find("career-a", "shared-player"))
        val b = requireNotNull(store.find("career-b", "shared-player"))
        assertEquals(61, a.runtime.overall)
        assertEquals(74, b.runtime.overall)
        assertEquals("Generated A", a.procedural?.name)
        assertEquals("Generated B", b.procedural?.name)
        assertEquals("club-a", a.membership?.clubId)
        assertEquals("club-b", b.membership?.clubId)
        assertEquals(0, database.playerDao().count())
        assertEquals(0, database.squadMembershipDao().count())
    }

    @Test
    fun `procedural player and rng state rollback together when membership write fails`() = runBlocking {
        val careerStateRepository = RoomCareerStateRepository(database) { 200L }
        val before = CareerStateFactory.create("career-a", seed = 42L)
        careerStateRepository.save(before)

        val advanced = before.copy(
            random = before.random.copy(
                internalState = (before.random.internalState + 1L) and ((1L shl 48) - 1L),
                draws = before.random.draws + 7L,
            ),
        )

        try {
            store.saveProceduralPlayerAndCareerState(
                state = advanced,
                runtime = runtime("career-a", "rolled-back", overall = 72),
                procedural = procedural("career-a", "rolled-back", "Rolled Back"),
                membership = membership("career-a", "rolled-back", "missing-club", 0),
            )
            fail("Expected club foreign key failure after career state write")
        } catch (_: SQLiteConstraintException) {
            // Expected: the final membership FK must roll back runtime/procedural rows and RNG state.
        }

        assertNull(store.find("career-a", "rolled-back"))
        assertEquals(before, requireNotNull(careerStateRepository.findById("career-a")))
    }

    @Test
    fun `membership rejects nonexistent target club without leaking runtime rows`() = runBlocking {
        try {
            store.saveProceduralPlayer(
                runtime("career-a", "orphan-player", overall = 63),
                procedural("career-a", "orphan-player", "Orphan"),
                membership("career-a", "orphan-player", "missing-club", 0),
            )
            fail("Expected club foreign key failure")
        } catch (_: SQLiteConstraintException) {
            // Expected.
        }
        assertNull(store.find("career-a", "orphan-player"))
    }

    @Test
    fun `annual movement persists exact player-local T1 effects and exposes deferred club effects`() = runBlocking {
        store.saveProceduralPlayer(
            runtime(
                careerId = "career-a",
                playerId = "move-player",
                overall = 66,
                marketValue = 1234,
                legacyQ = false,
                legacyX = true,
                legacyY = true,
                legacyZ = true,
            ),
            procedural("career-a", "move-player", "Mover"),
            membership("career-a", "move-player", "source-club", 4),
        )
        val plan = LegacyAnnualPlayerMovementRules.annualT1Plan(
            sourceExists = true,
            sourceManaged = true,
            targetManaged = true,
            amount = 1234,
        )

        val result = store.applyAnnualMovement(
            careerId = "career-a",
            playerId = "move-player",
            targetClubId = "target-club",
            rosterKind = "SENIOR",
            sourceOrdinal = 2,
            contractEndEpochMillis = 999_000L,
            plan = plan,
        )

        assertEquals("target-club", result.snapshot.membership?.clubId)
        assertEquals(999_000L, result.snapshot.runtime.contractEndEpochMillis)
        assertEquals(1234, result.snapshot.runtime.legacyPreviousMarketValue)
        assertTrue(result.snapshot.runtime.legacyQ)
        assertFalse(result.snapshot.runtime.legacyX)
        assertTrue(result.snapshot.runtime.legacyY)
        assertFalse(result.snapshot.runtime.legacyZ)
        assertEquals(1234, result.deferredSourceCode1Amount)
        assertEquals(1234, result.deferredTargetCode1Amount)
        assertTrue(result.deferredSourceSpecialReferenceClear)
        assertTrue(result.deferredSourceE1Clear)
    }

    @Test
    fun `deleting runtime cascades procedural facts and membership only inside same career`() = runBlocking {
        store.saveProceduralPlayer(
            runtime("career-a", "cascade-player", overall = 60),
            procedural("career-a", "cascade-player", "Cascade A"),
            membership("career-a", "cascade-player", "club-a", 0),
        )
        store.saveProceduralPlayer(
            runtime("career-b", "cascade-player", overall = 70),
            procedural("career-b", "cascade-player", "Cascade B"),
            membership("career-b", "cascade-player", "club-b", 0),
        )

        database.careerPlayerRuntimeDao().deleteRuntime("career-a", "cascade-player")

        assertNull(store.find("career-a", "cascade-player"))
        val b = requireNotNull(store.find("career-b", "cascade-player"))
        assertEquals("Cascade B", b.procedural?.name)
        assertEquals("club-b", b.membership?.clubId)
    }

    @Test
    fun `career scoped runtime and rng survive database reopen together`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase7-career-runtime-reopen.db"
        context.deleteDatabase(name)
        var fileDb = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        fileDb.clubDao().upsertAll(listOf(club("reopen-club")))
        RoomCareerRepository(fileDb) { 100L }.save(
            Career(
                id = "reopen-career",
                displayName = "Reopen",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
            )
        )
        val initialState = CareerStateFactory.create("reopen-career", seed = 73L)
        val persistedState = initialState.copy(
            random = initialState.random.copy(
                internalState = (initialState.random.internalState + 123L) and ((1L shl 48) - 1L),
                draws = initialState.random.draws + 9L,
            ),
        )
        CareerPlayerRuntimeStore(fileDb).saveProceduralPlayerAndCareerState(
            state = persistedState,
            runtime = runtime("reopen-career", "reopen-player", overall = 77),
            procedural = procedural("reopen-career", "reopen-player", "Reopen Player"),
            membership = membership("reopen-career", "reopen-player", "reopen-club", 1),
        )
        fileDb.close()

        fileDb = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        val loaded = requireNotNull(CareerPlayerRuntimeStore(fileDb).find("reopen-career", "reopen-player"))
        val loadedState = requireNotNull(RoomCareerStateRepository(fileDb).findById("reopen-career"))
        assertEquals(77, loaded.runtime.overall)
        assertEquals("Reopen Player", loaded.procedural?.name)
        assertEquals("reopen-club", loaded.membership?.clubId)
        assertEquals(persistedState, loadedState)
        assertEquals(persistedState.random.internalState, loadedState.random.internalState)
        assertEquals(persistedState.random.draws, loadedState.random.draws)
        fileDb.close()
        context.deleteDatabase(name)
        Unit
    }

    private suspend fun createCareer(id: String) {
        RoomCareerRepository(database) { 100L }.save(
            Career(
                id = id,
                displayName = id,
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
            )
        )
    }

    private fun club(id: String) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = id,
        name = id,
        country = 0,
        state = 0,
        level = 1,
        stadium = "",
        capacity = 0,
        reputation = 0,
        primaryColor = "",
        secondaryColor = "",
        coach = "",
        coachCountry = 0,
        baseColor = 0,
        legacyAid = 0,
        legacySid = 0,
        legacyTid = 0,
        legacyVid = 0,
        legacyId = 0,
        legacyValid = true,
    )

    private fun runtime(
        careerId: String,
        playerId: String,
        overall: Int,
        marketValue: Int = overall * 100,
        legacyQ: Boolean = false,
        legacyX: Boolean = false,
        legacyY: Boolean = false,
        legacyZ: Boolean = false,
    ) = CareerPlayerRuntimeEntity(
        careerId = careerId,
        playerId = playerId,
        sourceType = CareerPlayerRuntimeStore.SOURCE_PROCEDURAL,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = 18,
        overall = overall,
        marketValue = marketValue,
        star = false,
        worldTop = false,
        legacyHash = 7,
        legacyGeneratedO = 50,
        legacyCreatedYear = 2026,
        contractEndEpochMillis = 300_000L,
        legacyPreviousMarketValue = 0,
        legacyQ = legacyQ,
        legacyX = legacyX,
        legacyY = legacyY,
        legacyZ = legacyZ,
    )

    private fun procedural(careerId: String, playerId: String, name: String) =
        CareerProceduralPlayerEntity(
            careerId = careerId,
            playerId = playerId,
            name = name,
            country = 29,
            position = 3,
            status = 0,
            side = 1,
            cr1 = 4,
            cr2 = 11,
        )

    private fun membership(careerId: String, playerId: String, clubId: String, sourceOrdinal: Int) =
        CareerSquadMembershipEntity(
            careerId = careerId,
            playerId = playerId,
            clubId = clubId,
            rosterKind = "SENIOR",
            sourceOrdinal = sourceOrdinal,
        )
}
