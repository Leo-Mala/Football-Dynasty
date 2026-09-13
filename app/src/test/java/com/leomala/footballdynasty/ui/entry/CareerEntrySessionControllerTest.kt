package com.leomala.footballdynasty.ui.entry

import com.leomala.footballdynasty.application.career.CareerEntrySummary
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerEntrySessionControllerTest {
    @Test
    fun `close clears in-memory career and refreshes persisted entry catalog`() = runBlocking {
        var catalogReads = 0
        var persisted = career()
        val coordinator = CareerEntryFlowCoordinator(
            listCareers = {
                catalogReads += 1
                listOf(summary(persisted))
            },
            loadCareer = { id -> if (id == CAREER_ID) persisted else null },
            listClubs = { emptyList() },
        )
        val session = CareerEntrySessionController(coordinator)

        var state = session.openEntry()
        state = session.openCareer(state, CAREER_ID)
        assertEquals(1, catalogReads)
        assertEquals(CAREER_ID, state.loadedCareer?.id)

        val newDayIndex = persisted.calendar.currentDayIndex + 1
        persisted = persisted.copy(
            calendar = persisted.calendar.copy(currentDayIndex = newDayIndex),
        )

        state = session.closeCareer(state)

        assertEquals(2, catalogReads)
        assertNull(state.loadedCareer)
        assertFalse(state.loadError)
        assertEquals(newDayIndex, state.entry?.careers?.single()?.currentDayIndex)
    }

    @Test
    fun `reopen after close loads durable state instead of closed snapshot`() = runBlocking {
        var persisted = career()
        val coordinator = CareerEntryFlowCoordinator(
            listCareers = { listOf(summary(persisted)) },
            loadCareer = { id -> if (id == CAREER_ID) persisted else null },
            listClubs = { emptyList() },
        )
        val session = CareerEntrySessionController(coordinator)

        var state = session.openCareer(session.openEntry(), CAREER_ID)
        val originalDayIndex = state.loadedCareer!!.calendar.currentDayIndex
        state = session.closeCareer(state)

        persisted = persisted.copy(
            calendar = persisted.calendar.copy(currentDayIndex = originalDayIndex + 1),
        )
        state = session.openCareer(state, CAREER_ID)

        assertEquals(originalDayIndex + 1, state.loadedCareer?.calendar?.currentDayIndex)
        assertFalse(state.loadError)
    }

    @Test
    fun `failed reopen never preserves previously loaded career`() = runBlocking {
        val persisted = career()
        var loadable = true
        val coordinator = CareerEntryFlowCoordinator(
            listCareers = { listOf(summary(persisted)) },
            loadCareer = { id -> if (id == CAREER_ID && loadable) persisted else null },
            listClubs = { emptyList() },
        )
        val session = CareerEntrySessionController(coordinator)

        var state = session.openCareer(session.openEntry(), CAREER_ID)
        assertTrue(state.loadedCareer != null)

        state = session.closeCareer(state)
        loadable = false
        state = session.openCareer(state, CAREER_ID)

        assertNull(state.loadedCareer)
        assertTrue(state.loadError)
    }

    private fun career(): CareerState = CareerStateFactory.create(
        id = CAREER_ID,
        seed = 42L,
        managedClubId = CLUB_ID,
    )

    private fun summary(state: CareerState) = CareerEntrySummary(
        careerId = state.id,
        displayName = state.id,
        updatedAtEpochMillis = 1L,
        loadable = true,
        seasonNumber = state.season.number,
        seasonYear = state.season.year,
        currentDayIndex = state.calendar.currentDayIndex,
        managedClubId = state.managedClub?.clubId,
        managedClubName = state.managedClub?.clubId,
    )

    private companion object {
        const val CAREER_ID = "career-ui-session"
        const val CLUB_ID = "club-a"
    }
}
