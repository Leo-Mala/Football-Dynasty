package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyLoanManagementRuntimeRuleTest {
    private val records = listOf(
        LegacyLoanRecord(playerCode = 7, sourceClubCode = 10, expiryMillis = 1L),
        LegacyLoanRecord(playerCode = 7, sourceClubCode = 20, expiryMillis = 2L),
        LegacyLoanRecord(playerCode = 8, sourceClubCode = 30, expiryMillis = 3L),
    )

    @Test
    fun `active loan executes exact early return plan without touching list flag`() {
        val initial = LegacyLoanManagementRuntimeState(
            listedForLoan = true,
            loanRecords = records,
        )

        val result = LegacyLoanManagementRuntimeRule.execute(
            state = initial,
            currentlyOnLoan = true,
            firstClubLoanCount = 99,
            secondClubLoanCount = 99,
            firstIdentityMatchIndex = 1,
        )

        assertEquals(LegacyLoanManagementRule.ACTION_CANCEL_CURRENT_LOAN, result.decision.actionCode)
        assertEquals(LegacyLoanListMutation.UNCHANGED, result.listMutation)
        assertEquals(1, result.earlyReturnPlan?.matchingRecordIndex)
        assertTrue(result.invokeReturnMove)
        assertEquals(20, result.returnMoveSourceClubCode)
        assertTrue(result.state.listedForLoan)
        assertEquals(listOf(records[0], records[2]), result.state.loanRecords)
        assertTrue(result.state.mainTeamDirty)
    }

    @Test
    fun `listed player action clears only the independent loan-list flag`() {
        val initial = LegacyLoanManagementRuntimeState(
            listedForLoan = true,
            loanRecords = records,
            mainTeamDirty = true,
        )

        val result = LegacyLoanManagementRuntimeRule.execute(
            state = initial,
            currentlyOnLoan = false,
            firstClubLoanCount = 3,
            secondClubLoanCount = 4,
            firstIdentityMatchIndex = null,
        )

        assertEquals(LegacyLoanManagementRule.ACTION_REMOVE_FROM_LOAN_LIST, result.decision.actionCode)
        assertEquals(LegacyLoanListMutation.SET_FALSE, result.listMutation)
        assertFalse(result.state.listedForLoan)
        assertEquals(records, result.state.loanRecords)
        assertTrue(result.state.mainTeamDirty)
        assertFalse(result.invokeReturnMove)
        assertNull(result.earlyReturnPlan)
    }

    @Test
    fun `unlisted player below limit is added to loan list with no other mutation`() {
        val initial = LegacyLoanManagementRuntimeState(
            listedForLoan = false,
            loanRecords = records,
        )

        val result = LegacyLoanManagementRuntimeRule.execute(
            state = initial,
            currentlyOnLoan = false,
            firstClubLoanCount = 1,
            secondClubLoanCount = 1,
            firstIdentityMatchIndex = null,
        )

        assertEquals(LegacyLoanManagementRule.ACTION_ADD_TO_LOAN_LIST, result.decision.actionCode)
        assertEquals(LegacyLoanListMutation.SET_TRUE, result.listMutation)
        assertTrue(result.state.listedForLoan)
        assertEquals(records, result.state.loanRecords)
        assertFalse(result.state.mainTeamDirty)
        assertFalse(result.invokeReturnMove)
    }

    @Test
    fun `exact combined loan limit is a complete runtime no-op`() {
        val initial = LegacyLoanManagementRuntimeState(
            listedForLoan = false,
            loanRecords = records,
            mainTeamDirty = true,
        )

        val result = LegacyLoanManagementRuntimeRule.execute(
            state = initial,
            currentlyOnLoan = false,
            firstClubLoanCount = 1,
            secondClubLoanCount = 2,
            firstIdentityMatchIndex = null,
        )

        assertNull(result.decision.actionCode)
        assertTrue(result.decision.blockedByClubLoanLimit)
        assertEquals(LegacyLoanListMutation.UNCHANGED, result.listMutation)
        assertEquals(initial, result.state)
        assertFalse(result.invokeReturnMove)
    }

    @Test
    fun `missing identity record preserves U1 null effect and dirty flag`() {
        val initial = LegacyLoanManagementRuntimeState(
            listedForLoan = false,
            loanRecords = records,
        )

        val result = LegacyLoanManagementRuntimeRule.execute(
            state = initial,
            currentlyOnLoan = true,
            firstClubLoanCount = 0,
            secondClubLoanCount = 0,
            firstIdentityMatchIndex = null,
        )

        assertTrue(result.invokeReturnMove)
        assertNull(result.returnMoveSourceClubCode)
        assertEquals(records, result.state.loanRecords)
        assertTrue(result.state.mainTeamDirty)
    }
}
