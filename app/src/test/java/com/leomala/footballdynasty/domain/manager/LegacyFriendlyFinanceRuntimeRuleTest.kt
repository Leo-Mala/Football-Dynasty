package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyFriendlyFinanceRuntimeRuleTest {
    @Test
    fun `accepted paid friendly debits cash and records miscellaneous expense`() {
        val state = LegacyFinanceRuntimeState(
            cash = 600_000L,
            ledger = LegacyFinanceLedgerState(
                miscellaneousExpense = 12_000,
                borrowed = 500_000,
                monthlyBorrowingCharge = 15_000,
            ),
        )
        val decision = LegacyFriendlySchedulingResult(
            rawDecisionCode = LegacyFriendlySchedulingRule.REQUIRES_PAYMENT,
            requestedPayment = 300_000,
        )

        val result = LegacyFriendlyFinanceRuntimeRule.acceptRequestedPayment(state, decision)

        requireNotNull(result)
        assertEquals(300_000L, result.cash)
        assertEquals(312_000, result.ledger.miscellaneousExpense)
        assertEquals(500_000, result.ledger.borrowed)
        assertEquals(15_000, result.ledger.monthlyBorrowingCharge)
    }

    @Test
    fun `paid friendly preserves legacy no-affordability-check behavior`() {
        val state = LegacyFinanceRuntimeState(
            cash = 100_000L,
            ledger = LegacyFinanceLedgerState(miscellaneousExpense = 1_000),
        )
        val decision = LegacyFriendlySchedulingResult(
            rawDecisionCode = LegacyFriendlySchedulingRule.REQUIRES_PAYMENT,
            requestedPayment = 200_000,
        )

        val result = LegacyFriendlyFinanceRuntimeRule.acceptRequestedPayment(state, decision)

        requireNotNull(result)
        assertEquals(-100_000L, result.cash)
        assertEquals(201_000, result.ledger.miscellaneousExpense)
    }

    @Test
    fun `non-payment decisions do not execute the paid confirmation path`() {
        val state = LegacyFinanceRuntimeState(
            cash = 500_000L,
            ledger = LegacyFinanceLedgerState(miscellaneousExpense = 7_000),
        )

        for (code in listOf(
            LegacyFriendlySchedulingRule.REFUSED,
            LegacyFriendlySchedulingRule.SCHEDULE_DIRECTLY,
            LegacyFriendlySchedulingRule.ALREADY_SCHEDULED,
        )) {
            val decision = LegacyFriendlySchedulingResult(
                rawDecisionCode = code,
                requestedPayment = 0,
            )
            assertNull(LegacyFriendlyFinanceRuntimeRule.acceptRequestedPayment(state, decision))
        }
    }
}
