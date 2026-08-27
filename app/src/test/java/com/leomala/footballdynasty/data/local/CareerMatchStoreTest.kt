package com.leomala.footballdynasty.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerMatchStoreTest {
    @Test
    fun `resolved match rng calendar and schedule survive database reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase9-match-store-reopen"
        context.deleteDatabase(name)
        var database = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        database.clubDao().upsertAll(listOf(club("club-a"), club("club-b"), club("club-c"), club("club-d")))
        RoomCareerRepository(database) { 100L }.save(
            Career("career-match", "Phase 9", null, null)
        )

        val initial = CareerStateFactory.create("career-match", 998877L)
        val firstDay = initial.calendar.currentDayIndex
        val secondDay = firstDay + 2
        val schedule = listOf(
            ScheduledCareerMatch("m1", firstDay, 1, "club-a", "club-b"),
            ScheduledCareerMatch("m2", secondDay, 1, "club-c", "club-d"),
        )
        val store = CareerMatchStore(database) { 200L }
        store.initializeSchedule(initial, schedule)
        val resolved = CareerMatchRuntimeBridge.run(initial, schedule, "m1") { scheduled, random ->
            Match(
                scheduled.matchId,
                scheduled.homeClubId,
                scheduled.awayClubId,
                random.nextInt(4),
                random.nextInt(4),
            )
        }
        store.commitMatch(resolved)
        database.close()

        database = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        val reopenedStore = CareerMatchStore(database)
        val reopenedState = requireNotNull(RoomCareerStateRepository(database).findById("career-match"))
        val reopenedSchedule = reopenedStore.loadSchedule("career-match")
        val reopenedMatch = requireNotNull(reopenedStore.findResult("career-match", "m1"))

        assertEquals(resolved.state, reopenedState)
        assertEquals(resolved.schedule, reopenedSchedule)
        assertEquals(resolved.match, reopenedMatch)
        assertEquals(initial.random.draws + 2L, reopenedState.random.draws)
        assertEquals(secondDay, reopenedState.calendar.currentDayIndex)

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `schedule initialization rolls back career state when club foreign key fails`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        database.clubDao().upsertAll(listOf(club("club-a")))
        RoomCareerRepository(database) { 100L }.save(
            Career("rollback-career", "Rollback", null, null)
        )
        val state = CareerStateFactory.create("rollback-career", 123L)
        val store = CareerMatchStore(database)

        try {
            store.initializeSchedule(
                state,
                listOf(
                    ScheduledCareerMatch(
                        "bad-match",
                        state.calendar.currentDayIndex,
                        1,
                        "club-a",
                        "missing-club",
                    )
                ),
            )
            fail("Expected scheduled match club foreign key rejection")
        } catch (_: SQLiteConstraintException) {
            // Expected: schedule insert fails after core-state write and the transaction rolls both back.
        }

        assertNull(database.careerCoreStateDao().findById("rollback-career"))
        assertEquals(emptyList<ScheduledCareerMatch>(), store.loadSchedule("rollback-career"))
        database.close()
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
