package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchAutomaticPostSimulationRulesTest {
    @Test
    fun `false Z short circuits a0 and P0 path while both club flags still clear`() {
        var p0Calls = 0

        val result = LegacyMatchAutomaticPostSimulationRules.resolve(
            legacyZFlag = false,
            legacyA0Flag = true,
            resolveP0 = { p0Calls++; true },
        )

        assertFalse(result.evaluatedP0)
        assertFalse(result.invokeLegacyO)
        assertTrue(result.clearFirstClubFlag)
        assertTrue(result.clearSecondClubFlag)
        org.junit.Assert.assertEquals(0, p0Calls)
    }

    @Test
    fun `false a0 short circuits P0 while both club flags still clear`() {
        var p0Calls = 0

        val result = LegacyMatchAutomaticPostSimulationRules.resolve(
            legacyZFlag = true,
            legacyA0Flag = false,
            resolveP0 = { p0Calls++; true },
        )

        assertFalse(result.evaluatedP0)
        assertFalse(result.invokeLegacyO)
        assertTrue(result.clearFirstClubFlag)
        assertTrue(result.clearSecondClubFlag)
        org.junit.Assert.assertEquals(0, p0Calls)
    }

    @Test
    fun `true flags evaluate P0 once and false P0 does not invoke legacy o`() {
        var p0Calls = 0

        val result = LegacyMatchAutomaticPostSimulationRules.resolve(
            legacyZFlag = true,
            legacyA0Flag = true,
            resolveP0 = { p0Calls++; false },
        )

        assertTrue(result.evaluatedP0)
        assertFalse(result.invokeLegacyO)
        assertTrue(result.clearFirstClubFlag)
        assertTrue(result.clearSecondClubFlag)
        org.junit.Assert.assertEquals(1, p0Calls)
    }

    @Test
    fun `true flags and true P0 route to legacy o before both club flags clear`() {
        var p0Calls = 0

        val result = LegacyMatchAutomaticPostSimulationRules.resolve(
            legacyZFlag = true,
            legacyA0Flag = true,
            resolveP0 = { p0Calls++; true },
        )

        assertTrue(result.evaluatedP0)
        assertTrue(result.invokeLegacyO)
        assertTrue(result.clearFirstClubFlag)
        assertTrue(result.clearSecondClubFlag)
        org.junit.Assert.assertEquals(1, p0Calls)
    }
}
