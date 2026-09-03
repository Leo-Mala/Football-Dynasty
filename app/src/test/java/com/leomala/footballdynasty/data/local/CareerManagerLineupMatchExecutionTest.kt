package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.local.entity.PlayerEntity
import com.leomala.footballdynasty.data.repository.RoomCareerRepository
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitRule
import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitSlot
import com.leomala.footballdynasty.domain.match.LegacyMatchEventType
import com.leomala.footballdynasty.domain.match.LegacyMatchModernResultMapper
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime
import com.leomala.footballdynasty.domain.model.Career
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerManagerLineupMatchExecutionTest {
    @Test
    fun `characterized manager lineup drives substitution and persists proven player effects after reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase11-lineup-match-reopen"
        context.deleteDatabase(name)
        var database = database(context, name)
        seed(database)

        val initial = CareerStateFactory.create("career-lineup", 424242L)
        val scheduled = ScheduledCareerMatch(
            matchId = "match-lineup",
            dayIndex = initial.calendar.currentDayIndex,
            eventTypeCode = 1,
            homeClubId = "home",
            awayClubId = "away",
        )
        CareerMatchStore(database).initializeSchedule(initial, listOf(scheduled))

        val homeLineup = LegacyLineupCommitRule.commit(
            starterSlots = listOf(LegacyLineupCommitSlot("home-starter", 10)),
            benchPlayers = listOf("home-bench"),
            eligibleRoster = listOf("home-bench"),
            matchSideIndex = 0,
            mainTeamActivityPresent = false,
        )
        val awayLineup = LegacyLineupCommitRule.commit(
            starterSlots = listOf(LegacyLineupCommitSlot("away-starter", 10)),
            benchPlayers = emptyList(),
            eligibleRoster = emptyList(),
            matchSideIndex = 1,
            mainTeamActivityPresent = false,
        )

        var substitutionApplied = false
        val result = CareerMatchExecutionCoordinator(database) { 77L }.execute(
            careerId = "career-lineup",
            matchId = "match-lineup",
            homeLineup = homeLineup,
            awayLineup = awayLineup,
            homeSubstitutionsRemaining = 3,
            awaySubstitutionsRemaining = 3,
            homeLegacyModeFlag = false,
            awayLegacyModeFlag = false,
        ) { event, state, random ->
            assertEquals(listOf("home-starter"), state.home.active.map { it.value.playerId })
            assertEquals(listOf("home-bench"), state.home.bench.map { it.value.playerId })
            assertEquals(10, state.home.active.single().legacyG0)
            assertEquals(26, state.home.bench.single().legacyG0)
            assertTrue(state.home.active.single().selectedOrUsed)
            assertFalse(state.home.bench.single().selectedOrUsed)

            val injured = state.home.active.single()
            val applied = LegacyMatchTransientRuntime.applyEvent(
                state = state,
                legacyType = LegacyMatchEventType.INJURY.legacyCode,
                legacySubtype = -1,
                eventClub = state.home,
                originalPrimary = injured,
                legacyPeriod = 2,
                legacyMinute = 7,
                random = random,
            )
            substitutionApplied = applied.substitutionApplied
            assertTrue(substitutionApplied)
            assertEquals(listOf("home-bench"), state.home.active.map { it.value.playerId })
            assertTrue(state.home.bench.isEmpty())
            assertEquals(listOf("home-bench"), state.home.used.map { it.value.playerId })
            assertEquals(listOf(5, 6), state.events.map { it.legacyType })

            LegacyMatchModernResultMapper.map(
                state = state,
                matchId = event.matchId,
                homeClubId = event.homeClubId,
                awayClubId = event.awayClubId,
            )
        }

        assertTrue(substitutionApplied)
        assertEquals(0, result.match.homeGoals)
        assertEquals(0, result.match.awayGoals)
        database.close()

        database = database(context, name)
        val store = CareerMatchStore(database)
        assertTrue(store.loadSchedule("career-lineup").single().processed)
        assertEquals(result.match, store.findResult("career-lineup", "match-lineup"))

        val dao = database.careerPlayerRuntimeDao()
        val starterStats = dao.clubSeasonStatsForPlayer("career-lineup", "home-starter").single()
        assertEquals(initial.season.number, starterStats.legacySeasonId)
        assertEquals(101, starterStats.legacyClubId)
        assertEquals(1, starterStats.legacyH)
        assertTrue(requireNotNull(dao.findRuntime("career-lineup", "home-starter")).injuryUntilEpochDay >= 0L)

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `lineup evidence mapper rejects swapped legacy sides instead of silently redirecting clubs`() {
        val homeLineup = LegacyLineupCommitRule.commit(
            starterSlots = listOf(LegacyLineupCommitSlot("home-starter", 10)),
            benchPlayers = emptyList(),
            eligibleRoster = emptyList(),
            matchSideIndex = 1,
            mainTeamActivityPresent = false,
        )
        val awayLineup = LegacyLineupCommitRule.commit(
            starterSlots = listOf(LegacyLineupCommitSlot("away-starter", 10)),
            benchPlayers = emptyList(),
            eligibleRoster = emptyList(),
            matchSideIndex = 0,
            mainTeamActivityPresent = false,
        )

        var rejected = false
        try {
            CareerLineupMatchEvidenceMapper.fromLineups(
                home = homeLineup,
                away = awayLineup,
                homeSubstitutionsRemaining = 3,
                awaySubstitutionsRemaining = 3,
                homeLegacyModeFlag = false,
                awayLegacyModeFlag = false,
            )
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
    }

    private fun database(context: Context, name: String): FootballDynastyDatabase =
        Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()

    private suspend fun seed(database: FootballDynastyDatabase) {
        database.clubDao().upsertAll(listOf(club("home", 101), club("away", 202)))
        database.playerDao().upsertAll(
            listOf(
                player("home-starter", 35, 80),
                player("home-bench", 25, 72),
                player("away-starter", 25, 78),
            )
        )
        RoomCareerRepository(database) { 10L }.save(Career("career-lineup", "Phase 11", null, null))
        val dao = database.careerPlayerRuntimeDao()
        dao.upsertRuntime(runtime("home-starter", 35, 80))
        dao.upsertMembership(membership("home-starter", "home", 0))
        dao.upsertRuntime(runtime("home-bench", 25, 72))
        dao.upsertMembership(membership("home-bench", "home", 1))
        dao.upsertRuntime(runtime("away-starter", 25, 78))
        dao.upsertMembership(membership("away-starter", "away", 0))
    }

    private fun runtime(playerId: String, age: Int, overall: Int) = CareerPlayerRuntimeEntity(
        careerId = "career-lineup",
        playerId = playerId,
        sourceType = CareerPlayerRuntimeStore.SOURCE_CANONICAL,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = age,
        overall = overall,
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
        energy = 60,
        injuryUntilEpochDay = 0L,
    )

    private fun membership(playerId: String, clubId: String, ordinal: Int) =
        CareerSquadMembershipEntity(
            careerId = "career-lineup",
            playerId = playerId,
            clubId = clubId,
            rosterKind = "SENIOR",
            sourceOrdinal = ordinal,
        )

    private fun player(id: String, age: Int, overall: Int) = PlayerEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        name = id,
        age = age,
        country = 1,
        position = 3,
        status = 2,
        side = 0,
        cr1 = overall,
        cr2 = overall,
        star = false,
        worldTop = false,
        legacyAid = 0,
        legacySid = 0,
        legacyTid = 0,
        legacyHash = id.hashCode(),
    )

    private fun club(id: String, legacyId: Int) = ClubEntity(
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
        legacyId = legacyId,
        legacyValid = true,
    )
}
