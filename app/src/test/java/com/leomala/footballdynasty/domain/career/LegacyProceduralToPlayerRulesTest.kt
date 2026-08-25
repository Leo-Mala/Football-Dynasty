package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyProceduralToPlayerRulesTest {
    @Test
    fun `club band preserves r0 and p0 tables plus J zero bonus`() {
        assertEquals(27, LegacyProceduralToPlayerRules.clubBand(true, 1, 0, 0))
        assertEquals(17, LegacyProceduralToPlayerRules.clubBand(true, 2, 0, 4))
        assertEquals(25, LegacyProceduralToPlayerRules.clubBand(false, 0, 5, 0))
        assertEquals(7, LegacyProceduralToPlayerRules.clubBand(false, 0, 2, 3))
        assertEquals(5, LegacyProceduralToPlayerRules.clubBand(false, 0, 1, 3))
    }

    @Test
    fun `conversion always consumes nextInt five and high n consumes nextInt ten`() {
        val high = QueueRandomSource(4, 9)
        val highValue = LegacyProceduralToPlayerRules.convertedLegacyN(high, true, 1, 0, 1, 9, 100)
        assertEquals(44, highValue)
        assertEquals(listOf(5, 10), high.bounds)
        val low = QueueRandomSource(4)
        LegacyProceduralToPlayerRules.convertedLegacyN(low, true, 1, 0, 1, 8, 100)
        assertEquals(listOf(5), low.bounds)
    }

    @Test
    fun `draft b true flag path consumes only nextInt three`() {
        val random = QueueRandomSource(1)
        val flags = LegacyProceduralToPlayerRules.flags(random, true)
        assertTrue(flags.setO0)
        assertFalse(flags.setM)
        assertEquals(listOf(3), random.bounds)
    }

    @Test
    fun `draft b false stops after nextInt two hundred success`() {
        val random = QueueRandomSource(1)
        val flags = LegacyProceduralToPlayerRules.flags(random, false)
        assertTrue(flags.setO0)
        assertFalse(flags.setM)
        assertEquals(listOf(200), random.bounds)
    }

    @Test
    fun `draft b false falls through to nextInt three hundred`() {
        val random = QueueRandomSource(0, 1)
        val flags = LegacyProceduralToPlayerRules.flags(random, false)
        assertTrue(flags.setO0)
        assertTrue(flags.setM)
        assertEquals(listOf(200, 300), random.bounds)
    }

    @Test
    fun `o0 post flag raises only values below eight`() {
        assertEquals(8, LegacyProceduralToPlayerRules.finalLegacyNAfterFlags(4, true))
        assertEquals(9, LegacyProceduralToPlayerRules.finalLegacyNAfterFlags(9, true))
        assertEquals(4, LegacyProceduralToPlayerRules.finalLegacyNAfterFlags(4, false))
    }

    private class QueueRandomSource(vararg values: Int) : RandomSource {
        private val values = values.toMutableList()
        val bounds = mutableListOf<Int>()
        override var draws: Long = 0
            private set
        override fun nextInt(bound: Int): Int {
            val value = values.removeAt(0)
            require(value in 0 until bound) { "value=$value bound=$bound" }
            bounds += bound
            draws++
            return value
        }
        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}
