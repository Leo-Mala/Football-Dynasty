package com.leomala.footballdynasty.domain.career

/**
 * Pure control-flow boundary for the reachable inner traversal of legacy `best.k0.c(index)`.
 *
 * The official SMALI evidence already frozen for Phase 15 proves that the method visits
 * `components.n1` selectors in this exact order after its `U()`/`best.h0` setup:
 *
 * `[0, 1, 2, 2, 5, 6, 6, 3, 3, 4, 4]`
 *
 * This rule intentionally freezes only that selector ordering and multiplicity. It does not
 * assign sporting meaning to selector values and does not claim equivalence for the still-open
 * `components.n1` thresholds, `best.h0` collections, player-flag side effect, or persistence.
 */
object LegacyAnnualTournamentEntryResetRules {
    val SELECTOR_SEQUENCE: List<Int> = listOf(0, 1, 2, 2, 5, 6, 6, 3, 3, 4, 4)

    data class Action(
        val ordinal: Int,
        val selector: Int,
    )

    fun planSelectorTraversal(): List<Action> =
        SELECTOR_SEQUENCE.mapIndexed { ordinal, selector ->
            Action(
                ordinal = ordinal,
                selector = selector,
            )
        }
}
