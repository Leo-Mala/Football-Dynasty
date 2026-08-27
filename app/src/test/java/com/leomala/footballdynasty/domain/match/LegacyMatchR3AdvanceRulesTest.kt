package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchR3AdvanceRulesTest {
    @Test
    fun `matching J and zero I produces event without direct bound one hundred draw`() {
        val random = IntQueueRandomSource()
        val trace = mutableListOf<String>()

        val result = LegacyMatchR3AdvanceRules.advance(
            currentSide = 0,
            random = random,
            resolveJ = { trace += "J"; 0 },
            resolveI = { trace += "I"; 0 },
            produceGoalEvent = { trace += "goal"; "event" },
        )

        assertEquals(listOf("J", "I", "goal"), trace)
        assertEquals(LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_W_CURRENT, result.counterMutation)
        assertEquals(0, result.counterSide)
        assertEquals("event", result.event)
        assertEquals(1, result.nextSide)
        assertTrue(result.incrementTick)
        assertFalse(result.consumedDirectBound100Draw)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `J mismatch short circuits I and draw forty nine mutates opposite q0`() {
        val random = IntQueueRandomSource(49)
        var iCalls = 0

        val result = LegacyMatchR3AdvanceRules.advance(
            currentSide = 0,
            random = random,
            resolveJ = { 1 },
            resolveI = { iCalls++; 0 },
            produceGoalEvent = { error("must not run") },
        )

        assertEquals(0, iCalls)
        assertEquals(LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_Q0_OPPOSITE, result.counterMutation)
        assertEquals(1, result.counterSide)
        assertNull(result.event)
        assertEquals(listOf(100), random.bounds)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `direct draw fifty uses current a0 counter`() {
        val random = IntQueueRandomSource(50)

        val result = LegacyMatchR3AdvanceRules.advance(
            currentSide = 1,
            random = random,
            resolveJ = { 0 },
            resolveI = { error("must short circuit") },
            produceGoalEvent = { error("must not run") },
        )

        assertEquals(LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_A0_CURRENT, result.counterMutation)
        assertEquals(1, result.counterSide)
        assertEquals(0, result.nextSide)
        assertTrue(result.consumedDirectBound100Draw)
    }

    @Test
    fun `matching J with nonzero I takes same direct fifty fifty branch`() {
        val random = IntQueueRandomSource(0)
        val trace = mutableListOf<String>()

        val result = LegacyMatchR3AdvanceRules.advance(
            currentSide = 1,
            random = random,
            resolveJ = { trace += "J"; 1 },
            resolveI = { trace += "I"; 1 },
            produceGoalEvent = { error("must not run") },
        )

        assertEquals(listOf("J", "I"), trace)
        assertEquals(LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_Q0_OPPOSITE, result.counterMutation)
        assertEquals(0, result.counterSide)
        assertNull(result.event)
        assertEquals(listOf(100), random.bounds)
    }

    @Test
    fun `side alternation is exact for zero and one`() {
        val zero = LegacyMatchR3AdvanceRules.advance(
            0, IntQueueRandomSource(99), { 1 }, { 0 }, { "x" },
        )
        val one = LegacyMatchR3AdvanceRules.advance(
            1, IntQueueRandomSource(99), { 0 }, { 0 }, { "x" },
        )

        assertEquals(1, zero.nextSide)
        assertEquals(0, one.nextSide)
    }

    private class IntQueueRandomSource(vararg values: Int) : RandomSource {
        private val queue = values.toMutableList()
        val bounds = mutableListOf<Int>()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            check(queue.isNotEmpty()) { "No queued RNG value for bound=$bound" }
            val value = queue.removeAt(0)
            require(value in 0 until bound) { "value=$value bound=$bound" }
            bounds += bound
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}
