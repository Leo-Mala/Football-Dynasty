package com.leomala.footballdynasty.domain.manager

/**
 * Executable composition of the finance behavior already reconstructed from
 * legacy `ActivityFinancas`, `best.m`, `best.a.r()` and the `c0.B/D` cash
 * mutation boundary.
 *
 * This type deliberately introduces no new finance policy. It only connects
 * the independently characterized rules so that cash, outstanding borrowing
 * and the period ledger mutate together exactly as the proven legacy calls do.
 */
data class LegacyFinanceRuntimeState(
    val cash: Long,
    val ledger: LegacyFinanceLedgerState,
)

data class LegacyFinanceRuntimeResult(
    val state: LegacyFinanceRuntimeState,
    val accepted: Boolean,
    val rawCashCategoryCode: Int? = null,
)

object LegacyFinanceRuntimeRule {
    /**
     * Composes `best.m.y(c0)` with `c0.B(500000, -1)`.
     *
     * The cash category `-1` is not handled by `best.m.a(int,int)`, so borrowing
     * changes cash/debt but does not create a period income bucket entry.
     */
    fun borrow(
        state: LegacyFinanceRuntimeState,
        rawDivisionCode: Int,
    ): LegacyFinanceRuntimeResult {
        val borrowing = LegacyFinanceBorrowingRule.initial(
            cash = state.cash,
            borrowed = state.ledger.borrowed,
        )
        val result = LegacyFinanceBorrowingRule.borrow(borrowing, rawDivisionCode)
        if (!result.accepted) {
            return LegacyFinanceRuntimeResult(
                state = state,
                accepted = false,
            )
        }

        return LegacyFinanceRuntimeResult(
            state = state.copy(
                cash = result.state.cash,
                ledger = state.ledger.copy(
                    borrowed = result.state.borrowed,
                    monthlyBorrowingCharge = result.state.monthlyBorrowingCharge,
                ),
            ),
            accepted = true,
            rawCashCategoryCode = result.rawCashCategoryCode,
        )
    }

    /**
     * Composes `best.m.x(c0)` with `c0.D(500000, -1)`.
     *
     * Unlike the income dispatcher, legacy `best.m.d(int,int)` routes an unknown
     * expense code to its miscellaneous bucket. Therefore a successful debt
     * repayment is also accumulated as miscellaneous period expense.
     */
    fun repay(state: LegacyFinanceRuntimeState): LegacyFinanceRuntimeResult {
        val borrowing = LegacyFinanceBorrowingRule.initial(
            cash = state.cash,
            borrowed = state.ledger.borrowed,
        )
        val result = LegacyFinanceBorrowingRule.repay(borrowing)
        if (!result.accepted) {
            return LegacyFinanceRuntimeResult(
                state = state,
                accepted = false,
            )
        }

        val category = requireNotNull(result.rawCashCategoryCode)
        val ledgerAfterExpense = LegacyFinanceLedgerRule.addExpense(
            state = state.ledger,
            amount = LegacyFinanceBorrowingRule.STEP,
            rawCategoryCode = category,
        ).copy(
            borrowed = result.state.borrowed,
            monthlyBorrowingCharge = result.state.monthlyBorrowingCharge,
        )

        return LegacyFinanceRuntimeResult(
            state = LegacyFinanceRuntimeState(
                cash = result.state.cash,
                ledger = ledgerAfterExpense,
            ),
            accepted = true,
            rawCashCategoryCode = category,
        )
    }

    /**
     * Applies the single-club effect of the legacy calendar event `"dJ"`.
     * A strictly positive precomputed charge is debited with category `4`, even
     * when doing so takes cash below zero, and is accumulated in the matching
     * borrowing-charge ledger bucket.
     */
    fun applyMonthlyBorrowingCharge(state: LegacyFinanceRuntimeState): LegacyFinanceRuntimeState {
        val charge = state.ledger.monthlyBorrowingCharge
        if (charge <= 0) return state

        return LegacyFinanceRuntimeState(
            cash = state.cash - charge.toLong(),
            ledger = LegacyFinanceLedgerRule.addExpense(
                state = state.ledger,
                amount = charge,
                rawCategoryCode = LegacyMonthlyBorrowingChargeRule.RAW_EXPENSE_CATEGORY_CODE,
            ),
        )
    }

    /** Delegates the legacy period rollover while preserving debt fields. */
    fun resetPeriod(state: LegacyFinanceRuntimeState): LegacyFinanceRuntimeState =
        state.copy(ledger = LegacyFinanceLedgerRule.resetPeriod(state.ledger))
}
