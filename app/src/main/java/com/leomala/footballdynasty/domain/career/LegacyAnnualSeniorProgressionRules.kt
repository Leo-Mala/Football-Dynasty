package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Composed executable boundary for reachable legacy `best.o.e()` -> `s()`/`t()`.
 *
 * The component rules below were characterized separately from the pinned official SMALI. This
 * class only composes their already-proven order against the durable legacy fields M/N and keeps
 * raw obfuscated inputs raw. It introduces no new sporting semantics.
 */
object LegacyAnnualSeniorProgressionRules {
    data class ClubState(
        val hasCurrentClub: Boolean,
        val legacyF0: Int,
        val legacyR0: Boolean,
        val legacyP0: Int,
        val legacyJ: Int,
        val legacyJ0: Int,
        val legacyO: Int,
    )

    data class PlayerState(
        val age: Int,
        val overall: Int,
        val legacyN: Double,
        val legacyMFlag: Boolean,
        /** Raw serialized/transient caller input corresponding to legacy `d0`. */
        val legacyD0: Int,
        /** Raw caller input corresponding to legacy integer field `m`. */
        val legacyM: Int,
        val legacyStar: Boolean,
        val legacyWorldTop: Boolean,
    )

    data class Result(
        val overall: Int,
        val legacyN: Double,
        val legacyMFlag: Boolean,
    )

    fun apply(
        club: ClubState,
        player: PlayerState,
        random: RandomSource,
    ): Result {
        val steps = LegacyAnnualSeniorProgressionRoutingRules.steps(
            hasCurrentClub = club.hasCurrentClub,
            age = player.age,
        )
        if (steps.isEmpty()) {
            return Result(player.overall, player.legacyN, player.legacyMFlag)
        }

        var nextOverall = player.overall
        var nextN = player.legacyN
        var nextM = player.legacyMFlag

        for (step in steps) {
            when (step) {
                LegacyAnnualSeniorProgressionRoutingRules.Step.APPLY_GROWTH -> {
                    val accumulated = LegacyAnnualSeniorGrowthAccumulationRules.accumulate(
                        currentN = nextN,
                        club = LegacyAnnualSeniorGrowthAccumulationRules.ClubState(
                            legacyF0 = club.legacyF0,
                            legacyR0 = club.legacyR0,
                            legacyP0 = club.legacyP0,
                            legacyJ = club.legacyJ,
                            legacyJ0 = club.legacyJ0,
                        ),
                        player = LegacyAnnualSeniorGrowthAccumulationRules.PlayerState(
                            legacyE = player.age,
                            legacyJ = nextOverall,
                            legacyD0 = player.legacyD0,
                            legacyM = player.legacyM,
                            legacyMFlag = nextM,
                            legacyW0 = player.legacyWorldTop,
                            legacyO0 = player.legacyStar,
                        ),
                    )
                    nextN = accumulated.updatedN
                    val target = LegacyAnnualSeniorGrowthTargetRules.calculate(
                        LegacyAnnualSeniorGrowthTargetRules.ClubState(
                            legacyR0 = club.legacyR0,
                            legacyO = club.legacyO,
                            legacyP0 = club.legacyP0,
                            legacyJ = club.legacyJ,
                            legacyJ0 = club.legacyJ0,
                        )
                    )
                    val finalized = LegacyAnnualSeniorGrowthFinalizationRules.apply(
                        input = LegacyAnnualSeniorGrowthFinalizationRules.Input(
                            overall = nextOverall,
                            legacyN = nextN,
                            cappedTarget = target.cappedTarget,
                            d0 = player.legacyD0,
                            m = player.legacyM,
                        ),
                        random = random,
                    )
                    nextOverall = finalized.overall
                    nextN = finalized.legacyN
                }

                LegacyAnnualSeniorProgressionRoutingRules.Step.APPLY_DECLINE -> {
                    val declined = LegacyAnnualSeniorDeclineRules.apply(
                        LegacyAnnualSeniorDeclineRules.Input(
                            age = player.age,
                            overall = nextOverall,
                            legacyN = nextN,
                            clubO = club.legacyO,
                            clubF0 = club.legacyF0,
                            clubR0 = club.legacyR0,
                            clubP0 = club.legacyP0,
                        )
                    )
                    nextOverall = declined.overall
                    nextN = declined.legacyN
                }

                LegacyAnnualSeniorProgressionRoutingRules.Step.CLEAR_LEGACY_M -> {
                    nextM = false
                }
            }
        }

        return Result(nextOverall, nextN, nextM)
    }
}
