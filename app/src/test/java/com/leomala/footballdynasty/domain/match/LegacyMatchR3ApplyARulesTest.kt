package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchR3ApplyARulesTest {
    @Test
    fun `first side zero update yields one hundred zero`() {
        val result = LegacyMatchR3ApplyARules.apply(listOf(0, 0), side = 0)

        assertEquals(listOf(1, 0), result.updatedLegacyB0)
        assertEquals(listOf(100, 0), result.updatedLegacyE)
    }

    @Test
    fun `balanced counts yield rounded fifty fifty`() {
        val result = LegacyMatchR3ApplyARules.apply(listOf(1, 0), side = 1)

        assertEquals(listOf(1, 1), result.updatedLegacyB0)
        assertEquals(listOf(50, 50), result.updatedLegacyE)
    }

    @Test
    fun `one third uses float division and Math round`() {
        val result = LegacyMatchR3ApplyARules.apply(listOf(1, 1), side = 1)

        assertEquals(listOf(1, 2), result.updatedLegacyB0)
        assertEquals(listOf(33, 67), result.updatedLegacyE)
    }

    @Test
    fun `legacy percent matches smali float conversion rather than integer division`() {
        assertEquals(25, LegacyMatchR3ApplyARules.legacyPercent(1, 4))
        assertEquals(75, LegacyMatchR3ApplyARules.legacyPercent(3, 4))
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `invalid side is not normalized`() {
        LegacyMatchR3ApplyARules.apply(listOf(0, 0), side = 2)
    }
}
