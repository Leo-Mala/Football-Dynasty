package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Exact side-normalized numeric core of legacy `best.o.n(best.s,int,int)` that produces `O/y0`.
 *
 * The legacy method constructs a fresh `java.util.Random` for these adjustments. Callers MUST supply
 * a compatibility/implicit random source here; the persisted career RNG is a different ownership
 * boundary and must not be consumed by this rule.
 */
object LegacyMatchPlayerRatingRules {
    private val INITIAL_S_BY_G = intArrayOf(1, 2, 7, 15, 23)

    data class ParticipationEvent(
        val primaryIsPlayer: Boolean,
        val secondaryIsPlayer: Boolean,
        val legacyPeriod: Int,
        val legacyMinute: Int,
    )

    data class Input(
        val legacyPlayerJ: Int,
        val legacyP0Flag: Boolean,
        val legacyG0: Int,
        val legacyS: Int,
        val legacyGCategory: Int,
        val legacyF: Int,
        val legacyGCode: Int,
        val currentGoals: Int,
        val opponentGoals: Int,
        val currentLegacyE: Int,
        val opponentLegacyE: Int,
        val currentLegacyQ0: Int,
        val opponentLegacyQ0: Int,
        val opponentLegacyW: Int,
        val opponentLegacyY: Int,
        val star: Boolean,
        val worldTop: Boolean,
        val n2: LegacyMatchN2CounterRules.GetterSnapshot,
        val participationMarker: Int,
    )

    data class Result(
        val ratingY0: Double,
        val resolvedLegacyS: Int,
    )

    /** Exact `v7` traversal at the beginning of legacy `best.o.n(...)`. Last matching event wins. */
    fun resolveParticipationMarker(events: Iterable<ParticipationEvent>): Int {
        var marker = 90
        events.forEach { event ->
            if (event.primaryIsPlayer) {
                marker = if (event.legacyPeriod == 1) {
                    event.legacyMinute
                } else {
                    event.legacyMinute + 48
                }
            }
            if (event.secondaryIsPlayer) {
                marker = if (event.legacyPeriod == 1) {
                    (50 - event.legacyMinute) + 48
                } else {
                    50 - event.legacyMinute
                }
            }
        }
        return marker
    }

    fun resolve(input: Input, implicitRandom: RandomSource): Result {
        val outcome = when {
            input.currentGoals > input.opponentGoals -> 1
            input.currentGoals < input.opponentGoals -> 2
            else -> 0
        }
        val s = if (input.legacyS < 1) INITIAL_S_BY_G[input.legacyGCategory] else input.legacyS
        var value = initialValue(outcome, input.legacyPlayerJ)

        if (input.legacyP0Flag) {
            value -= 1.5
            if (input.legacyG0 == 1) value -= 1.5
        }

        if (s in 10..17) {
            when {
                input.currentLegacyE > input.opponentLegacyE -> {
                    value += if (implicitRandom.nextInt(3) == 1) 0.3 else 0.8
                    if (input.legacyF == 0) value += 0.3
                    if (input.legacyGCode == 11 || input.legacyGCode == 4) value += 0.5
                }
                input.currentLegacyE < input.opponentLegacyE -> {
                    value -= if (implicitRandom.nextInt(3) == 1) 0.3 else 0.8
                    if (input.legacyF == 0) value -= 0.5
                }
            }
        }

        val n2 = input.n2
        if (n2.legacyH > 0) value += n2.legacyH * 0.9
        if (n2.legacyI > 0) value -= n2.legacyI * 1.5
        // Bytecode tests k() but subtracts i() * 1.2. Preserve that legacy quirk exactly.
        if (n2.legacyK > 0) value -= n2.legacyI * 1.2
        if (n2.legacyB > 0) value -= n2.legacyB * 0.2
        if (n2.legacyC > 0) value -= n2.legacyC * 0.8
        if (n2.legacyA > 0) value += n2.legacyA * 0.4

        if (s in 1..13) {
            when {
                input.currentLegacyQ0 > input.opponentLegacyQ0 -> {
                    value += if (implicitRandom.nextInt(3) == 1) 0.9 else 0.6
                    if (s in 2..9 && implicitRandom.nextInt(3) == 1) value += 0.6
                    if (s in 11..13 && implicitRandom.nextInt(3) == 1) value += 0.6
                }
                input.currentLegacyQ0 < input.opponentLegacyQ0 -> {
                    value -= 0.5
                    if (s in 3..8 && implicitRandom.nextInt(4) == 1) value -= 0.6
                    if (s in 11..13 && implicitRandom.nextInt(4) == 1) value -= 0.6
                }
            }
        }

        if (n2.legacyE > 0) value += n2.legacyE * 0.3

        if (s == 1) {
            value -= 0.8
            value += input.opponentLegacyY * 0.2
            if (n2.legacyJ > 0) value += n2.legacyJ * 1.2
            // The later >15 and >20 branches are unreachable in bytecode because >10 jumps past them.
            if (input.opponentLegacyW > 10) value += 0.2

            value -= when {
                input.opponentGoals >= 5 -> 2.0
                input.opponentGoals >= 4 -> 1.5
                input.opponentGoals >= 2 -> 1.0
                input.opponentGoals >= 1 -> 0.5
                else -> -1.0 // legacy adds 1.0 when the opponent score is zero
            }
            if (input.opponentLegacyY == 0) value -= 1.5
        }

        if (s in 1..13) {
            if (input.opponentGoals == 0) {
                value += 0.5
                if (s in 2..9) value += 0.5
                if (s in 11..13 && implicitRandom.nextInt(3) == 1) value += 0.5
            } else if (input.opponentGoals > 1) {
                value -= input.opponentGoals * 0.1
            }
            if (s in 2..13 && implicitRandom.nextInt(3) == 1) value -= 0.4
        }

        if (input.star) value += 0.4
        if (input.worldTop) value += 0.6
        if (value > 10.0) value = 10.0

        var finalValue = if (value < 0.0) 1.0 else value
        finalValue -= when {
            input.participationMarker < 15 -> 2.5
            input.participationMarker < 45 -> 1.5
            else -> 0.0
        }
        if (finalValue < 2.0) finalValue = 2.0
        if (input.participationMarker < 20 && finalValue <= 2.0) finalValue = 0.0

        return Result(
            ratingY0 = finalValue,
            resolvedLegacyS = s,
        )
    }

    private fun initialValue(outcome: Int, legacyPlayerJ: Int): Double = when (outcome) {
        0 -> when {
            legacyPlayerJ <= 30 -> 5.5
            legacyPlayerJ <= 60 -> 5.8
            legacyPlayerJ <= 90 -> 6.2
            else -> 6.8
        }
        1 -> when {
            legacyPlayerJ <= 60 -> 6.0
            legacyPlayerJ <= 90 -> 6.7
            else -> 7.2
        }
        2 -> when {
            legacyPlayerJ <= 30 -> 5.0
            legacyPlayerJ <= 60 -> 5.2
            legacyPlayerJ <= 90 -> 5.5
            else -> 6.0
        }
        else -> error("Legacy match outcome must be 0, 1 or 2: $outcome")
    }
}
