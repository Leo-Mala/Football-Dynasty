package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyStadiumConstructionRuleTest {
    @Test
    fun `insufficient cash rejects construction without debit or record`() {
        val plan = LegacyStadiumConstructionRule.startPlan(
            clubCash = 99_999,
            quoteCost = 100_000,
            stadiumCode = 7,
            endTimestampMillis = 1234L,
            additions = listOf(1, 2, 3, 4),
        )

        assertFalse(plan.accepted)
        assertEquals(0, plan.debitAmount)
        assertNull(plan.financialCategoryCode)
        assertNull(plan.recordToAppend)
    }

    @Test
    fun `exact cash accepts construction and preserves debit category seven`() {
        val plan = LegacyStadiumConstructionRule.startPlan(
            clubCash = 100_000,
            quoteCost = 100_000,
            stadiumCode = 42,
            endTimestampMillis = 9999L,
            additions = listOf(10, 0, 20, 0),
        )

        assertTrue(plan.accepted)
        assertEquals(100_000, plan.debitAmount)
        assertEquals(7, plan.financialCategoryCode)
        assertEquals(
            LegacyStadiumConstructionRecord(
                stadiumCode = 42,
                endTimestampMillis = 9999L,
                additions = listOf(10, 0, 20, 0),
            ),
            plan.recordToAppend,
        )
    }

    @Test
    fun `completion comparison is strictly before and not equal`() {
        val records = listOf(
            LegacyStadiumConstructionRecord(1, 999L, listOf(1, 0, 0, 0)),
            LegacyStadiumConstructionRecord(2, 1_000L, listOf(0, 2, 0, 0)),
            LegacyStadiumConstructionRecord(3, 1_001L, listOf(0, 0, 3, 0)),
        )

        val sweep = LegacyStadiumConstructionRule.sweepCompleted(
            records = records,
            currentTimestampMillis = 1_000L,
        )

        assertEquals(listOf(1), sweep.completed.map { it.stadiumCode })
        assertEquals(listOf(2, 3), sweep.remainingRecords.map { it.stadiumCode })
    }

    @Test
    fun `completion preserves source order and applies only positive additions`() {
        val records = listOf(
            LegacyStadiumConstructionRecord(10, 100L, listOf(5, 0, -2, 7)),
            LegacyStadiumConstructionRecord(20, 50L, listOf(0, 3, 4, 0)),
        )

        val sweep = LegacyStadiumConstructionRule.sweepCompleted(
            records = records,
            currentTimestampMillis = 1_000L,
        )

        assertEquals(listOf(10, 20), sweep.completed.map { it.stadiumCode })
        assertEquals(listOf(5, 0, 0, 7), sweep.completed[0].positiveAdditionsByCategory)
        assertEquals(listOf(0, 3, 4, 0), sweep.completed[1].positiveAdditionsByCategory)
        assertTrue(sweep.remainingRecords.isEmpty())
    }
}
