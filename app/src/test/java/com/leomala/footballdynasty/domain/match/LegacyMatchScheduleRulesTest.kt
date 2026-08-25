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
        // A second legacy-style shuffle operates on the already-mutated static pool, not a hidden reset.
        org.junit.Assert.assertNotEquals(firstCoreOrder, pools.coreMinutes)
        org.junit.Assert.assertNotEquals(firstSchedule.core, secondSchedule.core)
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
}
