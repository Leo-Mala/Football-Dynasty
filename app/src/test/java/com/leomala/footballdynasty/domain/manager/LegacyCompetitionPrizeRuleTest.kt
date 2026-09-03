package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class LegacyCompetitionPrizeRuleTest {
    @Test
    fun `type two applies legacy Q1 stage offset`() {
        assertEquals(10_000, LegacyCompetitionPrizeRule.prizeAmount(2, 0, 6, 0))
        assertEquals(100_000, LegacyCompetitionPrizeRule.prizeAmount(2, 0, 4, 0))
        assertEquals(3_500_000, LegacyCompetitionPrizeRule.prizeAmount(2, 6, 6, 0))
        assertEquals(0, LegacyCompetitionPrizeRule.prizeAmount(2, 7, 6, 0))
    }

    @Test
    fun `type four uses raw nonnegative row and row one for negative p code`() {
        assertEquals(7_000_000, LegacyCompetitionPrizeRule.prizeAmount(4, 3, 0, 0))
        assertEquals(5_000_000, LegacyCompetitionPrizeRule.prizeAmount(4, 3, 0, -1))
        assertEquals(2_000_000, LegacyCompetitionPrizeRule.prizeAmount(4, 3, 0, 3))
        assertEquals(0, LegacyCompetitionPrizeRule.prizeAmount(4, 6, 0, 0))
    }

    @Test
    fun `type five uses the recovered T1 vector`() {
        assertEquals(2_000_000, LegacyCompetitionPrizeRule.prizeAmount(5, 0, 0, 0))
        assertEquals(5_000_000, LegacyCompetitionPrizeRule.prizeAmount(5, 1, 0, 0))
        assertEquals(5_000_000, LegacyCompetitionPrizeRule.prizeAmount(5, 2, 0, 0))
        assertEquals(0, LegacyCompetitionPrizeRule.prizeAmount(5, 3, 0, 0))
    }

    @Test
    fun `type six preserves outer-array stage bound and defaults invalid p code to row one`() {
        assertEquals(100_000, LegacyCompetitionPrizeRule.prizeAmount(6, 0, 0, 0))
        assertEquals(200_000, LegacyCompetitionPrizeRule.prizeAmount(6, 1, 0, 0))
        assertEquals(200_000, LegacyCompetitionPrizeRule.prizeAmount(6, 1, 0, 9))
        // U1 inner rows contain a stage-2 value, but legacy compares stage with outer length == 2.
        assertEquals(0, LegacyCompetitionPrizeRule.prizeAmount(6, 2, 0, 0))
    }

    @Test
    fun `type eight always returns first V1 value regardless of stage`() {
        assertEquals(1_000_000, LegacyCompetitionPrizeRule.prizeAmount(8, 0, 0, 0))
        assertEquals(1_000_000, LegacyCompetitionPrizeRule.prizeAmount(8, 99, 0, 0))
        assertEquals(0, LegacyCompetitionPrizeRule.prizeAmount(7, 0, 0, 0))
    }

    @Test
    fun `positive prize credits winner cash and category three ledger`() {
        val before = LegacyFinanceRuntimeState(
            cash = 50L,
            ledger = LegacyFinanceLedgerState(prizeIncome = 7, sponsorIncome = 9),
        )
        val after = LegacyCompetitionPrizeRule.applyWinnerPrize(before, 500_000, true)
        assertEquals(500_050L, after.cash)
        assertEquals(500_007, after.ledger.prizeIncome)
        assertEquals(9, after.ledger.sponsorIncome)
    }

    @Test
    fun `nonpositive prize or disabled winner Q0 is exact no op`() {
        val before = LegacyFinanceRuntimeState(123L, LegacyFinanceLedgerState(prizeIncome = 4))
        assertSame(before, LegacyCompetitionPrizeRule.applyWinnerPrize(before, 0, true))
        assertSame(before, LegacyCompetitionPrizeRule.applyWinnerPrize(before, 100_000, false))
    }
}
