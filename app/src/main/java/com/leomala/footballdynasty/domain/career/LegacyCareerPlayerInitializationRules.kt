package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Exact RNG/value projection of legacy `best.o.q1()`, invoked for every active player by
 * `best.b.z1()` during new-career initialization.
 *
 * Legacy `best.o.E` is populated from `e.g.getStatus()` by the canonical player constructor.
 */
object LegacyCareerPlayerInitializationRules {
    data class Input(
        val targetR0: Boolean,
        val targetO: Int,
        val targetP0: Int,
        val targetF0: Int,
        val playerStatus: Int,
        val playerStar: Boolean,
        val playerWorldTop: Boolean,
    )

    data class Result(
        val overall: Int,
        val contractDays: Long,
    )

    fun initialize(random: RandomSource, input: Input): Result {
        var overall =
            mappedTargetF0(input.targetF0) +
                targetBand(input.targetR0, input.targetO, input.targetP0) +
                random.nextInt(3)

        if (input.playerStatus == 1) {
            overall += 8 + random.nextInt(2)
        }

        if (input.playerStar || input.playerWorldTop) {
            overall += 9 + random.nextInt(3)
        }

        overall = overall.coerceAtMost(100)
        val contractDays = (random.nextInt(30) + 210).toLong()
        return Result(overall = overall, contractDays = contractDays)
    }

    fun targetBand(targetR0: Boolean, targetO: Int, targetP0: Int): Int =
        if (targetR0) {
            when (targetO) {
                1 -> 20
                2 -> 15
                3 -> 5
                else -> 1
            }
        } else {
            when (targetP0) {
                1, 2, 3 -> 5
                4 -> 15
                5 -> 22
                else -> 1
            }
        }

    fun mappedTargetF0(targetF0: Int): Int =
        when (targetF0) {
            in Int.MIN_VALUE..15 -> targetF0
            16 -> 17
            17 -> 18
            18 -> 19
            19 -> 21
            20 -> 25
            21 -> 26
            22 -> 27
            23 -> 28
            24 -> 29
            25 -> 30
            else -> 0
        }
}
