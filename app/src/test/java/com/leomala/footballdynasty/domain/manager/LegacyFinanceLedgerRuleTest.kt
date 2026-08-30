package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyFinanceLedgerRuleTest {
    @Test
    fun `routes proven income categories and ignores unknown income categories`() {
        var state = LegacyFinanceLedgerState()
        state = LegacyFinanceLedgerRule.addIncome(state, 10, LegacyFinanceLedgerRule.INCOME_PLAYER_SALE)
        state = LegacyFinanceLedgerRule.addIncome(state, 20, LegacyFinanceLedgerRule.INCOME_PRIZE)
        state = LegacyFinanceLedgerRule.addIncome(state, 30, LegacyFinanceLedgerRule.INCOME_TICKET)
        state = LegacyFinanceLedgerRule.addIncome(state, 40, LegacyFinanceLedgerRule.INCOME_SPONSOR)
        state = LegacyFinanceLedgerRule.addIncome(state, 999, -123)

        assertEquals(10L, state.playerSaleIncome)
        assertEquals(20, state.prizeIncome)
        assertEquals(30, state.ticketIncome)
        assertEquals(40, state.sponsorIncome)
        assertEquals(100L, state.totalIncome())
    }

    @Test
    fun `routes proven expense categories and sends every other code to miscellaneous`() {
        var state = LegacyFinanceLedgerState()
        state = LegacyFinanceLedgerRule.addExpense(state, 10, LegacyFinanceLedgerRule.EXPENSE_PLAYER_PURCHASE)
        state = LegacyFinanceLedgerRule.addExpense(state, 20, LegacyFinanceLedgerRule.EXPENSE_SALARY)
        state = LegacyFinanceLedgerRule.addExpense(state, 30, LegacyFinanceLedgerRule.EXPENSE_BORROWING_CHARGE)
        state = LegacyFinanceLedgerRule.addExpense(state, 40, LegacyFinanceLedgerRule.EXPENSE_STADIUM)
        state = LegacyFinanceLedgerRule.addExpense(state, 50, LegacyFinanceLedgerRule.EXPENSE_FINE)
        state = LegacyFinanceLedgerRule.addExpense(state, 60, 999)
        state = LegacyFinanceLedgerRule.addExpense(state, 70, -1)

        assertEquals(10L, state.playerPurchaseExpense)
        assertEquals(20L, state.salaryExpense)
        assertEquals(30, state.borrowingChargeExpense)
        assertEquals(40, state.stadiumExpense)
        assertEquals(50, state.fineExpense)
        assertEquals(130, state.miscellaneousExpense)
        assertEquals(280L, state.totalExpense())
    }

    @Test
    fun `totals and balance preserve legacy long promotion points`() {
        val state = LegacyFinanceLedgerState(
            ticketIncome = Int.MAX_VALUE,
            playerSaleIncome = Int.MAX_VALUE.toLong(),
            prizeIncome = 3,
            sponsorIncome = 4,
            playerPurchaseExpense = Int.MAX_VALUE.toLong(),
            stadiumExpense = 5,
            salaryExpense = Int.MAX_VALUE.toLong(),
            borrowingChargeExpense = 6,
            fineExpense = 7,
            miscellaneousExpense = 8,
        )

        val income = Int.MAX_VALUE.toLong() * 2L + 7L
        val expense = Int.MAX_VALUE.toLong() * 2L + 26L
        assertEquals(income, state.totalIncome())
        assertEquals(expense, state.totalExpense())
        assertEquals(income - expense, state.balance())
    }

    @Test
    fun `period reset clears ledger movements but preserves borrowing state`() {
        val state = LegacyFinanceLedgerState(
            ticketIncome = 1,
            playerSaleIncome = 2,
            prizeIncome = 3,
            sponsorIncome = 4,
            playerPurchaseExpense = 5,
            stadiumExpense = 6,
            salaryExpense = 7,
            borrowingChargeExpense = 8,
            fineExpense = 9,
            miscellaneousExpense = 10,
            borrowed = 1_500_000,
            monthlyBorrowingCharge = 45_000,
        )

        val reset = LegacyFinanceLedgerRule.resetPeriod(state)

        assertEquals(0L, reset.totalIncome())
        assertEquals(0L, reset.totalExpense())
        assertEquals(0L, reset.balance())
        assertEquals(1_500_000, reset.borrowed)
        assertEquals(45_000, reset.monthlyBorrowingCharge)
    }

    @Test
    fun `integer-backed buckets retain JVM overflow behavior`() {
        val state = LegacyFinanceLedgerState(prizeIncome = Int.MAX_VALUE)
        val overflowed = LegacyFinanceLedgerRule.addIncome(
            state,
            1,
            LegacyFinanceLedgerRule.INCOME_PRIZE,
        )
        assertEquals(Int.MIN_VALUE, overflowed.prizeIncome)
    }
}
