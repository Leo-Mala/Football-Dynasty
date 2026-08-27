package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Structural parity for reachable legacy `components.r3.J()` and `I()`. */
object LegacyMatchR3DecisionRules {
    enum class Mutation {
        J_T_OPPOSITE,
        J_S_CURRENT,
        J_P_CURRENT,
        J_APPLY_A_CURRENT,
        J_T_CURRENT,
        J_S_OPPOSITE,
        J_Q_OPPOSITE,
        J_APPLY_A_OPPOSITE,
        I_O_CURRENT,
        I_R_OPPOSITE,
    }

    data class DecisionResult(
        val weightedIndex: Int,
        val returnedValue: Int,
        val firstModifier: Double,
        val secondModifier: Double,
        val storedLegacyG: Double?,
        val mutations: List<Mutation>,
    )

    fun difference(
        first: Double,
        second: Double,
        legacyGlobalJValue: Int,
    ): Double {
        val divisor = when {
            legacyGlobalJValue >= 5 -> 11.0
            // Preserved dead branch from the legacy ternary ordering.
            legacyGlobalJValue >= 9 -> 12.0
            else -> 8.0
        }
        return (first - second) / divisor
    }

    fun resolveJ(
        currentSide: Int,
        metricYCurrent: Double,
        metricYOpposite: Double,
        legacyNeutralFlag: Boolean,
        legacyGlobalJValue: Int,
        random: RandomSource,
    ): DecisionResult {
        val opposite = if (currentSide == 1) 0 else 1
        var first = difference(metricYCurrent, metricYOpposite, legacyGlobalJValue) + 1.0
        var second = difference(metricYOpposite, metricYCurrent, legacyGlobalJValue) + 1.0
        if (!legacyNeutralFlag && currentSide == 0) {
            first += 0.3
        }
        if (first < 0.2) first = 0.2
        if (second < 0.2) second = 0.2

        val index = LegacyMatchWeightedChoiceRules.selectIndex(
            weights = doubleArrayOf(55.0, 45.0),
            multipliers = doubleArrayOf(first, second),
            random = random,
        )

        return when (index) {
            0 -> DecisionResult(
                weightedIndex = index,
                returnedValue = currentSide,
                firstModifier = first,
                secondModifier = second,
                storedLegacyG = null,
                mutations = listOf(
                    Mutation.J_T_OPPOSITE,
                    Mutation.J_S_CURRENT,
                    Mutation.J_P_CURRENT,
                    Mutation.J_APPLY_A_CURRENT,
                ),
            )
            1 -> DecisionResult(
                weightedIndex = index,
                returnedValue = opposite,
                firstModifier = first,
                secondModifier = second,
                storedLegacyG = null,
                mutations = listOf(
                    Mutation.J_T_CURRENT,
                    Mutation.J_S_OPPOSITE,
                    Mutation.J_Q_OPPOSITE,
                    Mutation.J_APPLY_A_OPPOSITE,
                ),
            )
            else -> DecisionResult(
                weightedIndex = index,
                returnedValue = 0,
                firstModifier = first,
                secondModifier = second,
                storedLegacyG = null,
                mutations = emptyList(),
            )
        }
    }

    fun resolveI(
        currentSide: Int,
        metricUOpposite: Double,
        metricZCurrent: Double,
        legacyNeutralFlag: Boolean,
        legacyGlobalJValue: Int,
        random: RandomSource,
    ): DecisionResult {
        var first = difference(metricZCurrent, metricUOpposite, legacyGlobalJValue) + 1.0
        var second = difference(metricUOpposite, metricZCurrent, legacyGlobalJValue) + 1.0
        if (metricUOpposite == 0.0) {
            second = 0.1
        }
        if (!legacyNeutralFlag && currentSide == 0) {
            first += 0.3
        }
        if (metricZCurrent == 0.0) {
            first = 0.1
        }
        if (first < 0.2) first = 0.2
        if (second < 0.2) second = 0.2

        val index = LegacyMatchWeightedChoiceRules.selectIndex(
            weights = doubleArrayOf(50.0, 50.0),
            multipliers = doubleArrayOf(first, second),
            random = random,
        )
        val mutations = when (index) {
            0 -> listOf(Mutation.I_O_CURRENT)
            1 -> listOf(Mutation.I_R_OPPOSITE)
            else -> emptyList()
        }
        return DecisionResult(
            weightedIndex = index,
            returnedValue = index,
            firstModifier = first,
            secondModifier = second,
            storedLegacyG = metricUOpposite,
            mutations = mutations,
        )
    }
}
