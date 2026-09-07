package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Pure projection of the final reachable mutation block of legacy `best.o.s()`.
 *
 * The caller supplies the cap already produced by the preceding club-specific SMALI branches and
 * the already-updated persistent accumulator `N`. This rule deliberately does not assign sporting
 * semantics to the still-obfuscated legacy fields `d0` and `m`.
 *
 * Executable authority: official `best/o.smali` from the pinned Brasfoot corpus.
 */
object LegacyAnnualSeniorGrowthFinalizationRules {
    data class Input(
        val overall: Int,
        val legacyN: Double,
        val cappedTarget: Int,
        val d0: Int,
        val m: Int,
    )

    data class Result(
        val overall: Int,
        val legacyN: Double,
        val effectiveTarget: Int,
    )

    fun apply(
        input: Input,
        random: RandomSource,
    ): Result {
        val effectiveTarget = LegacyAnnualRandomRules.bestOSApplyHighD0CapAdjustment(
            random = random,
            cappedTarget = input.cappedTarget,
            d0 = input.d0,
            m = input.m,
        )

        var nextOverall = input.overall
        var nextN = input.legacyN

        // SMALI uses strict N > 1.0 and additionally requires current overall < 100.
        if (nextN > 1.0 && nextOverall < 100) {
            if (nextOverall < effectiveTarget) {
                nextOverall += 1
                nextN -= 1.0
            } else {
                // Legacy collapses any excess accumulator to exactly one when the cap blocks growth.
                nextN = 1.0
            }
        }

        if (nextOverall > 100) {
            nextOverall = 100
        }

        return Result(
            overall = nextOverall,
            legacyN = nextN,
            effectiveTarget = effectiveTarget,
        )
    }
}
