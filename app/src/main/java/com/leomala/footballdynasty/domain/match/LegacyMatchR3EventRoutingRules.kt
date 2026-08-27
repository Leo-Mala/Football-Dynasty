package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Exact numeric/mutation routing parity for reachable legacy `components.r3.b()` / `c()`. */
object LegacyMatchR3EventRoutingRules {
    enum class WeightTable {
        B0, C0, D0, E0,
        A, B, C, D, E, F, G, H,
    }

    enum class SIncrementTiming {
        BEFORE_WEIGHTED_DRAW,
        AFTER_WEIGHTED_DRAW,
    }

    enum class Mutation {
        INCREMENT_S_CURRENT,
        MATERIALIZE_GOAL_CURRENT,
        INCREMENT_Y_CURRENT,
        INCREMENT_PRIMARY_R0_P,
        INCREMENT_Z_CURRENT,
    }

    data class Result(
        val selectedIndex: Int,
        val weightTable: WeightTable,
        val multipliers: List<Double>,
        val storedLegacyGAfter: Double,
        val sIncrementTiming: SIncrementTiming,
        val mutations: List<Mutation>,
    ) {
        val materializesGoal: Boolean
            get() = Mutation.MATERIALIZE_GOAL_CURRENT in mutations
    }

    /** Legacy `r3.e(...)`: unlike `d(...)`, the >=5 divisor is 10. */
    fun differenceE(first: Double, second: Double, legacyGlobalJValue: Int): Double =
        (first - second) / if (legacyGlobalJValue >= 5) 10.0 else 8.0

    fun resolveB(
        currentSide: Int,
        metricUOpposite: Double,
        metricZCurrent: Double,
        metricEOpposite: Double,
        metricDCurrent: Double,
        oppositeClubQ0Flag: Boolean,
        legacyNeutralFlag: Boolean,
        legacyHCurrent: Int,
        oppositeMinusCurrentP0: Int,
        primaryPlayerPresent: Boolean,
        legacyGlobalJValue: Int,
        random: RandomSource,
    ): Result {
        var second = differenceE(metricEOpposite, metricDCurrent, legacyGlobalJValue) + 1.0
        var third = differenceE(metricUOpposite, metricZCurrent, legacyGlobalJValue) + 1.0

        if (metricUOpposite == 0.0 && oppositeClubQ0Flag) {
            second = java.lang.Math.round(second * 0.2).toDouble()
        }

        if (!legacyNeutralFlag) {
            if (currentSide == 0) {
                second += 0.1
                // Legacy overwrites dE2 from the adjusted dE; it does not increment the old dE2.
                third = second + 0.1
            }
            if (currentSide == 1) {
                second -= 0.1
                third = second - 0.1
            }
        }

        var table = when {
            legacyHCurrent >= 6 -> WeightTable.E0
            legacyHCurrent >= 5 -> WeightTable.D0
            legacyHCurrent >= 3 -> WeightTable.C0
            else -> WeightTable.B0
        }
        if (legacyHCurrent >= 2 && oppositeMinusCurrentP0 >= 2) {
            table = WeightTable.D0
        }

        if (second < 0.2) second = 0.2
        if (third < 0.2) third = 0.2
        val multipliers = listOf(1.0, second, third)
        val selectedIndex = LegacyMatchWeightedChoiceRules.selectIndex(
            weights = weights(table),
            multipliers = multipliers.toDoubleArray(),
            random = random,
        )

        return Result(
            selectedIndex = selectedIndex,
            weightTable = table,
            multipliers = multipliers,
            storedLegacyGAfter = metricUOpposite,
            sIncrementTiming = SIncrementTiming.AFTER_WEIGHTED_DRAW,
            mutations = outcomeMutations(selectedIndex, primaryPlayerPresent),
        )
    }

    fun resolveC(
        currentSide: Int,
        currentMinute: Int,
        metricDCurrent: Double,
        metricEOpposite: Double,
        storedLegacyG: Double,
        legacyNeutralFlag: Boolean,
        legacyHCurrent: Int,
        oppositeMinusCurrentP0: Int,
        primaryPlayerPresent: Boolean,
        legacyGlobalJValue: Int,
        random: RandomSource,
    ): Result {
        var second = 1.0 - LegacyMatchR3DecisionRules.difference(
            metricDCurrent,
            metricEOpposite,
            legacyGlobalJValue,
        )
        var first = 1.0 - LegacyMatchR3DecisionRules.difference(
            metricEOpposite,
            metricDCurrent,
            legacyGlobalJValue,
        )
        var third = 1.0

        if (!legacyNeutralFlag) {
            if (currentSide == 0) {
                second -= 0.1
                third = 0.9
            }
            if (currentSide == 1) {
                first -= 0.1
            }
        }
        // This assignment is after the side adjustment in bytecode and therefore overrides it.
        if (storedLegacyG == 0.0) {
            first = 20.0
        }

        var table = when {
            currentMinute < 30 -> WeightTable.A
            currentMinute < 70 -> WeightTable.B
            else -> WeightTable.C
        }
        table = when {
            legacyHCurrent == 3 -> WeightTable.D
            legacyHCurrent == 4 -> WeightTable.E
            legacyHCurrent == 5 -> WeightTable.F
            legacyHCurrent >= 6 -> WeightTable.G
            else -> table
        }
        if (legacyHCurrent >= 3 && oppositeMinusCurrentP0 >= 2) {
            table = WeightTable.H
        }

        val multipliers = listOf(first, second, third)
        val selectedIndex = LegacyMatchWeightedChoiceRules.selectIndex(
            weights = weights(table),
            multipliers = multipliers.toDoubleArray(),
            random = random,
        )

        return Result(
            selectedIndex = selectedIndex,
            weightTable = table,
            multipliers = multipliers,
            storedLegacyGAfter = storedLegacyG,
            // `c()` increments i[current] before invoking weighted helper A(...).
            sIncrementTiming = SIncrementTiming.BEFORE_WEIGHTED_DRAW,
            mutations = outcomeMutations(selectedIndex, primaryPlayerPresent),
        )
    }

    private fun outcomeMutations(selectedIndex: Int, primaryPlayerPresent: Boolean): List<Mutation> =
        buildList {
            add(Mutation.INCREMENT_S_CURRENT)
            when (selectedIndex) {
                0 -> {
                    add(Mutation.MATERIALIZE_GOAL_CURRENT)
                    add(Mutation.INCREMENT_Y_CURRENT)
                }
                1 -> {
                    add(Mutation.INCREMENT_Y_CURRENT)
                    if (primaryPlayerPresent) add(Mutation.INCREMENT_PRIMARY_R0_P)
                }
                2 -> add(Mutation.INCREMENT_Z_CURRENT)
            }
        }

    private fun weights(table: WeightTable): DoubleArray =
        when (table) {
            WeightTable.B0 -> doubleArrayOf(5.5, 35.55, 15.0)
            WeightTable.C0 -> doubleArrayOf(4.5, 40.55, 15.0)
            WeightTable.D0 -> doubleArrayOf(3.0, 40.55, 15.0)
            WeightTable.E0 -> doubleArrayOf(0.5, 40.55, 15.0)
            WeightTable.A -> doubleArrayOf(7.8, 45.78, 53.52)
            WeightTable.B -> doubleArrayOf(10.8, 43.78, 53.52)
            WeightTable.C -> doubleArrayOf(13.2, 36.78, 44.52)
            WeightTable.D -> doubleArrayOf(7.8, 37.78, 45.52)
            WeightTable.E -> doubleArrayOf(5.8, 37.78, 45.52)
            WeightTable.F -> doubleArrayOf(2.8, 37.78, 45.52)
            WeightTable.G -> doubleArrayOf(1.8, 45.78, 53.52)
            WeightTable.H -> doubleArrayOf(1.0, 55.78, 63.52)
        }
}
