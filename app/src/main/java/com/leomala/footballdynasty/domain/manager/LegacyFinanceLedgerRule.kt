package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of the finance ledger stored by legacy `best.m`.
 *
 * Evidence source: official Brasfoot 2026 Java + SMALI corpus. The category
 * routing below mirrors `best.m.a(int,int)` (income) and `best.m.d(int,int)`
 * (expense). Categories that are not explicitly handled by the legacy income
 * method are ignored; unknown expense categories fall into the miscellaneous
 * bucket exactly as the original code does.
 */
data class LegacyFinanceLedgerState(
    val ticketIncome: Int = 0,
    val playerSaleIncome: Long = 0L,
    val prizeIncome: Int = 0,
    val sponsorIncome: Int = 0,
    val playerPurchaseExpense: Long = 0L,
    val stadiumExpense: Int = 0,
    val salaryExpense: Long = 0L,
    val borrowingChargeExpense: Int = 0,
    val fineExpense: Int = 0,
    val miscellaneousExpense: Int = 0,
    /** `best.m.f4372m`: deliberately retained across ledger reset. */
    val borrowed: Int = 0,
    /** `best.m.f4373n`: deliberately retained across ledger reset. */
    val monthlyBorrowingCharge: Int = 0,
) {
    fun totalIncome(): Long =
        ticketIncome.toLong() + playerSaleIncome + prizeIncome.toLong() + sponsorIncome.toLong()

    fun totalExpense(): Long =
        stadiumExpense.toLong() +
            playerPurchaseExpense +
            salaryExpense +
            borrowingChargeExpense.toLong() +
            miscellaneousExpense.toLong() +
            fineExpense.toLong()

    fun balance(): Long = totalIncome() - totalExpense()
}

object LegacyFinanceLedgerRule {
    // Income categories from best.m.a(int,int).
    const val INCOME_PLAYER_SALE: Int = 1
    const val INCOME_PRIZE: Int = 3
    const val INCOME_TICKET: Int = 5
    const val INCOME_SPONSOR: Int = 6

    // Expense categories from best.m.d(int,int).
    const val EXPENSE_PLAYER_PURCHASE: Int = 1
    const val EXPENSE_SALARY: Int = 2
    const val EXPENSE_BORROWING_CHARGE: Int = 4
    const val EXPENSE_STADIUM: Int = 7
    const val EXPENSE_FINE: Int = 8

    fun addIncome(
        state: LegacyFinanceLedgerState,
        amount: Int,
        rawCategoryCode: Int,
    ): LegacyFinanceLedgerState = when (rawCategoryCode) {
        INCOME_PLAYER_SALE -> state.copy(playerSaleIncome = state.playerSaleIncome + amount.toLong())
        INCOME_PRIZE -> state.copy(prizeIncome = state.prizeIncome + amount)
        INCOME_TICKET -> state.copy(ticketIncome = state.ticketIncome + amount)
        INCOME_SPONSOR -> state.copy(sponsorIncome = state.sponsorIncome + amount)
        else -> state
    }

    fun addExpense(
        state: LegacyFinanceLedgerState,
        amount: Int,
        rawCategoryCode: Int,
    ): LegacyFinanceLedgerState = when (rawCategoryCode) {
        EXPENSE_PLAYER_PURCHASE -> state.copy(playerPurchaseExpense = state.playerPurchaseExpense + amount.toLong())
        EXPENSE_BORROWING_CHARGE -> state.copy(borrowingChargeExpense = state.borrowingChargeExpense + amount)
        EXPENSE_SALARY -> state.copy(salaryExpense = state.salaryExpense + amount.toLong())
        EXPENSE_STADIUM -> state.copy(stadiumExpense = state.stadiumExpense + amount)
        EXPENSE_FINE -> state.copy(fineExpense = state.fineExpense + amount)
        else -> state.copy(miscellaneousExpense = state.miscellaneousExpense + amount)
    }

    /**
     * Exact separate long-valued salary path from legacy `best.m.e(long)`.
     *
     * The automatic salary calendar does not route through `d(int, 2)`: `c0.E(long)` calls this
     * long accumulator directly, which is why totals above Int.MAX_VALUE must remain lossless.
     */
    fun addSalaryExpense(
        state: LegacyFinanceLedgerState,
        amount: Long,
    ): LegacyFinanceLedgerState = state.copy(salaryExpense = state.salaryExpense + amount)

    /**
     * Reconstructs `best.m.z()`: all period income/expense accumulators are
     * cleared, while borrowed principal and its precomputed monthly charge are
     * intentionally not touched.
     */
    fun resetPeriod(state: LegacyFinanceLedgerState): LegacyFinanceLedgerState =
        LegacyFinanceLedgerState(
            borrowed = state.borrowed,
            monthlyBorrowingCharge = state.monthlyBorrowingCharge,
        )
}
