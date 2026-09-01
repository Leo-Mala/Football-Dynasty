package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyStadiumInitialSectorRuleTest {
    @Test
    fun `constructor derives four sectors in legacy order`() {
        assertEquals(
            listOf(1_500, 7_510, 900, 90),
            LegacyStadiumInitialSectorRule.fromAggregateCapacity(10_000),
        )
    }

    @Test
    fun `invalid aggregate capacity falls back to legacy ten thousand`() {
        assertEquals(
            LegacyStadiumInitialSectorRule.fromAggregateCapacity(10_000),
            LegacyStadiumInitialSectorRule.fromAggregateCapacity(999),
        )
        assertEquals(
            LegacyStadiumInitialSectorRule.fromAggregateCapacity(10_000),
            LegacyStadiumInitialSectorRule.fromAggregateCapacity(120_001),
        )
    }

    @Test
    fun `sector caps do not redistribute removed capacity`() {
        assertEquals(
            listOf(18_000, 80_000, 9_000, 700),
            LegacyStadiumInitialSectorRule.fromAggregateCapacity(120_000),
        )
    }
}
