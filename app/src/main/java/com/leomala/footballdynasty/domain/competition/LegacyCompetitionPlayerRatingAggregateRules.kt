package com.leomala.footballdynasty.domain.competition

/**
 * Exact composition of the reachable legacy `best.o.n(...) -> best.k0.a(best.o)` rating aggregate.
 *
 * The serialized legacy owner is `best.k0.g`, whose `components.n1` rows retain rating sum, sample
 * count, average and the final positional category. The call is reached only for a positive rating
 * and `best.k0.E() == 1`; those caller gates are preserved here so persistence adapters cannot
 * accidentally aggregate ratings for other competition types.
 */
object LegacyCompetitionPlayerRatingAggregateRules {
    private val CATEGORY_BY_G = intArrayOf(
        -1, 0, 5, 2, 2, 2, 2, 2, 2, 1, 3, 6, 6,
        6, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4,
    )

    data class Aggregate(
        val legacyRatingSum: Double,
        val legacyRatingCount: Double,
        val legacyAverageRating: Double,
        val legacyCategory: Int,
    )

    data class Input(
        val legacyCompetitionType: Int,
        val ratingY0: Double,
        val legacyG0: Int,
        val legacyL0: Int,
        val legacyF0: Int,
        val legacyR: Int,
    )

    /**
     * Returns the updated serialized `components.n1` projection, or null when this legacy call makes
     * no mutation. A null result means "retain any existing row unchanged", never delete it.
     */
    fun apply(existing: Aggregate?, input: Input): Aggregate? {
        if (input.ratingY0 <= 0.0 || input.legacyCompetitionType != 1) return null
        if (!isEligible(input)) return null
        require(input.legacyG0 in CATEGORY_BY_G.indices) {
            "Legacy competition rating g0 is outside category table: ${input.legacyG0}"
        }

        val sum = (existing?.legacyRatingSum ?: 0.0) + input.ratingY0
        val count = (existing?.legacyRatingCount ?: 0.0) + 1.0
        val baseCategory = CATEGORY_BY_G[input.legacyG0]
        val category = when {
            baseCategory == 3 && input.legacyR == 0 -> 6
            baseCategory == 3 && input.legacyR == 1 -> 3
            baseCategory == 5 && input.legacyL0 == 3 -> 3
            baseCategory == 1 && input.legacyL0 == 3 -> 3
            else -> baseCategory
        }

        return Aggregate(
            legacyRatingSum = sum,
            legacyRatingCount = count,
            legacyAverageRating = sum / count,
            legacyCategory = category,
        )
    }

    private fun isEligible(input: Input): Boolean {
        val l0 = input.legacyL0
        val f0 = input.legacyF0
        val g0 = input.legacyG0

        var eligible =
            (l0 != 0 || g0 == 1) &&
                (l0 != 1 || f0 != 0 || g0 == 9) &&
                (l0 != 1 || f0 != 1 || g0 == 2) &&
                when (l0) {
                    2 -> g0 in 3..8
                    3 -> g0 in 10..17
                    4 -> g0 >= 18
                    else -> true
                }

        // Exact special cases at the end of legacy `best.k0.a(best.o)`.
        if (l0 == 1 && f0 == 0 && g0 == 17) eligible = true
        if (l0 == 1 && f0 == 1 && g0 == 10) eligible = true
        return eligible
    }
}
