package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAnnualA0OrchestrationRulesTest {
    @Test
    fun `best a0 b pass tiers are one two three four`() {
        assertEquals(
            LegacyAnnualA0OrchestrationRules.BPasses(1, 1),
            LegacyAnnualA0OrchestrationRules.bestA0BPasses(0),
        )
        assertEquals(
            LegacyAnnualA0OrchestrationRules.BPasses(1, 1),
            LegacyAnnualA0OrchestrationRules.bestA0BPasses(1),
        )
        assertEquals(
            LegacyAnnualA0OrchestrationRules.BPasses(2, 2),
            LegacyAnnualA0OrchestrationRules.bestA0BPasses(2),
        )
        assertEquals(
            LegacyAnnualA0OrchestrationRules.BPasses(2, 2),
            LegacyAnnualA0OrchestrationRules.bestA0BPasses(5),
        )
        assertEquals(
            LegacyAnnualA0OrchestrationRules.BPasses(3, 3),
            LegacyAnnualA0OrchestrationRules.bestA0BPasses(6),
        )
        assertEquals(
            LegacyAnnualA0OrchestrationRules.BPasses(3, 3),
            LegacyAnnualA0OrchestrationRules.bestA0BPasses(10),
        )
        assertEquals(
            LegacyAnnualA0OrchestrationRules.BPasses(4, 4),
            LegacyAnnualA0OrchestrationRules.bestA0BPasses(11),
        )
    }

    @Test
    fun `best a0 a first loop calls I only when A exists`() {
        assertTrue(LegacyAnnualA0OrchestrationRules.bestA0AShouldCallI(true))
        assertFalse(LegacyAnnualA0OrchestrationRules.bestA0AShouldCallI(false))
    }

    @Test
    fun `best a0 a structural failures skip rng`() {
        val random = FixedIntRandomSource(99)
        assertFalse(LegacyAnnualA0OrchestrationRules.bestA0AShouldCallY(random, false, false, 3))
        assertFalse(LegacyAnnualA0OrchestrationRules.bestA0AShouldCallY(random, true, true, 3))
        assertFalse(LegacyAnnualA0OrchestrationRules.bestA0AShouldCallY(random, true, false, 2))
        assertEquals(0L, random.draws)
    }

    @Test
    fun `best a0 a uses strict greater than thirty gate`() {
        val fail = FixedIntRandomSource(30)
        val pass = FixedIntRandomSource(31)
        assertFalse(LegacyAnnualA0OrchestrationRules.bestA0AShouldCallY(fail, true, false, 3))
        assertTrue(LegacyAnnualA0OrchestrationRules.bestA0AShouldCallY(pass, true, false, 3))
        assertEquals(1L, fail.draws)
        assertEquals(1L, pass.draws)
    }

    @Test
    fun `best a0 c action preserves L0 and Q0 branch`() {
        assertEquals(
            LegacyAnnualA0OrchestrationRules.BestA0CAction.NONE,
            LegacyAnnualA0OrchestrationRules.bestA0CAction(false, false),
        )
        assertEquals(
            LegacyAnnualA0OrchestrationRules.BestA0CAction.CALL_H1,
            LegacyAnnualA0OrchestrationRules.bestA0CAction(true, true),
        )
        assertEquals(
            LegacyAnnualA0OrchestrationRules.BestA0CAction.SET_S0_FALSE,
            LegacyAnnualA0OrchestrationRules.bestA0CAction(true, false),
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
