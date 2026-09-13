package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitResult
import com.leomala.footballdynasty.domain.manager.LegacyLineupFormationTables

/**
 * Fail-closed Phase 17 handoff for the opponent lineup of the next managed match.
 *
 * ActivityEscalacao proves how to build a lineup after a formation index is supplied; it does not
 * prove which formation the opponent must choose. This boundary therefore never derives formation
 * from tactics, side, club id or a numeric fallback. A caller must supply both a source-proven
 * formation index and the committed lineup produced from that same proven choice.
 */
data class CareerOpponentLineupSelection(
    val careerId: String,
    val matchId: String,
    val clubId: String,
    val side: CareerLineupInputCatalogStore.ManagedMatchSide,
    val formationIndex: Int,
    val lineup: LegacyLineupCommitResult<String>,
)

object CareerOpponentLineupSelectionPlanner {
    enum class Blocker {
        NO_MANAGED_MATCH,
        OPPONENT_CLUB_UNRESOLVED,
        FORMATION_OWNER_UNRESOLVED,
        INVALID_FORMATION,
        LINEUP_UNRESOLVED,
        LINEUP_SIDE_MISMATCH,
    }

    data class Resolution(
        val selection: CareerOpponentLineupSelection? = null,
        val blocker: Blocker? = null,
    ) {
        init {
            require((selection == null) xor (blocker == null)) {
                "Opponent lineup resolution must be either selected or blocked"
            }
        }

        val ready: Boolean
            get() = selection != null
    }

    fun resolve(
        inputs: CareerLineupInputCatalogStore.LineupInputs,
        formationIndex: Int?,
        lineup: LegacyLineupCommitResult<String>?,
    ): Resolution {
        val preparation = inputs.matchPreparation
        val matchId = preparation.matchId
        val managedSide = preparation.managedSide
        if (matchId == null || managedSide == null) {
            return Resolution(blocker = Blocker.NO_MANAGED_MATCH)
        }

        val opponentClubId = when (managedSide) {
            CareerLineupInputCatalogStore.ManagedMatchSide.HOME -> preparation.awayClubId
            CareerLineupInputCatalogStore.ManagedMatchSide.AWAY -> preparation.homeClubId
        } ?: return Resolution(blocker = Blocker.OPPONENT_CLUB_UNRESOLVED)

        if (formationIndex == null) {
            return Resolution(blocker = Blocker.FORMATION_OWNER_UNRESOLVED)
        }
        if (formationIndex !in LegacyLineupFormationTables.formationSlots.indices) {
            return Resolution(blocker = Blocker.INVALID_FORMATION)
        }

        val committed = lineup ?: return Resolution(blocker = Blocker.LINEUP_UNRESOLVED)
        val opponentSideIndex = when (managedSide) {
            CareerLineupInputCatalogStore.ManagedMatchSide.HOME -> 1
            CareerLineupInputCatalogStore.ManagedMatchSide.AWAY -> 0
        }
        if (committed.matchLists.sideIndex != opponentSideIndex) {
            return Resolution(blocker = Blocker.LINEUP_SIDE_MISMATCH)
        }

        return Resolution(
            selection = CareerOpponentLineupSelection(
                careerId = inputs.careerId,
                matchId = matchId,
                clubId = opponentClubId,
                side = when (managedSide) {
                    CareerLineupInputCatalogStore.ManagedMatchSide.HOME ->
                        CareerLineupInputCatalogStore.ManagedMatchSide.AWAY
                    CareerLineupInputCatalogStore.ManagedMatchSide.AWAY ->
                        CareerLineupInputCatalogStore.ManagedMatchSide.HOME
                },
                formationIndex = formationIndex,
                lineup = committed,
            )
        )
    }
}
