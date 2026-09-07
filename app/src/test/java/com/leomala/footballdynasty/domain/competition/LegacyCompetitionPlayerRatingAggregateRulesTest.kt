package com.leomala.footballdynasty.domain.competition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyCompetitionPlayerRatingAggregateRulesTest {
    @Test
    fun `caller gates reject non positive ratings and non type one competitions`() {
        val base = input(ratingY0 = 7.0, legacyCompetitionType = 1, legacyG0 = 1, legacyL0 = 0)

        assertNull(
            LegacyCompetitionPlayerRatingAggregateRules.apply(
                existing = null,
                input = base.copy(ratingY0 = 0.0),
            )
        )
        assertNull(
            LegacyCompetitionPlayerRatingAggregateRules.apply(
                existing = null,
                input = base.copy(legacyCompetitionType = 2),
            )
        )
    }

    @Test
    fun `ineligible positional combination does not mutate k0 g`() {
        assertNull(
            LegacyCompetitionPlayerRatingAggregateRules.apply(
                existing = null,
                input = input(
                    ratingY0 = 7.0,
                    legacyCompetitionType = 1,
                    legacyG0 = 2,
                    legacyL0 = 0,
                ),
            )
        )
    }

    @Test
    fun `eligible goalkeeper creates exact first n1 aggregate`() {
        val result = requireNotNull(
            LegacyCompetitionPlayerRatingAggregateRules.apply(
                existing = null,
                input = input(
                    ratingY0 = 7.25,
                    legacyCompetitionType = 1,
                    legacyG0 = 1,
                    legacyL0 = 0,
                ),
            )
        )

        assertEquals(7.25, result.legacyRatingSum, 0.0)
        assertEquals(1.0, result.legacyRatingCount, 0.0)
        assertEquals(7.25, result.legacyAverageRating, 0.0)
        assertEquals(0, result.legacyCategory)
    }

    @Test
    fun `existing n1 aggregate accumulates sum count and average exactly`() {
        val existing = LegacyCompetitionPlayerRatingAggregateRules.Aggregate(
            legacyRatingSum = 7.0,
            legacyRatingCount = 1.0,
            legacyAverageRating = 7.0,
            legacyCategory = 0,
        )
        val result = requireNotNull(
            LegacyCompetitionPlayerRatingAggregateRules.apply(
                existing = existing,
                input = input(
                    ratingY0 = 5.0,
                    legacyCompetitionType = 1,
                    legacyG0 = 1,
                    legacyL0 = 0,
                ),
            )
        )

        assertEquals(12.0, result.legacyRatingSum, 0.0)
        assertEquals(2.0, result.legacyRatingCount, 0.0)
        assertEquals(6.0, result.legacyAverageRating, 0.0)
        assertEquals(0, result.legacyCategory)
    }

    @Test
    fun `category three uses legacy R split`() {
        val rZero = requireNotNull(
            LegacyCompetitionPlayerRatingAggregateRules.apply(
                existing = null,
                input = input(
                    ratingY0 = 6.0,
                    legacyCompetitionType = 1,
                    legacyG0 = 14,
                    legacyL0 = 3,
                    legacyR = 0,
                ),
            )
        )
        val rOne = requireNotNull(
            LegacyCompetitionPlayerRatingAggregateRules.apply(
                existing = null,
                input = input(
                    ratingY0 = 6.0,
                    legacyCompetitionType = 1,
                    legacyG0 = 14,
                    legacyL0 = 3,
                    legacyR = 1,
                ),
            )
        )

        assertEquals(6, rZero.legacyCategory)
        assertEquals(3, rOne.legacyCategory)
    }

    @Test
    fun `legacy l0 one special cases override the generic eligibility gate`() {
        val left = LegacyCompetitionPlayerRatingAggregateRules.apply(
            existing = null,
            input = input(
                ratingY0 = 6.5,
                legacyCompetitionType = 1,
                legacyG0 = 17,
                legacyL0 = 1,
                legacyF0 = 0,
            ),
        )
        val right = LegacyCompetitionPlayerRatingAggregateRules.apply(
            existing = null,
            input = input(
                ratingY0 = 6.5,
                legacyCompetitionType = 1,
                legacyG0 = 10,
                legacyL0 = 1,
                legacyF0 = 1,
            ),
        )

        assertEquals(3, requireNotNull(left).legacyCategory)
        assertEquals(3, requireNotNull(right).legacyCategory)
    }

    private fun input(
        ratingY0: Double,
        legacyCompetitionType: Int,
        legacyG0: Int,
        legacyL0: Int,
        legacyF0: Int = 0,
        legacyR: Int = 0,
    ) = LegacyCompetitionPlayerRatingAggregateRules.Input(
        legacyCompetitionType = legacyCompetitionType,
        ratingY0 = ratingY0,
        legacyG0 = legacyG0,
        legacyL0 = legacyL0,
        legacyF0 = legacyF0,
        legacyR = legacyR,
    )
}
