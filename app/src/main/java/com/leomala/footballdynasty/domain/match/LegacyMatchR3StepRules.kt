package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Pure composition of the recovered reachable `components.r3.J() -> I() -> K()` decision step.
 *
 * This class does not apply mutations or invent persistent match state. It only preserves the
 * already-characterized call order on one shared [RandomSource] and returns each pure mutation plan
 * to the caller for a later, separately proven integration boundary.
 */
object LegacyMatchR3StepRules {
    data class Result<T>(
        val jDecision: LegacyMatchR3DecisionRules.DecisionResult,
        val iDecision: LegacyMatchR3DecisionRules.DecisionResult?,
        val advance: LegacyMatchR3AdvanceRules.Result<T>,
    )

    fun <T> advance(
        currentSide: Int,
        metricYCurrent: Double,
        metricYOpposite: Double,
        metricUOpposite: Double,
        metricZCurrent: Double,
        legacyNeutralFlag: Boolean,
        legacyGlobalJValue: Int,
        random: RandomSource,
        produceGoalEvent: () -> T,
    ): Result<T> {
        var jDecision: LegacyMatchR3DecisionRules.DecisionResult? = null
        var iDecision: LegacyMatchR3DecisionRules.DecisionResult? = null

        val advance = LegacyMatchR3AdvanceRules.advance<T>(
            currentSide = currentSide,
            random = random,
            resolveJ = {
                LegacyMatchR3DecisionRules.resolveJ(
                    currentSide = currentSide,
                    metricYCurrent = metricYCurrent,
                    metricYOpposite = metricYOpposite,
                    legacyNeutralFlag = legacyNeutralFlag,
                    legacyGlobalJValue = legacyGlobalJValue,
                    random = random,
                ).also { jDecision = it }.returnedValue
            },
            resolveI = {
                LegacyMatchR3DecisionRules.resolveI(
                    currentSide = currentSide,
                    metricUOpposite = metricUOpposite,
                    metricZCurrent = metricZCurrent,
                    legacyNeutralFlag = legacyNeutralFlag,
                    legacyGlobalJValue = legacyGlobalJValue,
                    random = random,
                ).also { iDecision = it }.returnedValue
            },
            produceGoalEvent = produceGoalEvent,
        )

        return Result(
            jDecision = requireNotNull(jDecision) { "Legacy K must resolve J exactly once" },
            iDecision = iDecision,
            advance = advance,
        )
    }
}
