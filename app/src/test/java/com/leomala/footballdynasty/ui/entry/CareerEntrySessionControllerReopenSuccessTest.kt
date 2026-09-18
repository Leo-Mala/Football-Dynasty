package com.leomala.footballdynasty.ui.entry

import com.leomala.footballdynasty.application.career.CareerEntrySummary
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Test

class CareerEntrySessionControllerReopenSuccessTest {
    @Test
    fun `successful reopen refreshes entry and replaces in-memory career instance`() = runBlocking {
        val first = career(dayOffset = 0)
        val reopened = career(dayOffset = 1)
        var current = first
        var entryReads = 0
        val coordinator = CareerEntryFlowCoordinator(
            listCareers = {
                entryReads += 1
                listOf(summary(current))
            },
            loadCareer = { id -> if (id == CAREER_ID) current else null },
            listClubs = { emptyList() },
        )
        val session = CareerEntrySessionController(coordinator)

        var state = session.openEntry()
        state = session.openCareer(state, CAREER_ID)
        val loadedBeforeClose = state.loadedCareer

        current = reopened
        state = session.reopenCareer(state, CAREER_ID)

        assertEquals(2, entryReads)
        assertNotSame(loadedBeforeClose, state.loadedCareer)
        assertEquals(reopened.calendar.currentDayIndex, state.loadedCareer?.calendar?.currentDayIndex)
        assertFalse(state.loadError)
    }

    private fun career(dayOffset: Int): CareerState = CareerStateFactory.create(
        id = CAREER_ID,
        seed = 42L,
        managedClubId = CLUB_ID,
    ).let { base ->
        if (dayOffset == 0) base else base.copy(
            calendar = base.calendar.copy(currentDayIndex = base.calendar.currentDayIndex + dayOffset),
        )
    }

    private fun summary(state: CareerState) = CareerEntrySummary(
        careerId = state.id,
        displayName = state.id,
        updatedAtEpochMillis = state.calendar.currentDayIndex.toLong(),
        loadable = true,
        seasonNumber = state.season.number,
        seasonYear = state.season.year,
        currentDayIndex = state.calendar.currentDayIndex,
        managedClubId = state.managedClub?.clubId,
        managedClubName = state.managedClub?.clubId,
    )

    private companion object {
        const val CAREER_ID = "career-ui-session-reopen-success"
        const val CLUB_ID = "club-a"
    }
}
