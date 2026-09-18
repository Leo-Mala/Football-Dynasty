package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchPlayerP0RuntimeRulesTest {
    @Test
    fun `runtime consumes committed starter slot as legacy S`() {
        val matchingDefender = player(legacyG0 = 14, legacyL0 = 3)
        val mismatchingDefender = player(legacyG0 = 14, legacyL0 = 2)

        assertFalse(LegacyMatchPlayerP0RuntimeRules.resolve(matchingDefender))
        assertTrue(LegacyMatchPlayerP0RuntimeRules.resolve(mismatchingDefender))
    }

    @Test
    fun `special legacy slots preserve recovered l0 one exception`() {
        assertFalse(LegacyMatchPlayerP0RuntimeRules.resolve(player(legacyG0 = 10, legacyL0 = 1)))
        assertFalse(LegacyMatchPlayerP0RuntimeRules.resolve(player(legacyG0 = 17, legacyL0 = 1)))
        assertTrue(LegacyMatchPlayerP0RuntimeRules.resolve(player(legacyG0 = 10, legacyL0 = 2)))
    }

    @Test
    fun `unset and bench lineup codes preserve early branches without slot lookup`() {
        assertTrue(LegacyMatchPlayerP0RuntimeRules.resolve(player(legacyG0 = 0, legacyL0 = 3)))
        assertTrue(LegacyMatchPlayerP0RuntimeRules.resolve(player(legacyG0 = -1, legacyL0 = 3)))
        assertFalse(LegacyMatchPlayerP0RuntimeRules.resolve(player(legacyG0 = 26, legacyL0 = 3)))
        assertFalse(LegacyMatchPlayerP0RuntimeRules.resolve(player(legacyG0 = 35, legacyL0 = 3)))
    }

    private fun player(
        legacyG0: Int,
        legacyL0: Int,
    ) = LegacyMatchTransientRuntime.Player(
        value = "player",
        legacyG0 = legacyG0,
        legacyL0 = legacyL0,
        legacyF0 = 0,
        legacyR = 0,
        age = 25,
        energy = 100,
        skill = 80,
    )
}
