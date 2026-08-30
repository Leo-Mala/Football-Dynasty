package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyTransferDestinationAcceptanceRuleTest {
    @Test
    fun `destination J code zero short-circuits to acceptance code zero`() {
        val result = LegacyTransferDestinationAcceptanceRule.evaluate(
            baseInput(destinationJCode = 0),
        )

        assertEquals(0, result.acceptanceCode)
        assertNull(result.requiredSalary)
    }

    @Test
    fun `nonzero player E or lower seller P0 follows the proven zero-code branches`() {
        val sellerAndDestinationHigh = LegacyTransferDestinationAcceptanceRule.evaluate(
            baseInput(playerECode = 1, sellerP0Code = 3, destinationP0Code = 3),
        )
        val sellerThreeDestinationTwo = LegacyTransferDestinationAcceptanceRule.evaluate(
            baseInput(playerECode = 1, sellerP0Code = 3, destinationP0Code = 2),
        )
        val sellerThreeDestinationOne = LegacyTransferDestinationAcceptanceRule.evaluate(
            baseInput(playerECode = 1, sellerP0Code = 3, destinationP0Code = 1),
        )
        val sellerLow = LegacyTransferDestinationAcceptanceRule.evaluate(
            baseInput(playerECode = 0, sellerP0Code = 2, destinationP0Code = 0),
        )

        assertEquals(0, sellerAndDestinationHigh.acceptanceCode)
        assertEquals(0, sellerThreeDestinationTwo.acceptanceCode)
        assertEquals(0, sellerThreeDestinationOne.acceptanceCode)
        assertEquals(0, sellerLow.acceptanceCode)
    }

    @Test
    fun `otherwise unmatched first branch returns legacy rejection code one`() {
        val result = LegacyTransferDestinationAcceptanceRule.evaluate(
            baseInput(
                playerECode = 1,
                sellerP0Code = 4,
                destinationP0Code = 2,
                destinationOCode = 0,
            ),
        )

        assertEquals(1, result.acceptanceCode)
        assertNull(result.requiredSalary)
    }

    @Test
    fun `high seller branch accepts destination P0 four or destination O one with code zero`() {
        val highDestination = LegacyTransferDestinationAcceptanceRule.evaluate(
            baseInput(
                playerECode = 0,
                sellerP0Code = 4,
                destinationP0Code = 4,
                destinationOCode = 0,
            ),
        )
        val destinationOOne = LegacyTransferDestinationAcceptanceRule.evaluate(
            baseInput(
                playerECode = 0,
                sellerP0Code = 4,
                destinationP0Code = 0,
                destinationOCode = 1,
            ),
        )

        assertEquals(0, highDestination.acceptanceCode)
        assertEquals(0, destinationOOne.acceptanceCode)
        assertNull(highDestination.requiredSalary)
        assertNull(destinationOOne.requiredSalary)
    }

    @Test
    fun `destination P0 three returns code two and exactly doubles salary`() {
        val result = LegacyTransferDestinationAcceptanceRule.evaluate(
            baseInput(
                playerECode = 0,
                sellerP0Code = 4,
                destinationP0Code = 3,
                destinationOCode = 0,
                playerSalary = 12345,
            ),
        )

        assertEquals(2, result.acceptanceCode)
        assertEquals(24690, result.requiredSalary)
    }

    @Test
    fun `salary doubling preserves JVM Int overflow behavior`() {
        val result = LegacyTransferDestinationAcceptanceRule.evaluate(
            baseInput(
                playerECode = 0,
                sellerP0Code = 4,
                destinationP0Code = 3,
                destinationOCode = 0,
                playerSalary = Int.MAX_VALUE,
            ),
        )

        assertEquals(2, result.acceptanceCode)
        assertEquals(-2, result.requiredSalary)
    }

    @Test
    fun `high seller branch with low destination codes returns rejection code one`() {
        val result = LegacyTransferDestinationAcceptanceRule.evaluate(
            baseInput(
                playerECode = 0,
                sellerP0Code = 4,
                destinationP0Code = 2,
                destinationOCode = 0,
            ),
        )

        assertEquals(1, result.acceptanceCode)
        assertNull(result.requiredSalary)
    }

    private fun baseInput(
        destinationJCode: Int = 1,
        playerECode: Int = 0,
        sellerP0Code: Int = 4,
        destinationP0Code: Int = 2,
        destinationOCode: Int = 0,
        playerSalary: Int = 1000,
    ): LegacyTransferDestinationAcceptanceInput = LegacyTransferDestinationAcceptanceInput(
        destinationJCode = destinationJCode,
        playerECode = playerECode,
        sellerP0Code = sellerP0Code,
        destinationP0Code = destinationP0Code,
        destinationOCode = destinationOCode,
        playerSalary = playerSalary,
    )
}
