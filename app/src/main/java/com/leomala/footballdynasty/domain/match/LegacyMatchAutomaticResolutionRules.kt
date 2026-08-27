package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Executes the already-proven automatic `Q0()` flow through reachable legacy `best.s.o()`.
 * Club-flag clearing remains an unapplied operation from [LegacyMatchAutomaticFlowRules].
 */
object LegacyMatchAutomaticResolutionRules {
    data class Result<T>(
        val flow: LegacyMatchAutomaticFlowRules.Result<T>,
        val legacyO: LegacyMatchPostGateORules.Result?,
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
        val flow = LegacyMatchAutomaticFlowRules.run(
            random = random,
            runMinuteRule = runMinuteRule,
            advanceR3 = advanceR3,
            halftimeTransition = halftimeTransition,
            legacyZFlag = legacyZFlag,
            legacyA0Flag = legacyA0Flag,
            resolveP0 = resolveP0,
        )
        val legacyO =
            if (flow.postSimulation.invokeLegacyO) {
                LegacyMatchPostGateORules.resolve(random)
            } else {
                null
            }
        return Result(
            flow = flow,
            legacyO = legacyO,
        )
    }
}
