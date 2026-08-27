package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchP0RulesTest {
    @Test
    fun `single state side one win resolves before any aggregate logic`() {
        assertEquals(
            LegacyMatchP0Rules.Outcome.LEGACY_SIDE_1,
            LegacyMatchP0Rules.resolve(input(b0 = 2, d0 = 1)),
        )
    }

    @Test
    fun `single state side two win resolves symmetrically`() {
        assertEquals(
            LegacyMatchP0Rules.Outcome.LEGACY_SIDE_2,
            LegacyMatchP0Rules.resolve(input(b0 = 0, d0 = 1)),
        )
    }

    @Test
    fun `single state draw remains unresolved`() {
        val value = input(b0 = 1, d0 = 1)

        assertEquals(LegacyMatchP0Rules.Outcome.UNRESOLVED, LegacyMatchP0Rules.resolve(value))
        assertEquals(true, LegacyMatchP0Rules.isUnresolved(value))
    }

    @Test
    fun `two state leg-count comparison precedes aggregate comparison`() {
        val value = input(
            e0Enabled = true,
            b0 = 1,
            d0 = 0,
            c0 = 0,
            e0 = 1,
        )

        assertEquals(LegacyMatchP0Rules.Outcome.LEGACY_SIDE_1, LegacyMatchP0Rules.resolve(value))
    }

    @Test
    fun `tied leg-count falls through to aggregate side one`() {
        val value = input(
            e0Enabled = true,
            b0 = 2,
            d0 = 0,
            c0 = 1,
            e0 = 0,
        )

        assertEquals(LegacyMatchP0Rules.Outcome.LEGACY_SIDE_1, LegacyMatchP0Rules.resolve(value))
    }

    @Test
    fun `tied leg-count falls through to aggregate side two`() {
        val value = input(
            e0Enabled = true,
            b0 = 0,
            d0 = 2,
            c0 = 0,
            e0 = 1,
        )

        assertEquals(LegacyMatchP0Rules.Outcome.LEGACY_SIDE_2, LegacyMatchP0Rules.resolve(value))
    }

    @Test
    fun `third criterion is skipped when competition flag is false`() {
        val value = input(
            competitionP0 = false,
            e0Enabled = true,
            b0 = 1,
            d0 = 0,
            c0 = 2,
            e0 = 1,
        )

        assertEquals(LegacyMatchP0Rules.Outcome.UNRESOLVED, LegacyMatchP0Rules.resolve(value))
    }

    @Test
    fun `third criterion selects side one only after prior comparisons remain tied`() {
        val value = input(
            competitionP0 = true,
            e0Enabled = true,
            b0 = 1,
            d0 = 0,
            c0 = 2,
            e0 = 1,
        )

        assertEquals(LegacyMatchP0Rules.Outcome.LEGACY_SIDE_1, LegacyMatchP0Rules.resolve(value))
    }

    @Test
    fun `third criterion selects side two on inverse comparison`() {
        val value = input(
            competitionP0 = true,
            e0Enabled = true,
            b0 = 2,
            d0 = 3,
            c0 = 0,
            e0 = 1,
        )

        assertEquals(LegacyMatchP0Rules.Outcome.LEGACY_SIDE_2, LegacyMatchP0Rules.resolve(value))
    }

    @Test
    fun `third criterion equality remains unresolved`() {
        val value = input(
            competitionP0 = true,
            e0Enabled = true,
            b0 = 1,
            d0 = 1,
            c0 = 1,
            e0 = 1,
        )

        assertEquals(LegacyMatchP0Rules.Outcome.UNRESOLVED, LegacyMatchP0Rules.resolve(value))
    }

    @Test
    fun `disabled second state ignores legacy C0 E0 and competition flag`() {
        val value = input(
            competitionP0 = true,
            e0Enabled = false,
            b0 = 1,
            d0 = 1,
            c0 = 99,
            e0 = 0,
        )

        assertEquals(LegacyMatchP0Rules.Outcome.UNRESOLVED, LegacyMatchP0Rules.resolve(value))
    }

    private fun input(
        competitionP0: Boolean = false,
        e0Enabled: Boolean = false,
        b0: Int,
        d0: Int,
        c0: Int = 0,
        e0: Int = 0,
    ) = LegacyMatchP0Rules.Input(
        legacyCompetitionP0 = competitionP0,
        legacyE0Enabled = e0Enabled,
        legacyB0 = b0,
        legacyD0 = d0,
        legacyC0 = c0,
        legacyE0 = e0,
    )
}
