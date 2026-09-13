package com.leomala.footballdynasty.domain.match

/**
 * Applies the external `best.s` counter mutations and tick/side transition already recovered from
 * reachable legacy `components.r3.K()`.
 *
 * Goal materialization remains the responsibility of the caller that owns the complete `best.l`
 * runtime. This boundary only applies effects represented explicitly by [LegacyMatchR3AdvanceRules]
 * and therefore does not invent any missing match state.
 */
object LegacyMatchR3AdvanceRuntimeRules {
    data class State(
        val legacyTick: Int = 0,
        val legacyWBySide: List<Int> = listOf(0, 0),
        val legacyQ0BySide: List<Int> = listOf(0, 0),
        val legacyA0BySide: List<Int> = listOf(0, 0),
    ) {
        init {
            require(legacyTick >= 0) { "Legacy r3 tick must not be negative" }
            listOf(legacyWBySide, legacyQ0BySide, legacyA0BySide).forEach { values ->
                require(values.size == 2) { "Legacy K side counters must contain exactly two values" }
            }
        }
    }

    data class Result<T>(
        val state: State,
        val event: T?,
        val nextSide: Int,
    )

    fun <T> apply(
        state: State,
        advance: LegacyMatchR3AdvanceRules.Result<T>,
    ): Result<T> {
        require(advance.counterSide in 0..1) { "Legacy K counter side must be 0 or 1" }
        require(advance.nextSide in 0..1) { "Legacy K next side must be 0 or 1" }

        var legacyW = state.legacyWBySide
        var legacyQ0 = state.legacyQ0BySide
        var legacyA0 = state.legacyA0BySide

        when (advance.counterMutation) {
            LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_W_CURRENT ->
                legacyW = increment(legacyW, advance.counterSide)

            LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_Q0_OPPOSITE ->
                legacyQ0 = increment(legacyQ0, advance.counterSide)

            LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_A0_CURRENT ->
                legacyA0 = increment(legacyA0, advance.counterSide)
        }

        val next = state.copy(
            legacyTick = state.legacyTick + if (advance.incrementTick) 1 else 0,
            legacyWBySide = legacyW,
            legacyQ0BySide = legacyQ0,
            legacyA0BySide = legacyA0,
        )
        return Result(
            state = next,
            event = advance.event,
            nextSide = advance.nextSide,
        )
    }

    private fun increment(values: List<Int>, side: Int): List<Int> =
        values.toMutableList().also { it[side] = it[side] + 1 }
}
