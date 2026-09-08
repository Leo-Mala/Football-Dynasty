package com.leomala.footballdynasty.domain.match

/**
 * Exact transient projection of the `best.s` integer arrays consumed by legacy `best.o.n(...)`.
 *
 * Ownership remains match-local: legacy `best.s.R/W/T/U` are initialized for a match and mutated by
 * `components.r3`; none of these arrays belong to career persistence. The names below follow the
 * public legacy getters used by the rating method (`E`, `q0`, `w`, `y`) instead of guessing sporting
 * labels for the raw counters.
 */
object LegacyMatchRatingMetricRuntimeRules {
    data class State(
        val legacyB0BySide: List<Int> = listOf(0, 0),
        val legacyEBySide: List<Int> = listOf(0, 0),
        val legacyQ0BySide: List<Int> = listOf(0, 0),
        val legacyWBySide: List<Int> = listOf(0, 0),
        val legacyYBySide: List<Int> = listOf(0, 0),
    )

    data class RatingInputs(
        val currentLegacyE: Int,
        val opponentLegacyE: Int,
        val currentLegacyQ0: Int,
        val opponentLegacyQ0: Int,
        val opponentLegacyW: Int,
        val opponentLegacyY: Int,
    )

    /** Applies the `components.r3.J()` calls to private `a(side)` that update `B0` and recompute `E`. */
    fun applyJMutations(
        state: State,
        currentSide: Int,
        mutations: Iterable<LegacyMatchR3DecisionRules.Mutation>,
    ): State {
        require(currentSide in 0..1) { "Legacy match side must be 0 or 1: $currentSide" }
        val oppositeSide = if (currentSide == 0) 1 else 0
        var next = state
        mutations.forEach { mutation ->
            val side = when (mutation) {
                LegacyMatchR3DecisionRules.Mutation.J_APPLY_A_CURRENT -> currentSide
                LegacyMatchR3DecisionRules.Mutation.J_APPLY_A_OPPOSITE -> oppositeSide
                else -> null
            }
            if (side != null) {
                val applied = LegacyMatchR3ApplyARules.apply(next.legacyB0BySide, side)
                next = next.copy(
                    legacyB0BySide = applied.updatedLegacyB0,
                    legacyEBySide = applied.updatedLegacyE,
                )
            }
        }
        return next
    }

    /** Applies the three mutually-exclusive counter outcomes at the end of reachable `r3.K()`. */
    fun applyAdvanceCounter(
        state: State,
        mutation: LegacyMatchR3AdvanceRules.CounterMutation,
        counterSide: Int,
    ): State {
        require(counterSide in 0..1) { "Legacy match side must be 0 or 1: $counterSide" }
        return when (mutation) {
            // `r3.K()` increments `best.s.w()[currentSide]` before materializing a goal.
            LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_W_CURRENT -> state.copy(
                legacyWBySide = incrementAt(state.legacyWBySide, counterSide),
            )
            // Both no-goal branches increment `best.s.q0()[F()]` on the resolved opposite side.
            LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_Q0_OPPOSITE -> state.copy(
                legacyQ0BySide = incrementAt(state.legacyQ0BySide, counterSide),
            )
            // `best.s.a0()` is a different array and is not consumed by `best.o.n()`.
            LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_A0_CURRENT -> state
        }
    }

    /** Applies the `best.s.y()[currentSide]` mutation already exposed by the certified b/c routing plan. */
    fun applyEventMutations(
        state: State,
        currentSide: Int,
        mutations: Iterable<LegacyMatchR3EventRoutingRules.Mutation>,
    ): State {
        require(currentSide in 0..1) { "Legacy match side must be 0 or 1: $currentSide" }
        var next = state
        mutations.forEach { mutation ->
            if (mutation == LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_Y_CURRENT) {
                next = next.copy(legacyYBySide = incrementAt(next.legacyYBySide, currentSide))
            }
        }
        return next
    }

    /** Side-normalized view consumed directly by [LegacyMatchPlayerRatingRules.Input]. */
    fun ratingInputs(state: State, currentSide: Int): RatingInputs {
        require(currentSide in 0..1) { "Legacy match side must be 0 or 1: $currentSide" }
        val opponentSide = if (currentSide == 0) 1 else 0
        return RatingInputs(
            currentLegacyE = state.legacyEBySide[currentSide],
            opponentLegacyE = state.legacyEBySide[opponentSide],
            currentLegacyQ0 = state.legacyQ0BySide[currentSide],
            opponentLegacyQ0 = state.legacyQ0BySide[opponentSide],
            opponentLegacyW = state.legacyWBySide[opponentSide],
            opponentLegacyY = state.legacyYBySide[opponentSide],
        )
    }

    private fun incrementAt(values: List<Int>, side: Int): List<Int> =
        values.toMutableList().also { it[side] = it[side] + 1 }.toList()
}
