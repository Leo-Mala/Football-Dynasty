package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchAutomaticFlowRulesTest {
    @Test
    fun `P0 is evaluated only after both automatic half loops complete`() {
        val random = QueueRandomSource(0, 0)
        val trace = mutableListOf<String>()

        val result = LegacyMatchAutomaticFlowRules.run<String>(
            random = random,
            runMinuteRule = { half, minute -> trace += "k:$half:$minute" },
            advanceR3 = { half, minute -> trace += "K:$half:$minute"; null },
            halftimeTransition = { half, minute -> trace += "j:$half:$minute" },
            legacyZFlag = true,
            legacyA0Flag = true,
            resolveP0 = { trace += "P0"; true },
        )

        // The minimum second-half added time is one minute, so 0..45 executes before P0.
        assertEquals("K:2:45", trace[trace.lastIndex - 1])
        assertEquals("P0", trace.last())
        assertTrue(result.postSimulation.evaluatedP0)
        assertTrue(result.postSimulation.invokeLegacyO)
        assertEquals(
            listOf(
                LegacyMatchAutomaticPostSimulationRules.Operation.INVOKE_LEGACY_O,
                LegacyMatchAutomaticPostSimulationRules.Operation.CLEAR_BOTH_CLUB_FLAGS,
            ),
            result.postSimulation.operations,
        )
    }

    @Test
    fun `short circuited post gate still returns final clear plan without extra RNG`() {
        val random = QueueRandomSource(2, 4)
        var p0Calls = 0

        val result = LegacyMatchAutomaticFlowRules.run<String>(
            random = random,
            runMinuteRule = { _, _ -> },
            advanceR3 = { _, _ -> null },
            halftimeTransition = { _, _ -> },
            legacyZFlag = false,
            legacyA0Flag = true,
            resolveP0 = { p0Calls++; true },
        )

        assertFalse(result.postSimulation.evaluatedP0)
        assertFalse(result.postSimulation.invokeLegacyO)
        assertEquals(0, p0Calls)
        assertEquals(
            listOf(LegacyMatchAutomaticPostSimulationRules.Operation.CLEAR_BOTH_CLUB_FLAGS),
            result.postSimulation.operations,
        )
        assertEquals(listOf(3, 5), random.bounds)
        assertEquals(2L, random.draws)
        assertEquals(47, 45 + result.simulation.firstHalfAddedMinutes)
        assertEquals(50, 45 + result.simulation.secondHalfAddedMinutes)
    }

    @Test
    fun `events remain stamped before post simulation resolution`() {
        val random = QueueRandomSource(0, 0)
        val trace = mutableListOf<String>()

        val result = LegacyMatchAutomaticFlowRules.run(
            random = random,
            runMinuteRule = { _, _ -> },
            advanceR3 = { half, minute ->
                if (half == 2 && minute == 44) {
                    trace += "event"
                    "late-event"
                } else {
                    null
                }
            },
            halftimeTransition = { _, _ -> },
            legacyZFlag = true,
            legacyA0Flag = true,
            resolveP0 = { trace += "P0"; false },
        )

        assertEquals(listOf("event", "P0"), trace)
        assertEquals(
            listOf(LegacyMatchAutomaticSimulationRules.StampedEvent("late-event", 44, 2)),
            result.simulation.events,
        )
        assertTrue(result.postSimulation.evaluatedP0)
        assertFalse(result.postSimulation.invokeLegacyO)
    }

    private class QueueRandomSource(vararg values: Int) : RandomSource {
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
