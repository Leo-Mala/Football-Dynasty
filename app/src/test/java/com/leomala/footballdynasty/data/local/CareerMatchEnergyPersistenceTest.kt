package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.repository.RoomCareerRepository
import com.leomala.footballdynasty.data.repository.RoomCareerStateRepository
import com.leomala.footballdynasty.domain.career.CareerMatchRuntimeBridge
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.model.Career
import com.leomala.footballdynasty.domain.model.Match
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerMatchEnergyPersistenceTest {
    @Test
    fun `resolved match proven player effects survive database reopen with score calendar and rng`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase9-player-effects-reopen"
        context.deleteDatabase(name)
        var database = fileDatabase(context, name)
        seedCareer(database, includeOtherClubPlayer = false)
        val initial = CareerStateFactory.create("career-energy", 12345L)
        val schedule = schedule(initial.calendar.currentDayIndex)
        val store = CareerMatchStore(database) { 50L }
        store.initializeSchedule(initial, schedule)

        val resolved = CareerMatchRuntimeBridge.run(initial, schedule, "m1") { event, random ->
            Match(event.matchId, event.homeClubId, event.awayClubId, random.nextInt(4), random.nextInt(4))
        }
        store.commitMatch(
            resolved,
            playerRuntimeUpdates = listOf(
                CareerMatchPlayerRuntimeUpdate(
                    playerId = "home-player",
                    energy = 42,
                    overall = 75,
                    injuryUntilEpochDay = 123L,
                )
            ),
            playerClubSeasonStatUpdates = listOf(
                CareerMatchPlayerClubSeasonStatUpdate(
                    playerId = "home-player",
                    legacySeasonId = 1,
                    legacyClubId = 101,
                    legacyC = 1,
                    legacyD = 2,
                    legacyE = 3,
                    legacyF = 4,
                    legacyG = 5,
                    legacyH = 6,
                )
            ),
        )
        database.close()

        database = fileDatabase(context, name)
        val reopenedStore = CareerMatchStore(database)
        val dao = database.careerPlayerRuntimeDao()
        val runtime = requireNotNull(dao.findRuntime("career-energy", "home-player"))
        assertEquals(42, runtime.energy)
        assertEquals(75, runtime.overall)
        assertEquals(123L, runtime.injuryUntilEpochDay)
        val stats = dao.clubSeasonStatsForPlayer("career-energy", "home-player").single()
        assertEquals(1, stats.legacySeasonId)
        assertEquals(101, stats.legacyClubId)
        assertEquals(6, stats.legacyH)
        assertEquals(resolved.match, reopenedStore.findResult("career-energy", "m1"))
        assertEquals(
            resolved.state,
            RoomCareerStateRepository(database).findById("career-energy"),
        )

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `invalid non match effect rolls back player stats score calendar and rng writes`() = runBlocking {
        val database = inMemoryDatabase()
        seedCareer(database, includeOtherClubPlayer = true)
        val initial = CareerStateFactory.create("career-energy", 98765L)
        val schedule = schedule(initial.calendar.currentDayIndex)
        val store = CareerMatchStore(database) { 50L }
        store.initializeSchedule(initial, schedule)

        val resolved = CareerMatchRuntimeBridge.run(initial, schedule, "m1") { event, random ->
            Match(event.matchId, event.homeClubId, event.awayClubId, random.nextInt(4), random.nextInt(4))
        }
        val error = runCatching {
            store.commitMatch(
                resolved,
                playerRuntimeUpdates = listOf(
                    CareerMatchPlayerRuntimeUpdate("home-player", 42, 75, 123L),
                ),
                playerClubSeasonStatUpdates = listOf(
                    CareerMatchPlayerClubSeasonStatUpdate(
                        "home-player", 1, 101, 0, 0, 0, 0, 0, 1,
                    ),
                    CareerMatchPlayerClubSeasonStatUpdate(
                        "other-player", 1, 303, 0, 0, 0, 0, 0, 1,
                    ),
                ),
            )
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
        val dao = database.careerPlayerRuntimeDao()
        val runtime = requireNotNull(dao.findRuntime("career-energy", "home-player"))
        assertEquals(100, runtime.energy)
        assertEquals(80, runtime.overall)
        assertEquals(0L, runtime.injuryUntilEpochDay)
        assertTrue(dao.clubSeasonStatsForPlayer("career-energy", "home-player").isEmpty())
        assertNull(store.findResult("career-energy", "m1"))
        assertEquals(initial, RoomCareerStateRepository(database).findById("career-energy"))
        database.close()
        Unit
    }

    private fun inMemoryDatabase(): FootballDynastyDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private fun fileDatabase(context: Context, name: String): FootballDynastyDatabase =
        Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()

    private suspend fun seedCareer(
        database: FootballDynastyDatabase,
        includeOtherClubPlayer: Boolean,
    ) {
        val clubs = mutableListOf(club("home"), club("away"))
        if (includeOtherClubPlayer) clubs += club("other")
        database.clubDao().upsertAll(clubs)
        RoomCareerRepository(database) { 10L }.save(
            Career("career-energy", "Phase 9 energy", null, null)
        )
        val dao = database.careerPlayerRuntimeDao()
        dao.upsertRuntime(runtime("home-player"))
        dao.upsertMembership(membership("home-player", "home", 0))
        if (includeOtherClubPlayer) {
            dao.upsertRuntime(runtime("other-player"))
            dao.upsertMembership(membership("other-player", "other", 0))
        }
    }

    private fun schedule(day: Int) = listOf(
        ScheduledCareerMatch("m1", day, 1, "home", "away"),
    )

    private fun runtime(playerId: String) = CareerPlayerRuntimeEntity(
        careerId = "career-energy",
        playerId = playerId,
        sourceType = CareerPlayerRuntimeStore.SOURCE_CANONICAL,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = 25,
        overall = 80,
        marketValue = 1_000,
        star = false,
        worldTop = false,
        legacyHash = playerId.hashCode(),
        legacyGeneratedO = 0,
        legacyCreatedYear = 0,
        contractEndEpochMillis = 0L,
        legacyPreviousMarketValue = 0,
        legacyQ = false,
        legacyX = false,
        legacyY = false,
        legacyZ = false,
        energy = 100,
        injuryUntilEpochDay = 0L,
    )

    private fun membership(
        playerId: String,
        clubId: String,
        ordinal: Int,
    ) = CareerSquadMembershipEntity(
        careerId = "career-energy",
        playerId = playerId,
        clubId = clubId,
        rosterKind = "SENIOR",
        sourceOrdinal = ordinal,
    )

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
        legacyId = when (id) {
            "home" -> 101
            "away" -> 202
            else -> 303
        },
        legacyValid = true,
    )
}
