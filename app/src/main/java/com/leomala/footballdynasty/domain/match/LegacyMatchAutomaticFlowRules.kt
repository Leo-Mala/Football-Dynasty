package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Pure composition of the proven automatic legacy `best.s.Q0()` boundary.
 *
 * This composes the already-characterized two-half automatic simulation with the proven post-loop
 * short-circuit gate. It deliberately does not implement the internals of legacy `o()` and does not
 * apply the final club-flag mutation itself; callers receive the ordered post-simulation plan.
 */
object LegacyMatchAutomaticFlowRules {
    data class Result<T>(
        val simulation: LegacyMatchAutomaticSimulationRules.Result<T>,
        val postSimulation: LegacyMatchAutomaticPostSimulationRules.Result,
    )

    fun <T> run(
        random: RandomSource,
        runMinuteRule: (half: Int, minute: Int) -> Unit,
        advanceR3: (half: Int, minute: Int) -> T?,
        halftimeTransition: (half: Int, minute: Int) -> Unit,
        legacyZFlag: Boolean,
        legacyA0Flag: Boolean,
        resolveP0: () -> Boolean,
    ): Result<T> {
        val simulation = LegacyMatchAutomaticSimulationRules.run(
            random = random,
            runMinuteRule = runMinuteRule,
            advanceR3 = advanceR3,
            halftimeTransition = halftimeTransition,
        )
        val postSimulation = LegacyMatchAutomaticPostSimulationRules.resolve(
            legacyZFlag = legacyZFlag,
            legacyA0Flag = legacyA0Flag,
            resolveP0 = resolveP0,
        )
        return Result(
            simulation = simulation,
            postSimulation = postSimulation,
        )
    }
}
