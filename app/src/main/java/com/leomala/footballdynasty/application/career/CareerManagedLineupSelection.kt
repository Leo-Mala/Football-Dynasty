package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitResult

/**
 * Stable Phase 17 payload produced only from an explicit formation choice for the currently
 * prepared managed match.
 *
 * The modern Compose UI is not the legacy ActivityMainTeam owner, therefore the exact lineup
 * commit is produced with legacyMainTeamActivityPresent=false. No saved-lineup owner, tactic
 * index or fallback formation is synthesized here.
 */
data class CareerManagedLineupSelection(
    val careerId: String,
    val matchId: String,
    val clubId: String,
    val managedSide: CareerLineupInputCatalogStore.ManagedMatchSide,
    val formationIndex: Int,
    val lineup: LegacyLineupCommitResult<String>,
)

object CareerManagedLineupSelectionPlanner {
    fun confirm(
        inputs: CareerLineupInputCatalogStore.LineupInputs,
        formationIndex: Int,
    ): CareerManagedLineupSelection {
        val preparation = inputs.matchPreparation
        val matchId = requireNotNull(preparation.matchId) {
            "Managed lineup selection requires a prepared match"
        }
        val managedSide = requireNotNull(preparation.managedSide) {
            "Managed lineup selection requires a managed match side"
        }
        val resolution = CareerManagedLineupFormationPlanner.prepare(
            inputs = inputs,
            formationIndex = formationIndex,
        )
        val prepared = requireNotNull(resolution.prepared) {
            "Managed lineup selection is blocked by ${resolution.blocker}"
        }
        require(prepared.managedSide == managedSide) {
            "Prepared lineup side must match current managed match side"
        }

        return CareerManagedLineupSelection(
            careerId = inputs.careerId,
            matchId = matchId,
            clubId = inputs.clubId,
            managedSide = managedSide,
            formationIndex = formationIndex,
            lineup = CareerManagedLineupFormationPlanner.commit(
                prepared = prepared,
                legacyMainTeamActivityPresent = false,
            ),
        )
    }

    fun isCurrent(
        selection: CareerManagedLineupSelection,
        inputs: CareerLineupInputCatalogStore.LineupInputs,
    ): Boolean =
        selection.careerId == inputs.careerId &&
            selection.clubId == inputs.clubId &&
            selection.matchId == inputs.matchPreparation.matchId &&
            selection.managedSide == inputs.matchPreparation.managedSide
}

/**
 * Process-local Phase 17 handoff for the manager's explicitly confirmed lineup.
 *
 * This is intentionally transient: it does not invent or persist a legacy saved-lineup owner.
 * A selection is reusable only while the exact career/match/club/side preparation remains
 * current. Observing different inputs evicts the stale payload so it cannot reappear later.
 */
class CareerManagedLineupSelectionSession {
    private var selection: CareerManagedLineupSelection? = null

    fun remember(selection: CareerManagedLineupSelection): CareerManagedLineupSelection {
        this.selection = selection
        return selection
    }

    fun currentFor(
        inputs: CareerLineupInputCatalogStore.LineupInputs,
    ): CareerManagedLineupSelection? {
        val current = selection ?: return null
        if (!CareerManagedLineupSelectionPlanner.isCurrent(current, inputs)) {
            selection = null
            return null
        }
        return current
    }

    fun clearFor(inputs: CareerLineupInputCatalogStore.LineupInputs) {
        val current = selection ?: return
        if (CareerManagedLineupSelectionPlanner.isCurrent(current, inputs)) {
            selection = null
        }
    }

    fun clear() {
        selection = null
    }
}

object CareerManagedLineupSelectionSessions {
    val process: CareerManagedLineupSelectionSession = CareerManagedLineupSelectionSession()
}
