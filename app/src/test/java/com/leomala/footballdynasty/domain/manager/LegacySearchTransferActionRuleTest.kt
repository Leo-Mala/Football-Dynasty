package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacySearchTransferActionRuleTest {
    @Test
    fun `maps exact ActivityProcura u action codes`() {
        assertEquals(
            LegacySearchTransferAction.PurchaseDefault,
            LegacySearchTransferActionRule.resolve(1, 700, 9000),
        )
        assertEquals(
            LegacySearchTransferAction.Loan,
            LegacySearchTransferActionRule.resolve(2, 700, 9000),
        )
        assertEquals(
            LegacySearchTransferAction.PurchaseWithSalary(700),
            LegacySearchTransferActionRule.resolve(3, 700, 9000),
        )
        assertEquals(
            LegacySearchTransferAction.PurchaseCounterOffer(9000),
            LegacySearchTransferActionRule.resolve(4, 700, 9000),
        )
        assertEquals(
            LegacySearchTransferAction.None,
            LegacySearchTransferActionRule.resolve(0, 700, 9000),
        )
        assertEquals(
            LegacySearchTransferAction.None,
            LegacySearchTransferActionRule.resolve(5, 700, 9000),
        )
    }

    @Test
    fun `default purchase uses player value fallback exactly like x with minus one inputs`() {
        val execution = LegacySearchTransferActionRule.purchaseExecution(
            action = LegacySearchTransferAction.PurchaseDefault,
            playerValue = 12_345,
        )
        requireNotNull(execution)

        assertEquals(12_345, execution.transferValue)
        assertNull(execution.salaryAfterPurchase)
    }

    @Test
    fun `salary action keeps player value fallback and applies positive suggested salary`() {
        val execution = LegacySearchTransferActionRule.purchaseExecution(
            action = LegacySearchTransferAction.PurchaseWithSalary(777),
            playerValue = 12_345,
        )
        requireNotNull(execution)

        assertEquals(12_345, execution.transferValue)
        assertEquals(777, execution.salaryAfterPurchase)
    }

    @Test
    fun `counter proposal action uses stored counter offer and no salary mutation`() {
        val execution = LegacySearchTransferActionRule.purchaseExecution(
            action = LegacySearchTransferAction.PurchaseCounterOffer(22_000),
            playerValue = 12_345,
        )
        requireNotNull(execution)

        assertEquals(22_000, execution.transferValue)
        assertNull(execution.salaryAfterPurchase)
    }

    @Test
    fun `loan and unknown action do not create purchase execution`() {
        assertNull(
            LegacySearchTransferActionRule.purchaseExecution(
                action = LegacySearchTransferAction.Loan,
                playerValue = 12_345,
            ),
        )
        assertNull(
            LegacySearchTransferActionRule.purchaseExecution(
                action = LegacySearchTransferAction.None,
                playerValue = 12_345,
            ),
        )
    }
}
