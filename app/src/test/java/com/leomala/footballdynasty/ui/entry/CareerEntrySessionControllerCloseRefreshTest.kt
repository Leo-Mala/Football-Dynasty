package com.leomala.footballdynasty.ui.entry

import com.leomala.footballdynasty.application.career.CareerEntrySummary
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CareerEntrySessionControllerCloseRefreshTest {
    @Test
    fun `close career refreshes persisted entry catalog on every invocation`() = runBlocking {
        var catalogReads = 0
        val career = CareerStateFactory.create(
            id = CAREER_ID,
            seed = 42L,
            managedClubId = CLUB_ID,
        )
        val coordinator = CareerEntryFlowCoordinator(
            listCareers = {
                catalogReads += 1
                listOf(
                    CareerEntrySummary(
                        careerId = career.id,
                        displayName = career.id,
                        updatedAtEpochMillis = catalogReads.toLong(),
                        loadable = true,
                        seasonNumber = career.season.number,
                        seasonYear = career.season.year,
                        currentDayIndex = career.calendar.currentDayIndex,
                        managedClubId = career.managedClub?.clubId,
                        managedClubName = career.managedClub?.clubId,
                    ),
                )
            },
            loadCareer = { career },
            listClubs = { emptyList() },
        )
        val session = CareerEntrySessionController(coordinator)

        var state = session.openEntry()
        assertEquals(1L, state.entry?.careers?.single()?.updatedAtEpochMillis)

        state = session.openCareer(state, CAREER_ID)
        state = session.closeCareer(state)
        assertEquals(2L, state.entry?.careers?.single()?.updatedAtEpochMillis)

        state = session.closeCareer(state)
        assertEquals(3L, state.entry?.careers?.single()?.updatedAtEpochMillis)
        assertEquals(3, catalogReads)
    }

    private companion object {
        const val CAREER_ID = "career-ui-session-close-refresh"
        const val CLUB_ID = "club-a"
    }
}
