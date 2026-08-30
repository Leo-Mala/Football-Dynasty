package com.leomala.footballdynasty.domain.manager

/**
 * Executable composition of the already-characterized stadium construction and
 * finance rules.
 *
 * Legacy `ActivityEstadio.d()` first refuses the construction when the managed
 * club cannot cover the quoted cost. An accepted construction debits that exact
 * cost through raw finance category `7` and appends the construction record.
 * Calendar materialization remains outside this rule: callers must provide the
 * legacy-derived end timestamp because the original Activity seeds Calendar
 * from wall-clock state before replacing only its date fields.
 */
data class LegacyStadiumFinanceRuntimeResult(
    val state: LegacyFinanceRuntimeState,
    val accepted: Boolean,
    val rawCashCategoryCode: Int? = null,
    val recordToAppend: LegacyStadiumConstructionRecord? = null,
)

object LegacyStadiumFinanceRuntimeRule {
    fun startConstruction(
        state: LegacyFinanceRuntimeState,
        quote: LegacyStadiumExpansionQuote,
        stadiumCode: Int,
        endTimestampMillis: Long,
    ): LegacyStadiumFinanceRuntimeResult {
        if (!quote.accepted) {
            return LegacyStadiumFinanceRuntimeResult(
                state = state,
                accepted = false,
            )
        }

        val cost = requireNotNull(quote.totalCost)
        val plan = LegacyStadiumConstructionRule.startPlan(
            clubCash = state.cash,
            quoteCost = cost,
            stadiumCode = stadiumCode,
            endTimestampMillis = endTimestampMillis,
            additions = quote.additions,
        )
        if (!plan.accepted) {
            return LegacyStadiumFinanceRuntimeResult(
                state = state,
                accepted = false,
            )
        }

        val category = requireNotNull(plan.financialCategoryCode)
        val record = requireNotNull(plan.recordToAppend)
        return LegacyStadiumFinanceRuntimeResult(
            state = LegacyFinanceRuntimeState(
                cash = state.cash - plan.debitAmount.toLong(),
                ledger = LegacyFinanceLedgerRule.addExpense(
                    state = state.ledger,
                    amount = plan.debitAmount,
                    rawCategoryCode = category,
                ),
            ),
            accepted = true,
            rawCashCategoryCode = category,
            recordToAppend = record,
        )
    }
}
