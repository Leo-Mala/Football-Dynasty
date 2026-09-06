package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyAnnualClubPayrollCompositionRulesTest {
    @Test
    fun `empty payroll composes to zero`() {
        assertEquals(
            LegacyAnnualClubPayrollCompositionRules.Result(0L, 0L, 0L),
            LegacyAnnualClubPayrollCompositionRules.compose(emptyList(), emptyList()),
        )
    }

    @Test
    fun `SMALI scalar getters are direct signed int to long contributions`() {
        assertEquals(-17L, LegacyAnnualClubPayrollCompositionRules.seniorContributionFromLegacyN(-17))
        assertEquals(
            Int.MAX_VALUE.toLong(),
            LegacyAnnualClubPayrollCompositionRules.juniorContributionFromLegacyI(Int.MAX_VALUE),
        )
    }

    @Test
    fun `raw senior n fields are accumulated before raw junior i fields`() {
        assertEquals(
            LegacyAnnualClubPayrollCompositionRules.Result(
                seniorTotal = 60L,
                juniorTotal = 15L,
                total = 75L,
            ),
            LegacyAnnualClubPayrollCompositionRules.composeFromRawFields(
                seniorLegacyN = listOf(10, 20, 30),
                juniorLegacyI = listOf(7, 8),
            ),
        )
    }

    @Test
    fun `senior contributions are accumulated before junior contributions`() {
        assertEquals(
            LegacyAnnualClubPayrollCompositionRules.Result(
                seniorTotal = 60L,
                juniorTotal = 15L,
                total = 75L,
            ),
            LegacyAnnualClubPayrollCompositionRules.compose(
                seniorContributions = listOf(10L, 20L, 30L),
                juniorContributions = listOf(7L, 8L),
            ),
        )
    }

    @Test
    fun `composition preserves JVM long overflow semantics instead of clamping`() {
        assertEquals(
            LegacyAnnualClubPayrollCompositionRules.Result(
                seniorTotal = Long.MIN_VALUE,
                juniorTotal = 0L,
                total = Long.MIN_VALUE,
            ),
            LegacyAnnualClubPayrollCompositionRules.compose(
                seniorContributions = listOf(Long.MAX_VALUE, 1L),
                juniorContributions = emptyList(),
            ),
        )
    }

    @Test
    fun `junior accumulation continues from senior running total`() {
        assertEquals(
            LegacyAnnualClubPayrollCompositionRules.Result(
                seniorTotal = Long.MAX_VALUE,
                juniorTotal = 1L,
                total = Long.MIN_VALUE,
            ),
            LegacyAnnualClubPayrollCompositionRules.compose(
                seniorContributions = listOf(Long.MAX_VALUE),
                juniorContributions = listOf(1L),
            ),
        )
    }
}
