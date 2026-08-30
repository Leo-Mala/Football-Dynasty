package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyStadiumFinanceRuntimeRuleTest {
    @Test
    fun acceptedConstructionDebitsCashAndAccumulatesRawStadiumExpense() {
        val initial = LegacyFinanceRuntimeState(
            cash = 2_000_000L,
            ledger = LegacyFinanceLedgerState(
                stadiumExpense = 13,
                borrowed = 500_000,
                monthlyBorrowingCharge = 2_000,
            ),
        )
        val quote = LegacyStadiumExpansionRule.quote(
            currentCapacities = listOf(1_000, 5_000, 1_000, 100),
            additions = listOf(100, 0, 0, 0),
            legacyJValue = 0,
        )
        val cost = requireNotNull(quote.totalCost)

        val result = LegacyStadiumFinanceRuntimeRule.startConstruction(
            state = initial,
            quote = quote,
            stadiumCode = 91,
            endTimestampMillis = 123_456_789L,
        )

        assertTrue(result.accepted)
        assertEquals(LegacyStadiumConstructionRule.LEGACY_STADIUM_DEBIT_CATEGORY, result.rawCashCategoryCode)
        assertEquals(initial.cash - cost.toLong(), result.state.cash)
        assertEquals(13 + cost, result.state.ledger.stadiumExpense)
        assertEquals(initial.ledger.borrowed, result.state.ledger.borrowed)
        assertEquals(initial.ledger.monthlyBorrowingCharge, result.state.ledger.monthlyBorrowingCharge)
        assertEquals(
            LegacyStadiumConstructionRecord(
                stadiumCode = 91,
                endTimestampMillis = 123_456_789L,
                additions = quote.additions,
            ),
            result.recordToAppend,
        )
    }

    @Test
    fun insufficientCashRejectsWithoutLedgerMutationOrConstructionRecord() {
        val initial = LegacyFinanceRuntimeState(
            cash = 99_999L,
            ledger = LegacyFinanceLedgerState(stadiumExpense = 77),
        )
        val quote = LegacyStadiumExpansionRule.quote(
            currentCapacities = listOf(1_000, 5_000, 1_000, 100),
            additions = listOf(1, 0, 0, 0),
            legacyJValue = 0,
        )

        val result = LegacyStadiumFinanceRuntimeRule.startConstruction(
            state = initial,
            quote = quote,
            stadiumCode = 4,
            endTimestampMillis = 10L,
        )

        assertFalse(result.accepted)
        assertSame(initial, result.state)
        assertNull(result.rawCashCategoryCode)
        assertNull(result.recordToAppend)
    }

    @Test
    fun rejectedQuoteCannotReachTheFinanceMutationBoundary() {
        val initial = LegacyFinanceRuntimeState(
            cash = 5_000_000L,
            ledger = LegacyFinanceLedgerState(stadiumExpense = 22),
        )
        val rejectedQuote = LegacyStadiumExpansionRule.quote(
            currentCapacities = listOf(1_000, 5_000, 1_000, 100),
            additions = listOf(0, 0, 0, 0),
            legacyJValue = 0,
        )

        val result = LegacyStadiumFinanceRuntimeRule.startConstruction(
            state = initial,
            quote = rejectedQuote,
            stadiumCode = 5,
            endTimestampMillis = 20L,
        )

        assertFalse(result.accepted)
        assertSame(initial, result.state)
        assertNull(result.rawCashCategoryCode)
        assertNull(result.recordToAppend)
    }
}
