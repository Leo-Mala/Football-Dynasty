package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.CareerMatchStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.repository.RoomCareerStateRepository
import com.leomala.footballdynasty.domain.career.CareerScheduleCalendarProjection
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.foundation.error.InvalidCareerStateException
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
class CareerCalendarCommandStoreTest {
    @Test
    fun `next persisted playable day is selected and survives database reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase17-calendar-next-event"
        context.deleteDatabase(name)
        var database = database(context, name)
        database.clubDao().upsertAll(listOf(club(CLUB_A), club(CLUB_B)))
        val initial = CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
            careerId = CAREER,
            displayName = "Calendar career",
            seed = 17L,
            managedClubId = CLUB_A,
        )
        CareerMatchStore(database, clockMillis = { 100L }).initializeSchedule(
            initial,
            listOf(
                scheduled("already-done", dayIndex = 4, eventTypeCode = 1, processed = true),
                scheduled("next-a", dayIndex = 6, eventTypeCode = 1, processed = false),
                scheduled("next-b", dayIndex = 6, eventTypeCode = 2, processed = false, reverse = true),
                scheduled("later", dayIndex = 9, eventTypeCode = 3, processed = false),
            ),
        )

        val transition = CareerCalendarCommandStore(database, clockMillis = { 200L })
            .moveToNextScheduledEvent(CAREER)

        assertEquals(true, transition.eventFound)
        assertEquals(6, transition.legacyReturnValue)
        assertEquals(6, transition.state.calendar.currentDayIndex)

        database.close()
        database = database(context, name)
        val reopened = requireNotNull(RoomCareerStateRepository(database).findById(CAREER))
        assertEquals(6, reopened.calendar.currentDayIndex)
        assertEquals(transition.state, reopened)

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `canonical day projection preserves certified multi match aggregation`() {
        val days = CareerScheduleCalendarProjection.calendarDays(
            listOf(
                scheduled("a", dayIndex = 8, eventTypeCode = 1, processed = true),
                scheduled("b", dayIndex = 8, eventTypeCode = 3, processed = false, reverse = true),
                scheduled("c", dayIndex = 10, eventTypeCode = 2, processed = true),
            )
        )

        assertEquals(listOf(8, 10), days.map { it.dayIndex })
        assertEquals(3, days[0].eventTypeCode)
        assertEquals(2, days[0].matchCount)
        assertFalse(days[0].processed)
        assertEquals(2, days[1].eventTypeCode)
        assertEquals(1, days[1].matchCount)
        assertTrue(days[1].processed)
    }

    @Test
    fun `no unprocessed scheduled event preserves persisted career position`() = runBlocking {
        val database = inMemoryDatabase()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A), club(CLUB_B)))
            val initial = CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
                careerId = CAREER,
                displayName = null,
                seed = 19L,
                managedClubId = CLUB_A,
            )
            CareerMatchStore(database, clockMillis = { 100L }).initializeSchedule(
                initial,
                listOf(scheduled("done", dayIndex = 7, eventTypeCode = 1, processed = true)),
            )

            val transition = CareerCalendarCommandStore(database, clockMillis = { 200L })
                .moveToNextScheduledEvent(CAREER)

            assertEquals(false, transition.eventFound)
            assertEquals(0, transition.legacyReturnValue)
            assertEquals(initial.calendar.currentDayIndex, transition.state.calendar.currentDayIndex)
            assertEquals(initial, requireNotNull(RoomCareerStateRepository(database).findById(CAREER)))
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing career fails closed instead of synthesizing state`() = runBlocking {
        val database = inMemoryDatabase()
        try {
            var failed = false
            try {
                CareerCalendarCommandStore(database).moveToNextScheduledEvent("missing")
            } catch (_: InvalidCareerStateException) {
                failed = true
            }
            assertTrue(failed)
        } finally {
            database.close()
        }
    }

    private fun scheduled(
        matchId: String,
        dayIndex: Int,
        eventTypeCode: Int,
        processed: Boolean,
        reverse: Boolean = false,
    ) = ScheduledCareerMatch(
        matchId = matchId,
        dayIndex = dayIndex,
        eventTypeCode = eventTypeCode,
        homeClubId = if (reverse) CLUB_B else CLUB_A,
        awayClubId = if (reverse) CLUB_A else CLUB_B,
        processed = processed,
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

    private fun inMemoryDatabase(): FootballDynastyDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private fun database(context: Context, name: String): FootballDynastyDatabase =
        Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .build()

    private companion object {
        const val CAREER = "career-calendar-command"
        const val CLUB_A = "club-a"
        const val CLUB_B = "club-b"
    }
}
