package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyTransferPrecheckRulesTest {
    @Test
    fun `willingness accepts destination J zero immediately`() {
        assertTrue(
            LegacyTransferWillingnessRule.acceptsDestination(
                willingness(destinationJ = 0, playerE = 0, sellerP0 = 99, destinationP0 = 0, destinationO = 0),
            ),
        )
    }

    @Test
    fun `high seller P0 with player E zero requires destination P0 four or O one`() {
        assertTrue(
            LegacyTransferWillingnessRule.acceptsDestination(
                willingness(destinationJ = 1, playerE = 0, sellerP0 = 4, destinationP0 = 4, destinationO = 0),
            ),
        )
        assertTrue(
            LegacyTransferWillingnessRule.acceptsDestination(
                willingness(destinationJ = 1, playerE = 0, sellerP0 = 4, destinationP0 = 0, destinationO = 1),
            ),
        )
        assertFalse(
            LegacyTransferWillingnessRule.acceptsDestination(
                willingness(destinationJ = 1, playerE = 0, sellerP0 = 4, destinationP0 = 3, destinationO = 0),
            ),
        )
    }

    @Test
    fun `remaining willingness branches preserve exact legacy P0 comparisons`() {
        assertTrue(LegacyTransferWillingnessRule.acceptsDestination(willingness(1, 1, 3, 3, 0)))
        assertTrue(LegacyTransferWillingnessRule.acceptsDestination(willingness(1, 1, 3, 2, 0)))
        assertTrue(LegacyTransferWillingnessRule.acceptsDestination(willingness(1, 1, 3, 1, 0)))
        assertTrue(LegacyTransferWillingnessRule.acceptsDestination(willingness(1, 1, 2, 0, 0)))
        assertFalse(LegacyTransferWillingnessRule.acceptsDestination(willingness(1, 1, 4, 2, 0)))
    }

    @Test
    fun `purchase precheck preserves legacy branch precedence and thresholds`() {
        val eligible = purchase()

        assertEquals(
            LegacyPurchasePrecheckDecision.INVALID_OR_NOT_FOR_SALE,
            LegacyPurchasePrecheckRule.evaluate(eligible.copy(playerPresent = false, alreadyAtDestination = true)),
        )
        assertEquals(
            LegacyPurchasePrecheckDecision.INVALID_OR_NOT_FOR_SALE,
            LegacyPurchasePrecheckRule.evaluate(eligible.copy(destinationPresent = false)),
        )
        assertEquals(
            LegacyPurchasePrecheckDecision.INVALID_OR_NOT_FOR_SALE,
            LegacyPurchasePrecheckRule.evaluate(eligible.copy(playerForSale = false)),
        )
        assertEquals(
            LegacyPurchasePrecheckDecision.ALREADY_AT_DESTINATION,
            LegacyPurchasePrecheckRule.evaluate(eligible.copy(alreadyAtDestination = true, buyerFunds = 0)),
        )
        assertEquals(
            LegacyPurchasePrecheckDecision.INSUFFICIENT_FUNDS,
            LegacyPurchasePrecheckRule.evaluate(eligible.copy(buyerFunds = 999, destinationRosterSize = 30)),
        )
        assertEquals(
            LegacyPurchasePrecheckDecision.DESTINATION_ROSTER_FULL,
            LegacyPurchasePrecheckRule.evaluate(eligible.copy(destinationRosterSize = 30, destinationAcceptedByPlayer = false)),
        )
        assertEquals(
            LegacyPurchasePrecheckDecision.DOES_NOT_WANT_DESTINATION,
            LegacyPurchasePrecheckRule.evaluate(eligible.copy(destinationAcceptedByPlayer = false)),
        )
        assertEquals(LegacyPurchasePrecheckDecision.ELIGIBLE, LegacyPurchasePrecheckRule.evaluate(eligible))
    }

    @Test
    fun `purchase funds equal to player value are sufficient exactly as legacy`() {
        assertEquals(
            LegacyPurchasePrecheckDecision.ELIGIBLE,
            LegacyPurchasePrecheckRule.evaluate(purchase(buyerFunds = 1000, playerValue = 1000)),
        )
    }

    @Test
    fun `loan precheck preserves exact order roster threshold and four-loan gate`() {
        val eligible = loan()

        assertEquals(
            LegacyLoanPrecheckDecision.INVALID,
            LegacyLoanPrecheckRule.evaluate(eligible.copy(playerPresent = false, playerAvailableForLoan = false)),
        )
        assertEquals(
            LegacyLoanPrecheckDecision.NOT_AVAILABLE_FOR_LOAN,
            LegacyLoanPrecheckRule.evaluate(eligible.copy(playerAvailableForLoan = false, alreadyAtDestination = true)),
        )
        assertEquals(
            LegacyLoanPrecheckDecision.ALREADY_AT_DESTINATION,
            LegacyLoanPrecheckRule.evaluate(eligible.copy(alreadyAtDestination = true, destinationRosterSize = 30)),
        )
        assertEquals(
            LegacyLoanPrecheckDecision.DESTINATION_ROSTER_FULL,
            LegacyLoanPrecheckRule.evaluate(eligible.copy(destinationRosterSize = 30, activeLoansToDestination = 4)),
        )
        assertEquals(
            LegacyLoanPrecheckDecision.DESTINATION_LOAN_LIMIT_REACHED,
            LegacyLoanPrecheckRule.evaluate(eligible.copy(activeLoansToDestination = 4, destinationAcceptedByPlayer = false)),
        )
        assertEquals(
            LegacyLoanPrecheckDecision.DOES_NOT_WANT_DESTINATION,
            LegacyLoanPrecheckRule.evaluate(eligible.copy(destinationAcceptedByPlayer = false)),
        )
        assertEquals(LegacyLoanPrecheckDecision.ELIGIBLE, LegacyLoanPrecheckRule.evaluate(eligible))
    }

    @Test
    fun `three existing loans still pass while four are rejected`() {
        assertEquals(
            LegacyLoanPrecheckDecision.ELIGIBLE,
            LegacyLoanPrecheckRule.evaluate(loan(activeLoansToDestination = 3)),
        )
        assertEquals(
            LegacyLoanPrecheckDecision.DESTINATION_LOAN_LIMIT_REACHED,
            LegacyLoanPrecheckRule.evaluate(loan(activeLoansToDestination = 4)),
        )
    }

    private fun willingness(
        destinationJ: Int,
        playerE: Int,
        sellerP0: Int,
        destinationP0: Int,
        destinationO: Int,
    ) = LegacyTransferWillingnessInput(
        destinationJCode = destinationJ,
        playerECode = playerE,
        sellerP0Code = sellerP0,
        destinationP0Code = destinationP0,
        destinationOCode = destinationO,
    )

    private fun purchase(
        playerPresent: Boolean = true,
        destinationPresent: Boolean = true,
        playerForSale: Boolean = true,
        alreadyAtDestination: Boolean = false,
        buyerFunds: Long = 1000,
        playerValue: Int = 1000,
        destinationRosterSize: Int = 29,
        destinationAcceptedByPlayer: Boolean = true,
    ) = LegacyPurchasePrecheckInput(
        playerPresent = playerPresent,
        destinationPresent = destinationPresent,
        playerForSale = playerForSale,
        alreadyAtDestination = alreadyAtDestination,
        buyerFunds = buyerFunds,
        playerValue = playerValue,
        destinationRosterSize = destinationRosterSize,
        destinationAcceptedByPlayer = destinationAcceptedByPlayer,
    )

    private fun loan(
        playerPresent: Boolean = true,
        destinationPresent: Boolean = true,
        playerAvailableForLoan: Boolean = true,
        alreadyAtDestination: Boolean = false,
        destinationRosterSize: Int = 29,
        activeLoansToDestination: Int = 3,
        destinationAcceptedByPlayer: Boolean = true,
    ) = LegacyLoanPrecheckInput(
        playerPresent = playerPresent,
        destinationPresent = destinationPresent,
        playerAvailableForLoan = playerAvailableForLoan,
        alreadyAtDestination = alreadyAtDestination,
        destinationRosterSize = destinationRosterSize,
        activeLoansToDestination = activeLoansToDestination,
        destinationAcceptedByPlayer = destinationAcceptedByPlayer,
    )
}
