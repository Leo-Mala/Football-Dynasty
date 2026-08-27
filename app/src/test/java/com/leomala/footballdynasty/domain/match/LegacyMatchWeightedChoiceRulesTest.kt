package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchWeightedChoiceRulesTest {
    @Test
    fun `exact boundary uses strict less than and advances to next bucket`() {
        val random = DoubleQueueRandomSource(0.5)

        val index = LegacyMatchWeightedChoiceRules.selectIndex(
            doubleArrayOf(1.0, 1.0),
            doubleArrayOf(1.0, 1.0),
            random,
        )

        assertEquals(1, index)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `zero draw selects first positive bucket`() {
        val index = LegacyMatchWeightedChoiceRules.selectIndex(
            doubleArrayOf(1.0, 1.0),
            doubleArrayOf(1.0, 1.0),
            DoubleQueueRandomSource(0.0),
        )

        assertEquals(0, index)
    }

    @Test
    fun `multipliers are applied before total and cumulative sums`() {
        val index = LegacyMatchWeightedChoiceRules.selectIndex(
            doubleArrayOf(1.0, 1.0, 1.0),
            doubleArrayOf(0.0, 1.0, 3.0),
            DoubleQueueRandomSource(0.40),
        )

        assertEquals(2, index)
    }

    @Test
    fun `all zero products return legacy length sentinel`() {
        val random = DoubleQueueRandomSource(0.0)

        val index = LegacyMatchWeightedChoiceRules.selectIndex(
            doubleArrayOf(2.0, 3.0),
            doubleArrayOf(0.0, 0.0),
            random,
        )

        assertEquals(2, index)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `empty arrays still consume one nextDouble and return zero length sentinel`() {
        val random = DoubleQueueRandomSource(0.25)

        val index = LegacyMatchWeightedChoiceRules.selectIndex(
            doubleArrayOf(),
            doubleArrayOf(),
            random,
        )

        assertEquals(0, index)
        assertEquals(1L, random.draws)
    }

    private class DoubleQueueRandomSource(vararg values: Double) : RandomSource {
        private val queue = values.toMutableList()
        override var draws: Long = 0
            private set

        override fun nextDouble(): Double {
            check(queue.isNotEmpty()) { "No queued nextDouble value" }
            val value = queue.removeAt(0)
            require(value >= 0.0 && value < 1.0) { "value=$value" }
            draws++
            return value
        }

        override fun nextInt(bound: Int): Int = error("not used")
        override fun nextBoolean(): Boolean = error("not used")
    }
}
