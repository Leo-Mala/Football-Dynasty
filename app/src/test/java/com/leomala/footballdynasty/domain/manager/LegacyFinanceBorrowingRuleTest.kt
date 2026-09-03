package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyFinanceBorrowingRuleTest {
    @Test
    fun `uses exact legacy borrowing ceilings by raw division code`() {
        assertEquals(1_000_000, LegacyFinanceBorrowingRule.ceilingFor(0))
        assertEquals(5_000_000, LegacyFinanceBorrowingRule.ceilingFor(1))
        assertEquals(3_000_000, LegacyFinanceBorrowingRule.ceilingFor(2))
        assertEquals(2_000_000, LegacyFinanceBorrowingRule.ceilingFor(3))
        assertEquals(1_500_000, LegacyFinanceBorrowingRule.ceilingFor(4))
        assertEquals(1_000_000, LegacyFinanceBorrowingRule.ceilingFor(-1))
        assertEquals(1_000_000, LegacyFinanceBorrowingRule.ceilingFor(5))
    }

    @Test
    fun `borrows exactly five hundred thousand while below ceiling`() {
        val initial = LegacyFinanceBorrowingRule.initial(cash = 125_000L, borrowed = 1_000_000)

        val result = LegacyFinanceBorrowingRule.borrow(initial, rawDivisionCode = 4)

        assertTrue(result.accepted)
        assertEquals(625_000L, result.state.cash)
        assertEquals(1_500_000, result.state.borrowed)
        assertEquals(45_000, result.state.monthlyBorrowingCharge)
        assertEquals(-1, result.rawCashCategoryCode)
    }

    @Test
    fun `borrowing is rejected exactly at or above the legacy ceiling`() {
        val atCeiling = LegacyFinanceBorrowingRule.initial(cash = 10L, borrowed = 1_500_000)
        val aboveCeiling = LegacyFinanceBorrowingRule.initial(cash = 10L, borrowed = 2_000_000)

        val atResult = LegacyFinanceBorrowingRule.borrow(atCeiling, rawDivisionCode = 4)
        val aboveResult = LegacyFinanceBorrowingRule.borrow(aboveCeiling, rawDivisionCode = 4)

        assertFalse(atResult.accepted)
        assertEquals(atCeiling, atResult.state)
        assertNull(atResult.rawCashCategoryCode)
        assertFalse(aboveResult.accepted)
        assertEquals(aboveCeiling, aboveResult.state)
    }

    @Test
    fun `repayment requires positive debt and at least one full legacy step in cash`() {
        val insufficientCash = LegacyFinanceBorrowingRule.initial(cash = 499_999L, borrowed = 500_000)
        val noDebt = LegacyFinanceBorrowingRule.initial(cash = 5_000_000L, borrowed = 0)

        assertFalse(LegacyFinanceBorrowingRule.repay(insufficientCash).accepted)
        assertFalse(LegacyFinanceBorrowingRule.repay(noDebt).accepted)
    }

    @Test
    fun `repays exactly five hundred thousand and recalculates three percent charge`() {
        val initial = LegacyFinanceBorrowingRule.initial(cash = 900_000L, borrowed = 1_500_000)

        val result = LegacyFinanceBorrowingRule.repay(initial)

        assertTrue(result.accepted)
        assertEquals(400_000L, result.state.cash)
        assertEquals(1_000_000, result.state.borrowed)
        assertEquals(30_000, result.state.monthlyBorrowingCharge)
        assertEquals(-1, result.rawCashCategoryCode)
    }

    @Test
    fun `monthly charge preserves legacy integer arithmetic`() {
        assertEquals(0, LegacyFinanceBorrowingRule.monthlyCharge(0))
        assertEquals(0, LegacyFinanceBorrowingRule.monthlyCharge(-1))
        assertEquals(15_000, LegacyFinanceBorrowingRule.monthlyCharge(500_000))
        assertEquals(30_000, LegacyFinanceBorrowingRule.monthlyCharge(1_000_000))
        assertEquals(45_000, LegacyFinanceBorrowingRule.monthlyCharge(1_500_000))
    }
}
