package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchAutomaticPostSimulationRulesTest {
    @Test
    fun `false Z short circuits P0 while the post-loop flag clear still remains`() {
        var p0Calls = 0

        val result = LegacyMatchAutomaticPostSimulationRules.resolve(
            legacyZFlag = false,
            legacyA0Flag = true,
            resolveP0 = { p0Calls++; true },
        )

        assertFalse(result.evaluatedP0)
        assertFalse(result.invokeLegacyO)
        assertTrue(result.clearBothClubFlags)
        assertEquals(
            listOf(LegacyMatchAutomaticPostSimulationRules.Operation.CLEAR_BOTH_CLUB_FLAGS),
            result.operations,
        )
        assertEquals(0, p0Calls)
    }

    @Test
    fun `false a0 short circuits P0 while the post-loop flag clear still remains`() {
        var p0Calls = 0

        val result = LegacyMatchAutomaticPostSimulationRules.resolve(
            legacyZFlag = true,
            legacyA0Flag = false,
            resolveP0 = { p0Calls++; true },
        )

        assertFalse(result.evaluatedP0)
        assertFalse(result.invokeLegacyO)
        assertTrue(result.clearBothClubFlags)
        assertEquals(
            listOf(LegacyMatchAutomaticPostSimulationRules.Operation.CLEAR_BOTH_CLUB_FLAGS),
            result.operations,
        )
        assertEquals(0, p0Calls)
    }

    @Test
    fun `true flags evaluate P0 once and false P0 skips o before flag clear`() {
        var p0Calls = 0

        val result = LegacyMatchAutomaticPostSimulationRules.resolve(
            legacyZFlag = true,
            legacyA0Flag = true,
            resolveP0 = { p0Calls++; false },
        )

        assertTrue(result.evaluatedP0)
        assertFalse(result.invokeLegacyO)
        assertEquals(
            listOf(LegacyMatchAutomaticPostSimulationRules.Operation.CLEAR_BOTH_CLUB_FLAGS),
            result.operations,
        )
        assertEquals(1, p0Calls)
    }

    @Test
    fun `true flags and true P0 order legacy o before aggregate flag clear`() {
        var p0Calls = 0

        val result = LegacyMatchAutomaticPostSimulationRules.resolve(
            legacyZFlag = true,
            legacyA0Flag = true,
            resolveP0 = { p0Calls++; true },
        )

        assertTrue(result.evaluatedP0)
        assertTrue(result.invokeLegacyO)
        assertTrue(result.clearBothClubFlags)
        assertEquals(
            listOf(
                LegacyMatchAutomaticPostSimulationRules.Operation.INVOKE_LEGACY_O,
                LegacyMatchAutomaticPostSimulationRules.Operation.CLEAR_BOTH_CLUB_FLAGS,
            ),
            result.operations,
        )
        assertEquals(1, p0Calls)
    }
}
