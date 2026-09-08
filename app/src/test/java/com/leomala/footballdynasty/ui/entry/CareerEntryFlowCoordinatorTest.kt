package com.leomala.footballdynasty.ui.entry

import com.leomala.footballdynasty.application.career.CareerEntrySummary
import com.leomala.footballdynasty.application.career.CareerSelectableClub
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.ui.navigation.LegacyUiDestination
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerEntryFlowCoordinatorTest {
    @Test
    fun `entry exposes persisted careers without inventing state`() = runBlocking {
        val careers = listOf(summary(CAREER_A, loadable = true), summary(CAREER_B, loadable = false))
        val coordinator = coordinator(careers = careers)

        val state = coordinator.openEntry()

        assertEquals(LegacyUiDestination.NEW_OR_LOAD_CAREER, state.destination)
        assertEquals(careers, state.careers)
        assertTrue(state.clubs.isEmpty())
    }

    @Test
    fun `club selection preserves canonical list and only accepts members`() = runBlocking {
        val clubs = listOf(club(CLUB_A), club(CLUB_B))
        val coordinator = coordinator(clubs = clubs)
        val state = coordinator.openClubSelection()

        assertEquals(LegacyUiDestination.CLUB_SELECTION, state.destination)
        assertEquals(clubs, state.clubs)
        assertEquals(CLUB_B, coordinator.selectClub(state, CLUB_B).selectedClubId)

        var rejected = false
        try {
            coordinator.selectClub(state, "missing-club")
        } catch (_: IllegalArgumentException) {
            rejected = true
        }
        assertTrue(rejected)
    }

    @Test
    fun `load fails closed when persisted career is not loadable`() = runBlocking {
        val coordinator = coordinator(loadedCareer = null)

        assertEquals(
            CareerEntryOpenResult.NotLoadable(CAREER_A),
            coordinator.openCareer(CAREER_A),
        )
    }

    @Test
    fun `load returns certified career state without mutation`() = runBlocking {
        val expected = CareerStateFactory.create(
            id = CAREER_A,
            seed = 42L,
            managedClubId = CLUB_A,
        )
        val coordinator = coordinator(loadedCareer = expected)

        val result = coordinator.openCareer(CAREER_A)

        assertTrue(result is CareerEntryOpenResult.Loaded)
        assertEquals(expected, (result as CareerEntryOpenResult.Loaded).state)
    }

    private fun coordinator(
        careers: List<CareerEntrySummary> = emptyList(),
        clubs: List<CareerSelectableClub> = emptyList(),
        loadedCareer: com.leomala.footballdynasty.domain.career.CareerState? = null,
    ) = CareerEntryFlowCoordinator(
        listCareers = { careers },
        loadCareer = { loadedCareer },
        listClubs = { clubs },
    )

    private fun summary(id: String, loadable: Boolean) = CareerEntrySummary(
        careerId = id,
        displayName = id,
        updatedAtEpochMillis = 1L,
        loadable = loadable,
        seasonNumber = if (loadable) 1 else null,
        seasonYear = if (loadable) 2026 else null,
        currentDayIndex = if (loadable) 0 else null,
        managedClubId = if (loadable) CLUB_A else null,
        managedClubName = if (loadable) "Club A" else null,
    )

    private fun club(id: String) = CareerSelectableClub(
        id = id,
        sourceFileRef = "teams/$id.ban",
        name = id,
        country = 1,
        state = 1,
        level = 1,
    )

    private companion object {
        const val CAREER_A = "career-a"
        const val CAREER_B = "career-b"
        const val CLUB_A = "club-a"
        const val CLUB_B = "club-b"
    }
}
