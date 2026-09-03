package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of the borrowing lifecycle exposed by `ActivityFinancas`
 * and implemented in legacy `best.m.x()/y()/b()`.
 *
 * Evidence source: official Brasfoot 2026 Java + SMALI corpus. Values here are
 * gameplay evidence, not modernized policy: borrowing and repayment happen in
 * fixed 500_000 steps; the debt ceiling depends on `c0.O()`; and the displayed
 * monthly borrowing charge is exactly 3% of the outstanding debt after each
 * mutation.
 */
data class LegacyFinanceBorrowingState(
    val cash: Long,
    val borrowed: Int,
    val monthlyBorrowingCharge: Int,
)

data class LegacyFinanceBorrowingResult(
    val state: LegacyFinanceBorrowingState,
    val accepted: Boolean,
    /** Mirrors the legacy cash mutation category passed to `c0.B/D`: -1. */
    val rawCashCategoryCode: Int? = null,
)

object LegacyFinanceBorrowingRule {
    const val STEP: Int = 500_000
    const val RAW_CASH_CATEGORY_CODE: Int = -1

    /**
     * Exact ceiling table from `best.m.y(c0)`:
     * index 0 fallback, then divisions/codes 1..4.
     */
    private val borrowingCeilings = listOf(
        1_000_000,
        5_000_000,
        3_000_000,
        2_000_000,
        1_500_000,
    )

    fun ceilingFor(rawDivisionCode: Int): Int =
        if (rawDivisionCode in 1..4) borrowingCeilings[rawDivisionCode]
        else borrowingCeilings[0]

    fun initial(cash: Long, borrowed: Int): LegacyFinanceBorrowingState =
        LegacyFinanceBorrowingState(
            cash = cash,
            borrowed = borrowed,
            monthlyBorrowingCharge = monthlyCharge(borrowed),
        )

    /** Reconstructs `best.m.y(c0)`. */
    fun borrow(
        state: LegacyFinanceBorrowingState,
        rawDivisionCode: Int,
    ): LegacyFinanceBorrowingResult {
        if (state.borrowed >= ceilingFor(rawDivisionCode)) {
            return LegacyFinanceBorrowingResult(state = state, accepted = false)
        }

        val nextBorrowed = state.borrowed + STEP
        return LegacyFinanceBorrowingResult(
            state = LegacyFinanceBorrowingState(
                cash = state.cash + STEP,
                borrowed = nextBorrowed,
                monthlyBorrowingCharge = monthlyCharge(nextBorrowed),
            ),
            accepted = true,
            rawCashCategoryCode = RAW_CASH_CATEGORY_CODE,
        )
    }

    /** Reconstructs `best.m.x(c0)`. */
    fun repay(state: LegacyFinanceBorrowingState): LegacyFinanceBorrowingResult {
        if (state.borrowed <= 0 || state.cash < STEP) {
            return LegacyFinanceBorrowingResult(state = state, accepted = false)
        }

        val nextBorrowed = state.borrowed - STEP
        return LegacyFinanceBorrowingResult(
            state = LegacyFinanceBorrowingState(
                cash = state.cash - STEP,
                borrowed = nextBorrowed,
                monthlyBorrowingCharge = monthlyCharge(nextBorrowed),
            ),
            accepted = true,
            rawCashCategoryCode = RAW_CASH_CATEGORY_CODE,
        )
    }

    /** Exact integer arithmetic from legacy `best.m.b()`: `(borrowed * 3) / 100`. */
    fun monthlyCharge(borrowed: Int): Int =
        if (borrowed > 0) (borrowed * 3) / 100 else 0
}
