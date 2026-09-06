package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacySeniorPayrollScalarRulesTest {
    private fun calculate(
        v0: Boolean = true,
        o: Int = 1,
        f0: Int = 0,
        g: Int = 3,
        j: Int = 10,
        e: Int = 25,
        c: Boolean = false,
        d: Boolean = false,
        globalV1: Boolean = false,
    ) = LegacySeniorPayrollScalarRules.calculate(
        club = LegacySeniorPayrollScalarRules.ClubState(v0, o, f0),
        player = LegacySeniorPayrollScalarRules.PlayerState(g, j, e, c, d),
        legacyGlobalV1 = globalV1,
    )

    @Test
    fun `V0 and O preserve the executable scalar matrix`() {
        assertEquals(7500, calculate(v0 = true, o = 1))
        assertEquals(5500, calculate(v0 = true, o = 2))
        assertEquals(5000, calculate(v0 = true, o = 3))
        assertEquals(4500, calculate(v0 = true, o = 4))
        assertEquals(4500, calculate(v0 = true, o = 5))
        assertEquals(3500, calculate(v0 = true, o = 99))

        assertEquals(6000, calculate(v0 = false, o = 1))
        assertEquals(5000, calculate(v0 = false, o = 2))
        assertEquals(4500, calculate(v0 = false, o = 3))
        assertEquals(4000, calculate(v0 = false, o = 4))
        assertEquals(4000, calculate(v0 = false, o = 5))
        assertEquals(3500, calculate(v0 = false, o = 99))
    }

    @Test
    fun `f0 threshold and g adjustments occur before half rounding`() {
        assertEquals(8000, calculate(f0 = 21))
        assertEquals(6800, calculate(g = 0))
        assertEquals(7200, calculate(g = 1))
        assertEquals(7100, calculate(g = 2))
        assertEquals(7000, calculate(g = 4))
    }

    @Test
    fun `legacy E age adjustment applies only from 32 onward`() {
        assertEquals(7500, calculate(e = 31))
        assertEquals(7500, calculate(e = 32))
        assertEquals(7200, calculate(e = 33))
    }

    @Test
    fun `legacy C or D adds J times 250 and D then applies 1 point 4 multiplier`() {
        assertEquals(10000, calculate(c = true))
        assertEquals(14000, calculate(d = true))
        assertEquals(14000, calculate(c = true, d = true))
    }

    @Test
    fun `raw scalar has executable floor before D and global multipliers`() {
        assertEquals(500, calculate(j = 0))
        assertEquals(700, calculate(j = 0, d = true))
        assertEquals(2000, calculate(j = 0, globalV1 = true))
        assertEquals(2800, calculate(j = 0, d = true, globalV1 = true))
    }

    @Test
    fun `global V1 multiplies the final raw scalar by four`() {
        assertEquals(30000, calculate(globalV1 = true))
    }

    @Test
    fun `arithmetic keeps JVM int overflow instead of clamping`() {
        assertEquals(
            500,
            calculate(j = Int.MAX_VALUE, globalV1 = false),
        )
    }
}
