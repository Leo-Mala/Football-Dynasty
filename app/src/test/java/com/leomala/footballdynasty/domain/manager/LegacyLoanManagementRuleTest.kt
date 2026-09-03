package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyLoanManagementRuleTest {
    @Test
    fun `active loan always offers cancel action before list state or club limit`() {
        val decision = LegacyLoanManagementRule.menuDecision(
            currentlyOnLoan = true,
            listedForLoan = true,
            firstClubLoanCount = 99,
            secondClubLoanCount = 99,
        )

        assertEquals(LegacyLoanManagementRule.ACTION_CANCEL_CURRENT_LOAN, decision.actionCode)
        assertFalse(decision.blockedByClubLoanLimit)
    }

    @Test
    fun `listed player offers removal before applying club loan limit`() {
        val decision = LegacyLoanManagementRule.menuDecision(
            currentlyOnLoan = false,
            listedForLoan = true,
            firstClubLoanCount = 3,
            secondClubLoanCount = 7,
        )

        assertEquals(LegacyLoanManagementRule.ACTION_REMOVE_FROM_LOAN_LIST, decision.actionCode)
        assertFalse(decision.blockedByClubLoanLimit)
        assertEquals(
            LegacyLoanListMutation.SET_FALSE,
            LegacyLoanManagementRule.listMutation(decision.actionCode!!),
        )
    }

    @Test
    fun `unlisted player is blocked at the exact legacy combined count of three`() {
        val below = LegacyLoanManagementRule.menuDecision(
            currentlyOnLoan = false,
            listedForLoan = false,
            firstClubLoanCount = 1,
            secondClubLoanCount = 1,
        )
        assertEquals(LegacyLoanManagementRule.ACTION_ADD_TO_LOAN_LIST, below.actionCode)
        assertFalse(below.blockedByClubLoanLimit)
        assertEquals(
            LegacyLoanListMutation.SET_TRUE,
            LegacyLoanManagementRule.listMutation(below.actionCode!!),
        )

        val exact = LegacyLoanManagementRule.menuDecision(
            currentlyOnLoan = false,
            listedForLoan = false,
            firstClubLoanCount = 1,
            secondClubLoanCount = 2,
        )
        assertNull(exact.actionCode)
        assertTrue(exact.blockedByClubLoanLimit)
    }

    @Test
    fun `cancel action does not mutate the independent listed for loan flag`() {
        assertEquals(
            LegacyLoanListMutation.UNCHANGED,
            LegacyLoanManagementRule.listMutation(LegacyLoanManagementRule.ACTION_CANCEL_CURRENT_LOAN),
        )
        assertEquals(LegacyLoanListMutation.UNCHANGED, LegacyLoanManagementRule.listMutation(99))
    }

    @Test
    fun `early return uses source club from exact first identity match and removes only that record`() {
        val records = listOf(
            LegacyLoanRecord(playerCode = 7, sourceClubCode = 10, expiryMillis = 1L),
            LegacyLoanRecord(playerCode = 7, sourceClubCode = 20, expiryMillis = 2L),
            LegacyLoanRecord(playerCode = 8, sourceClubCode = 30, expiryMillis = 3L),
        )

        val plan = LegacyLoanManagementRule.earlyReturnPlan(
            records = records,
            firstIdentityMatchIndex = 1,
        )

        assertEquals(1, plan.matchingRecordIndex)
        assertEquals(20, plan.storedSourceClubCode)
        assertTrue(plan.removeMatchingRecord)
        assertTrue(plan.invokeReturnMove)
        assertTrue(plan.markMainTeamDirty)
        assertEquals(listOf(records[0], records[2]), LegacyLoanManagementRule.removeMatchingRecord(records, plan))
    }

    @Test
    fun `missing identity match preserves legacy U1 null invocation without inventing source club`() {
        val records = listOf(LegacyLoanRecord(playerCode = 7, sourceClubCode = 10, expiryMillis = 1L))

        val plan = LegacyLoanManagementRule.earlyReturnPlan(
            records = records,
            firstIdentityMatchIndex = null,
        )

        assertNull(plan.matchingRecordIndex)
        assertNull(plan.storedSourceClubCode)
        assertFalse(plan.removeMatchingRecord)
        assertTrue(plan.invokeReturnMove)
        assertTrue(plan.markMainTeamDirty)
        assertEquals(records, LegacyLoanManagementRule.removeMatchingRecord(records, plan))
    }

    @Test
    fun `out of range identity index is treated as no recovered record`() {
        val records = listOf(LegacyLoanRecord(playerCode = 7, sourceClubCode = 10, expiryMillis = 1L))
        val plan = LegacyLoanManagementRule.earlyReturnPlan(records, firstIdentityMatchIndex = 9)

        assertNull(plan.matchingRecordIndex)
        assertNull(plan.storedSourceClubCode)
        assertFalse(plan.removeMatchingRecord)
        assertTrue(plan.invokeReturnMove)
    }
}
