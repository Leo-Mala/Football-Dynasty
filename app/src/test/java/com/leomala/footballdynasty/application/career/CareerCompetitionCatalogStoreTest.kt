package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionEntity
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionStandingEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerCompetitionCatalogStoreTest {
    @Test
    fun `competition projection preserves persisted competition and stable standing order`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A), club(CLUB_B)))
            CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
                careerId = CAREER_A,
                displayName = "Career A",
                seed = 7L,
                managedClubId = CLUB_A,
            )
            database.careerCompetitionDao().upsertCompetition(
                CareerCompetitionEntity(
                    careerId = CAREER_A,
                    competitionId = COMPETITION,
                    legacyCompetitionType = 4,
                    legacyFormatCode = 2,
                    currentRoundNumber = 3,
                    totalRounds = 10,
                )
            )
            database.careerCompetitionDao().upsertStandings(
                listOf(
                    standing(CLUB_B, stableOrdinal = 1, points = 7),
                    standing(CLUB_A, stableOrdinal = 0, points = 9),
                )
            )

            val competitions = CareerCompetitionCatalogStore(database).loadCompetitions(CAREER_A)

            assertEquals(1, competitions.size)
            val competition = competitions.single()
            assertEquals(COMPETITION, competition.competitionId)
            assertEquals(3, competition.currentRoundNumber)
            assertEquals(10, competition.totalRounds)
            assertEquals(listOf(CLUB_A, CLUB_B), competition.standings.map { it.clubId })
            assertEquals(listOf(0, 1), competition.standings.map { it.stableOrdinal })
            assertEquals(listOf(9, 7), competition.standings.map { it.points })
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing career fails closed instead of synthesizing competitions`() = runBlocking {
        val database = database()
        try {
            assertTrue(CareerCompetitionCatalogStore(database).loadCompetitions("missing").isEmpty())
        } finally {
            database.close()
        }
    }

    private fun standing(clubId: String, stableOrdinal: Int, points: Int) =
        CareerCompetitionStandingEntity(
            careerId = CAREER_A,
            competitionId = COMPETITION,
            clubId = clubId,
            stableOrdinal = stableOrdinal,
            points = points,
            played = 3,
            wins = 2,
            losses = 0,
            goalsFor = 5,
            goalsAgainst = 2,
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
    }
}
