package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacySearchTransferOfferRuleTest {
    @Test
    fun `one-player scarcity uses the exact legacy premium and Java rounding`() {
        val input = baseInput(
            position = 0,
            sellerCounts = intArrayOf(1, 4, 4, 5, 4, 4),
            value = 1001,
        )

        assertEquals(2503, LegacySearchTransferOfferRule.minimumAcceptedOffer(input))
    }

    @Test
    fun `other scarcity uses the position-specific legacy premium`() {
        val input = baseInput(
            position = 5,
            sellerCounts = intArrayOf(3, 4, 4, 5, 4, 0),
            value = 1000,
        )

        assertEquals(3000, LegacySearchTransferOfferRule.minimumAcceptedOffer(input))
    }

    @Test
    fun `adequate depth with strong young player uses established-player premium`() {
        val input = baseInput(
            position = 1,
            sellerCounts = intArrayOf(3, 4, 4, 5, 4, 4),
            strength = 30,
            age = 35,
            value = 1001,
        )

        assertEquals(1201, LegacySearchTransferOfferRule.minimumAcceptedOffer(input))
    }

    @Test
    fun `weak or old player uses integer discount ordering from smali`() {
        val weak = baseInput(
            position = 0,
            sellerCounts = intArrayOf(3, 4, 4, 5, 4, 4),
            strength = 29,
            age = 25,
            value = 1001,
        )
        val old = weak.copy(strength = 99, age = 36)

        assertEquals(851, LegacySearchTransferOfferRule.minimumAcceptedOffer(weak))
        assertEquals(851, LegacySearchTransferOfferRule.minimumAcceptedOffer(old))
    }

    @Test
    fun `accepted offer maps the exact destination acceptance codes`() {
        val input = baseInput(
            position = 1,
            sellerCounts = intArrayOf(3, 4, 4, 5, 4, 4),
            strength = 40,
            age = 25,
            value = 1000,
            offer = 1200,
            buyerFunds = 0,
        )

        assertEquals(
            LegacySearchTransferOfferDecision.PLAYER_BOUGHT,
            LegacySearchTransferOfferRule.evaluate(input.copy(destinationAcceptanceCode = 0)).decision,
        )
        assertEquals(
            LegacySearchTransferOfferDecision.DOES_NOT_WANT_TO_JOIN,
            LegacySearchTransferOfferRule.evaluate(input.copy(destinationAcceptanceCode = 1)).decision,
        )
        assertEquals(
            LegacySearchTransferOfferDecision.WANTS_NEW_SALARY,
            LegacySearchTransferOfferRule.evaluate(input.copy(destinationAcceptanceCode = 2)).decision,
        )
    }

    @Test
    fun `below-threshold offer returns exact counter proposal when funds and code allow it`() {
        val input = baseInput(
            position = 0,
            sellerCounts = intArrayOf(1, 4, 4, 5, 4, 4),
            value = 1000,
            offer = 2499,
            buyerFunds = 2500,
            destinationAcceptanceCode = 0,
        )

        val result = LegacySearchTransferOfferRule.evaluate(input)

        assertEquals(LegacySearchTransferOfferDecision.COUNTER_PROPOSAL, result.decision)
        assertEquals(2500, result.counterOfferValue)
    }

    @Test
    fun `salary-code player can still counter below threshold exactly as legacy`() {
        val input = baseInput(
            position = 0,
            sellerCounts = intArrayOf(1, 4, 4, 5, 4, 4),
            value = 1000,
            offer = 2000,
            buyerFunds = 2500,
            destinationAcceptanceCode = 2,
        )

        val result = LegacySearchTransferOfferRule.evaluate(input)

        assertEquals(LegacySearchTransferOfferDecision.COUNTER_PROPOSAL, result.decision)
        assertEquals(2500, result.counterOfferValue)
    }

    @Test
    fun `below-threshold offer is refused when funds or destination code fail legacy gate`() {
        val input = baseInput(
            position = 0,
            sellerCounts = intArrayOf(1, 4, 4, 5, 4, 4),
            value = 1000,
            offer = 2000,
            buyerFunds = 2500,
        )

        val rejectedByDestination = LegacySearchTransferOfferRule.evaluate(
            input.copy(destinationAcceptanceCode = 1),
        )
        val rejectedByFunds = LegacySearchTransferOfferRule.evaluate(
            input.copy(destinationAcceptanceCode = 0, buyerFunds = 2499),
        )

        assertEquals(LegacySearchTransferOfferDecision.OFFER_REFUSED, rejectedByDestination.decision)
        assertNull(rejectedByDestination.counterOfferValue)
        assertEquals(LegacySearchTransferOfferDecision.OFFER_REFUSED, rejectedByFunds.decision)
        assertNull(rejectedByFunds.counterOfferValue)
    }

    private fun baseInput(
        position: Int,
        sellerCounts: IntArray,
        strength: Int = 40,
        age: Int = 25,
        value: Int = 1000,
        offer: Int = 0,
        buyerFunds: Long = 0,
        destinationAcceptanceCode: Int = 0,
    ): LegacySearchTransferOfferInput = LegacySearchTransferOfferInput(
        playerPositionCode = position,
        sellerPositionCounts = sellerCounts,
        playerStrength = strength,
        playerAge = age,
        playerValue = value,
        offerValue = offer,
        buyerFunds = buyerFunds,
        destinationAcceptanceCode = destinationAcceptanceCode,
    )
}
