package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyTransferExecutionRuleTest {
    private val day = 86_400_000L

    @Test
    fun purchaseWrapperMatchesActivityProcuraX() {
        assertEquals(
            LegacySearchPurchaseExecution(800, 70),
            LegacyTransferExecutionRule.purchaseExecution(800, 500, 70),
        )
        assertEquals(
            LegacySearchPurchaseExecution(500, null),
            LegacyTransferExecutionRule.purchaseExecution(0, 500, -1),
        )
    }

    @Test
    fun directPurchaseMatchesT1MutationPlan() {
        val result = LegacyTransferExecutionRule.plan(input(transferValue = 1_000))
        assertTrue(result.markMainTeamDirty)
        assertEquals(42, result.destinationClubId)
        assertEquals(LegacyBooleanMutation.SET_FALSE, result.rawXMutation)
        assertEquals(LegacyBooleanMutation.UNCHANGED, result.rawYMutation)
        assertEquals(LegacyBooleanMutation.SET_FALSE, result.rawZMutation)
        assertEquals(1_000L, result.sellerFundsDelta)
        assertEquals(-1_000L, result.buyerFundsDelta)
        assertEquals(185L * day, result.contractEndMillisAfter)
        assertTrue(result.clearSourcePrimarySlot)
        assertTrue(result.setPlayerCrossActiveFlag)
        assertTrue(result.removeFromSourceRoster)
        assertTrue(result.addToDestinationRoster)
        assertTrue(result.resetSourceClubStateFlag)
    }

    @Test
    fun secondaryChargePreservesLegacyThresholds() {
        assertEquals(120, LegacyTransferExecutionRule.calculateSecondaryCharge(1_000, 30))
        assertEquals(200, LegacyTransferExecutionRule.calculateSecondaryCharge(1_000, 31))
        assertEquals(220, LegacyTransferExecutionRule.calculateSecondaryCharge(1_000, 61))
        assertEquals(250, LegacyTransferExecutionRule.calculateSecondaryCharge(1_000, 91))
        assertEquals(300, LegacyTransferExecutionRule.calculateSecondaryCharge(1_000, 181))

        val result = LegacyTransferExecutionRule.plan(
            input(
                transferValue = 1_001,
                legacySecondaryChargeFlag = true,
                playerContractEndMillisBefore = 31 * day,
                currentCalendarMillis = 0,
            ),
        )
        assertEquals(200, result.secondarySellerCharge)
        assertEquals(801L, result.sellerFundsDelta)
        assertEquals(-1_001L, result.buyerFundsDelta)
    }

    @Test
    fun loanMatchesQThenT1Path() {
        val result = LegacyTransferExecutionRule.plan(
            input(
                transferValue = Int.MAX_VALUE,
                legacySecondaryChargeFlag = true,
                loanMove = true,
                playerContractEndMillisBefore = 900 * day,
                currentCalendarMillis = day,
            ),
        )
        assertEquals(0L, result.sellerFundsDelta)
        assertEquals(0L, result.buyerFundsDelta)
        assertEquals(0, result.secondarySellerCharge)
        assertEquals(LegacyBooleanMutation.SET_TRUE, result.rawYMutation)
        assertEquals(366L * day, result.contractEndMillisAfter)
        assertEquals(365L, result.contractDurationDays)
    }

    @Test
    fun nonFinancialFlagAndMissingSourcePreserveLegacyBranches() {
        val result = LegacyTransferExecutionRule.plan(
            input(
                sourceClubPresent = false,
                sourceClubActive = false,
                destinationClubActive = false,
                legacyNonFinancialMoveFlag = true,
                currentCalendarMillis = null,
            ),
        )
        assertFalse(result.markMainTeamDirty)
        assertEquals(LegacyBooleanMutation.SET_FALSE, result.rawYMutation)
        assertEquals(0L, result.sellerFundsDelta)
        assertEquals(0L, result.buyerFundsDelta)
        assertNull(result.contractEndMillisAfter)
        assertFalse(result.removeFromSourceRoster)
        assertTrue(result.addToDestinationRoster)
    }

    @Test
    fun remainingContractDaysUsesLegacyTruncation() {
        assertEquals(2, LegacyTransferExecutionRule.remainingContractDays(day, 3 * day + day / 2))
        assertEquals(0, LegacyTransferExecutionRule.remainingContractDays(5 * day, 5 * day))
    }

    private fun input(
        sourceClubPresent: Boolean = true,
        sourceClubActive: Boolean = true,
        destinationClubActive: Boolean = true,
        transferValue: Int = 500,
        legacySecondaryChargeFlag: Boolean = false,
        loanMove: Boolean = false,
        legacyNonFinancialMoveFlag: Boolean = false,
        playerContractEndMillisBefore: Long = 10 * day,
        currentCalendarMillis: Long? = 5 * day,
    ) = LegacyTransferExecutionInput(
        sourceClubPresent = sourceClubPresent,
        sourceClubActive = sourceClubActive,
        destinationClubActive = destinationClubActive,
        destinationClubId = 42,
        transferValue = transferValue,
        legacySecondaryChargeFlag = legacySecondaryChargeFlag,
        loanMove = loanMove,
        legacyNonFinancialMoveFlag = legacyNonFinancialMoveFlag,
        playerContractEndMillisBefore = playerContractEndMillisBefore,
        currentGameMillis = 0,
        currentCalendarMillis = currentCalendarMillis,
        sourcePrimarySlotMatchesPlayer = true,
        sourceSecondarySlotMatchesPlayer = false,
    )
}
