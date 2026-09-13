package com.leomala.footballdynasty.ui.entry

import com.leomala.footballdynasty.application.career.CareerEntrySummary
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerEntrySessionControllerFailureTest {
    @Test
    fun `failed reopen clears loaded career but preserves freshly read entry catalog`() = runBlocking {
        val persisted = career()
        var loadable = true
        val coordinator = CareerEntryFlowCoordinator(
            listCareers = { listOf(summary(persisted)) },
            loadCareer = { id -> if (id == CAREER_ID && loadable) persisted else null },
            listClubs = { emptyList() },
        )
        val session = CareerEntrySessionController(coordinator)

        var state = session.openEntry()
        state = session.openCareer(state, CAREER_ID)
        assertEquals(CAREER_ID, state.loadedCareer?.id)

        loadable = false
        state = session.openCareer(state, CAREER_ID)

        assertNull(state.loadedCareer)
        assertTrue(state.loadError)
        assertEquals(CAREER_ID, state.entry?.careers?.single()?.careerId)
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
        const val CAREER_ID = "career-ui-session-failure"
        const val CLUB_ID = "club-a"
    }
}
