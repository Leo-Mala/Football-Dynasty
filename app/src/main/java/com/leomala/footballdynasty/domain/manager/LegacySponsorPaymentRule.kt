package com.leomala.footballdynasty.domain.manager

/**
 * Exact single-club reconstruction of the annual sponsor mutation reached from legacy new-year flow.
 *
 * Official Java + SMALI evidence:
 * - `best.b.d()` (new year) calls private `best.b.s()`;
 * - `best.b.s()` iterates `core.a.f13450b.g1()` and calls `best.c0.p()` for every club;
 * - `best.c0.p()` first adds `(long) (cash + q() * 3.2d)` only when country code is not `29`
 *   and raw `best.b.J1()` (`isJogaEstadual`) is true;
 * - regardless of that bonus branch, valid division codes `0..4` call
 *   `c0.B(best.j0.Y1[division][0], 6)`;
 * - `c0.B` always adds the fixed sponsor amount to cash and only forwards it to `best.m.a(...)`
 *   when the legacy club ledger flag `Q0()` is true;
 * - `best.m.a(..., 6)` accumulates the fixed amount in the sponsor bucket exposed by
 *   `ActivityFinancas.in_patroR <- best.m.q()`.
 *
 * The salary-derived 3.2 multiplier is intentionally NOT added to the sponsor ledger because the
 * legacy code mutates cash directly before calling `B(...)`. The JVM double-to-long truncation is
 * also preserved rather than replaced by integer or decimal arithmetic.
 */
object LegacySponsorPaymentRule {
    const val RAW_INCOME_CATEGORY_CODE: Int = LegacyFinanceLedgerRule.INCOME_SPONSOR
    const val COUNTRY_CODE_WITHOUT_STATE_CHAMPIONSHIP_BONUS: Int = 29
    const val STATE_CHAMPIONSHIP_PAYROLL_MULTIPLIER: Double = 3.2

    private val fixedSponsorByDivision = intArrayOf(
        3_500_000,
        6_500_000,
        5_000_000,
        3_000_000,
        2_500_000,
    )

    /** Mirrors the `j0.Y1[division][0]` lookup guarded by `division in 0..4`. */
    fun fixedSponsorForDivision(rawDivisionCode: Int): Int? =
        fixedSponsorByDivision.getOrNull(rawDivisionCode)

    /** Mirrors the shared `c0.q()` payroll sum used by this path. */
    fun totalPayroll(
        seniorSalaryCodes: Iterable<Int>,
        youthSalaryCodes: Iterable<Int>,
    ): Long = LegacySalaryPaymentRule.totalSalary(seniorSalaryCodes, youthSalaryCodes)

    /** Mirrors one invocation of `best.c0.p()`, including its uncategorized payroll-based bonus. */
    fun apply(
        state: LegacyFinanceRuntimeState,
        rawCountryCode: Int,
        rawDivisionCode: Int,
        playStateChampionship: Boolean,
        seniorSalaryCodes: Iterable<Int>,
        youthSalaryCodes: Iterable<Int>,
        recordFinanceLedger: Boolean,
    ): LegacyFinanceRuntimeState {
        var cash = state.cash
        if (rawCountryCode != COUNTRY_CODE_WITHOUT_STATE_CHAMPIONSHIP_BONUS && playStateChampionship) {
            val payroll = totalPayroll(seniorSalaryCodes, youthSalaryCodes)
            cash = (cash.toDouble() + payroll.toDouble() * STATE_CHAMPIONSHIP_PAYROLL_MULTIPLIER).toLong()
        }

        val fixedSponsor = fixedSponsorForDivision(rawDivisionCode) ?: return state.copy(cash = cash)
        val ledger = if (recordFinanceLedger) {
            LegacyFinanceLedgerRule.addIncome(
                state = state.ledger,
                amount = fixedSponsor,
                rawCategoryCode = RAW_INCOME_CATEGORY_CODE,
            )
        } else {
            state.ledger
        }

        return LegacyFinanceRuntimeState(
            cash = cash + fixedSponsor.toLong(),
            ledger = ledger,
        )
    }
}
