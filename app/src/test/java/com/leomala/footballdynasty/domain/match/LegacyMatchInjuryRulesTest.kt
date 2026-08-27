package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchInjuryRulesTest {
    @Test
    fun `young player still consumes all three legacy RNG draws when duration stays zero`() {
        val random = QueueRandomSource(0, 19, 10)

        val result = LegacyMatchInjuryRules.resolve(age = 20, energy = 0, skill = 80, random = random)

        assertEquals(0, result.durationDays)
        assertEquals(80, result.updatedSkill)
        assertFalse(result.shouldSetInjuryUntil)
        assertEquals(LegacyMatchEventType.INJURY, result.eventType)
        assertEquals(listOf(14, 20, 100), random.bounds)
        assertEquals(3L, random.draws)
    }

    @Test
    fun `age twenty one starts applying the energy modifier and plus one`() {
        val random = QueueRandomSource(0, 0, 10)

        val result = LegacyMatchInjuryRules.resolve(age = 21, energy = 9, skill = 80, random = random)

        assertEquals(6, result.durationDays)
        assertTrue(result.shouldSetInjuryUntil)
    }

    @Test
    fun `energy thresholds are strict below ten and below fifty`() {
        fun duration(energy: Int): Int = LegacyMatchInjuryRules.resolve(
            age = 21,
            energy = energy,
            skill = 80,
            random = QueueRandomSource(0, 0, 10),
        ).durationDays

        assertEquals(6, duration(9))
        assertEquals(2, duration(10))
        assertEquals(2, duration(49))
        assertEquals(1, duration(50))
    }

    @Test
    fun `age boundary modifiers follow the recovered legacy buckets`() {
        fun duration(age: Int): Int = LegacyMatchInjuryRules.resolve(
            age = age,
            energy = 50,
            skill = 80,
            random = QueueRandomSource(0, 0, 10),
        ).durationDays

        assertEquals(0, duration(20))
        assertEquals(1, duration(21))
        assertEquals(1, duration(25))
        assertEquals(2, duration(26))
        assertEquals(2, duration(30))
        assertEquals(3, duration(31))
        assertEquals(3, duration(35))
        assertEquals(5, duration(36))
        assertEquals(5, duration(40))
        assertEquals(5, duration(41))
        assertEquals(5, duration(45))
        assertEquals(15, duration(46))
    }

    @Test
    fun `age forty six keeps the extra ten days plus the older-age draw`() {
        val random = QueueRandomSource(13, 19, 10)

        val result = LegacyMatchInjuryRules.resolve(age = 46, energy = 9, skill = 80, random = random)

        assertEquals(52, result.durationDays)
        assertEquals(listOf(14, 20, 100), random.bounds)
    }

    @Test
    fun `age thirty five reduces skill by five but exact zero is not clamped`() {
        val random = QueueRandomSource(0, 0, 10)

        val result = LegacyMatchInjuryRules.resolve(age = 35, energy = 50, skill = 5, random = random)

        assertEquals(0, result.updatedSkill)
    }

    @Test
    fun `negative skill after age reduction is clamped to one`() {
        val random = QueueRandomSource(0, 0, 10)

        val result = LegacyMatchInjuryRules.resolve(age = 35, energy = 50, skill = 4, random = random)

        assertEquals(1, result.updatedSkill)
    }

    @Test
    fun `players below thirty five do not lose skill`() {
        val random = QueueRandomSource(0, 0, 10)

        val result = LegacyMatchInjuryRules.resolve(age = 34, energy = 50, skill = 4, random = random)

        assertEquals(4, result.updatedSkill)
    }

    @Test
    fun `severity roll exactly one adds seventy days`() {
        val result = LegacyMatchInjuryRules.resolve(
            age = 20,
            energy = 50,
            skill = 80,
            random = QueueRandomSource(0, 0, 1),
        )

        assertEquals(70, result.durationDays)
    }

    @Test
    fun `severity rolls zero two and three add forty days`() {
        for (roll in listOf(0, 2, 3)) {
            val result = LegacyMatchInjuryRules.resolve(
                age = 20,
                energy = 50,
                skill = 80,
                random = QueueRandomSource(0, 0, roll),
            )
            assertEquals("roll=$roll", 40, result.durationDays)
        }
    }

    @Test
    fun `severity rolls four through nine add twenty days at both boundaries`() {
        for (roll in listOf(4, 9)) {
            val result = LegacyMatchInjuryRules.resolve(
                age = 20,
                energy = 50,
                skill = 80,
                random = QueueRandomSource(0, 0, roll),
            )
            assertEquals("roll=$roll", 20, result.durationDays)
        }
    }

    @Test
    fun `severity roll ten adds nothing`() {
        val result = LegacyMatchInjuryRules.resolve(
            age = 20,
            energy = 50,
            skill = 80,
            random = QueueRandomSource(0, 0, 10),
        )

        assertEquals(0, result.durationDays)
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
