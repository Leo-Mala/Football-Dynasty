package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Weighted player selectors recovered from reachable `components.r3.n()` and `components.r3.j()`. */
object LegacyMatchGoalPlayerSelectionRules {
    data class Player<T>(
        val value: T,
        val legacyG0: Int,
        val legacyL0: Int,
        val legacyG: Int = 0,
        val legacyH: Int = 0,
    )

    /** Legacy `r3.n()`: primary player selector for the current side. */
    fun <T> selectPrimary(
        active: List<Player<T>>,
        random: RandomSource,
    ): Player<T>? {
        var total = 0.0
        for (player in active) {
            if (isPrimaryEligible(player)) {
                total += primaryWeight(player)
            }
        }

        val target = random.nextDouble() * total
        var cumulative = 0.0
        for (player in active) {
            if (isPrimaryEligible(player)) {
                cumulative += primaryWeight(player)
            }
            // Intentionally outside the eligibility block, matching the legacy method.
            if (target <= cumulative) {
                return player
            }
        }

        // Legacy fallback scans from the final element down to index 1, excluding index 0.
        for (index in active.lastIndex downTo 1) {
            return active[index]
        }
        return null
    }

    /** Legacy `r3.j()`: weighted author selector from the opposing active list for own goals. */
    fun <T> selectOwnGoalAuthor(
        opponentActive: List<Player<T>>,
        random: RandomSource,
    ): Player<T>? {
        var total = 0.0
        for (player in opponentActive) {
            val position = player.legacyG0
            if (position >= 0 && position < OWN_GOAL_WEIGHTS.size) {
                total += OWN_GOAL_WEIGHTS[position]
            }
        }

        val target = random.nextDouble() * total
        var cumulative = 0.0
        for (player in opponentActive) {
            val position = player.legacyG0
            if (position >= 0 && position < OWN_GOAL_WEIGHTS.size) {
                cumulative += OWN_GOAL_WEIGHTS[position]
            }
            // Same legacy quirk: comparison is outside the position-validity block.
            if (target <= cumulative) {
                return player
            }
        }
        return null
    }

    private fun <T> isPrimaryEligible(player: Player<T>): Boolean {
        val position = player.legacyG0
        return position != 1 && player.legacyL0 != 0 && position > 0 && position < PRIMARY_WEIGHTS.size
    }

    private fun <T> primaryWeight(player: Player<T>): Double {
        var weight = PRIMARY_WEIGHTS[player.legacyG0]
        if (player.legacyG == 9 || player.legacyH == 9) {
            weight += 4.0
        } else if (player.legacyG == 5 || player.legacyH == 5) {
            weight += 2.0
            if (player.legacyL0 == 2) {
                weight += 2.0
            }
        }
        return weight
    }

    private val PRIMARY_WEIGHTS = doubleArrayOf(
        -1.0, -1.0,
        1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
        8.0,
        4.0, 4.0, 4.0,
        8.0, 8.0, 8.0, 8.0,
        22.0, 22.0, 22.0, 22.0, 22.0, 22.0, 22.0, 22.0,
    )

    private val OWN_GOAL_WEIGHTS = doubleArrayOf(
        -1.0,
        1.0,
        5.0,
        18.0, 18.0, 18.0, 18.0, 18.0, 18.0,
        5.0,
        1.0,
        5.0, 5.0, 5.0,
        1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
    )
}
