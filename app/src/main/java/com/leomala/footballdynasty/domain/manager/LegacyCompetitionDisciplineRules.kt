package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.match.LegacyMatchEventType
import com.leomala.footballdynasty.foundation.random.RandomSource

/** Raw serializable `best.r` counters used by `best.o.V0(k0)`. */
data class LegacyCompetitionDisciplineState(
    val legacyThreshold3Counter: Int = 0,
    val legacyThreshold1Counter: Int = 0,
) {
    init {
        require(legacyThreshold3Counter >= 0)
        require(legacyThreshold1Counter >= 0)
    }
}

/**
 * Exact `best.r.n/a/i/j/k` projection plus only the discipline-counter part of `best.o.E0`.
 *
 * `best.o.E0(4,...)` starts with i3=2 and tests `nextInt(1000) > 800` before an unreachable
 * `else if (> 950)`. Consequently the raw suspension counter receives 2 increments for 801..999
 * and 1 increment for 0..800. Other `E0` side effects are intentionally outside this rule.
 */
object LegacyCompetitionDisciplineRules {
    fun isExcluded(state: LegacyCompetitionDisciplineState): Boolean =
        state.legacyThreshold3Counter >= 3 || state.legacyThreshold1Counter >= 1

    /** Exact `best.r.a()`: reset the >=3 counter first, otherwise decrement the >=1 counter. */
    fun consumeExistingSuspension(
        state: LegacyCompetitionDisciplineState,
    ): LegacyCompetitionDisciplineState = when {
        state.legacyThreshold3Counter >= 3 -> state.copy(legacyThreshold3Counter = 0)
        state.legacyThreshold1Counter >= 1 ->
            state.copy(legacyThreshold1Counter = state.legacyThreshold1Counter - 1)
        else -> state
    }

    /** Exact counter mutation reached after the legacy `best.o.S0()` gate has passed. */
    fun applyEvent(
        state: LegacyCompetitionDisciplineState,
        legacyEventType: Int,
        directRedRandom: RandomSource? = null,
    ): LegacyCompetitionDisciplineState = when (legacyEventType) {
        LegacyMatchEventType.YELLOW_CARD.legacyCode ->
            state.copy(legacyThreshold3Counter = state.legacyThreshold3Counter + 1)

        LegacyMatchEventType.SECOND_YELLOW_RED.legacyCode ->
            state.copy(
                legacyThreshold3Counter = state.legacyThreshold3Counter + 1,
                legacyThreshold1Counter = state.legacyThreshold1Counter + 1,
            )

        LegacyMatchEventType.RED_CARD.legacyCode -> {
            val random = requireNotNull(directRedRandom) {
                "Direct red legacy E0 mutation requires its fresh java.util.Random draw"
            }
            val increments = if (random.nextInt(1000) > 800) 2 else 1
            state.copy(
                legacyThreshold1Counter = state.legacyThreshold1Counter + increments,
            )
        }

        else -> state
    }
}
