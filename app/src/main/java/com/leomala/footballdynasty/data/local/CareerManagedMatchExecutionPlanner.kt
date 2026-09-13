package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.application.career.CareerLineupInputCatalogStore
import com.leomala.footballdynasty.application.career.CareerManagedLineupSelection
import com.leomala.footballdynasty.application.career.CareerManagedLineupSelectionPlanner
import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitResult
import com.leomala.footballdynasty.domain.manager.LegacyTacticsRawState

/**
 * Single fail-closed Phase 17 handoff from the persisted lineup surface to the productive
 * [CareerMatchExecutionCommandStore].
 *
 * The planner deliberately does not choose an opponent formation or fabricate an opponent lineup.
 * A caller must supply a source-proven opponent [LegacyLineupCommitResult]. Likewise, the productive
 * runtime composition must be wired on the exact command store that will execute the returned
 * command. This keeps the UI from maintaining a parallel interpretation of match readiness.
 */
class CareerManagedMatchExecutionPlanner internal constructor(
    private val loadTactics: suspend (careerId: String, clubId: String) -> LegacyTacticsRawState?,
    private val runtimeCompositionAvailable: () -> Boolean,
) {
    constructor(
        database: FootballDynastyDatabase,
        commandStore: CareerMatchExecutionCommandStore,
    ) : this(
        loadTactics = { careerId, clubId -> CareerClubTacticsStore(database).load(careerId, clubId) },
        runtimeCompositionAvailable = { commandStore.runtimeCompositionAvailable },
    )

    enum class Blocker {
        MATCH_PREPARATION_BLOCKED,
        MANAGED_LINEUP_NOT_CONFIRMED,
        OPPONENT_LINEUP_UNRESOLVED,
        OPPONENT_LINEUP_SIDE_MISMATCH,
        HOME_TACTICS_UNRESOLVED,
        AWAY_TACTICS_UNRESOLVED,
        TRANSIENT_MATCH_OWNERS_UNRESOLVED,
        MATCH_RUNTIME_COMPOSITION_UNWIRED,
    }

    data class Resolution(
        val command: CareerMatchExecutionCommandStore.Command?,
        val blockers: Set<Blocker>,
        val preparationBlockers: Set<CareerLineupInputCatalogStore.MatchPreparationBlocker>,
    ) {
        val executable: Boolean
            get() = command != null && blockers.isEmpty() && preparationBlockers.isEmpty()
    }

    suspend fun prepare(
        inputs: CareerLineupInputCatalogStore.LineupInputs,
        managedSelection: CareerManagedLineupSelection?,
        opponentLineup: LegacyLineupCommitResult<String>?,
    ): Resolution {
        val preparation = inputs.matchPreparation
        val unresolvedPreparation = preparation.blockers
            .filterNot {
                it == CareerLineupInputCatalogStore.MatchPreparationBlocker.MATCH_RUNTIME_COMPOSITION_UNWIRED
            }
            .toSet()
        val blockers = linkedSetOf<Blocker>()
        if (unresolvedPreparation.isNotEmpty()) {
            blockers += Blocker.MATCH_PREPARATION_BLOCKED
        }

        val currentManagedSelection = managedSelection?.takeIf {
            CareerManagedLineupSelectionPlanner.isCurrent(it, inputs)
        }
        if (currentManagedSelection == null) {
            blockers += Blocker.MANAGED_LINEUP_NOT_CONFIRMED
        }

        val matchId = preparation.matchId
        val homeClubId = preparation.homeClubId
        val awayClubId = preparation.awayClubId
        val managedSide = preparation.managedSide
        if (matchId == null || homeClubId == null || awayClubId == null || managedSide == null) {
            blockers += Blocker.MATCH_PREPARATION_BLOCKED
        }

        if (opponentLineup == null) {
            blockers += Blocker.OPPONENT_LINEUP_UNRESOLVED
        } else if (managedSide != null) {
            val expectedOpponentSide = when (managedSide) {
                CareerLineupInputCatalogStore.ManagedMatchSide.HOME -> 1
                CareerLineupInputCatalogStore.ManagedMatchSide.AWAY -> 0
            }
            if (opponentLineup.matchLists.sideIndex != expectedOpponentSide) {
                blockers += Blocker.OPPONENT_LINEUP_SIDE_MISMATCH
            }
        }

        if (!runtimeCompositionAvailable()) {
            blockers += Blocker.MATCH_RUNTIME_COMPOSITION_UNWIRED
        }

        val homeTactics = if (homeClubId != null) loadTactics(inputs.careerId, homeClubId) else null
        val awayTactics = if (awayClubId != null) loadTactics(inputs.careerId, awayClubId) else null
        if (homeTactics == null) blockers += Blocker.HOME_TACTICS_UNRESOLVED
        if (awayTactics == null) blockers += Blocker.AWAY_TACTICS_UNRESOLVED

        val homeSubstitutionsRemaining = preparation.homeSubstitutionsRemaining
        val awaySubstitutionsRemaining = preparation.awaySubstitutionsRemaining
        val homeLegacyModeFlag = preparation.homeLegacyModeFlag
        val awayLegacyModeFlag = preparation.awayLegacyModeFlag
        if (
            homeSubstitutionsRemaining == null ||
            awaySubstitutionsRemaining == null ||
            homeLegacyModeFlag == null ||
            awayLegacyModeFlag == null
        ) {
            blockers += Blocker.TRANSIENT_MATCH_OWNERS_UNRESOLVED
        }

        if (blockers.isNotEmpty() || unresolvedPreparation.isNotEmpty()) {
            return Resolution(
                command = null,
                blockers = blockers,
                preparationBlockers = unresolvedPreparation,
            )
        }

        val selection = requireNotNull(currentManagedSelection)
        val opponent = requireNotNull(opponentLineup)
        val homeLineup: LegacyLineupCommitResult<String>
        val awayLineup: LegacyLineupCommitResult<String>
        when (requireNotNull(managedSide)) {
            CareerLineupInputCatalogStore.ManagedMatchSide.HOME -> {
                homeLineup = selection.lineup
                awayLineup = opponent
            }
            CareerLineupInputCatalogStore.ManagedMatchSide.AWAY -> {
                homeLineup = opponent
                awayLineup = selection.lineup
            }
        }

        return Resolution(
            command = CareerMatchExecutionCommandStore.Command(
                careerId = inputs.careerId,
                matchId = requireNotNull(matchId),
                homeLineup = homeLineup,
                awayLineup = awayLineup,
                homeTactics = requireNotNull(homeTactics),
                awayTactics = requireNotNull(awayTactics),
                homeSubstitutionsRemaining = requireNotNull(homeSubstitutionsRemaining),
                awaySubstitutionsRemaining = requireNotNull(awaySubstitutionsRemaining),
                homeLegacyModeFlag = requireNotNull(homeLegacyModeFlag),
                awayLegacyModeFlag = requireNotNull(awayLegacyModeFlag),
            ),
            blockers = emptySet(),
            preparationBlockers = emptySet(),
        )
    }
}
