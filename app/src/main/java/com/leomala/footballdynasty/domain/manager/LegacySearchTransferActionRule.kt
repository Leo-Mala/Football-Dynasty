package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of `ActivityProcura.u(int)` from the official Brasfoot
 * SMALI corpus.
 *
 * The legacy UI uses four action codes after a proposal dialog. This rule keeps
 * that dispatch deterministic and independent from Android widgets: code 1 is
 * a normal purchase using the player's value fallback, code 2 takes the player
 * on loan, code 3 purchases while supplying the legacy salary suggestion from
 * `best.f.j()`, and code 4 accepts the previously stored counter-offer.
 */
sealed interface LegacySearchTransferAction {
    data object PurchaseDefault : LegacySearchTransferAction
    data object Loan : LegacySearchTransferAction
    data class PurchaseWithSalary(val salaryCode: Int) : LegacySearchTransferAction
    data class PurchaseCounterOffer(val counterOfferValue: Int) : LegacySearchTransferAction
    data object None : LegacySearchTransferAction
}

object LegacySearchTransferActionRule {
    fun resolve(
        legacyActionCode: Int,
        suggestedSalaryCode: Int,
        storedCounterOfferValue: Int,
    ): LegacySearchTransferAction = when (legacyActionCode) {
        1 -> LegacySearchTransferAction.PurchaseDefault
        2 -> LegacySearchTransferAction.Loan
        3 -> LegacySearchTransferAction.PurchaseWithSalary(suggestedSalaryCode)
        4 -> LegacySearchTransferAction.PurchaseCounterOffer(storedCounterOfferValue)
        else -> LegacySearchTransferAction.None
    }

    fun purchaseExecution(
        action: LegacySearchTransferAction,
        playerValue: Int,
    ): LegacySearchPurchaseExecution? = when (action) {
        LegacySearchTransferAction.PurchaseDefault ->
            LegacyTransferExecutionRule.purchaseExecution(
                offeredTransferValue = -1,
                playerValue = playerValue,
                acceptedSalary = -1,
            )

        is LegacySearchTransferAction.PurchaseWithSalary ->
            LegacyTransferExecutionRule.purchaseExecution(
                offeredTransferValue = -1,
                playerValue = playerValue,
                acceptedSalary = action.salaryCode,
            )

        is LegacySearchTransferAction.PurchaseCounterOffer ->
            LegacyTransferExecutionRule.purchaseExecution(
                offeredTransferValue = action.counterOfferValue,
                playerValue = playerValue,
                acceptedSalary = -1,
            )

        LegacySearchTransferAction.Loan,
        LegacySearchTransferAction.None,
        -> null
    }
}
