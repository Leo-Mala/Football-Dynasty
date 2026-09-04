package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.manager.LegacyBooleanMutation
import com.leomala.footballdynasty.domain.manager.LegacyTransferExecutionInput
import com.leomala.footballdynasty.domain.manager.LegacyTransferExecutionRule
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyAnnualDeferredTransferExecutionRuleTest {
    private val day = 86_400_000L

    @Test
    fun deferredAdapterMatchesExactU1T1ZeroFalseFalseTrueInvocation() {
        val input = LegacyAnnualDeferredTransferExecutionInput(
            sourceClubPresent = true,
            sourceClubActive = true,
            destinationClubActive = true,
            destinationClubId = 42,
            playerContractEndMillisBefore = 31 * day,
            currentGameMillis = 0L,
            currentCalendarMillis = 5 * day,
            sourcePrimarySlotMatchesPlayer = true,
            sourceSecondarySlotMatchesPlayer = false,
        )

        val actual = LegacyAnnualDeferredTransferExecutionRule.plan(input)
        val direct = LegacyTransferExecutionRule.plan(
            LegacyTransferExecutionInput(
                sourceClubPresent = true,
                sourceClubActive = true,
                destinationClubActive = true,
                destinationClubId = 42,
                transferValue = 0,
                legacySecondaryChargeFlag = false,
                loanMove = false,
                legacyNonFinancialMoveFlag = true,
                playerContractEndMillisBefore = 31 * day,
                currentGameMillis = 0L,
                currentCalendarMillis = 5 * day,
                sourcePrimarySlotMatchesPlayer = true,
                sourceSecondarySlotMatchesPlayer = false,
            ),
        )

        assertEquals(direct, actual)
        assertEquals(0, actual.secondarySellerCharge)
        assertEquals(0L, actual.sellerFundsDelta)
        assertEquals(0L, actual.buyerFundsDelta)
        assertEquals(180L, actual.contractDurationDays)
        assertEquals(LegacyBooleanMutation.SET_FALSE, actual.rawYMutation)
    }

    @Test
    fun deferredAdapterDoesNotInventFinancialOrLoanBranches() {
        val actual = LegacyAnnualDeferredTransferExecutionRule.plan(
            LegacyAnnualDeferredTransferExecutionInput(
                sourceClubPresent = true,
                sourceClubActive = true,
                destinationClubActive = true,
                destinationClubId = 7,
                playerContractEndMillisBefore = 0L,
                currentGameMillis = 10 * day,
                currentCalendarMillis = 12 * day,
                sourcePrimarySlotMatchesPlayer = false,
                sourceSecondarySlotMatchesPlayer = false,
            ),
        )

        assertEquals(0, actual.secondarySellerCharge)
        assertEquals(0L, actual.sellerFundsDelta)
        assertEquals(0L, actual.buyerFundsDelta)
        assertEquals(LegacyBooleanMutation.SET_FALSE, actual.rawYMutation)
        assertEquals(180L, actual.contractDurationDays)
        assertEquals(192L * day, actual.contractEndMillisAfter)
    }
}
