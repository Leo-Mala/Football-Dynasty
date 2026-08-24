package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.career.LegacyAnnualSelectionRules.BestA0IRoute
import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAnnualDeepSelectionTest {
    @Test
    fun `best a0 i club prefilter matches smali branches`() {
        assertTrue(LegacyAnnualSelectionRules.bestA0IClubEligible(false, legacyJ = 0, legacyP0 = 3))
        assertFalse(LegacyAnnualSelectionRules.bestA0IClubEligible(false, legacyJ = 0, legacyP0 = 4))
        assertTrue(LegacyAnnualSelectionRules.bestA0IClubEligible(false, legacyJ = 1, legacyP0 = 4))
        assertFalse(LegacyAnnualSelectionRules.bestA0IClubEligible(false, legacyJ = 1, legacyP0 = 5))
        assertFalse(LegacyAnnualSelectionRules.bestA0IClubEligible(true, legacyJ = 0, legacyP0 = 0))
    }

    @Test
    fun `best a0 i deterministic filters short circuit rng`() {
        val lowOverall = FixedIntRandomSource(99)
        assertFalse(LegacyAnnualSelectionRules.bestA0IPlayerEligible(lowOverall, 50, 20, true))
        assertEquals(0L, lowOverall.draws)

        val legacyWBoundary = FixedIntRandomSource(99)
        assertFalse(LegacyAnnualSelectionRules.bestA0IPlayerEligible(legacyWBoundary, 80, 31, true))
        assertEquals(0L, legacyWBoundary.draws)

        val missingO0 = FixedIntRandomSource(99)
        assertFalse(LegacyAnnualSelectionRules.bestA0IPlayerEligible(missingO0, 80, 20, false))
        assertEquals(0L, missingO0.draws)
    }

    @Test
    fun `best a0 i threshold is strictly greater than twenty five`() {
        val rejected = FixedIntRandomSource(25)
        val accepted = FixedIntRandomSource(26)

        assertFalse(LegacyAnnualSelectionRules.bestA0IPlayerEligible(rejected, 51, 30, true))
        assertTrue(LegacyAnnualSelectionRules.bestA0IPlayerEligible(accepted, 51, 30, true))
        assertEquals(1L, rejected.draws)
        assertEquals(1L, accepted.draws)
        assertEquals(BestA0IRoute.MODE_2_N_THEN_O_FALLBACK, LegacyAnnualSelectionRules.bestA0IRoute())
    }

    @Test
    fun `best a0 j modern position shuffle and site one draw are explicit`() {
        val random = FixedIntRandomSource(0, 0, 0, 0, 11)
        val selected = LegacyAnnualSelectionRules.bestA0JSelectOverloadedPosition(
            random = random,
            positionCounts = intArrayOf(0, 0, 0, 0, 0),
            highPass = false,
        )
        val site1 = LegacyAnnualSelectionRules.bestA0JInitialPlayerGate(random)

        assertNull(selected)
        assertTrue(site1)
        assertEquals(5L, random.draws)
    }

    @Test
    fun `best c0 z0 preserves minimum maximum roster and position caps`() {
        val counts = intArrayOf(0, 0, 0, 10, 0)
        assertTrue(
            LegacyAnnualSelectionRules.bestC0Z0(
                rosterSize = 29,
                targetR0 = false,
                targetO = 0,
                targetP0 = 3,
                subjectOverall = 50,
                subjectPosition = 3,
                enforcePositionCaps = true,
                positionCounts = counts,
            ),
        )
        assertFalse(LegacyAnnualSelectionRules.bestC0Z0(29, false, 0, 3, 39, 3, true, counts))
        assertFalse(LegacyAnnualSelectionRules.bestC0Z0(29, false, 0, 3, 86, 3, true, counts))
        assertFalse(
            LegacyAnnualSelectionRules.bestC0Z0(
                29,
                false,
                0,
                3,
                50,
                3,
                true,
                intArrayOf(0, 0, 0, 11, 0),
            ),
        )
        assertFalse(LegacyAnnualSelectionRules.bestC0Z0(30, false, 0, 3, 50, 3, false, counts))
    }

    @Test
    fun `best c0 a1 has minimum and roster checks but no maximum gate`() {
        assertTrue(LegacyAnnualSelectionRules.bestC0A1(29, false, 0, 3, 100))
        assertFalse(LegacyAnnualSelectionRules.bestC0A1(29, false, 0, 3, 39))
        assertFalse(LegacyAnnualSelectionRules.bestC0A1(30, false, 0, 3, 100))
    }

    @Test
    fun `best f q range preserves mode and low overall overrides`() {
        assertEquals(
            LegacyAnnualSelectionRules.BestFQRange(2, 4),
            LegacyAnnualSelectionRules.bestFQRange(0, 3, 2, 3, 50, 5),
        )
        assertEquals(
            LegacyAnnualSelectionRules.BestFQRange(1, 1),
            LegacyAnnualSelectionRules.bestFQRange(1, 3, 0, 4, 40, 5),
        )
        assertEquals(
            LegacyAnnualSelectionRules.BestFQRange(1, 2),
            LegacyAnnualSelectionRules.bestFQRange(1, 3, 1, 4, 40, 5),
        )
        assertEquals(
            LegacyAnnualSelectionRules.BestFQRange(0, 5),
            LegacyAnnualSelectionRules.bestFQRange(1, 3, 0, 4, 20, 5),
        )
    }

    @Test
    fun `best f mode two and fallback predicates retain capacity rules`() {
        assertTrue(LegacyAnnualSelectionRules.bestFMode2CandidateEligible(29, 4))
        assertFalse(LegacyAnnualSelectionRules.bestFMode2CandidateEligible(30, 4))
        assertFalse(LegacyAnnualSelectionRules.bestFMode2CandidateEligible(29, 3))

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

        override fun nextBoolean(): Boolean = error("not used by this characterization")
        override fun nextDouble(): Double = error("not used by this characterization")
    }
}
