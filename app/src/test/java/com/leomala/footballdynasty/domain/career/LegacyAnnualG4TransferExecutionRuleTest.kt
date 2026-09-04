package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.manager.LegacyBooleanMutation
import com.leomala.footballdynasty.domain.manager.LegacyTransferExecutionInput
import com.leomala.footballdynasty.domain.manager.LegacyTransferExecutionRule
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyAnnualG4TransferExecutionRuleTest {
    private val day = 86_400_000L

    @Test
    fun annualG4AdapterMatchesExactT1TrueFalseFalseInvocation() {
        val input = LegacyAnnualG4TransferExecutionInput(
            sourceClubPresent = true,
            sourceClubActive = true,
            destinationClubActive = true,
            destinationClubId = 42,
            transferValue = 1_000,
            playerContractEndMillisBefore = 31 * day,
            currentGameMillis = 0L,
            currentCalendarMillis = 5 * day,
            sourcePrimarySlotMatchesPlayer = true,
            sourceSecondarySlotMatchesPlayer = false,
        )

        val actual = LegacyAnnualG4TransferExecutionRule.plan(input)
        val direct = LegacyTransferExecutionRule.plan(
            LegacyTransferExecutionInput(
                sourceClubPresent = true,
                sourceClubActive = true,
                destinationClubActive = true,
                destinationClubId = 42,
                transferValue = 1_000,
                legacySecondaryChargeFlag = true,
                loanMove = false,
                legacyNonFinancialMoveFlag = false,
                playerContractEndMillisBefore = 31 * day,
                currentGameMillis = 0L,
                currentCalendarMillis = 5 * day,
                sourcePrimarySlotMatchesPlayer = true,
                sourceSecondarySlotMatchesPlayer = false,
            ),
        )

        assertEquals(direct, actual)
        assertEquals(200, actual.secondarySellerCharge)
        assertEquals(800L, actual.sellerFundsDelta)
        assertEquals(-1_000L, actual.buyerFundsDelta)
        assertEquals(180L, actual.contractDurationDays)
        assertEquals(185L * day, actual.contractEndMillisAfter)
        assertEquals(LegacyBooleanMutation.UNCHANGED, actual.rawYMutation)
    }

    @Test
    fun adapterDoesNotInventLoanOrNonFinancialT1Branches() {
        val actual = LegacyAnnualG4TransferExecutionRule.plan(
            LegacyAnnualG4TransferExecutionInput(
                sourceClubPresent = true,
                sourceClubActive = false,
                destinationClubActive = false,
                destinationClubId = 7,
                transferValue = 500,
                playerContractEndMillisBefore = 0L,
                currentGameMillis = 10 * day,
                currentCalendarMillis = 12 * day,
                sourcePrimarySlotMatchesPlayer = false,
                sourceSecondarySlotMatchesPlayer = false,
            ),
        )

        assertEquals(LegacyBooleanMutation.UNCHANGED, actual.rawYMutation)
        assertEquals(0L, actual.sellerFundsDelta)
        assertEquals(0L, actual.buyerFundsDelta)
        assertEquals(180L, actual.contractDurationDays)
        assertEquals(192L * day, actual.contractEndMillisAfter)
    }
}
