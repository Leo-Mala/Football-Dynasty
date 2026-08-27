package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Shared deterministic replacements for implicit match-engine randomness. */
object LegacyMatchRandomRules {
    fun <T> shuffleInPlace(values: MutableList<T>, random: RandomSource) {
        for (index in values.lastIndex downTo 1) {
            val other = random.nextInt(index + 1)
            if (other != index) {
                val tmp = values[index]
                values[index] = values[other]
                values[other] = tmp
            }
        }
    }
}
