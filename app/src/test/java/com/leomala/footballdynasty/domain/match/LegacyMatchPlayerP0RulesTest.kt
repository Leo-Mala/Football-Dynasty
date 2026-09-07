package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchPlayerP0RulesTest {
    @Test
    fun `nonpositive legacy slot is mismatch exactly as legacy early return`() {
        assertTrue(LegacyMatchPlayerP0Rules.resolve(0, 3, null))
        assertTrue(LegacyMatchPlayerP0Rules.resolve(-1, 3, null))
    }

    @Test
    fun `slot above legacy table is not mismatch`() {
        assertFalse(LegacyMatchPlayerP0Rules.resolve(26, 3, null))
    }

    @Test
    fun `matching base position is not mismatch`() {
        assertFalse(LegacyMatchPlayerP0Rules.resolve(14, 3, 3))
    }

    @Test
    fun `special slots ten and seventeen accept legacy l0 one`() {
        assertFalse(LegacyMatchPlayerP0Rules.resolve(10, 1, 3))
        assertFalse(LegacyMatchPlayerP0Rules.resolve(17, 1, 3))
        assertTrue(LegacyMatchPlayerP0Rules.resolve(10, 2, 3))
    }
}
