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
import com.leomala.footballdynasty.domain.match.LegacyMatchRatingMetricRuntimeRules
import com.leomala.footballdynasty.domain.model.Career
import com.leomala.footballdynasty.domain.model.Match
import com.leomala.footballdynasty.foundation.random.SeededRandomSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerMatchRatedExecutionCoordinatorTest {
    @Test
    fun `rated execution uses simulator metrics persists V16 aggregate and leaves career rng untouched`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase16-rated-execution-reopen"
        context.deleteDatabase(name)
        var database = database(context, name)
        seed(database)

        val initial = CareerStateFactory.create("career-rated", 991122L)
        val scheduled = ScheduledCareerMatch(
            matchId = "rated-1",
            dayIndex = initial.calendar.currentDayIndex,
            eventTypeCode = 1,
            homeClubId = "home",
            awayClubId = "away",
        )
        CareerMatchStore(database).initializeSchedule(initial, listOf(scheduled))
        CareerCompetitionStore(database).initializeLeague(
            careerId = "career-rated",
            competitionId = "league-rated",
            legacyCompetitionType = 1,
            legacyFormatCode = -1,
            clubIds = listOf("home", "away"),
            roundMatchIds = listOf(listOf(scheduled.matchId)),
            legacyRelegationCount = 1,
            legacyLeagueSubtype = 1,
        )

        var implicitFactoryCalls = 0
        val result = CareerMatchExecutionCoordinator(database) { 44L }.executeWithRatings(
            careerId = "career-rated",
            matchId = scheduled.matchId,
            transientEvidence = transientEvidence(),
            implicitRatingRandomFactory = {
                implicitFactoryCalls += 1
                SeededRandomSource(implicitFactoryCalls.toLong())
            },
        ) { event, _, _ ->
            CareerMatchRatedSimulationResult(
                match = Match(event.matchId, event.homeClubId, event.awayClubId, 0, 0),
                ratingMetricState = LegacyMatchRatingMetricRuntimeRules.State(),
            )
        }

        assertEquals(2, implicitFactoryCalls)
        assertEquals(initial.random.draws, result.state.random.draws)
        assertEquals(result.match, CareerMatchStore(database).findResult("career-rated", scheduled.matchId))

        val beforeReopen = CareerCompetitionPlayerRatingStore(database).load(
            "career-rated",
            "league-rated",
        )
        assertEquals(listOf("away-player", "home-player"), beforeReopen.map { it.playerId })
        assertTrue(beforeReopen.all { it.legacyRatingCount == 1.0 })
        assertTrue(beforeReopen.all { it.legacyRatingSum > 0.0 })
        assertTrue(beforeReopen.all { it.legacyAverageRating > 0.0 })

        database.close()
        database = database(context, name)
        val reopened = CareerCompetitionPlayerRatingStore(database).load(
            "career-rated",
            "league-rated",
        )
        assertEquals(beforeReopen, reopened)
        assertEquals(result.match, CareerMatchStore(database).findResult("career-rated", scheduled.matchId))

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    private fun database(context: Context, name: String): FootballDynastyDatabase =
        Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()

    private suspend fun seed(database: FootballDynastyDatabase) {
        database.clubDao().upsertAll(listOf(club("home", 101), club("away", 202)))
        database.playerDao().upsertAll(
            listOf(player("home-player", 80), player("away-player", 78))
        )
        RoomCareerRepository(database) { 10L }.save(Career("career-rated", "Phase 16", null, null))
        val dao = database.careerPlayerRuntimeDao()
        dao.upsertRuntime(runtime("home-player", 80))
        dao.upsertMembership(membership("home-player", "home", 0))
        dao.upsertRuntime(runtime("away-player", 78))
        dao.upsertMembership(membership("away-player", "away", 0))
    }

    private fun transientEvidence() = CareerMatchPersistedRuntimeResolver.TransientMatchEvidence(
        home = CareerMatchPersistedRuntimeResolver.TransientClubEvidence(
            active = listOf(
                CareerMatchPersistedRuntimeResolver.TransientPlayerEvidence("home-player", legacyG0 = 10)
            ),
            bench = emptyList(),
            substitutionsRemaining = 0,
            legacyModeFlag = false,
        ),
        away = CareerMatchPersistedRuntimeResolver.TransientClubEvidence(
            active = listOf(
                CareerMatchPersistedRuntimeResolver.TransientPlayerEvidence("away-player", legacyG0 = 10)
            ),
            bench = emptyList(),
            substitutionsRemaining = 0,
            legacyModeFlag = false,
        ),
    )

    private fun runtime(playerId: String, overall: Int) = CareerPlayerRuntimeEntity(
        careerId = "career-rated",
        playerId = playerId,
        sourceType = CareerPlayerRuntimeStore.SOURCE_CANONICAL,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = 25,
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
        energy = 100,
        injuryUntilEpochDay = 0L,
    )

    private fun membership(playerId: String, clubId: String, ordinal: Int) =
        CareerSquadMembershipEntity(
            careerId = "career-rated",
            playerId = playerId,
            clubId = clubId,
            rosterKind = "SENIOR",
            sourceOrdinal = ordinal,
        )

    private fun player(id: String, overall: Int) = PlayerEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        name = id,
        age = 25,
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
