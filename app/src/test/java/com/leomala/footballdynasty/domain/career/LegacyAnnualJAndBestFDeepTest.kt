package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAnnualJAndBestFDeepTest {
    @Test
    fun `best a0 j modern position shuffle consumes four explicit draws`() {
        val random = FixedIntRandomSource(0, 0, 0, 0)
        val selected = LegacyAnnualSelectionRules.bestA0JSelectOverloadedPosition(
            random = random,
            positionCounts = intArrayOf(0, 0, 0, 0, 0),
            highPass = false,
        )

        assertNull(selected)
        assertEquals(4L, random.draws)
    }

    @Test
    fun `best a0 j site one draw remains unconditional after empty position scan`() {
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
    fun `best f mode two predicate is capacity plus p0 floor`() {
        assertTrue(LegacyAnnualSelectionRules.bestFMode2CandidateEligible(29, 4))
        assertTrue(LegacyAnnualSelectionRules.bestFMode2CandidateEligible(0, 5))
        assertFalse(LegacyAnnualSelectionRules.bestFMode2CandidateEligible(30, 4))
        assertFalse(LegacyAnnualSelectionRules.bestFMode2CandidateEligible(29, 3))
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
