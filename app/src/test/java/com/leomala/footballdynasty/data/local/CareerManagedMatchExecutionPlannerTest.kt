package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.application.career.CareerLineupInputCatalogStore
import com.leomala.footballdynasty.application.career.CareerManagedLineupSelection
import com.leomala.footballdynasty.application.career.CareerOpponentLineupSelection
import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitResult
import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitRule
import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitSlot
import com.leomala.footballdynasty.domain.manager.LegacyTacticsRawState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerManagedMatchExecutionPlannerTest {
    @Test
    fun `unresolved opponent and runtime remain fail closed without command`() = runBlocking {
        val inputs = inputs(
            blockers = setOf(
                CareerLineupInputCatalogStore.MatchPreparationBlocker.MATCH_RUNTIME_COMPOSITION_UNWIRED,
            )
        )
        val selection = selection(inputs, lineup(0, "managed"))
        val planner = CareerManagedMatchExecutionPlanner(
            loadTactics = { _, _ -> tactics() },
            runtimeCompositionAvailable = { false },
        )

        val resolution = planner.prepare(
            inputs = inputs,
            managedSelection = selection,
            opponentSelection = null,
        )

        assertNull(resolution.command)
        assertFalse(resolution.executable)
        assertTrue(
            CareerManagedMatchExecutionPlanner.Blocker.OPPONENT_LINEUP_UNRESOLVED in resolution.blockers
        )
        assertTrue(
            CareerManagedMatchExecutionPlanner.Blocker.MATCH_RUNTIME_COMPOSITION_UNWIRED in resolution.blockers
        )
        assertTrue(resolution.preparationBlockers.isEmpty())
    }

    @Test
    fun `resolved home manager handoff builds exact productive command`() = runBlocking {
        val inputs = inputs(
            blockers = setOf(
                CareerLineupInputCatalogStore.MatchPreparationBlocker.MATCH_RUNTIME_COMPOSITION_UNWIRED,
            )
        )
        val managed = lineup(0, "managed")
        val opponent = lineup(1, "opponent")
        val selection = selection(inputs, managed)
        val opponentSelection = opponentSelection(inputs, opponent)
        val homeTactics = tactics(1)
        val awayTactics = tactics(2)
        val planner = CareerManagedMatchExecutionPlanner(
            loadTactics = { _, clubId ->
                when (clubId) {
                    "home" -> homeTactics
                    "away" -> awayTactics
                    else -> null
                }
            },
            runtimeCompositionAvailable = { true },
        )

        val resolution = planner.prepare(inputs, selection, opponentSelection)
        val command = requireNotNull(resolution.command)

        assertTrue(resolution.executable)
        assertTrue(resolution.blockers.isEmpty())
        assertTrue(resolution.preparationBlockers.isEmpty())
        assertEquals("career", command.careerId)
        assertEquals("match", command.matchId)
        assertSame(managed, command.homeLineup)
        assertSame(opponent, command.awayLineup)
        assertSame(homeTactics, command.homeTactics)
        assertSame(awayTactics, command.awayTactics)
        assertEquals(5, command.homeSubstitutionsRemaining)
        assertEquals(5, command.awaySubstitutionsRemaining)
        assertFalse(command.homeLegacyModeFlag)
        assertTrue(command.awayLegacyModeFlag)
    }

    @Test
    fun `stale opponent selection cannot reach executor command`() = runBlocking {
        val inputs = inputs(emptySet())
        val planner = CareerManagedMatchExecutionPlanner(
            loadTactics = { _, _ -> tactics() },
            runtimeCompositionAvailable = { true },
        )
        val stale = opponentSelection(inputs, lineup(1, "opponent")).copy(matchId = "old-match")

        val resolution = planner.prepare(
            inputs = inputs,
            managedSelection = selection(inputs, lineup(0, "managed")),
            opponentSelection = stale,
        )

        assertNull(resolution.command)
        assertFalse(resolution.executable)
        assertTrue(
            CareerManagedMatchExecutionPlanner.Blocker.OPPONENT_LINEUP_CONTEXT_MISMATCH in resolution.blockers
        )
    }

    @Test
    fun `opponent lineup for wrong side cannot reach executor command`() = runBlocking {
        val inputs = inputs(emptySet())
        val planner = CareerManagedMatchExecutionPlanner(
            loadTactics = { _, _ -> tactics() },
            runtimeCompositionAvailable = { true },
        )

        val resolution = planner.prepare(
            inputs = inputs,
            managedSelection = selection(inputs, lineup(0, "managed")),
            opponentSelection = opponentSelection(inputs, lineup(0, "wrong-side")),
        )

        assertNull(resolution.command)
        assertTrue(
            CareerManagedMatchExecutionPlanner.Blocker.OPPONENT_LINEUP_SIDE_MISMATCH in resolution.blockers
        )
    }

    @Test
    fun `non runtime preparation blocker is preserved and prevents command`() = runBlocking {
        val inputs = inputs(
            setOf(CareerLineupInputCatalogStore.MatchPreparationBlocker.LINEUP_ELIGIBILITY_OWNER_UNRESOLVED)
        )
        val planner = CareerManagedMatchExecutionPlanner(
            loadTactics = { _, _ -> tactics() },
            runtimeCompositionAvailable = { true },
        )

        val resolution = planner.prepare(
            inputs = inputs,
            managedSelection = selection(inputs, lineup(0, "managed")),
            opponentSelection = opponentSelection(inputs, lineup(1, "opponent")),
        )

        assertNull(resolution.command)
        assertTrue(CareerManagedMatchExecutionPlanner.Blocker.MATCH_PREPARATION_BLOCKED in resolution.blockers)
        assertEquals(
            setOf(CareerLineupInputCatalogStore.MatchPreparationBlocker.LINEUP_ELIGIBILITY_OWNER_UNRESOLVED),
            resolution.preparationBlockers,
        )
    }

    private fun inputs(
        blockers: Set<CareerLineupInputCatalogStore.MatchPreparationBlocker>,
    ) = CareerLineupInputCatalogStore.LineupInputs(
        careerId = "career",
        clubId = "home",
        players = emptyList(),
        matchPreparation = CareerLineupInputCatalogStore.MatchPreparation(
            nextPlayableDayIndex = 7,
            matchId = "match",
            homeClubId = "home",
            awayClubId = "away",
            managedSide = CareerLineupInputCatalogStore.ManagedMatchSide.HOME,
            homeSeniorRosterCount = 20,
            awaySeniorRosterCount = 20,
            competitionId = "league",
            competitionRestrictionActive = false,
            competitionDisciplineOwnerResolved = true,
            lineupModeFlag = false,
            homeTacticIndex = 1,
            awayTacticIndex = 2,
            homeSubstitutionsRemaining = 5,
            awaySubstitutionsRemaining = 5,
            homeLegacyModeFlag = false,
            awayLegacyModeFlag = true,
            blockers = blockers,
        ),
    )

    private fun selection(
        inputs: CareerLineupInputCatalogStore.LineupInputs,
        lineup: LegacyLineupCommitResult<String>,
    ) = CareerManagedLineupSelection(
        careerId = inputs.careerId,
        matchId = requireNotNull(inputs.matchPreparation.matchId),
        clubId = inputs.clubId,
        managedSide = requireNotNull(inputs.matchPreparation.managedSide),
        formationIndex = 3,
        lineup = lineup,
    )

    private fun opponentSelection(
        inputs: CareerLineupInputCatalogStore.LineupInputs,
        lineup: LegacyLineupCommitResult<String>,
    ): CareerOpponentLineupSelection {
        val managedSide = requireNotNull(inputs.matchPreparation.managedSide)
        return CareerOpponentLineupSelection(
            careerId = inputs.careerId,
            matchId = requireNotNull(inputs.matchPreparation.matchId),
            clubId = when (managedSide) {
                CareerLineupInputCatalogStore.ManagedMatchSide.HOME ->
                    requireNotNull(inputs.matchPreparation.awayClubId)
                CareerLineupInputCatalogStore.ManagedMatchSide.AWAY ->
                    requireNotNull(inputs.matchPreparation.homeClubId)
            },
            side = when (managedSide) {
                CareerLineupInputCatalogStore.ManagedMatchSide.HOME ->
                    CareerLineupInputCatalogStore.ManagedMatchSide.AWAY
                CareerLineupInputCatalogStore.ManagedMatchSide.AWAY ->
                    CareerLineupInputCatalogStore.ManagedMatchSide.HOME
            },
            formationIndex = 3,
            lineup = lineup,
        )
    }

    private fun lineup(side: Int, playerId: String): LegacyLineupCommitResult<String> =
        LegacyLineupCommitRule.commit(
            starterSlots = listOf(LegacyLineupCommitSlot(playerId, 1)),
            benchPlayers = emptyList(),
            eligibleRoster = emptyList(),
            matchSideIndex = side,
            mainTeamActivityPresent = false,
        )

    private fun tactics(seed: Int = 0) = LegacyTacticsRawState(
        optionSlots = listOf(seed, seed + 1, seed + 2, seed + 3),
        checkboxT = seed % 2 == 0,
    )
}
