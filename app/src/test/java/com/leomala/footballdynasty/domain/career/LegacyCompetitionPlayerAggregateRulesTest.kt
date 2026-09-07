package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCompetitionPlayerAggregateRulesTest {
    @Test
    fun `position families preserve exact legacy eligibility boundaries`() {
        assertTrue(eligible(l0 = 0, f0 = 0, g0 = 1))
        assertFalse(eligible(l0 = 0, f0 = 0, g0 = 2))

        assertTrue(eligible(l0 = 2, f0 = 0, g0 = 3))
        assertTrue(eligible(l0 = 2, f0 = 0, g0 = 8))
        assertFalse(eligible(l0 = 2, f0 = 0, g0 = 9))

        assertTrue(eligible(l0 = 3, f0 = 0, g0 = 10))
        assertTrue(eligible(l0 = 3, f0 = 0, g0 = 17))
        assertFalse(eligible(l0 = 3, f0 = 0, g0 = 9))

        assertTrue(eligible(l0 = 4, f0 = 0, g0 = 18))
        assertFalse(eligible(l0 = 4, f0 = 0, g0 = 17))
    }

    @Test
    fun `legacy l0 one exceptional g0 overrides remain reachable`() {
        assertFalse(eligible(l0 = 1, f0 = 0, g0 = 10))
        assertTrue(eligible(l0 = 1, f0 = 0, g0 = 17))

        assertFalse(eligible(l0 = 1, f0 = 1, g0 = 17))
        assertTrue(eligible(l0 = 1, f0 = 1, g0 = 10))
    }

    @Test
    fun `first aggregate preserves raw sum count average and selector`() {
        val result = LegacyCompetitionPlayerAggregateRules.apply(
            existing = null,
            input = input(playerId = "p1", l0 = 2, f0 = 0, g0 = 3, r = 1, y0 = 7.5),
        )

        assertEquals(
            LegacyCompetitionPlayerAggregateRules.Aggregate(
                playerId = "p1",
                rawSumC = 7.5,
                rawCountD = 1.0,
                rawAverageE = 7.5,
                rawSelectorF = 2,
            ),
            result,
        )
    }

    @Test
    fun `subsequent aggregate adds y0 increments count recomputes average and overwrites selector`() {
        val existing = LegacyCompetitionPlayerAggregateRules.Aggregate(
            playerId = "p1",
            rawSumC = 14.0,
            rawCountD = 2.0,
            rawAverageE = 7.0,
            rawSelectorF = 2,
        )

        val result = requireNotNull(
            LegacyCompetitionPlayerAggregateRules.apply(
                existing = existing,
                input = input(playerId = "p1", l0 = 3, f0 = 0, g0 = 11, r = 1, y0 = 8.0),
            ),
        )

        assertEquals(22.0, result.rawSumC, 0.0)
        assertEquals(3.0, result.rawCountD, 0.0)
        assertEquals(22.0 / 3.0, result.rawAverageE, 0.0)
        assertEquals(6, result.rawSelectorF)
    }

    @Test
    fun `raw selector three remaps to six only when legacy R is zero`() {
        val rZero = requireNotNull(
            LegacyCompetitionPlayerAggregateRules.apply(
                existing = null,
                input = input(playerId = "p0", l0 = 3, f0 = 0, g0 = 10, r = 0, y0 = 6.0),
            ),
        )
        val rOne = requireNotNull(
            LegacyCompetitionPlayerAggregateRules.apply(
                existing = null,
                input = input(playerId = "p1", l0 = 3, f0 = 0, g0 = 10, r = 1, y0 = 6.0),
            ),
        )
        val rOther = requireNotNull(
            LegacyCompetitionPlayerAggregateRules.apply(
                existing = null,
                input = input(playerId = "p2", l0 = 3, f0 = 0, g0 = 10, r = 2, y0 = 6.0),
            ),
        )

        assertEquals(6, rZero.rawSelectorF)
        assertEquals(3, rOne.rawSelectorF)
        assertEquals(3, rOther.rawSelectorF)
    }

    @Test
    fun `ineligible player leaves existing aggregate untouched and creates no new aggregate`() {
        val existing = LegacyCompetitionPlayerAggregateRules.Aggregate(
            playerId = "p1",
            rawSumC = 10.0,
            rawCountD = 2.0,
            rawAverageE = 5.0,
            rawSelectorF = 0,
        )
        val ineligible = input(playerId = "p1", l0 = 2, f0 = 0, g0 = 9, r = 0, y0 = 9.0)

        assertSame(existing, LegacyCompetitionPlayerAggregateRules.apply(existing, ineligible))
        assertNull(
            LegacyCompetitionPlayerAggregateRules.apply(
                existing = null,
                input = ineligible.copy(playerId = "p2"),
            ),
        )
    }

    private fun eligible(l0: Int, f0: Int, g0: Int): Boolean =
        LegacyCompetitionPlayerAggregateRules.isEligible(
            input(playerId = "p", l0 = l0, f0 = f0, g0 = g0, r = 0, y0 = 1.0),
        )

    private fun input(
        playerId: String,
        l0: Int,
        f0: Int,
        g0: Int,
        r: Int,
        y0: Double,
    ) = LegacyCompetitionPlayerAggregateRules.PlayerInput(
        playerId = playerId,
        legacyL0 = l0,
        legacyF0 = f0,
        legacyG0 = g0,
        legacyR = r,
        matchValueY0 = y0,
    )
}
