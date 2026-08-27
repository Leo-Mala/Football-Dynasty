package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Weighted secondary-player selector recovered directly from truncated legacy `components.r3.i(best.o)`. */
object LegacyMatchGoalSecondarySelectionRules {
    data class Player<T>(
        val value: T,
        val legacyPositionIndex: Int,
        val legacyG: Int,
        val legacyH: Int,
        val legacyL0: Int,
    )

    fun <T> select(
        primary: Player<T>?,
        active: List<Player<T>>,
        legacyClubI0Index2: Int,
        random: RandomSource,
    ): Player<T>? {
        // Legacy creates a fresh Random for this gate. Modern determinism preserves the observable
        // draw site and order through RandomSource rather than claiming legacy seed equivalence.
        if (random.nextInt(100) > 80) {
            return null
        }

        var total = 0.0
        for (player in active) {
            if (isEligible(player, primary)) {
                total += weight(player, legacyClubI0Index2)
            }
        }

        val target = random.nextDouble() * total
        var cumulative = 0.0
        for (player in active) {
            if (isEligible(player, primary)) {
                cumulative += weight(player, legacyClubI0Index2)
            }
            // Preserved SMALI quirk: comparison is outside the eligibility block.
            if (target <= cumulative) {
                return player
            }
        }
        return null
    }

    fun <T> weight(
        player: Player<T>,
        legacyClubI0Index2: Int,
    ): Double {
        var result = BASE_WEIGHTS[player.legacyPositionIndex]
        val g = player.legacyG
        val h = player.legacyH

        if (g == 11 || h == 11) {
            result += 10.0
            if (g == 4 || h == 4) {
                result += 5.0
            }
        } else if (g == 4 || h == 4) {
            result += 2.0
            if (g == 8) {
                result += 2.0
            }
        } else if (g == 8 || h == 8) {
            result += 2.0
            if (g == 13) {
                result += 2.0
            }
        } else if (g == 13 || h == 13) {
            result += 1.0
            if (player.legacyL0 == 1) {
                result += 2.0
            }
        } else if (g == 6 || h == 6) {
            result += 5.0
            if (player.legacyL0 == 1) {
                result += 2.0
            }
        }

        if (legacyClubI0Index2 == 1 && player.legacyL0 == 1) {
            result += 20.0
        }
        return result
    }

    private fun <T> isEligible(player: Player<T>, primary: Player<T>?): Boolean =
        player !== primary &&
            player.legacyPositionIndex > 0 &&
            player.legacyPositionIndex < BASE_WEIGHTS.size

    private val BASE_WEIGHTS = doubleArrayOf(
        1.0, 1.0, 10.0,
        2.0, 2.0, 2.0, 2.0, 2.0, 2.0,
        10.0, 10.0,
        4.0, 4.0, 4.0,
        20.0, 20.0, 20.0,
        10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0,
    )
}
