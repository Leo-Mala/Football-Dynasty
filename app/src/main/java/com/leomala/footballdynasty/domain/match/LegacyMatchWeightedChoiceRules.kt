package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Exact weighted-index helper shared by legacy `components.r3.A(...)` and `B(...)`. */
object LegacyMatchWeightedChoiceRules {
    fun selectIndex(
        weights: DoubleArray,
        multipliers: DoubleArray,
        random: RandomSource,
    ): Int {
        val products = DoubleArray(weights.size)
        var total = 0.0
        for (index in weights.indices) {
            val product = weights[index] * multipliers[index]
            products[index] = product
            total += product
        }

        val target = random.nextDouble() * total
        var cumulative = 0.0
        for (index in products.indices) {
            cumulative += products[index]
            if (target < cumulative) {
                return index
            }
        }
        return weights.size
    }
}
