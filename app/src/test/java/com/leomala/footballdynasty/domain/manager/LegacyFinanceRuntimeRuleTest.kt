package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyFinanceRuntimeRuleTest {
    @Test
    fun `borrowing updates cash and debt without inventing period income`() {
        val initialLedger = LegacyFinanceLedgerState(
            ticketIncome = 25_000,
            sponsorIncome = 10_000,
            borrowed = 500_000,
            monthlyBorrowingCharge = 15_000,
        )
        val initial = LegacyFinanceRuntimeState(
            cash = 100_000L,
            ledger = initialLedger,
        )

        val result = LegacyFinanceRuntimeRule.borrow(initial, rawDivisionCode = 2)

        assertTrue(result.accepted)
        assertEquals(-1, result.rawCashCategoryCode)
        assertEquals(600_000L, result.state.cash)
        assertEquals(1_000_000, result.state.ledger.borrowed)
        assertEquals(30_000, result.state.ledger.monthlyBorrowingCharge)
        assertEquals(25_000, result.state.ledger.ticketIncome)
        assertEquals(10_000, result.state.ledger.sponsorIncome)
        assertEquals(initialLedger.totalIncome(), result.state.ledger.totalIncome())
    }

    @Test
    fun `rejected borrowing preserves complete runtime state`() {
        val initial = LegacyFinanceRuntimeState(
            cash = 40_000L,
            ledger = LegacyFinanceLedgerState(
                borrowed = 1_500_000,
                monthlyBorrowingCharge = 45_000,
            ),
        )

        val result = LegacyFinanceRuntimeRule.borrow(initial, rawDivisionCode = 4)

        assertFalse(result.accepted)
        assertNull(result.rawCashCategoryCode)
        assertEquals(initial, result.state)
    }

    @Test
    fun `repayment uses unknown minus one expense category and reaches miscellaneous bucket`() {
        val initial = LegacyFinanceRuntimeState(
            cash = 900_000L,
            ledger = LegacyFinanceLedgerState(
                fineExpense = 7_000,
                miscellaneousExpense = 12_000,
                borrowed = 1_500_000,
                monthlyBorrowingCharge = 45_000,
            ),
        )

        val result = LegacyFinanceRuntimeRule.repay(initial)

        assertTrue(result.accepted)
        assertEquals(-1, result.rawCashCategoryCode)
        assertEquals(400_000L, result.state.cash)
        assertEquals(1_000_000, result.state.ledger.borrowed)
        assertEquals(30_000, result.state.ledger.monthlyBorrowingCharge)
        assertEquals(512_000, result.state.ledger.miscellaneousExpense)
        assertEquals(7_000, result.state.ledger.fineExpense)
    }

    @Test
    fun `rejected repayment does not write an expense`() {
        val initial = LegacyFinanceRuntimeState(
            cash = 499_999L,
            ledger = LegacyFinanceLedgerState(
                miscellaneousExpense = 9_000,
                borrowed = 500_000,
                monthlyBorrowingCharge = 15_000,
            ),
        )

        val result = LegacyFinanceRuntimeRule.repay(initial)

        assertFalse(result.accepted)
        assertNull(result.rawCashCategoryCode)
        assertEquals(initial, result.state)
    }

    @Test
    fun `salary debit subtracts long cash and records same long value when Q0 is active`() {
        val initial = LegacyFinanceRuntimeState(
            cash = 5_000_000_000L,
            ledger = LegacyFinanceLedgerState(
                salaryExpense = 100L,
                sponsorIncome = 77,
            ),
        )
        val amount = Int.MAX_VALUE.toLong() + 42L

        val result = LegacyFinanceRuntimeRule.applySalaryDebit(
            state = initial,
            amount = amount,
            recordSalaryLedger = true,
        )

        assertEquals(5_000_000_000L - amount, result.cash)
        assertEquals(100L + amount, result.ledger.salaryExpense)
        assertEquals(77, result.ledger.sponsorIncome)
    }

    @Test
    fun `salary debit still mutates cash when Q0 is false but leaves ledger untouched`() {
        val initial = LegacyFinanceRuntimeState(
            cash = 1_000L,
            ledger = LegacyFinanceLedgerState(
                salaryExpense = 250L,
                miscellaneousExpense = 19,
            ),
        )

        val result = LegacyFinanceRuntimeRule.applySalaryDebit(
            state = initial,
            amount = 400L,
            recordSalaryLedger = false,
        )

        assertEquals(600L, result.cash)
        assertEquals(initial.ledger, result.ledger)
    }

    @Test
    fun `salary debit preserves JVM long overflow semantics for cash and ledger`() {
        val initial = LegacyFinanceRuntimeState(
            cash = Long.MIN_VALUE,
            ledger = LegacyFinanceLedgerState(salaryExpense = Long.MAX_VALUE),
        )

        val result = LegacyFinanceRuntimeRule.applySalaryDebit(
            state = initial,
            amount = 1L,
            recordSalaryLedger = true,
        )

        assertEquals(Long.MAX_VALUE, result.cash)
        assertEquals(Long.MIN_VALUE, result.ledger.salaryExpense)
    }

    @Test
    fun `monthly borrowing charge can make cash negative and records category four expense`() {
        val initial = LegacyFinanceRuntimeState(
            cash = 10_000L,
            ledger = LegacyFinanceLedgerState(
                borrowingChargeExpense = 2_000,
                borrowed = 500_000,
                monthlyBorrowingCharge = 15_000,
            ),
        )

        val result = LegacyFinanceRuntimeRule.applyMonthlyBorrowingCharge(initial)

        assertEquals(-5_000L, result.cash)
        assertEquals(17_000, result.ledger.borrowingChargeExpense)
        assertEquals(500_000, result.ledger.borrowed)
        assertEquals(15_000, result.ledger.monthlyBorrowingCharge)
    }

    @Test
    fun `nonpositive monthly borrowing charge leaves runtime untouched`() {
        val zero = LegacyFinanceRuntimeState(
            cash = 10L,
            ledger = LegacyFinanceLedgerState(monthlyBorrowingCharge = 0),
        )
        val negative = LegacyFinanceRuntimeState(
            cash = 10L,
            ledger = LegacyFinanceLedgerState(monthlyBorrowingCharge = -1),
        )

        assertEquals(zero, LegacyFinanceRuntimeRule.applyMonthlyBorrowingCharge(zero))
        assertEquals(negative, LegacyFinanceRuntimeRule.applyMonthlyBorrowingCharge(negative))
    }

    @Test
    fun `period reset clears ledger buckets but preserves cash debt and precomputed charge`() {
        val initial = LegacyFinanceRuntimeState(
            cash = 321_000L,
            ledger = LegacyFinanceLedgerState(
                ticketIncome = 50_000,
                salaryExpense = 20_000L,
                miscellaneousExpense = 3_000,
                borrowed = 1_000_000,
                monthlyBorrowingCharge = 30_000,
            ),
        )

        val result = LegacyFinanceRuntimeRule.resetPeriod(initial)

        assertEquals(321_000L, result.cash)
        assertEquals(0L, result.ledger.totalIncome())
        assertEquals(0L, result.ledger.totalExpense())
        assertEquals(1_000_000, result.ledger.borrowed)
        assertEquals(30_000, result.ledger.monthlyBorrowingCharge)
    }
}
