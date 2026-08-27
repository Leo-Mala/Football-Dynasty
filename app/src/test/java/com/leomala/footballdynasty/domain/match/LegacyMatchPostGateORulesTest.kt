package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchPostGateORulesTest {
    @Test
    fun `consumes exactly two bound seven draws and adds two`() {
        val random = QueueRandomSource(0, 6)
        val result = LegacyMatchPostGateORules.resolve(random)

        assertEquals(2, result.firstValue)
        assertEquals(8, result.comparisonValue)
        assertEquals(listOf(7, 7), random.bounds)
        assertEquals(2L, random.draws)
    }

    @Test
    fun `equality selects legacy E and stores first then first minus one`() {
        val result = LegacyMatchPostGateORules.resolve(QueueRandomSource(3, 3))

        assertEquals(LegacyMatchPostGateORules.SelectedSide.LEGACY_E, result.selectedSide)
        assertEquals(listOf(5, 4), result.legacyD0Values)
    }

    @Test
    fun `greater first value selects legacy E`() {
        val result = LegacyMatchPostGateORules.resolve(QueueRandomSource(6, 0))

        assertEquals(LegacyMatchPostGateORules.SelectedSide.LEGACY_E, result.selectedSide)
        assertEquals(listOf(8, 7), result.legacyD0Values)
    }

    @Test
    fun `smaller first value selects legacy F and stores first then first plus one`() {
        val result = LegacyMatchPostGateORules.resolve(QueueRandomSource(0, 6))

        assertEquals(LegacyMatchPostGateORules.SelectedSide.LEGACY_F, result.selectedSide)
        assertEquals(listOf(2, 3), result.legacyD0Values)
    }

    private class QueueRandomSource(vararg values: Int) : RandomSource {
        private val values = values.toMutableList()
        val bounds = mutableListOf<Int>()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            val value = values.removeAt(0)
            require(value in 0 until bound)
            bounds += bound
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}
