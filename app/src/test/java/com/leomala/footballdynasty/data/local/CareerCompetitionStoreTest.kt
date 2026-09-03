package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.repository.RoomCareerRepository
import com.leomala.footballdynasty.domain.career.CareerMatchRuntimeBridge
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.model.Career
import com.leomala.footballdynasty.domain.model.Match
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerCompetitionStoreTest {
    @Test
    fun `last match atomically advances standings and exact relegation count survives reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase10-competition-reopen"
        context.deleteDatabase(name)
        var database = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        database.clubDao().upsertAll(listOf(club("a"), club("b"), club("c"), club("d")))
        RoomCareerRepository(database) { 10L }.save(Career("career-league", "League", null, null))

        val initial = CareerStateFactory.create("career-league", 445566L)
        val firstDay = initial.calendar.currentDayIndex
        val secondDay = firstDay + 2
        val schedule = listOf(
            ScheduledCareerMatch("m1", firstDay, 1, "a", "b"),
            ScheduledCareerMatch("m2", firstDay, 1, "c", "d"),
            ScheduledCareerMatch("m3", secondDay, 1, "a", "c"),
            ScheduledCareerMatch("m4", secondDay, 1, "b", "d"),
        )
        val matchStore = CareerMatchStore(database)
        matchStore.initializeSchedule(initial, schedule)
        val competitionStore = CareerCompetitionStore(database)
        competitionStore.initializeLeague(
            careerId = "career-league",
            competitionId = "league-1",
            legacyCompetitionType = 1,
            legacyFormatCode = -1,
            clubIds = listOf("a", "b", "c", "d"),
            roundMatchIds = listOf(listOf("m1", "m2"), listOf("m3", "m4")),
            legacyRelegationCount = 2,
        )

        val first = CareerMatchRuntimeBridge.run(initial, schedule, "m1") { scheduled, _ ->
            Match(scheduled.matchId, scheduled.homeClubId, scheduled.awayClubId, 2, 0)
        }
        matchStore.commitMatch(first)
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { competitionStore.completeCurrentRound("career-league", "league-1") }
        }
        assertEquals(
            1,
            requireNotNull(competitionStore.load("career-league", "league-1")).currentRoundNumber,
        )

        val second = CareerMatchRuntimeBridge.run(first.state, first.schedule, "m2") { scheduled, _ ->
            Match(scheduled.matchId, scheduled.homeClubId, scheduled.awayClubId, 1, 1)
        }
        matchStore.commitMatch(second)
        val advanced = requireNotNull(competitionStore.load("career-league", "league-1"))

        assertEquals(2, advanced.currentRoundNumber)
        assertEquals(2, advanced.legacyRelegationCount)
        assertEquals(listOf("a", "c", "d", "b"), advanced.standings.map { it.clubId })
        assertEquals(listOf(3, 1, 1, 0), advanced.standings.map { it.points })
        assertEquals(listOf(1, 1, 1, 1), advanced.standings.map { it.played })

        database.close()
        database = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        val reopened = requireNotNull(CareerCompetitionStore(database).load("career-league", "league-1"))
        assertEquals(advanced, reopened)
        assertEquals(2, reopened.legacyRelegationCount)

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
