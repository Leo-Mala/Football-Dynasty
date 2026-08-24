package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.career.LegacyAnnualSelectionRules.BestA0JExecution
import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAnnualSelectionRulesTest {
    @Test
    fun `legacy threshold tables match c0 and j0 corpus constants`() {
        assertEquals(40, LegacyAnnualSelectionRules.legacyMinimumOverall(true, 1, 0))
        assertEquals(30, LegacyAnnualSelectionRules.legacyMinimumOverall(true, 2, 0))
        assertEquals(20, LegacyAnnualSelectionRules.legacyMinimumOverall(true, 3, 0))
        assertEquals(5, LegacyAnnualSelectionRules.legacyMinimumOverall(true, 4, 0))

        assertEquals(1, LegacyAnnualSelectionRules.legacyMinimumOverall(false, 0, 0))
        assertEquals(10, LegacyAnnualSelectionRules.legacyMinimumOverall(false, 0, 1))
        assertEquals(20, LegacyAnnualSelectionRules.legacyMinimumOverall(false, 0, 2))
        assertEquals(40, LegacyAnnualSelectionRules.legacyMinimumOverall(false, 0, 3))
        assertEquals(50, LegacyAnnualSelectionRules.legacyMinimumOverall(false, 0, 4))
        assertEquals(55, LegacyAnnualSelectionRules.legacyMinimumOverall(false, 0, 5))
    }

    @Test
    fun `best c0 M1 is the thirty player roster cap`() {
        assertFalse(LegacyAnnualSelectionRules.bestC0M1(29))
        assertTrue(LegacyAnnualSelectionRules.bestC0M1(30))
    }

    @Test
    fun `best c0 a1 ignores position caps and only enforces roster and minimum overall`() {
        assertTrue(
            LegacyAnnualSelectionRules.bestC0A1(
                rosterSize = 29,
                targetR0 = false,
                targetO = 0,
                targetP0 = 4,
                subjectOverall = 50,
            )
        )
        assertFalse(
            LegacyAnnualSelectionRules.bestC0A1(
                rosterSize = 29,
                targetR0 = false,
                targetO = 0,
                targetP0 = 4,
                subjectOverall = 49,
            )
        )
        assertFalse(
            LegacyAnnualSelectionRules.bestC0A1(
                rosterSize = 30,
                targetR0 = false,
                targetO = 0,
                targetP0 = 4,
                subjectOverall = 100,
            )
        )
    }

    @Test
    fun `best c0 Z0 enforces divisional min max and optional position cap`() {
        val counts = intArrayOf(3, 5, 5, 10, 5)
        assertTrue(
            LegacyAnnualSelectionRules.bestC0Z0(
                rosterSize = 29,
                targetR0 = false,
                targetO = 0,
                targetP0 = 3,
                subjectOverall = 40,
                subjectPosition = 0,
                enforcePositionCaps = true,
                positionCounts = counts,
            )
        )
        assertFalse(
            LegacyAnnualSelectionRules.bestC0Z0(
                rosterSize = 29,
                targetR0 = false,
                targetO = 0,
                targetP0 = 3,
                subjectOverall = 86,
                subjectPosition = 0,
                enforcePositionCaps = false,
                positionCounts = counts,
            )
        )
        assertFalse(
            LegacyAnnualSelectionRules.bestC0Z0(
                rosterSize = 29,
                targetR0 = false,
                targetO = 0,
                targetP0 = 3,
                subjectOverall = 50,
                subjectPosition = 0,
                enforcePositionCaps = true,
                positionCounts = intArrayOf(4, 0, 0, 0, 0),
            )
        )
    }

    @Test
    fun `best a0 j high pass tries primary counts then fallback counts in shuffled order`() {
        val shuffled = listOf(3, 0, 1, 2, 4)
        assertEquals(
            3,
            LegacyAnnualSelectionRules.bestA0JOverloadedPosition(
                positionCounts = intArrayOf(3, 4, 4, 10, 6),
                shuffledPositions = shuffled,
                highPass = true,
            )
        )
        assertEquals(
            0,
            LegacyAnnualSelectionRules.bestA0JOverloadedPosition(
                positionCounts = intArrayOf(3, 3, 3, 7, 5),
                shuffledPositions = listOf(0, 3, 4, 1, 2),
                highPass = true,
            )
        )
    }

    @Test
    fun `best a0 j low pass uses strict greater than thresholds`() {
        assertEquals(
            null,
            LegacyAnnualSelectionRules.bestA0JOverloadedPosition(
                positionCounts = intArrayOf(3, 4, 4, 6, 4),
                shuffledPositions = listOf(0, 1, 2, 3, 4),
                highPass = false,
            )
        )
        assertEquals(
            2,
            LegacyAnnualSelectionRules.bestA0JOverloadedPosition(
                positionCounts = intArrayOf(3, 4, 5, 6, 4),
                shuffledPositions = listOf(0, 1, 2, 3, 4),
                highPass = false,
            )
        )
    }

    @Test
    fun `best a0 j SITE 1 only gates O0 players`() {
        assertTrue(LegacyAnnualSelectionRules.bestA0JPlayerEligible(2, 2, false, false, false))
        assertFalse(LegacyAnnualSelectionRules.bestA0JPlayerEligible(2, 2, false, true, false))
        assertTrue(LegacyAnnualSelectionRules.bestA0JPlayerEligible(2, 2, false, true, true))
        assertFalse(LegacyAnnualSelectionRules.bestA0JPlayerEligible(2, 2, true, true, true))
        assertFalse(LegacyAnnualSelectionRules.bestA0JPlayerEligible(1, 2, false, false, true))
    }

    @Test
    fun `best a0 j p1 path uses SITE 2 only below overall forty`() {
        val lowFail = FixedIntRandomSource(91)
        assertEquals(
            BestA0JExecution(mode = 0, useN = false),
            LegacyAnnualSelectionRules.bestA0JExecution(lowFail, true, 39, false),
        )
        assertEquals(1L, lowFail.draws)

        val high = FixedIntRandomSource()
        assertEquals(
            BestA0JExecution(mode = 0, useN = true),
            LegacyAnnualSelectionRules.bestA0JExecution(high, true, 40, false),
        )
        assertEquals(0L, high.draws)
    }

    @Test
    fun `best a0 j O0 path can consume SITE 3 then one rating band site`() {
        val site3Pass = FixedIntRandomSource(31)
        assertEquals(
            BestA0JExecution(mode = 1, useN = true),
            LegacyAnnualSelectionRules.bestA0JExecution(site3Pass, false, 95, true),
        )
        assertEquals(1L, site3Pass.draws)

        val site3FailThenBandPass = FixedIntRandomSource(30, 31)
        assertEquals(
            BestA0JExecution(mode = 1, useN = true),
            LegacyAnnualSelectionRules.bestA0JExecution(site3FailThenBandPass, false, 95, true),
        )
        assertEquals(2L, site3FailThenBandPass.draws)
    }

    @Test
    fun `best f q range preserves normal mode one and all low overall overrides`() {
        assertEquals(
            LegacyAnnualSelectionRules.BestFQRange(9, 11),
            LegacyAnnualSelectionRules.bestFQRange(0, 10, 0, 4, 50, 7),
        )
        assertEquals(
            LegacyAnnualSelectionRules.BestFQRange(1, 1),
            LegacyAnnualSelectionRules.bestFQRange(1, 10, 0, 4, 40, 7),
        )
        assertEquals(
            LegacyAnnualSelectionRules.BestFQRange(1, 2),
            LegacyAnnualSelectionRules.bestFQRange(1, 10, 1, 4, 40, 7),
        )
        assertEquals(
            LegacyAnnualSelectionRules.BestFQRange(0, 7),
            LegacyAnnualSelectionRules.bestFQRange(1, 10, 1, 2, 20, 7),
        )
        assertEquals(
            LegacyAnnualSelectionRules.BestFQRange(0, 7),
            LegacyAnnualSelectionRules.bestFQRange(1, 10, 1, 2, 6, 7),
        )
    }

    @Test
    fun `best f q candidate filter enforces range identity flags and roster cap`() {
        val range = LegacyAnnualSelectionRules.BestFQRange(2, 4)
        assertTrue(LegacyAnnualSelectionRules.bestFQCandidateEligible(3, range, false, false, 29))
        assertFalse(LegacyAnnualSelectionRules.bestFQCandidateEligible(5, range, false, false, 29))
        assertFalse(LegacyAnnualSelectionRules.bestFQCandidateEligible(3, range, true, false, 29))
        assertFalse(LegacyAnnualSelectionRules.bestFQCandidateEligible(3, range, false, true, 29))
        assertFalse(LegacyAnnualSelectionRules.bestFQCandidateEligible(3, range, false, false, 30))
    }

    @Test
    fun `best f p fallback eligibility preserves flags and roster cap`() {
        assertTrue(LegacyAnnualSelectionRules.bestFPFallbackEligible(false, false, 29))
        assertFalse(LegacyAnnualSelectionRules.bestFPFallbackEligible(true, false, 29))
        assertFalse(LegacyAnnualSelectionRules.bestFPFallbackEligible(false, true, 29))
        assertFalse(LegacyAnnualSelectionRules.bestFPFallbackEligible(false, false, 30))
    }

    private class FixedIntRandomSource(
        vararg values: Int,
    ) : RandomSource {
        private val iterator = values.iterator()

        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            val value = iterator.nextInt()
            require(value in 0 until bound) { "value=$value bound=$bound" }
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}
