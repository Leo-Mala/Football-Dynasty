package com.leomala.footballdynasty.application.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerManagedLineupFormationPlannerTest {
    @Test
    fun `missing formation never falls back to a legacy index`() {
        val resolution = CareerManagedLineupFormationPlanner.prepare(
            inputs = inputs(),
            formationIndex = null,
        )

        assertFalse(resolution.ready)
        assertNull(resolution.prepared)
        assertEquals(
            CareerManagedLineupFormationPlanner.Blocker.FORMATION_NOT_SELECTED,
            resolution.blocker,
        )
    }

    @Test
    fun `invalid formation is rejected instead of being coerced to zero or four`() {
        listOf(-1, 11).forEach { invalid ->
            val resolution = CareerManagedLineupFormationPlanner.prepare(
                inputs = inputs(),
                formationIndex = invalid,
            )

            assertFalse(resolution.ready)
            assertEquals(
                CareerManagedLineupFormationPlanner.Blocker.INVALID_FORMATION,
                resolution.blocker,
            )
        }
        assertEquals((0..10).toList(), CareerManagedLineupFormationPlanner.availableFormationIndices)
    }

    @Test
    fun `unresolved K0 owner keeps formation preparation fail closed`() {
        val resolution = CareerManagedLineupFormationPlanner.prepare(
            inputs = inputs(lineupModeFlag = null),
            formationIndex = 3,
        )

        assertFalse(resolution.ready)
        assertEquals(
            CareerManagedLineupFormationPlanner.Blocker.LINEUP_ELIGIBILITY_UNRESOLVED,
            resolution.blocker,
        )
    }

    @Test
    fun `explicit selection feeds exact formation and commit side without tactic fallback`() {
        val resolution = CareerManagedLineupFormationPlanner.prepare(
            inputs = inputs(managedSide = CareerLineupInputCatalogStore.ManagedMatchSide.HOME),
            formationIndex = 3,
        )
        val prepared = requireNotNull(resolution.prepared)

        assertTrue(resolution.ready)
        assertEquals(3, prepared.formationIndex)
        assertEquals(3, prepared.state.formationIndex)
        assertEquals(11, prepared.state.starters.count { it.player != null })

        val withoutLegacyActivity = CareerManagedLineupFormationPlanner.commit(
            prepared = prepared,
            legacyMainTeamActivityPresent = false,
        )
        assertEquals(0, withoutLegacyActivity.matchLists.sideIndex)
        assertEquals(11, withoutLegacyActivity.clubStarters.size)
        assertEquals(withoutLegacyActivity.clubStarters, withoutLegacyActivity.matchLists.startersPrimary)
        assertEquals(withoutLegacyActivity.clubStarters, withoutLegacyActivity.matchLists.startersMirror)
        assertTrue(withoutLegacyActivity.playerWrites.take(11).all { it.starterFlagWrite == true })
        assertTrue(withoutLegacyActivity.playerWrites.take(11).all { it.lineupCode in 1..25 })
        assertFalse(withoutLegacyActivity.finalizePlan.finishMainTeamActivity)

        val withLegacyActivity = CareerManagedLineupFormationPlanner.commit(
            prepared = prepared,
            legacyMainTeamActivityPresent = true,
        )
        assertTrue(withLegacyActivity.finalizePlan.finishMainTeamActivity)
        assertEquals(withoutLegacyActivity.clubStarters, withLegacyActivity.clubStarters)
        assertEquals(withoutLegacyActivity.clubBench, withLegacyActivity.clubBench)
        assertEquals(withoutLegacyActivity.playerWrites, withLegacyActivity.playerWrites)
    }

    @Test
    fun `away selection commits to legacy side one`() {
        val prepared = requireNotNull(
            CareerManagedLineupFormationPlanner.prepare(
                inputs = inputs(managedSide = CareerLineupInputCatalogStore.ManagedMatchSide.AWAY),
                formationIndex = 7,
            ).prepared
        )

        val committed = CareerManagedLineupFormationPlanner.commit(
            prepared = prepared,
            legacyMainTeamActivityPresent = false,
        )

        assertEquals(1, committed.matchLists.sideIndex)
        assertTrue(committed.matchLists.replaceSideLists)
    }

    private fun inputs(
        managedSide: CareerLineupInputCatalogStore.ManagedMatchSide = CareerLineupInputCatalogStore.ManagedMatchSide.HOME,
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
            matchPreparation = preparation(
                managedSide = managedSide,
                lineupModeFlag = lineupModeFlag,
            ),
        )

    private fun preparation(
        managedSide: CareerLineupInputCatalogStore.ManagedMatchSide,
        lineupModeFlag: Boolean?,
    ) = CareerLineupInputCatalogStore.MatchPreparation(
        nextPlayableDayIndex = 4,
        matchId = "match-1",
        homeClubId = "club-managed",
        awayClubId = "club-away",
        managedSide = managedSide,
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
    )
}
