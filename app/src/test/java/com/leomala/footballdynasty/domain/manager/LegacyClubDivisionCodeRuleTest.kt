package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyClubDivisionCodeRuleTest {
    @Test
    fun `setter preserves negative zero and divisions one through four`() {
        assertEquals(-7, LegacyClubDivisionCodeRule.write(-7))
        assertEquals(0, LegacyClubDivisionCodeRule.write(0))
        for (division in 1..4) {
            assertEquals(division, LegacyClubDivisionCodeRule.write(division))
        }
    }

    @Test
    fun `setter resets only values above four to zero`() {
        assertEquals(0, LegacyClubDivisionCodeRule.write(5))
        assertEquals(0, LegacyClubDivisionCodeRule.write(99))
    }

    @Test
    fun `league assignment reuses setter semantics and country pool is zero`() {
        assertEquals(3, LegacyClubDivisionCodeRule.assignFromLeague(3))
        assertEquals(0, LegacyClubDivisionCodeRule.assignFromLeague(5))
        assertEquals(-1, LegacyClubDivisionCodeRule.assignFromLeague(-1))
        assertEquals(0, LegacyClubDivisionCodeRule.returnToCountryPool())
    }
}
