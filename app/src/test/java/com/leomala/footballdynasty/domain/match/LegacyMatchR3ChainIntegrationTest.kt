package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchR3ChainIntegrationTest {
    @Test
    fun `J current and I zero consume weighted draws then goal without direct draw`() {
        val random = TraceRandomSource(
            doubles = listOf(0.0, 0.0),
        )

        val result = LegacyMatchR3AdvanceRules.advance(
            currentSide = 0,
            random = random,
            resolveJ = {
                LegacyMatchR3DecisionRules.resolveJ(
                    currentSide = 0,
                    metricYCurrent = 1.0,
                    metricYOpposite = 1.0,
                    legacyNeutralFlag = true,
                    legacyGlobalJValue = 0,
                    random = random,
                ).returnedValue
            },
            resolveI = {
                LegacyMatchR3DecisionRules.resolveI(
                    currentSide = 0,
                    metricUOpposite = 1.0,
                    metricZCurrent = 1.0,
                    legacyNeutralFlag = true,
                    legacyGlobalJValue = 0,
                    random = random,
                ).returnedValue
            },
            produceGoalEvent = {
                random.trace += "goal"
                "event"
            },
        )

        assertEquals(listOf("double", "double", "goal"), random.trace)
        assertEquals("event", result.event)
        assertEquals(LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_W_CURRENT, result.counterMutation)
        assertFalse(result.consumedDirectBound100Draw)
        assertEquals(2L, random.draws)
    }

    @Test
    fun `J current and I nonzero consume two weighted draws before direct bound one hundred`() {
        val random = TraceRandomSource(
            doubles = listOf(0.0, 0.75),
            ints = listOf(49),
        )

        val result = LegacyMatchR3AdvanceRules.advance(
            currentSide = 0,
            random = random,
            resolveJ = {
                LegacyMatchR3DecisionRules.resolveJ(
                    currentSide = 0,
                    metricYCurrent = 1.0,
                    metricYOpposite = 1.0,
                    legacyNeutralFlag = true,
                    legacyGlobalJValue = 0,
                    random = random,
                ).returnedValue
            },
            resolveI = {
                LegacyMatchR3DecisionRules.resolveI(
                    currentSide = 0,
                    metricUOpposite = 1.0,
                    metricZCurrent = 1.0,
                    legacyNeutralFlag = true,
                    legacyGlobalJValue = 0,
                    random = random,
                ).returnedValue
            },
            produceGoalEvent = { error("must not produce a goal event") },
        )

        assertEquals(listOf("double", "double", "int:100"), random.trace)
        assertNull(result.event)
        assertEquals(LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_Q0_OPPOSITE, result.counterMutation)
        assertTrue(result.consumedDirectBound100Draw)
        assertEquals(3L, random.draws)
    }

    @Test
    fun `J mismatch skips I weighted draw and goes directly to bound one hundred`() {
        val random = TraceRandomSource(
            doubles = listOf(0.9),
            ints = listOf(50),
        )

        val result = LegacyMatchR3AdvanceRules.advance(
            currentSide = 0,
            random = random,
            resolveJ = {
                LegacyMatchR3DecisionRules.resolveJ(
                    currentSide = 0,
                    metricYCurrent = 1.0,
                    metricYOpposite = 1.0,
                    legacyNeutralFlag = true,
                    legacyGlobalJValue = 0,
                    random = random,
                ).returnedValue
            },
            resolveI = {
                error("I must be short-circuited when J returns the opposite side")
            },
            produceGoalEvent = { error("must not produce a goal event") },
        )

        assertEquals(listOf("double", "int:100"), random.trace)
        assertNull(result.event)
        assertEquals(LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_A0_CURRENT, result.counterMutation)
        assertTrue(result.consumedDirectBound100Draw)
        assertEquals(2L, random.draws)
    }

    private class TraceRandomSource(
        doubles: List<Double> = emptyList(),
        ints: List<Int> = emptyList(),
    ) : RandomSource {
        private val doubleQueue = doubles.toMutableList()
        private val intQueue = ints.toMutableList()
        val trace = mutableListOf<String>()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            check(intQueue.isNotEmpty()) { "No queued integer RNG value for bound=$bound" }
            val value = intQueue.removeAt(0)
            require(value in 0 until bound) { "value=$value bound=$bound" }
            trace += "int:$bound"
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")

        override fun nextDouble(): Double {
            check(doubleQueue.isNotEmpty()) { "No queued double RNG value" }
            val value = doubleQueue.removeAt(0)
            require(value >= 0.0 && value < 1.0) { "value=$value" }
            trace += "double"
            draws++
            return value
        }
    }
}
