package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitResult
import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitRule
import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerOpponentLineupSelectionPlannerTest {
    @Test
    fun `missing formation owner stays blocked without default`() {
        val resolution = CareerOpponentLineupSelectionPlanner.resolve(
            inputs = inputs(),
            formationIndex = null,
            lineup = lineup(side = 1),
        )

        assertFalse(resolution.ready)
        assertNull(resolution.selection)
        assertEquals(
            CareerOpponentLineupSelectionPlanner.Blocker.FORMATION_OWNER_UNRESOLVED,
            resolution.blocker,
        )
    }

    @Test
    fun `invalid formation cannot be normalized into a fallback`() {
        val resolution = CareerOpponentLineupSelectionPlanner.resolve(
            inputs = inputs(),
            formationIndex = Int.MAX_VALUE,
            lineup = lineup(side = 1),
        )

        assertFalse(resolution.ready)
        assertEquals(
            CareerOpponentLineupSelectionPlanner.Blocker.INVALID_FORMATION,
            resolution.blocker,
        )
    }

    @Test
    fun `lineup for managed side cannot be accepted as opponent`() {
        val resolution = CareerOpponentLineupSelectionPlanner.resolve(
            inputs = inputs(),
            formationIndex = 3,
            lineup = lineup(side = 0),
        )

        assertFalse(resolution.ready)
        assertEquals(
            CareerOpponentLineupSelectionPlanner.Blocker.LINEUP_SIDE_MISMATCH,
            resolution.blocker,
        )
    }

    @Test
    fun `source proven formation and opponent lineup preserve exact handoff`() {
        val opponent = lineup(side = 1)
        val resolution = CareerOpponentLineupSelectionPlanner.resolve(
            inputs = inputs(),
            formationIndex = 3,
            lineup = opponent,
        )
        val selection = requireNotNull(resolution.selection)

        assertTrue(resolution.ready)
        assertNull(resolution.blocker)
        assertEquals("career", selection.careerId)
        assertEquals("match", selection.matchId)
        assertEquals("away", selection.clubId)
        assertEquals(CareerLineupInputCatalogStore.ManagedMatchSide.AWAY, selection.side)
        assertEquals(3, selection.formationIndex)
        assertSame(opponent, selection.lineup)
    }

    private fun inputs() = CareerLineupInputCatalogStore.LineupInputs(
        careerId = "career",
        clubId = "home",
        players = emptyList(),
        matchPreparation = CareerLineupInputCatalogStore.MatchPreparation(
            nextPlayableDayIndex = 4,
            matchId = "match",
            homeClubId = "home",
            awayClubId = "away",
            managedSide = CareerLineupInputCatalogStore.ManagedMatchSide.HOME,
            homeSeniorRosterCount = 20,
            awaySeniorRosterCount = 20,
            competitionId = null,
            competitionRestrictionActive = false,
            competitionDisciplineOwnerResolved = true,
            lineupModeFlag = false,
            homeTacticIndex = 1,
            awayTacticIndex = 2,
            homeSubstitutionsRemaining = 5,
            awaySubstitutionsRemaining = 5,
            homeLegacyModeFlag = false,
            awayLegacyModeFlag = true,
            blockers = emptySet(),
        ),
    )

    private fun lineup(side: Int): LegacyLineupCommitResult<String> =
        LegacyLineupCommitRule.commit(
            starterSlots = listOf(LegacyLineupCommitSlot("player", 1)),
            benchPlayers = emptyList(),
            eligibleRoster = emptyList(),
            matchSideIndex = side,
            mainTeamActivityPresent = false,
        )
}
