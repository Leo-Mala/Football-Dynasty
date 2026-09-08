package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionEntity
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionMatchEntity
import com.leomala.footballdynasty.data.local.entity.CareerScheduledMatchEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerCalendarCatalogStoreTest {
    @Test
    fun `calendar projection preserves persisted day order scores and competition round links`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A), club(CLUB_B)))
            CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
                careerId = CAREER_A,
                displayName = "Career A",
                seed = 7L,
                managedClubId = CLUB_A,
            )
            database.careerScheduledMatchDao().upsertAll(
                listOf(
                    scheduled(matchId = MATCH_B, dayIndex = 2, processed = false),
                    scheduled(matchId = MATCH_A, dayIndex = 1, processed = true, homeGoals = 2, awayGoals = 1),
                )
            )
            database.careerCompetitionDao().upsertCompetition(
                CareerCompetitionEntity(
                    careerId = CAREER_A,
                    competitionId = COMPETITION,
                    legacyCompetitionType = 4,
                    legacyFormatCode = 2,
                    currentRoundNumber = 2,
                    totalRounds = 10,
                )
            )
            database.careerCompetitionDao().upsertMatches(
                listOf(
                    CareerCompetitionMatchEntity(
                        careerId = CAREER_A,
                        competitionId = COMPETITION,
                        matchId = MATCH_A,
                        roundNumber = 1,
                        fixtureOrdinal = 0,
                    ),
                    CareerCompetitionMatchEntity(
                        careerId = CAREER_A,
                        competitionId = COMPETITION,
                        matchId = MATCH_B,
                        roundNumber = 2,
                        fixtureOrdinal = 0,
                    ),
                )
            )

            val calendar = CareerCalendarCatalogStore(database).loadCalendar(CAREER_A)

            requireNotNull(calendar)
            assertEquals(3, calendar.currentDayIndex)
            assertEquals(listOf(MATCH_A, MATCH_B), calendar.matches.map { it.matchId })
            assertEquals(listOf(1, 2), calendar.matches.map { it.dayIndex })
            assertEquals("2:1", "${calendar.matches[0].homeGoals}:${calendar.matches[0].awayGoals}")
            assertEquals(true, calendar.matches[0].processed)
            assertEquals(false, calendar.matches[1].processed)
            assertEquals(COMPETITION, calendar.matches[0].competitionLinks.single().competitionId)
            assertEquals(1, calendar.matches[0].competitionLinks.single().roundNumber)
            assertEquals(2, calendar.matches[1].competitionLinks.single().roundNumber)
            assertEquals(CLUB_A, calendar.matches[0].homeClubName)
            assertEquals(CLUB_B, calendar.matches[0].awayClubName)
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing career fails closed instead of synthesizing calendar`() = runBlocking {
        val database = database()
        try {
            assertNull(CareerCalendarCatalogStore(database).loadCalendar("missing"))
        } finally {
            database.close()
        }
    }

    private fun scheduled(
        matchId: String,
        dayIndex: Int,
        processed: Boolean,
        homeGoals: Int? = null,
        awayGoals: Int? = null,
    ) = CareerScheduledMatchEntity(
        careerId = CAREER_A,
        matchId = matchId,
        dayIndex = dayIndex,
        eventTypeCode = 0,
        homeClubId = CLUB_A,
        awayClubId = CLUB_B,
        processed = processed,
        homeGoals = homeGoals,
        awayGoals = awayGoals,
    )

    private fun club(id: String) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = "teams/$id.ban",
        name = id,
        country = 11,
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

    private fun database(): FootballDynastyDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private companion object {
        const val CAREER_A = "career-a"
        const val CLUB_A = "club-a"
        const val CLUB_B = "club-b"
        const val COMPETITION = "competition-a"
        const val MATCH_A = "match-a"
        const val MATCH_B = "match-b"
    }
}
