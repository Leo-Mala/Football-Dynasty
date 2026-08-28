package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.repository.RoomCareerRepository
import com.leomala.footballdynasty.data.repository.RoomCareerStateRepository
import com.leomala.footballdynasty.domain.career.CareerMatchRuntimeBridge
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.competition.LegacyLeagueFixtureRules
import com.leomala.footballdynasty.domain.model.Career
import com.leomala.footballdynasty.domain.model.Match
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerCompetitionCycleIntegrationTest {
    @Test
    fun `calendar round match result table next round and finish survive reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase10-full-league-cycle"
        context.deleteDatabase(name)
        var database = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        val clubIds = listOf("a", "b", "c", "d")
        database.clubDao().upsertAll(clubIds.map(::club))
        RoomCareerRepository(database) { 10L }.save(Career("career-cycle", "Cycle", null, null))

        var state = CareerStateFactory.create("career-cycle", 987654321L)
        val initialDraws = state.random.draws
        val fixtureRounds = LegacyLeagueFixtureRules.generate(clubIds, legacyCycleCode = 1)
        assertEquals(3, fixtureRounds.size)
        assertTrue(fixtureRounds.flatten().all { it.homeClubId != null && it.awayClubId != null })

        val roundMatchIds = fixtureRounds.mapIndexed { roundIndex, round ->
            round.indices.map { fixtureIndex -> "r${roundIndex + 1}m${fixtureIndex + 1}" }
        }
        val firstDay = state.calendar.currentDayIndex
        var schedule = fixtureRounds.flatMapIndexed { roundIndex, round ->
            val dayIndex = firstDay + (roundIndex * 2)
            round.mapIndexed { fixtureIndex, fixture ->
                ScheduledCareerMatch(
                    matchId = roundMatchIds[roundIndex][fixtureIndex],
                    dayIndex = dayIndex,
                    eventTypeCode = 1,
                    homeClubId = requireNotNull(fixture.homeClubId),
                    awayClubId = requireNotNull(fixture.awayClubId),
                )
            }
        }

        val matchStore = CareerMatchStore(database)
        matchStore.initializeSchedule(state, schedule)
        val competitionStore = CareerCompetitionStore(database)
        competitionStore.initializeLeague(
            careerId = "career-cycle",
            competitionId = "league-cycle",
            legacyCompetitionType = 1,
            legacyFormatCode = -1,
            clubIds = clubIds,
            roundMatchIds = roundMatchIds,
        )

        fixtureRounds.forEachIndexed { roundIndex, round ->
            round.forEachIndexed { fixtureIndex, _ ->
                val matchId = roundMatchIds[roundIndex][fixtureIndex]
                val resolved = CareerMatchRuntimeBridge.run(state, schedule, matchId) { scheduled, random ->
                    Match(
                        id = scheduled.matchId,
                        homeClubId = scheduled.homeClubId,
                        awayClubId = scheduled.awayClubId,
                        homeGoals = random.nextInt(3),
                        awayGoals = random.nextInt(3),
                    )
                }
                matchStore.commitMatch(resolved)
                state = resolved.state
                schedule = resolved.schedule
            }
            val afterRound = requireNotNull(competitionStore.load("career-cycle", "league-cycle"))
            assertEquals(roundIndex + 2, afterRound.currentRoundNumber)
        }

        val finished = requireNotNull(competitionStore.load("career-cycle", "league-cycle"))
        assertEquals(3, finished.totalRounds)
        assertEquals(4, finished.currentRoundNumber)
        assertTrue(finished.finished)
        assertTrue(finished.standings.all { it.played == 3 })
        assertEquals(initialDraws + 12L, state.random.draws)

        database.close()
        database = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        val reopenedCompetition = requireNotNull(
            CareerCompetitionStore(database).load("career-cycle", "league-cycle")
        )
        val reopenedCareer = requireNotNull(RoomCareerStateRepository(database).findById("career-cycle"))
        assertEquals(finished, reopenedCompetition)
        assertEquals(state.random, reopenedCareer.random)
        assertEquals(schedule, CareerMatchStore(database).loadSchedule("career-cycle"))

        database.close()
        context.deleteDatabase(name)
        Unit
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
}
