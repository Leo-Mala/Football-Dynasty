package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchScheduleRulesTest {
    @Test
    fun `zero source preserves exact pool sizes bounds and disabled optional slots`() {
        val random = BoundAwareRandomSource(defaultValue = 0)
        val pools = LegacyMatchScheduleRules.Pools.initial()

        val schedule = LegacyMatchScheduleRules.initialize(random, pools)

        assertEquals(listOf(20, 21, -1), schedule.core[0])
        assertEquals(listOf(22, 23, -1), schedule.core[1])
        assertEquals(listOf(37, 38, -1, -1), schedule.auxiliary[0])
        assertEquals(listOf(39, 40, -1, -1), schedule.auxiliary[1])
        assertEquals(36L, random.draws)
        assertEquals(7, random.bounds.count { it == 100 })
        assertEquals(20, pools.coreMinutes.size)
        assertEquals(7, pools.lateMinutes.size)
        assertEquals(5, pools.endMinutes.size)
    }

    @Test
    fun `direct gates select early pool and all optional schedule slots at exact thresholds`() {
        val random = BoundAwareRandomSource(
            defaultValue = 0,
            valuesFor100 = mutableListOf(91, 31, 31, 21, 51, 21, 51),
        )
        val pools = LegacyMatchScheduleRules.Pools.initial()

        val schedule = LegacyMatchScheduleRules.initialize(random, pools)

        assertEquals(listOf(20, 21, 24), schedule.core[0])
        assertEquals(listOf(22, 23, 25), schedule.core[1])
        assertEquals(listOf(6, 7, 44, 45), schedule.auxiliary[0])
        assertEquals(listOf(8, 9, 46, 47), schedule.auxiliary[1])
        assertEquals(40L, random.draws)
        assertEquals(7, random.bounds.count { it == 100 })
    }

    @Test
    fun `pool order is explicit mutable state across match initializations`() {
        val pools = LegacyMatchScheduleRules.Pools.initial()
        val first = BoundAwareRandomSource(defaultValue = 0)
        val second = BoundAwareRandomSource(defaultValue = 0)

        val firstSchedule = LegacyMatchScheduleRules.initialize(first, pools)
        val firstCoreOrder = pools.coreMinutes.toList()
        val secondSchedule = LegacyMatchScheduleRules.initialize(second, pools)

        assertEquals(listOf(20, 21, -1), firstSchedule.core[0])
        org.junit.Assert.assertNotEquals(firstCoreOrder, pools.coreMinutes)
        org.junit.Assert.assertNotEquals(firstSchedule.core, secondSchedule.core)
    }

    @Test
    fun `Q0 first-half added time keeps exact zero-to-two range and bound`() {
        val random = ExactQueueRandomSource(2)

        val added = LegacyMatchScheduleRules.drawAutomaticFirstHalfAddedMinutes(random)

        assertEquals(2, added)
        assertEquals(listOf(3), random.bounds)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `Q0 second-half added time keeps exact one-to-five range and bound`() {
        val random = ExactQueueRandomSource(4)

        val added = LegacyMatchScheduleRules.drawAutomaticSecondHalfAddedMinutes(random)

        assertEquals(5, added)
        assertEquals(listOf(5), random.bounds)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `Q0 added-time helpers preserve consecutive bound-three then bound-five draws`() {
        val random = ExactQueueRandomSource(0, 0)

        val firstHalf = LegacyMatchScheduleRules.drawAutomaticFirstHalfAddedMinutes(random)
        val secondHalf = LegacyMatchScheduleRules.drawAutomaticSecondHalfAddedMinutes(random)

        assertEquals(0, firstHalf)
        assertEquals(1, secondHalf)
        assertEquals(listOf(3, 5), random.bounds)
        assertEquals(2L, random.draws)
    }

    @Test
    fun `Q0 structural flow pre-draws both added times before first-half RNG`() {
        val random = ExactQueueRandomSource(2, 4, 6)
        val trace = mutableListOf<String>()

        val landmarks = LegacyMatchScheduleRules.runAutomaticFlowLandmarks(
            random = random,
            simulateFirstHalf = { added ->
                trace += "first:$added"
                trace += "first-rng:${random.nextInt(7)}"
            },
            halftimeTransition = { half, minute -> trace += "halftime:$half:$minute" },
            simulateSecondHalf = { added -> trace += "second:$added" },
        )

        assertEquals(listOf("first:2", "first-rng:6", "halftime:2:0", "second:5"), trace)
        assertEquals(LegacyMatchScheduleRules.AutomaticFlowLandmarks(2, 5), landmarks)
        assertEquals(listOf(3, 5, 7), random.bounds)
        assertEquals(3L, random.draws)
    }

    @Test
    fun `Q0 halftime RNG occurs after both added-time draws`() {
        val random = ExactQueueRandomSource(1, 3, 8)
        val trace = mutableListOf<String>()

        val landmarks = LegacyMatchScheduleRules.runAutomaticFlowLandmarks(
            random = random,
            simulateFirstHalf = { added -> trace += "first:$added" },
            halftimeTransition = { half, minute ->
                trace += "halftime:$half:$minute"
                trace += "halftime-rng:${random.nextInt(9)}"
            },
            simulateSecondHalf = { added -> trace += "second:$added" },
        )

        assertEquals(listOf("first:1", "halftime:2:0", "halftime-rng:8", "second:4"), trace)
        assertEquals(LegacyMatchScheduleRules.AutomaticFlowLandmarks(1, 4), landmarks)
        assertEquals(listOf(3, 5, 9), random.bounds)
        assertEquals(3L, random.draws)
    }

    @Test
    fun `Q0 second-half callback RNG remains after both added-time draws`() {
        val random = ExactQueueRandomSource(1, 3, 10)
        val trace = mutableListOf<String>()

        val landmarks = LegacyMatchScheduleRules.runAutomaticFlowLandmarks(
            random = random,
            simulateFirstHalf = { added -> trace += "first:$added" },
            halftimeTransition = { half, minute -> trace += "halftime:$half:$minute" },
            simulateSecondHalf = { added ->
                trace += "second:$added"
                trace += "second-rng:${random.nextInt(11)}"
            },
        )

        assertEquals(listOf("first:1", "halftime:2:0", "second:4", "second-rng:10"), trace)
        assertEquals(LegacyMatchScheduleRules.AutomaticFlowLandmarks(1, 4), landmarks)
        assertEquals(listOf(3, 5, 11), random.bounds)
        assertEquals(3L, random.draws)
    }

    @Test
    fun `Q0 flow result reuses pre-drawn values without extra RNG`() {
        val random = ExactQueueRandomSource(0, 4)

        val landmarks = LegacyMatchScheduleRules.runAutomaticFlowLandmarks(
            random = random,
            simulateFirstHalf = {},
            halftimeTransition = { _, _ -> },
            simulateSecondHalf = {},
        )

        assertEquals(LegacyMatchScheduleRules.AutomaticFlowLandmarks(0, 5), landmarks)
        assertEquals(listOf(3, 5), random.bounds)
        assertEquals(2L, random.draws)
    }

    @Test
    fun `Q0 half minute loop is zero through forty-four plus added time`() {
        assertEquals(0..44, LegacyMatchScheduleRules.automaticHalfMinutes(0))
        assertEquals(0..46, LegacyMatchScheduleRules.automaticHalfMinutes(2))
        assertEquals(0..49, LegacyMatchScheduleRules.automaticHalfMinutes(5))
        assertEquals(45, LegacyMatchScheduleRules.automaticHalfMinutes(0).count())
        assertEquals(50, LegacyMatchScheduleRules.automaticHalfMinutes(5).count())
    }

    private class BoundAwareRandomSource(
        private val defaultValue: Int,
        private val valuesFor100: MutableList<Int> = mutableListOf(),
    ) : RandomSource {
        val bounds = mutableListOf<Int>()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            val value = if (bound == 100 && valuesFor100.isNotEmpty()) valuesFor100.removeAt(0) else defaultValue
            require(value in 0 until bound) { "value=$value bound=$bound" }
            bounds += bound
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }

    private class ExactQueueRandomSource(vararg values: Int) : RandomSource {
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
