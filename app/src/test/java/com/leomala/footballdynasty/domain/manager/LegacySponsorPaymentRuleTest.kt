package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacySponsorPaymentRuleTest {
    @Test
    fun `fixed sponsor values match legacy Y1 first column`() {
        assertEquals(3_500_000, LegacySponsorPaymentRule.fixedSponsorForDivision(0))
        assertEquals(6_500_000, LegacySponsorPaymentRule.fixedSponsorForDivision(1))
        assertEquals(5_000_000, LegacySponsorPaymentRule.fixedSponsorForDivision(2))
        assertEquals(3_000_000, LegacySponsorPaymentRule.fixedSponsorForDivision(3))
        assertEquals(2_500_000, LegacySponsorPaymentRule.fixedSponsorForDivision(4))
        assertNull(LegacySponsorPaymentRule.fixedSponsorForDivision(-1))
        assertNull(LegacySponsorPaymentRule.fixedSponsorForDivision(5))
    }

    @Test
    fun `state championship branch adds payroll times 3 point 2 to cash but not sponsor ledger`() {
        val before = LegacyFinanceRuntimeState(
            cash = 1_000L,
            ledger = LegacyFinanceLedgerState(sponsorIncome = 40),
        )
        val after = LegacySponsorPaymentRule.apply(
            state = before,
            rawCountryCode = 10,
            rawDivisionCode = 0,
            playStateChampionship = true,
            seniorSalaryCodes = listOf(100, 50),
            youthSalaryCodes = listOf(25),
            recordFinanceLedger = true,
        )

        assertEquals(3_501_560L, after.cash)
        assertEquals(3_500_040, after.ledger.sponsorIncome)
    }

    @Test
    fun `country 29 skips payroll bonus but still receives fixed sponsor credit`() {
        val after = LegacySponsorPaymentRule.apply(
            state = LegacyFinanceRuntimeState(1_000L, LegacyFinanceLedgerState()),
            rawCountryCode = 29,
            rawDivisionCode = 1,
            playStateChampionship = true,
            seniorSalaryCodes = listOf(999_999),
            youthSalaryCodes = listOf(999_999),
            recordFinanceLedger = true,
        )
        assertEquals(6_501_000L, after.cash)
        assertEquals(6_500_000, after.ledger.sponsorIncome)
    }

    @Test
    fun `disabled state championship flag skips payroll bonus`() {
        val after = LegacySponsorPaymentRule.apply(
            state = LegacyFinanceRuntimeState(7L, LegacyFinanceLedgerState()),
            rawCountryCode = 10,
            rawDivisionCode = 4,
            playStateChampionship = false,
            seniorSalaryCodes = listOf(123),
            youthSalaryCodes = listOf(456),
            recordFinanceLedger = true,
        )
        assertEquals(2_500_007L, after.cash)
        assertEquals(2_500_000, after.ledger.sponsorIncome)
    }

    @Test
    fun `invalid division preserves earlier payroll bonus and records no sponsor income`() {
        val before = LegacyFinanceRuntimeState(
            cash = 10L,
            ledger = LegacyFinanceLedgerState(sponsorIncome = 90),
        )
        val after = LegacySponsorPaymentRule.apply(
            state = before,
            rawCountryCode = 10,
            rawDivisionCode = 9,
            playStateChampionship = true,
            seniorSalaryCodes = listOf(3),
            youthSalaryCodes = emptyList(),
            recordFinanceLedger = true,
        )
        assertEquals(19L, after.cash)
        assertEquals(90, after.ledger.sponsorIncome)
    }

    @Test
    fun `legacy ledger flag can suppress sponsor bucket while fixed credit still changes cash`() {
        val before = LegacyFinanceRuntimeState(
            cash = 2L,
            ledger = LegacyFinanceLedgerState(sponsorIncome = 11),
        )
        val after = LegacySponsorPaymentRule.apply(
            state = before,
            rawCountryCode = 29,
            rawDivisionCode = 3,
            playStateChampionship = false,
            seniorSalaryCodes = emptyList(),
            youthSalaryCodes = emptyList(),
            recordFinanceLedger = false,
        )
        assertEquals(3_000_002L, after.cash)
        assertEquals(11, after.ledger.sponsorIncome)
    }
}
