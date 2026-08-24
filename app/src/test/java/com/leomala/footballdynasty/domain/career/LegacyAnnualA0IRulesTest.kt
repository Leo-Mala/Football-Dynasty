package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAnnualA0IRulesTest {
    @Test
    fun `club gate matches J and P0 branches`() {
        assertTrue(LegacyAnnualA0IRules.clubEligible(false, 1, 4))
        assertFalse(LegacyAnnualA0IRules.clubEligible(false, 1, 5))
        assertTrue(LegacyAnnualA0IRules.clubEligible(false, 0, 3))
        assertFalse(LegacyAnnualA0IRules.clubEligible(false, 0, 4))
        assertFalse(LegacyAnnualA0IRules.clubEligible(true, 1, 0))
    }

    @Test
    fun `player structural failures consume no rng`() {
        val random = FixedIntRandomSource(99)

        assertFalse(LegacyAnnualA0IRules.playerEligible(random, 50, 20, true))
        assertFalse(LegacyAnnualA0IRules.playerEligible(random, 80, 31, true))
        assertFalse(LegacyAnnualA0IRules.playerEligible(random, 80, 20, false))
        assertEquals(0L, random.draws)
    }

    @Test
    fun `player gate uses strict greater than twenty five`() {
        val fail = FixedIntRandomSource(25)
        val pass = FixedIntRandomSource(26)

        assertFalse(LegacyAnnualA0IRules.playerEligible(fail, 51, 30, true))
        assertTrue(LegacyAnnualA0IRules.playerEligible(pass, 51, 30, true))
        assertEquals(1L, fail.draws)
        assertEquals(1L, pass.draws)
    }

    @Test
    fun `mode and fallback order match smali`() {
        assertEquals(2, LegacyAnnualA0IRules.BEST_F_MODE)
        assertEquals(
            listOf(
                LegacyAnnualA0IRules.RelocationAttempt.N_PRIMARY,
                LegacyAnnualA0IRules.RelocationAttempt.O_FALLBACK,
            ),
            LegacyAnnualA0IRules.attemptOrder,
        )
    }

    private class FixedIntRandomSource(
        vararg values: Int,
    ) : RandomSource {
        private val iterator = values.iterator()

        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            val value = iterator.nextInt()
            require(value in 0 until bound)
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}
