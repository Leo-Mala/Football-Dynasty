package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAnnualClubPayrollRoutingRulesTest {
    @Test
    fun `empty global club list produces no payroll calls`() {
        val calls = LegacyAnnualClubPayrollRoutingRules.plan(
            legacyCalendarMonth = 5,
            clubs = emptyList(),
        )

        assertTrue(calls.isEmpty())
    }

    @Test
    fun `only clubs matching Y0 month predicate call z preserving source order`() {
        val calls = LegacyAnnualClubPayrollRoutingRules.plan(
            legacyCalendarMonth = 8,
            clubs = listOf(
                LegacyAnnualClubPayrollRoutingRules.ClubEntry(matchesLegacyMonthPredicate = false),
                LegacyAnnualClubPayrollRoutingRules.ClubEntry(matchesLegacyMonthPredicate = true),
                LegacyAnnualClubPayrollRoutingRules.ClubEntry(matchesLegacyMonthPredicate = false),
                LegacyAnnualClubPayrollRoutingRules.ClubEntry(matchesLegacyMonthPredicate = true),
            ),
        )

        assertEquals(listOf(1, 3), calls.map { it.sourceIndex })
        assertEquals(listOf(8, 8), calls.map { it.legacyCalendarMonth })
    }

    @Test
    fun `routing does not deduplicate adjacent eligible clubs`() {
        val calls = LegacyAnnualClubPayrollRoutingRules.plan(
            legacyCalendarMonth = 0,
            clubs = List(3) {
                LegacyAnnualClubPayrollRoutingRules.ClubEntry(matchesLegacyMonthPredicate = true)
            },
        )

        assertEquals(listOf(0, 1, 2), calls.map { it.sourceIndex })
    }
}
