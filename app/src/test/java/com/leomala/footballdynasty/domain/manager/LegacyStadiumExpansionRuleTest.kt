package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyStadiumExpansionRuleTest {
    @Test
    fun `legacy J branch ordering makes every value at least two use Q index one`() {
        val current = listOf(18_000, 80_000, 9_000, 700)

        assertEquals(
            listOf(20_000, 100_000, 10_000, 800),
            LegacyStadiumExpansionRule.effectiveLimits(current, legacyJValue = 2),
        )
        assertEquals(
            listOf(20_000, 100_000, 10_000, 800),
            LegacyStadiumExpansionRule.effectiveLimits(current, legacyJValue = 6),
        )
        assertEquals(
            listOf(20_000, 100_000, 10_000, 800),
            LegacyStadiumExpansionRule.effectiveLimits(current, legacyJValue = 10),
        )
    }

    @Test
    fun `below two default limits are raised to already existing capacity`() {
        assertEquals(
            listOf(19_000, 90_000, 9_500, 750),
            LegacyStadiumExpansionRule.effectiveLimits(
                currentCapacities = listOf(19_000, 90_000, 9_500, 750),
                legacyJValue = 1,
            ),
        )
    }

    @Test
    fun `zero expansion is rejected before cost and duration are produced`() {
        val quote = LegacyStadiumExpansionRule.quote(
            currentCapacities = listOf(10_000, 40_000, 4_000, 300),
            additions = listOf(0, 0, 0, 0),
            legacyJValue = 0,
        )

        assertFalse(quote.accepted)
        assertNull(quote.totalCost)
        assertNull(quote.constructionDays)
    }

    @Test
    fun `an addition above any available category is rejected`() {
        val quote = LegacyStadiumExpansionRule.quote(
            currentCapacities = listOf(18_000, 80_000, 9_000, 700),
            additions = listOf(2_001, 0, 0, 0),
            legacyJValue = 2,
        )

        assertFalse(quote.accepted)
        assertEquals(listOf(2_000, 20_000, 1_000, 100), quote.availableAdditions)
    }

    @Test
    fun `quote preserves projected-capacity unit brackets and fixed one hundred thousand cost`() {
        val quote = LegacyStadiumExpansionRule.quote(
            currentCapacities = listOf(900, 4_900, 900, 90),
            additions = listOf(100, 100, 100, 10),
            legacyJValue = 0,
        )

        // Projected capacities hit the first threshold in every category:
        // 100*80 + 100*120 + 100*300 + 10*1500 + 100000.
        assertTrue(quote.accepted)
        assertEquals(165_000, quote.totalCost)
        assertEquals(15, quote.constructionDays)
    }

    @Test
    fun `category cost selects the first threshold greater than or equal to projected capacity`() {
        assertEquals(
            80,
            LegacyStadiumExpansionRule.categoryCost(
                category = 0,
                currentCapacity = 999,
                addition = 1,
            ),
        )
        assertEquals(
            160,
            LegacyStadiumExpansionRule.categoryCost(
                category = 0,
                currentCapacity = 1_000,
                addition = 1,
            ),
        )
        assertEquals(
            700,
            LegacyStadiumExpansionRule.categoryCost(
                category = 0,
                currentCapacity = 18_000,
                addition = 1,
            ),
        )
    }

    @Test
    fun `construction duration preserves every legacy threshold edge`() {
        assertEquals(15, LegacyStadiumExpansionRule.constructionDays(999))
        assertEquals(20, LegacyStadiumExpansionRule.constructionDays(1_000))
        assertEquals(20, LegacyStadiumExpansionRule.constructionDays(9_999))
        assertEquals(30, LegacyStadiumExpansionRule.constructionDays(10_000))
        assertEquals(30, LegacyStadiumExpansionRule.constructionDays(29_999))
        assertEquals(40, LegacyStadiumExpansionRule.constructionDays(30_000))
    }
}
