package com.leomala.footballdynasty.application.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerManagedLineupSelectionPlannerTest {
    @Test
    fun `confirmation binds exact prepared match and modern activity ownership`() {
        val inputs = inputs()

        val selection = CareerManagedLineupSelectionPlanner.confirm(
            inputs = inputs,
            formationIndex = 3,
        )

        assertEquals("career-1", selection.careerId)
        assertEquals("match-1", selection.matchId)
        assertEquals("club-managed", selection.clubId)
        assertEquals(CareerLineupInputCatalogStore.ManagedMatchSide.HOME, selection.managedSide)
        assertEquals(3, selection.formationIndex)
        assertEquals(0, selection.lineup.matchLists.sideIndex)
        assertEquals(11, selection.lineup.clubStarters.size)
        assertFalse(selection.lineup.finalizePlan.finishMainTeamActivity)
        assertTrue(selection.lineup.playerWrites.take(11).all { it.lineupCode in 1..25 })
        assertTrue(CareerManagedLineupSelectionPlanner.isCurrent(selection, inputs))
    }

    @Test
    fun `selection is invalidated when prepared match changes`() {
        val original = inputs()
        val selection = CareerManagedLineupSelectionPlanner.confirm(original, formationIndex = 7)
        val changed = original.copy(
            matchPreparation = original.matchPreparation.copy(matchId = "match-2")
        )

        assertFalse(CareerManagedLineupSelectionPlanner.isCurrent(selection, changed))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `confirmation cannot bypass missing formation owner`() {
        CareerManagedLineupSelectionPlanner.confirm(
            inputs = inputs(lineupModeFlag = null),
            formationIndex = 3,
        )
    }

    private fun inputs(
        lineupModeFlag: Boolean? = true,
    ): CareerLineupInputCatalogStore.LineupInputs =
        CareerLineupInputCatalogStore.LineupInputs(
            careerId = "career-1",
            clubId = "club-managed",
            players = (0 until 12).map { index ->
                CareerLineupInputCatalogStore.PlayerInput(
                    playerId = "p-$index",
                    name = "Player $index",
                    positionCode = index % 5,
                    sideCode = if (index % 3 == 0) -1 else index % 2,
                    subroleCode = if (index % 4 == 0) -1 else index % 2,
                    skill = 70 + index,
                    energy = 100,
                    star = index == 0,
                    sourceOrdinal = index,
                    hasClubForPreparedMatch = true,
                    clubActiveQ0ForPreparedMatch = false,
                    blockedByM0ForPreparedMatch = false,
                    excludedByCompetitionV0ForPreparedMatch = false,
                )
            },
            matchPreparation = CareerLineupInputCatalogStore.MatchPreparation(
                nextPlayableDayIndex = 4,
                matchId = "match-1",
                homeClubId = "club-managed",
                awayClubId = "club-away",
                managedSide = CareerLineupInputCatalogStore.ManagedMatchSide.HOME,
                homeSeniorRosterCount = 12,
                awaySeniorRosterCount = 12,
                competitionId = null,
                competitionRestrictionActive = false,
                competitionDisciplineOwnerResolved = true,
                lineupModeFlag = lineupModeFlag,
                homeTacticIndex = 1,
                awayTacticIndex = 2,
                homeSubstitutionsRemaining = 5,
                awaySubstitutionsRemaining = 5,
                homeLegacyModeFlag = false,
                awayLegacyModeFlag = false,
                blockers = emptySet(),
            ),
        )
}
