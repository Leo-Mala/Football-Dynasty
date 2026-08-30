package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyFriendlySchedulingRuleTest {
    @Test
    fun `already scheduled short circuits all other branches and clears requested payment`() {
        val result = LegacyFriendlySchedulingRule.evaluate(
            requesterLevelCode = 0,
            opponentLevelCode = 5,
            sameRawGroupCode = false,
            rawSideCode = 1,
            opponentAlreadyScheduledOnDate = true,
            opponentControlled = true,
        )

        assertEquals(LegacyFriendlySchedulingRule.ALREADY_SCHEDULED, result.rawDecisionCode)
        assertEquals(0, result.requestedPayment)
        assertTrue(result.alreadyScheduled)
    }

    @Test
    fun `controlled opponent schedules directly before level and side restrictions`() {
        val result = LegacyFriendlySchedulingRule.evaluate(
            requesterLevelCode = 0,
            opponentLevelCode = 5,
            sameRawGroupCode = false,
            rawSideCode = 1,
            opponentAlreadyScheduledOnDate = false,
            opponentControlled = true,
        )

        assertTrue(result.scheduleWithoutPayment)
        assertEquals(0, result.requestedPayment)
    }

    @Test
    fun `side zero preserves shadowed level five refusal by requesting 300000 first`() {
        val result = LegacyFriendlySchedulingRule.evaluate(
            requesterLevelCode = 2,
            opponentLevelCode = 5,
            sameRawGroupCode = true,
            rawSideCode = 0,
            opponentAlreadyScheduledOnDate = false,
            opponentControlled = false,
        )

        assertTrue(result.requiresPaymentConfirmation)
        assertEquals(300_000, result.requestedPayment)
    }

    @Test
    fun `side zero preserves shadowed level four refusal by requesting 200000 first`() {
        val result = LegacyFriendlySchedulingRule.evaluate(
            requesterLevelCode = 1,
            opponentLevelCode = 4,
            sameRawGroupCode = true,
            rawSideCode = 0,
            opponentAlreadyScheduledOnDate = false,
            opponentControlled = false,
        )

        assertTrue(result.requiresPaymentConfirmation)
        assertEquals(200_000, result.requestedPayment)
    }

    @Test
    fun `side zero uses first payment table when raw group codes match`() {
        val result = LegacyFriendlySchedulingRule.evaluate(
            requesterLevelCode = 5,
            opponentLevelCode = 3,
            sameRawGroupCode = true,
            rawSideCode = 0,
            opponentAlreadyScheduledOnDate = false,
            opponentControlled = false,
        )

        assertEquals(LegacyFriendlySchedulingRule.REQUIRES_PAYMENT, result.rawDecisionCode)
        assertEquals(50_000, result.requestedPayment)
    }

    @Test
    fun `side zero uses second payment table when raw group codes differ`() {
        val result = LegacyFriendlySchedulingRule.evaluate(
            requesterLevelCode = 5,
            opponentLevelCode = 3,
            sameRawGroupCode = false,
            rawSideCode = 0,
            opponentAlreadyScheduledOnDate = false,
            opponentControlled = false,
        )

        assertEquals(80_000, result.requestedPayment)
    }

    @Test
    fun `nonzero side rejects level five opponent unless requester is also level five`() {
        val result = LegacyFriendlySchedulingRule.evaluate(
            requesterLevelCode = 4,
            opponentLevelCode = 5,
            sameRawGroupCode = true,
            rawSideCode = 1,
            opponentAlreadyScheduledOnDate = false,
            opponentControlled = false,
        )

        assertTrue(result.refused)
        assertEquals(0, result.requestedPayment)
    }

    @Test
    fun `nonzero side uses second table for same group and third table for different group`() {
        val sameGroup = LegacyFriendlySchedulingRule.evaluate(
            requesterLevelCode = 5,
            opponentLevelCode = 3,
            sameRawGroupCode = true,
            rawSideCode = 7,
            opponentAlreadyScheduledOnDate = false,
            opponentControlled = false,
        )
        val otherGroup = LegacyFriendlySchedulingRule.evaluate(
            requesterLevelCode = 5,
            opponentLevelCode = 3,
            sameRawGroupCode = false,
            rawSideCode = -1,
            opponentAlreadyScheduledOnDate = false,
            opponentControlled = false,
        )

        assertEquals(80_000, sameGroup.requestedPayment)
        assertEquals(100_000, otherGroup.requestedPayment)
    }

    @Test
    fun `zero table value returns direct scheduling code`() {
        val result = LegacyFriendlySchedulingRule.evaluate(
            requesterLevelCode = 5,
            opponentLevelCode = 1,
            sameRawGroupCode = false,
            rawSideCode = 0,
            opponentAlreadyScheduledOnDate = false,
            opponentControlled = false,
        )

        assertTrue(result.scheduleWithoutPayment)
        assertEquals(0, result.requestedPayment)
    }

    @Test
    fun `paid confirmation deducts exact amount with raw category minus one even below zero`() {
        val result = LegacyFriendlySchedulingResult(
            rawDecisionCode = LegacyFriendlySchedulingRule.REQUIRES_PAYMENT,
            requestedPayment = 250_000,
        )

        val payment = LegacyFriendlySchedulingRule.acceptRequestedPayment(100_000L, result)
        requireNotNull(payment)

        assertEquals(100_000L, payment.cashBefore)
        assertEquals(-150_000L, payment.cashAfter)
        assertEquals(250_000, payment.amount)
        assertEquals(-1, payment.rawCashCategoryCode)
    }

    @Test
    fun `non payment decisions never mutate cash`() {
        val direct = LegacyFriendlySchedulingResult(
            rawDecisionCode = LegacyFriendlySchedulingRule.SCHEDULE_DIRECTLY,
            requestedPayment = 0,
        )
        val refused = LegacyFriendlySchedulingResult(
            rawDecisionCode = LegacyFriendlySchedulingRule.REFUSED,
            requestedPayment = 0,
        )

        assertNull(LegacyFriendlySchedulingRule.acceptRequestedPayment(10L, direct))
        assertNull(LegacyFriendlySchedulingRule.acceptRequestedPayment(10L, refused))
        assertFalse(direct.requiresPaymentConfirmation)
    }
}
