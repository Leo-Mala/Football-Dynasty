package com.leomala.footballdynasty.domain.manager

/**
 * Executable composition of the paid-friendly path proven in
 * `ActivityAmistosos2021.d()` with the already characterized `c0.D` / `best.m.d`
 * finance boundary.
 *
 * This adds no scheduling policy. It is only valid after legacy decision code 3
 * has been accepted by the user. The caller schedules the friendly and then
 * debits the exact requested amount with raw category `-1`; that unknown expense
 * category is routed by `best.m.d` to the miscellaneous period-expense bucket.
 */
object LegacyFriendlyFinanceRuntimeRule {
    fun acceptRequestedPayment(
        state: LegacyFinanceRuntimeState,
        result: LegacyFriendlySchedulingResult,
    ): LegacyFinanceRuntimeState? {
        val payment = LegacyFriendlySchedulingRule.acceptRequestedPayment(
            cash = state.cash,
            result = result,
        ) ?: return null

        return LegacyFinanceRuntimeState(
            cash = payment.cashAfter,
            ledger = LegacyFinanceLedgerRule.addExpense(
                state = state.ledger,
                amount = payment.amount,
                rawCategoryCode = payment.rawCashCategoryCode,
            ),
        )
    }
}
