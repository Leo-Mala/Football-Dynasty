package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacySalaryPaymentRuleTest {
    @Test
    fun `raw true schedule selects only day of month two`() {
        assertTrue(
            LegacySalaryPaymentRule.shouldSchedule(
                useDayOfMonthTwoSchedule = true,
                dayOfMonth = 2,
                dayOfWeek = 4,
            )
        )
        assertFalse(
            LegacySalaryPaymentRule.shouldSchedule(
                useDayOfMonthTwoSchedule = true,
                dayOfMonth = 3,
                dayOfWeek = 1,
            )
        )
    }

    @Test
    fun `raw false schedule selects Sundays instead of day two`() {
        assertTrue(
            LegacySalaryPaymentRule.shouldSchedule(
                useDayOfMonthTwoSchedule = false,
                dayOfMonth = 9,
                dayOfWeek = 1,
            )
        )
        assertFalse(
            LegacySalaryPaymentRule.shouldSchedule(
                useDayOfMonthTwoSchedule = false,
                dayOfMonth = 2,
                dayOfWeek = 2,
            )
        )
    }

    @Test
    fun `calendar month eligibility requires a participating competition entry in same month`() {
        assertTrue(LegacySalaryPaymentRule.eligibleForCalendarMonth(7, listOf(1, 7, 10)))
        assertFalse(LegacySalaryPaymentRule.eligibleForCalendarMonth(7, listOf(1, 6, 10)))
    }

    @Test
    fun `salary total preserves senior then youth long accumulation without int truncation`() {
        val total = LegacySalaryPaymentRule.totalSalary(
            seniorSalaryCodes = listOf(Int.MAX_VALUE, Int.MAX_VALUE),
            youthSalaryCodes = listOf(25, 75),
        )
        assertEquals((Int.MAX_VALUE.toLong() * 2L) + 100L, total)
    }

    @Test
    fun `eligible salary event debits cash even below zero and accumulates long salary expense`() {
        val before = LegacyFinanceRuntimeState(
            cash = 100L,
            ledger = LegacyFinanceLedgerState(salaryExpense = 20L, fineExpense = 7),
        )
        val after = LegacySalaryPaymentRule.apply(
            state = before,
            seniorSalaryCodes = listOf(80, 70),
            youthSalaryCodes = listOf(25),
            eligibleForCurrentCalendarMonth = true,
        )

        assertEquals(-75L, after.cash)
        assertEquals(195L, after.ledger.salaryExpense)
        assertEquals(7, after.ledger.fineExpense)
    }

    @Test
    fun `ineligible club is exact no op`() {
        val before = LegacyFinanceRuntimeState(
            cash = 123L,
            ledger = LegacyFinanceLedgerState(salaryExpense = 456L),
        )
        val after = LegacySalaryPaymentRule.apply(
            state = before,
            seniorSalaryCodes = listOf(999),
            youthSalaryCodes = listOf(888),
            eligibleForCurrentCalendarMonth = false,
        )
        assertSame(before, after)
    }

    @Test
    fun `legacy arithmetic does not clamp negative salary codes`() {
        val before = LegacyFinanceRuntimeState(100L, LegacyFinanceLedgerState())
        val after = LegacySalaryPaymentRule.apply(
            state = before,
            seniorSalaryCodes = listOf(-10),
            youthSalaryCodes = emptyList(),
            eligibleForCurrentCalendarMonth = true,
        )
        assertEquals(110L, after.cash)
        assertEquals(-10L, after.ledger.salaryExpense)
    }
}
