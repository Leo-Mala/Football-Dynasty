package com.leomala.footballdynasty.domain.manager

/**
 * Exact reconstruction of the legacy salary-calendar path.
 *
 * Official Java + SMALI evidence:
 * - `best.b.y1()` schedules event code 2 (`"ds"`) on every day-of-month 2 when raw flag `i0`
 *   is true, otherwise on every Sunday (`Calendar.DAY_OF_WEEK == 1`);
 * - `best.a.J(int)` dispatches `"ds"` to `best.a.s()`;
 * - `best.a.s()` charges only clubs for which `c0.Y0(currentMonth)` is true;
 * - `c0.q()` sums `best.o.m0()` for the senior roster plus `best.p.u()` for the youth roster;
 * - `c0.z()` passes that long total to `c0.E(long)`;
 * - `c0.E(long)` subtracts cash without a sufficiency check and calls `best.m.e(long)`;
 * - `best.m.e(long)` adds the amount directly to the salary-expense long accumulator.
 *
 * No salary value is clamped or normalized here because the legacy path performs plain JVM integer
 * to long conversion followed by long addition/subtraction.
 */
object LegacySalaryPaymentRule {
    const val LEGACY_EVENT_CODE: String = "ds"
    const val LEGACY_EVENT_TYPE: Int = 2
    const val FIXED_DAY_OF_MONTH: Int = 2
    const val SUNDAY_DAY_OF_WEEK: Int = 1

    /** Mirrors the date-set choice in `best.b.y1()`. */
    fun shouldSchedule(
        useDayOfMonthTwoSchedule: Boolean,
        dayOfMonth: Int,
        dayOfWeek: Int,
    ): Boolean = if (useDayOfMonthTwoSchedule) {
        dayOfMonth == FIXED_DAY_OF_MONTH
    } else {
        dayOfWeek == SUNDAY_DAY_OF_WEEK
    }

    /**
     * Pure representation of `c0.Y0(month)`: at least one competition calendar entry involving the
     * club exists in the current zero-based `Calendar.MONTH` value.
     */
    fun eligibleForCalendarMonth(
        currentMonthCode: Int,
        participatingCalendarMonthCodes: Iterable<Int>,
    ): Boolean = participatingCalendarMonthCodes.any { it == currentMonthCode }

    /** Mirrors `c0.q()` exactly: senior `o.m0()` values followed by youth `p.u()` values. */
    fun totalSalary(
        seniorSalaryCodes: Iterable<Int>,
        youthSalaryCodes: Iterable<Int>,
    ): Long {
        var total = 0L
        seniorSalaryCodes.forEach { total += it.toLong() }
        youthSalaryCodes.forEach { total += it.toLong() }
        return total
    }

    /** Mirrors the single-club `s() -> z() -> E(q()) -> m.e(q())` mutation. */
    fun apply(
        state: LegacyFinanceRuntimeState,
        seniorSalaryCodes: Iterable<Int>,
        youthSalaryCodes: Iterable<Int>,
        eligibleForCurrentCalendarMonth: Boolean,
    ): LegacyFinanceRuntimeState {
        if (!eligibleForCurrentCalendarMonth) return state

        val total = totalSalary(seniorSalaryCodes, youthSalaryCodes)
        return state.copy(
            cash = state.cash - total,
            ledger = LegacyFinanceLedgerRule.addSalaryExpense(
                state = state.ledger,
                amount = total,
            ),
        )
    }
}
