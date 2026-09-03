package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LegacyTicketClubSourceRuleTest {
    @Test
    fun `p0 and J come from exact legacy team source fields`() {
        val result = LegacyTicketClubSourceRule.project(
            home = LegacyTicketClubSourceFields(country = 29, reputation = 9),
            away = LegacyTicketClubSourceFields(country = 11, reputation = -2),
        )

        assertEquals(5, result.homeRawP0)
        assertEquals(0, result.awayRawP0)
        assertEquals(1, result.homeRawJ) // best.y.P29 (BRA).g()
    }

    @Test
    fun `invalid home country preserves fail closed valueOf boundary`() {
        assertThrows(IllegalArgumentException::class.java) {
            LegacyTicketClubSourceRule.project(
                home = LegacyTicketClubSourceFields(country = 221, reputation = 3),
                away = LegacyTicketClubSourceFields(country = 11, reputation = 3),
            )
        }
    }
}
