package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Small pure projections for the non-selection control decisions in legacy `best.a0`. */
object LegacyAnnualA0OrchestrationRules {
    data class BPasses(
        val highPasses: Int,
        val lowPasses: Int,
    )

    /** `best.a0.b(...)` calls `j(..., true)` and `j(..., false)` this many times each. */
    fun bestA0BPasses(index: Int): BPasses {
        val count = when {
            index <= 1 -> 1
            index <= 5 -> 2
            index <= 10 -> 3
            else -> 4
        }
        return BPasses(highPasses = count, lowPasses = count)
    }

    /** First loop of `best.a0.a()`: `I()` is called whenever `A()` exists. */
    fun bestA0AShouldCallI(hasA: Boolean): Boolean = hasA

    /**
     * Second loop of `best.a0.a()`. The random draw is last in the short-circuit chain exactly as
     * in Java + SMALI, so structural failures consume no RNG.
     */
    fun bestA0AShouldCallY(
        random: RandomSource,
        hasA: Boolean,
        legacyK: Boolean,
        legacyZ: Int,
    ): Boolean {
        if (!hasA) return false
        if (legacyK) return false
        if (legacyZ < 3) return false
        return LegacyAnnualRandomRules.bestA0AGate(random)
    }

    enum class BestA0CAction {
        NONE,
        CALL_H1,
        SET_S0_FALSE,
    }

    /** Exact branch in `best.a0.c()` for each `K0()` entry. */
    fun bestA0CAction(
        hasL0: Boolean,
        l0Q0: Boolean,
    ): BestA0CAction = when {
        !hasL0 -> BestA0CAction.NONE
        l0Q0 -> BestA0CAction.CALL_H1
        else -> BestA0CAction.SET_S0_FALSE
    }
}
