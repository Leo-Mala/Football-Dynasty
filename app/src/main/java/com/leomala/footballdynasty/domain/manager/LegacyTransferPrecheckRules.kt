package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of the transfer willingness predicate `best.f.w(best.o, best.c0)`
 * and the search-screen prechecks `ActivityProcura.r(...)` / `ActivityProcura.s(...)`.
 *
 * The obfuscated J/E/p0/O scalar meanings stay raw where the legacy corpus does not
 * justify a safer domain name. Branch order and result codes match the Java + SMALI.
 */
data class LegacyTransferWillingnessInput(
    val destinationJCode: Int,
    val playerECode: Int,
    val sellerP0Code: Int,
    val destinationP0Code: Int,
    val destinationOCode: Int,
)

object LegacyTransferWillingnessRule {
    fun acceptsDestination(input: LegacyTransferWillingnessInput): Boolean {
        if (input.destinationJCode == 0) {
            return true
        }
        if (input.playerECode == 0 && input.sellerP0Code >= 4) {
            return input.destinationP0Code >= 4 || input.destinationOCode == 1
        }
        if (input.sellerP0Code >= 3 && input.destinationP0Code >= 3) {
            return true
        }
        if (input.sellerP0Code == 3 && input.destinationP0Code == 2) {
            return true
        }
        return (input.sellerP0Code == 3 && input.destinationP0Code == 1) ||
            input.sellerP0Code <= 2
    }
}

enum class LegacyPurchasePrecheckDecision(val legacyCode: Int) {
    INVALID_OR_NOT_FOR_SALE(0),
    ELIGIBLE(1),
    ALREADY_AT_DESTINATION(2),
    DESTINATION_ROSTER_FULL(3),
    DOES_NOT_WANT_DESTINATION(4),
    INSUFFICIENT_FUNDS(5),
}

data class LegacyPurchasePrecheckInput(
    val playerPresent: Boolean,
    val destinationPresent: Boolean,
    val playerForSale: Boolean,
    val alreadyAtDestination: Boolean,
    val buyerFunds: Long,
    val playerValue: Int,
    val destinationRosterSize: Int,
    val destinationAcceptedByPlayer: Boolean,
)

object LegacyPurchasePrecheckRule {
    fun evaluate(input: LegacyPurchasePrecheckInput): LegacyPurchasePrecheckDecision {
        if (!input.playerPresent || !input.destinationPresent || !input.playerForSale) {
            return LegacyPurchasePrecheckDecision.INVALID_OR_NOT_FOR_SALE
        }
        if (input.alreadyAtDestination) {
            return LegacyPurchasePrecheckDecision.ALREADY_AT_DESTINATION
        }
        if (input.buyerFunds < input.playerValue.toLong()) {
            return LegacyPurchasePrecheckDecision.INSUFFICIENT_FUNDS
        }
        if (input.destinationRosterSize >= 30) {
            return LegacyPurchasePrecheckDecision.DESTINATION_ROSTER_FULL
        }
        if (!input.destinationAcceptedByPlayer) {
            return LegacyPurchasePrecheckDecision.DOES_NOT_WANT_DESTINATION
        }
        return LegacyPurchasePrecheckDecision.ELIGIBLE
    }
}

enum class LegacyLoanPrecheckDecision(val legacyCode: Int) {
    INVALID(0),
    ELIGIBLE(1),
    ALREADY_AT_DESTINATION(2),
    DESTINATION_LOAN_LIMIT_REACHED(3),
    DOES_NOT_WANT_DESTINATION(4),
    NOT_AVAILABLE_FOR_LOAN(5),
    DESTINATION_ROSTER_FULL(6),
}

data class LegacyLoanPrecheckInput(
    val playerPresent: Boolean,
    val destinationPresent: Boolean,
    val playerAvailableForLoan: Boolean,
    val alreadyAtDestination: Boolean,
    val destinationRosterSize: Int,
    val activeLoansToDestination: Int,
    val destinationAcceptedByPlayer: Boolean,
)

object LegacyLoanPrecheckRule {
    fun evaluate(input: LegacyLoanPrecheckInput): LegacyLoanPrecheckDecision {
        if (!input.playerPresent || !input.destinationPresent) {
            return LegacyLoanPrecheckDecision.INVALID
        }
        if (!input.playerAvailableForLoan) {
            return LegacyLoanPrecheckDecision.NOT_AVAILABLE_FOR_LOAN
        }
        if (input.alreadyAtDestination) {
            return LegacyLoanPrecheckDecision.ALREADY_AT_DESTINATION
        }
        if (input.destinationRosterSize >= 30) {
            return LegacyLoanPrecheckDecision.DESTINATION_ROSTER_FULL
        }
        if (input.activeLoansToDestination >= 4) {
            return LegacyLoanPrecheckDecision.DESTINATION_LOAN_LIMIT_REACHED
        }
        if (!input.destinationAcceptedByPlayer) {
            return LegacyLoanPrecheckDecision.DOES_NOT_WANT_DESTINATION
        }
        return LegacyLoanPrecheckDecision.ELIGIBLE
    }
}
