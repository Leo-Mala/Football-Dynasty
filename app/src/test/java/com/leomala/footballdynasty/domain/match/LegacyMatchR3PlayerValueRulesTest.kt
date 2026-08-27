package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchR3PlayerValueRulesTest {
    @Test
    fun `plain legacy O value is divided by ten`() {
        assertEquals(
            8.0,
            LegacyMatchR3PlayerValueRules.value(80, false, false, false, null),
            0.0,
        )
    }

    @Test
    fun `P0 flag rounds seventy percent before later transformations`() {
        assertEquals(
            5.6,
            LegacyMatchR3PlayerValueRules.value(80, true, false, false, null),
            0.0,
        )
    }

    @Test
    fun `O0 uses one point zero two when club p0 is at most three`() {
        val club = LegacyMatchR3PlayerValueRules.LegacyClubState(legacyP0 = 3, legacyQ0 = false)

        assertEquals(
            8.2,
            LegacyMatchR3PlayerValueRules.value(80, false, true, false, club),
            0.0,
        )
    }

    @Test
    fun `O0 uses one point zero five without qualifying club`() {
        assertEquals(8.4, LegacyMatchR3PlayerValueRules.value(80, false, true, false, null), 0.0)
        assertEquals(
            8.4,
            LegacyMatchR3PlayerValueRules.value(
                80, false, true, false,
                LegacyMatchR3PlayerValueRules.LegacyClubState(4, false),
            ),
            0.0,
        )
    }

    @Test
    fun `O0 flag takes precedence when both O0 and W0 are true`() {
        assertEquals(
            8.4,
            LegacyMatchR3PlayerValueRules.value(80, false, true, true, null),
            0.0,
        )
    }

    @Test
    fun `W0 uses one point zero five for club p0 at most three`() {
        val club = LegacyMatchR3PlayerValueRules.LegacyClubState(3, false)

        assertEquals(8.4, LegacyMatchR3PlayerValueRules.value(80, false, false, true, club), 0.0)
    }

    @Test
    fun `W0 uses one point ten otherwise`() {
        assertEquals(8.8, LegacyMatchR3PlayerValueRules.value(80, false, false, true, null), 0.0)
    }

    @Test
    fun `club Q0 applies another rounded one point zero five after earlier multiplier`() {
        val club = LegacyMatchR3PlayerValueRules.LegacyClubState(3, true)

        val result = LegacyMatchR3PlayerValueRules.value(83, false, true, false, club)

        // round(83*1.02)=85; round(85*1.05)=89; /10 = 8.9
        assertEquals(8.9, result, 0.0)
    }

    @Test
    fun `rounding happens after every stage rather than once at the end`() {
        val club = LegacyMatchR3PlayerValueRules.LegacyClubState(3, true)

        val result = LegacyMatchR3PlayerValueRules.value(81, true, true, false, club)

        // round(81*.7)=57; round(57*1.02)=58; round(58*1.05)=61; /10 = 6.1
        assertEquals(6.1, result, 0.0)
    }
}
