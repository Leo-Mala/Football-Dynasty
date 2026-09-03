package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMonthlyBorrowingChargeRuleTest {
    @Test
    fun `skips clubs without finance state and non-positive charges`() {
        val clubs = listOf(
            LegacyMonthlyBorrowingClubState(clubCode = 10, cash = 1_000L, monthlyBorrowingCharge = null),
            LegacyMonthlyBorrowingClubState(clubCode = 11, cash = 2_000L, monthlyBorrowingCharge = 0),
            LegacyMonthlyBorrowingClubState(clubCode = 12, cash = 3_000L, monthlyBorrowingCharge = -1),
        )

        val result = LegacyMonthlyBorrowingChargeRule.apply(clubs)

        assertEquals(clubs, result.clubs)
        assertTrue(result.mutations.isEmpty())
    }

    @Test
    fun `deducts each positive monthly charge using exact raw category four`() {
        val result = LegacyMonthlyBorrowingChargeRule.apply(
            listOf(
                LegacyMonthlyBorrowingClubState(clubCode = 21, cash = 900_000L, monthlyBorrowingCharge = 15_000),
                LegacyMonthlyBorrowingClubState(clubCode = 22, cash = 1_250_000L, monthlyBorrowingCharge = 30_000),
            ),
        )

        assertEquals(885_000L, result.clubs[0].cash)
        assertEquals(1_220_000L, result.clubs[1].cash)
        assertEquals(
            listOf(
                LegacyMonthlyBorrowingChargeMutation(21, 15_000, 4),
                LegacyMonthlyBorrowingChargeMutation(22, 30_000, 4),
            ),
            result.mutations,
        )
    }

    @Test
    fun `preserves legacy behavior that borrowing charge can make cash negative`() {
        val result = LegacyMonthlyBorrowingChargeRule.apply(
            listOf(
                LegacyMonthlyBorrowingClubState(clubCode = 31, cash = 10_000L, monthlyBorrowingCharge = 15_000),
            ),
        )

        assertEquals(-5_000L, result.clubs.single().cash)
        assertEquals(15_000, result.mutations.single().amount)
        assertEquals(4, result.mutations.single().rawExpenseCategoryCode)
    }

    @Test
    fun `preserves source club order and mutation order`() {
        val clubs = listOf(
            LegacyMonthlyBorrowingClubState(clubCode = 41, cash = 100L, monthlyBorrowingCharge = 1),
            LegacyMonthlyBorrowingClubState(clubCode = 42, cash = 200L, monthlyBorrowingCharge = null),
            LegacyMonthlyBorrowingClubState(clubCode = 43, cash = 300L, monthlyBorrowingCharge = 2),
        )

        val result = LegacyMonthlyBorrowingChargeRule.apply(clubs)

        assertEquals(listOf(41, 42, 43), result.clubs.map { it.clubCode })
        assertEquals(listOf(41, 43), result.mutations.map { it.clubCode })
        assertEquals(listOf(99L, 200L, 298L), result.clubs.map { it.cash })
    }
}
