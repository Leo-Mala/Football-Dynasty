package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacySearchTransferRuntimeRuleTest {
    private val day = 86_400_000L

    @Test
    fun defaultPurchaseUsesPlayerValueAndMutatesRuntime() {
        val result = LegacySearchTransferRuntimeRule.execute(
            state = state(),
            legacyActionCode = 1,
            suggestedSalaryCode = 77,
            storedCounterOfferValue = 900,
            playerValue = 600,
            baseInput = input(),
        )

        assertEquals(LegacySearchTransferAction.PurchaseDefault, result.action)
        assertEquals(600L, result.executionPlan?.sellerFundsDelta)
        assertEquals(-600L, result.executionPlan?.buyerFundsDelta)
        assertEquals(10_600L, result.state.sourceClub?.funds)
        assertEquals(19_400L, result.state.destinationClub.funds)
        assertEquals(42, result.state.player.clubCode)
        assertEquals(50, result.state.player.salaryCode)
        assertTrue(result.state.destinationClub.rosterPlayerCodes.contains(7))
    }

    @Test
    fun salaryPurchaseUsesLegacySuggestedSalary() {
        val result = LegacySearchTransferRuntimeRule.execute(
            state = state(),
            legacyActionCode = 3,
            suggestedSalaryCode = 77,
            storedCounterOfferValue = 900,
            playerValue = 600,
            baseInput = input(),
        )

        assertEquals(LegacySearchTransferAction.PurchaseWithSalary(77), result.action)
        assertEquals(77, result.state.player.salaryCode)
        assertEquals(19_400L, result.state.destinationClub.funds)
    }

    @Test
    fun counterOfferPurchaseUsesStoredValue() {
        val result = LegacySearchTransferRuntimeRule.execute(
            state = state(),
            legacyActionCode = 4,
            suggestedSalaryCode = 77,
            storedCounterOfferValue = 900,
            playerValue = 600,
            baseInput = input(),
        )

        assertEquals(LegacySearchTransferAction.PurchaseCounterOffer(900), result.action)
        assertEquals(900L, result.executionPlan?.sellerFundsDelta)
        assertEquals(-900L, result.executionPlan?.buyerFundsDelta)
        assertEquals(10_900L, result.state.sourceClub?.funds)
        assertEquals(19_100L, result.state.destinationClub.funds)
    }

    @Test
    fun loanUsesT1LoanPathWithoutCashMutation() {
        val result = LegacySearchTransferRuntimeRule.execute(
            state = state(),
            legacyActionCode = 2,
            suggestedSalaryCode = 77,
            storedCounterOfferValue = 900,
            playerValue = 600,
            baseInput = input(transferValue = 900),
        )

        assertEquals(LegacySearchTransferAction.Loan, result.action)
        assertEquals(0L, result.executionPlan?.sellerFundsDelta)
        assertEquals(0L, result.executionPlan?.buyerFundsDelta)
        assertEquals(10_000L, result.state.sourceClub?.funds)
        assertEquals(20_000L, result.state.destinationClub.funds)
        assertTrue(result.state.player.rawY)
        assertEquals(365L, result.executionPlan?.contractDurationDays)
    }

    @Test
    fun unknownActionIsExactNoOp() {
        val before = state()
        val result = LegacySearchTransferRuntimeRule.execute(
            state = before,
            legacyActionCode = 99,
            suggestedSalaryCode = 77,
            storedCounterOfferValue = 900,
            playerValue = 600,
            baseInput = input(),
        )

        assertEquals(LegacySearchTransferAction.None, result.action)
        assertNull(result.executionPlan)
        assertEquals(before, result.state)
    }

    private fun state() = LegacyTransferRuntimeState(
        mainTeamDirty = false,
        player = LegacyTransferPlayerRuntimeState(
            playerCode = 7,
            clubCode = 11,
            salaryCode = 50,
            contractEndMillis = 10 * day,
            rawX = true,
            rawY = false,
            rawZ = true,
            rawCrossActiveFlag = false,
        ),
        sourceClub = LegacyTransferClubRuntimeState(
            clubCode = 11,
            active = true,
            funds = 10_000L,
            rosterPlayerCodes = listOf(7, 8),
            primarySlotPlayerCode = 7,
            secondarySlotPlayerCode = null,
            rawStateFlag = true,
        ),
        destinationClub = LegacyTransferClubRuntimeState(
            clubCode = 42,
            active = true,
            funds = 20_000L,
            rosterPlayerCodes = listOf(9),
            primarySlotPlayerCode = null,
            secondarySlotPlayerCode = null,
            rawStateFlag = true,
        ),
    )

    private fun input(transferValue: Int = 500) = LegacyTransferExecutionInput(
        sourceClubPresent = true,
        sourceClubActive = true,
        destinationClubActive = true,
        destinationClubId = 42,
        transferValue = transferValue,
        legacySecondaryChargeFlag = false,
        loanMove = false,
        legacyNonFinancialMoveFlag = false,
        playerContractEndMillisBefore = 10 * day,
        currentGameMillis = 0L,
        currentCalendarMillis = 5 * day,
        sourcePrimarySlotMatchesPlayer = true,
        sourceSecondarySlotMatchesPlayer = false,
    )
}
