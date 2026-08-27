package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Direct control-flow parity for reachable `components.r3.K()`. */
object LegacyMatchR3AdvanceRules {
    enum class CounterMutation {
        LEGACY_W_CURRENT,
        LEGACY_Q0_OPPOSITE,
        LEGACY_A0_CURRENT,
    }

    data class Result<T>(
        val incrementTick: Boolean,
        val counterMutation: CounterMutation,
        val counterSide: Int,
        val event: T?,
        val nextSide: Int,
        val consumedDirectBound100Draw: Boolean,
    )

    fun <T> advance(
        currentSide: Int,
        random: RandomSource,
        resolveJ: () -> Int,
        resolveI: () -> Int,
        produceGoalEvent: () -> T,
    ): Result<T> {
        val jResult = resolveJ()
        val oppositeSide = if (currentSide == 1) 0 else 1

        val counterMutation: CounterMutation
        val counterSide: Int
        val event: T?
        val consumedDirectDraw: Boolean

        if (jResult == currentSide) {
            val iResult = resolveI()
            if (iResult == 0) {
                counterMutation = CounterMutation.LEGACY_W_CURRENT
                counterSide = currentSide
                event = produceGoalEvent()
                consumedDirectDraw = false
            } else {
                val draw = random.nextInt(100)
                if (draw < 50) {
                    counterMutation = CounterMutation.LEGACY_Q0_OPPOSITE
                    counterSide = oppositeSide
                } else {
                    counterMutation = CounterMutation.LEGACY_A0_CURRENT
                    counterSide = currentSide
                }
                event = null
                consumedDirectDraw = true
            }
        } else {
            val draw = random.nextInt(100)
            if (draw < 50) {
                counterMutation = CounterMutation.LEGACY_Q0_OPPOSITE
                counterSide = oppositeSide
            } else {
                counterMutation = CounterMutation.LEGACY_A0_CURRENT
                counterSide = currentSide
            }
            event = null
            consumedDirectDraw = true
        }

        val nextSide = when (currentSide) {
            0 -> 1
            1 -> 0
            else -> currentSide
        }

        return Result(
            incrementTick = true,
            counterMutation = counterMutation,
            counterSide = counterSide,
            event = event,
            nextSide = nextSide,
            consumedDirectBound100Draw = consumedDirectDraw,
        )
    }
}
