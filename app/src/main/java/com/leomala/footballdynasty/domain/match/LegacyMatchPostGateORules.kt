package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Exact pure reconstruction of reachable legacy `best.s.o()`. */
object LegacyMatchPostGateORules {
    enum class SelectedSide {
        LEGACY_E,
        LEGACY_F,
    }

    data class Result(
        val firstValue: Int,
        val comparisonValue: Int,
        val selectedSide: SelectedSide,
        val legacyD0Values: List<Int>,
    )

    fun resolve(random: RandomSource): Result {
        val first = random.nextInt(7) + 2
        val comparison = random.nextInt(7) + 2
        return if (first >= comparison) {
            Result(
                firstValue = first,
                comparisonValue = comparison,
                selectedSide = SelectedSide.LEGACY_E,
                legacyD0Values = listOf(first, first - 1),
            )
        } else {
            Result(
                firstValue = first,
                comparisonValue = comparison,
                selectedSide = SelectedSide.LEGACY_F,
                legacyD0Values = listOf(first, first + 1),
            )
        }
    }
}
