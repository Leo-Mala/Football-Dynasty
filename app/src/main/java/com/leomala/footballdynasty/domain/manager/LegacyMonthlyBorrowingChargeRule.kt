package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of the scheduled borrowing-charge pass in legacy
 * `best.a.r()`.
 *
 * The calendar dispatcher `best.a.J(int)` invokes this pass for event code
 * `"dJ"`. The legacy loop visits every club in `best.b.g1()` in source order,
 * skips clubs without a finance object, and deducts `best.m.o()` only when that
 * value is strictly positive. The debit uses raw finance category `4`.
 *
 * No affordability guard exists in the legacy path: a positive charge is
 * deducted even when it makes club cash negative.
 */
data class LegacyMonthlyBorrowingClubState(
    val clubCode: Int,
    val cash: Long,
    /** Null mirrors legacy `c0.T() == null`. */
    val monthlyBorrowingCharge: Int?,
)

data class LegacyMonthlyBorrowingChargeMutation(
    val clubCode: Int,
    val amount: Int,
    /** Exact category passed by `best.a.r()` to `c0.D(int,int)`. */
    val rawExpenseCategoryCode: Int,
)

data class LegacyMonthlyBorrowingChargeResult(
    val clubs: List<LegacyMonthlyBorrowingClubState>,
    val mutations: List<LegacyMonthlyBorrowingChargeMutation>,
)

object LegacyMonthlyBorrowingChargeRule {
    const val RAW_EXPENSE_CATEGORY_CODE: Int = 4

    /** Reconstructs the full ordered loop in legacy `best.a.r()`. */
    fun apply(clubs: List<LegacyMonthlyBorrowingClubState>): LegacyMonthlyBorrowingChargeResult {
        val mutations = ArrayList<LegacyMonthlyBorrowingChargeMutation>()
        val updated = clubs.map { club ->
            val charge = club.monthlyBorrowingCharge
            if (charge == null || charge <= 0) {
                club
            } else {
                mutations += LegacyMonthlyBorrowingChargeMutation(
                    clubCode = club.clubCode,
                    amount = charge,
                    rawExpenseCategoryCode = RAW_EXPENSE_CATEGORY_CODE,
                )
                club.copy(cash = club.cash - charge.toLong())
            }
        }
        return LegacyMonthlyBorrowingChargeResult(
            clubs = updated,
            mutations = mutations,
        )
    }
}
