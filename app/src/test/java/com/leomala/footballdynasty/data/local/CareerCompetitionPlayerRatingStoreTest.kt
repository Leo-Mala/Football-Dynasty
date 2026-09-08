package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.repository.RoomCareerRepository
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.model.Career
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerCompetitionPlayerRatingStoreTest {
    @Test
    fun `store resolves proven competition owner accumulates and fails closed`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase16-competition-player-rating-store"
        context.deleteDatabase(name)
        val database = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        database.clubDao().upsertAll(listOf(club("a"), club("b")))
        RoomCareerRepository(database) { 10L }.save(Career("career-rating", "Rating", null, null))

        val state = CareerStateFactory.create("career-rating", 445566L)
        val day = state.calendar.currentDayIndex
        val schedule = listOf(
            ScheduledCareerMatch("m-type1", day, 1, "a", "b"),
            ScheduledCareerMatch("m-type2", day, 1, "a", "b"),
            ScheduledCareerMatch("m-orphan", day, 1, "a", "b"),
            ScheduledCareerMatch("m-multi", day, 1, "a", "b"),
        )
        CareerMatchStore(database).initializeSchedule(state, schedule)
        val competitionStore = CareerCompetitionStore(database)
        competitionStore.initializeLeague(
            careerId = "career-rating",
            competitionId = "league-type1",
            legacyCompetitionType = 1,
            legacyFormatCode = -1,
            clubIds = listOf("a", "b"),
            roundMatchIds = listOf(listOf("m-type1")),
        )
        competitionStore.initializeLeague(
            careerId = "career-rating",
            competitionId = "league-type2",
            legacyCompetitionType = 2,
            legacyFormatCode = -1,
            clubIds = listOf("a", "b"),
            roundMatchIds = listOf(listOf("m-type2")),
        )
        competitionStore.initializeLeague(
            careerId = "career-rating",
            competitionId = "league-multi-a",
            legacyCompetitionType = 1,
            legacyFormatCode = -1,
            clubIds = listOf("a", "b"),
            roundMatchIds = listOf(listOf("m-multi")),
        )
        competitionStore.initializeLeague(
            careerId = "career-rating",
            competitionId = "league-multi-b",
            legacyCompetitionType = 1,
            legacyFormatCode = -1,
            clubIds = listOf("a", "b"),
            roundMatchIds = listOf(listOf("m-multi")),
        )

        val store = CareerCompetitionPlayerRatingStore(database)
        val goalkeeper = CareerCompetitionPlayerRatingMutation(
            playerId = "p1",
            ratingY0 = 7.0,
            legacyG0 = 1,
            legacyL0 = 0,
            legacyF0 = 0,
            legacyR = 0,
        )

        assertTrue(store.applyForMatch("career-rating", "m-orphan", listOf(goalkeeper)).isEmpty())
        assertTrue(store.applyForMatch("career-rating", "m-type2", listOf(goalkeeper)).isEmpty())
        assertTrue(store.load("career-rating", "league-type2").isEmpty())

        store.applyForMatch("career-rating", "m-type1", listOf(goalkeeper))
        store.applyForMatch(
            "career-rating",
            "m-type1",
            listOf(goalkeeper.copy(ratingY0 = 5.0)),
        )
        val row = store.load("career-rating", "league-type1").single()
        assertEquals("p1", row.playerId)
        assertEquals(12.0, row.legacyRatingSum, 0.0)
        assertEquals(2.0, row.legacyRatingCount, 0.0)
        assertEquals(6.0, row.legacyAverageRating, 0.0)
        assertEquals(0, row.legacyCategory)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                store.applyForMatch("career-rating", "m-multi", listOf(goalkeeper))
            }
        }

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    private fun club(id: String) = ClubEntity(
        id = id, dataVersion = 1, importScope = null, sourceFileRef = id, name = id,
        country = 0, state = 0, level = 1, stadium = "", capacity = 0, reputation = 0,
        primaryColor = "", secondaryColor = "", coach = "", coachCountry = 0, baseColor = 0,
        legacyAid = 0, legacySid = 0, legacyTid = 0, legacyVid = 0, legacyId = 0, legacyValid = true,
    )
}
