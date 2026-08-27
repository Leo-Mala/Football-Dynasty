package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchAutomaticSimulationRulesTest {
    @Test
    fun `automatic flow predraws both added times before minute RNG`() {
        val random = QueueRandomSource(0, 0, 6)
        val trace = mutableListOf<String>()
        var consumedMinuteProbe = false

        val result = LegacyMatchAutomaticSimulationRules.run<String>(
            random = random,
            runMinuteRule = { half, minute ->
                if (!consumedMinuteProbe) {
                    consumedMinuteProbe = true
                    trace += "minute-rng:${random.nextInt(7)}@$half:$minute"
                }
            },
            advanceR3 = { _, _ -> null },
            halftimeTransition = { half, minute -> trace += "halftime:$half:$minute" },
        )

        assertEquals(0, result.firstHalfAddedMinutes)
        assertEquals(1, result.secondHalfAddedMinutes)
        assertEquals("minute-rng:6@1:0", trace.first())
        assertEquals(listOf(3, 5, 7), random.bounds)
        assertEquals(3L, random.draws)
    }

    @Test
    fun `each automatic minute runs k before K and stamps non-null events`() {
        val random = QueueRandomSource(0, 0)
        val trace = mutableListOf<String>()

        val result = LegacyMatchAutomaticSimulationRules.run(
            random = random,
            runMinuteRule = { half, minute -> trace += "k:$half:$minute" },
            advanceR3 = { half, minute ->
                trace += "K:$half:$minute"
                when {
                    half == 1 && minute == 44 -> "first-end"
                    half == 2 && minute == 0 -> "second-start"
                    else -> null
                }
            },
            halftimeTransition = { half, minute -> trace += "j:$half:$minute" },
        )

        assertEquals(listOf("k:1:0", "K:1:0", "k:1:1", "K:1:1"), trace.take(4))
        val halftimeIndex = trace.indexOf("j:2:0")
        assertEquals("K:1:44", trace[halftimeIndex - 1])
        assertEquals("k:2:0", trace[halftimeIndex + 1])
        assertEquals(
            listOf(
                LegacyMatchAutomaticSimulationRules.StampedEvent("first-end", 44, 1),
                LegacyMatchAutomaticSimulationRules.StampedEvent("second-start", 0, 2),
            ),
            result.events,
        )
    }

    @Test
    fun `automatic half loop counts are exactly forty five plus predrawn added minutes`() {
        val random = QueueRandomSource(2, 4)
        var firstK = 0
        var firstR3 = 0
        var secondK = 0
        var secondR3 = 0
        val transitions = mutableListOf<Pair<Int, Int>>()

        val result = LegacyMatchAutomaticSimulationRules.run<String>(
            random = random,
            runMinuteRule = { half, _ ->
                if (half == 1) firstK++ else secondK++
            },
            advanceR3 = { half, _ ->
                if (half == 1) firstR3++ else secondR3++
                null
            },
            halftimeTransition = { half, minute -> transitions += half to minute },
        )

        assertEquals(2, result.firstHalfAddedMinutes)
        assertEquals(5, result.secondHalfAddedMinutes)
        assertEquals(47, firstK)
        assertEquals(47, firstR3)
        assertEquals(50, secondK)
        assertEquals(50, secondR3)
        assertEquals(listOf(2 to 0), transitions)
        assertEquals(emptyList<LegacyMatchAutomaticSimulationRules.StampedEvent<String>>(), result.events)
        assertEquals(listOf(3, 5), random.bounds)
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
