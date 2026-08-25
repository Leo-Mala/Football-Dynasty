package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Structural parity for legacy `best.s.S/T/U/V/W` player selection. */
object LegacyMatchPlayerSelectionRules {
    data class Candidate<T>(
        val value: T,
        val legacyPositionIndex: Int,
    )

    data class PositionRange(
        val minInclusive: Int,
        val maxInclusive: Int,
    )

    private val ranges = arrayOf(
        PositionRange(10, 13),
        PositionRange(14, 17),
        PositionRange(3, 8),
        PositionRange(2, 3),
        PositionRange(8, 9),
        PositionRange(19, 24),
        PositionRange(1, 1),
        PositionRange(10, 13),
    )

    fun <T> selectS(candidates: List<Candidate<T>>, random: RandomSource): Candidate<T>? {
        val draw = random.nextInt(100)
        val bucket = when {
            draw < 25 -> 0
            draw < 40 -> 1
            draw < 65 -> 2
            draw < 73 -> 3
            draw < 82 -> 4
            draw < 85 -> 6
            else -> 5
        }
        return selectWithinRange(candidates, ranges[bucket], random)
    }

    fun <T> selectT(candidates: List<Candidate<T>>, random: RandomSource): Candidate<T>? {
        val draw = random.nextInt(500)
        val bucket = when {
            draw == 0 -> 6
            draw < 150 -> 0
            draw < 250 -> 1
            draw < 320 -> 2
            draw < 360 -> 3
            draw < 420 -> 4
            else -> 5
        }
        return selectWithinRange(candidates, ranges[bucket], random)
    }

    fun <T> selectU(candidates: List<Candidate<T>>, random: RandomSource): Candidate<T>? {
        val draw = random.nextInt(200)
        val bucket = when {
            draw == 0 -> 6
            draw < 80 -> 0
            draw < 110 -> 1
            draw < 160 -> 2
            draw < 170 -> 3
            draw < 190 -> 4
            else -> 5
        }
        return selectWithinRange(candidates, ranges[bucket], random)
    }

    fun <T> selectV(candidates: List<Candidate<T>>, random: RandomSource): Candidate<T>? {
        val draw = random.nextInt(1000)
        val bucket = when {
            draw == 0 -> 6
            draw < 150 -> 0
            draw < 350 -> 1
            draw < 400 -> 2
            draw < 450 -> 3
            draw < 500 -> 4
            else -> 5
        }
        return selectWithinRange(candidates, ranges[bucket], random)
    }

    fun <T> selectWithinRange(
        candidates: List<Candidate<T>>,
        range: PositionRange,
        random: RandomSource,
    ): Candidate<T>? {
        val eligible = candidates
            .filter { it.legacyPositionIndex in range.minInclusive..range.maxInclusive }
            .toMutableList()
        LegacyMatchRandomRules.shuffleInPlace(eligible, random)
        return eligible.firstOrNull()
    }
}
