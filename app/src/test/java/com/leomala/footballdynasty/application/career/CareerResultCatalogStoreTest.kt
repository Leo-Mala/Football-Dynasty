package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.CareerScheduledMatchEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerResultCatalogStoreTest {
    @Test
    fun `results include only processed matches and preserve persisted score order`() = runBlocking {
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
                    scheduled(MATCH_A, dayIndex = 1, processed = true, homeGoals = 3, awayGoals = 2),
                    scheduled(MATCH_B, dayIndex = 2, processed = false),
                )
            )

            val catalog = CareerResultCatalogStore(
                CareerCalendarCatalogStore(database)
            ).loadResults(CAREER_A)

            requireNotNull(catalog)
            assertEquals(CAREER_A, catalog.careerId)
            assertEquals(3, catalog.currentDayIndex)
            assertEquals(listOf(MATCH_A), catalog.rows.map { it.matchId })
            assertEquals(3, catalog.rows.single().homeGoals)
            assertEquals(2, catalog.rows.single().awayGoals)
            assertEquals(CLUB_A, catalog.rows.single().homeClubName)
            assertEquals(CLUB_B, catalog.rows.single().awayClubName)
        } finally {
            database.close()
        }
    }

    @Test
    fun `processed match without complete persisted score fails closed`() = runBlocking {
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
                listOf(scheduled(MATCH_A, dayIndex = 1, processed = true, homeGoals = 1, awayGoals = null))
            )

            try {
                CareerResultCatalogStore(CareerCalendarCatalogStore(database)).loadResults(CAREER_A)
                fail("Expected incomplete persisted result to fail closed")
            } catch (_: IllegalStateException) {
                Unit
            }
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing career stays unavailable`() = runBlocking {
        val database = database()
        try {
            assertNull(
                CareerResultCatalogStore(CareerCalendarCatalogStore(database)).loadResults("missing")
            )
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
        const val MATCH_A = "match-a"
        const val MATCH_B = "match-b"
    }
}
