package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyMatchPlayerSelectionRulesTest {
    private val candidates = listOf(
        candidate("keeper", 1),
        candidate("defender-low", 3),
        candidate("range-a", 10),
        candidate("range-a-2", 13),
        candidate("range-b", 14),
        candidate("range-c", 19),
        candidate("range-c-2", 24),
    )

    @Test
    fun `S draw 84 selects legacy single-position bucket six`() {
        val random = QueueRandomSource(84)

        val selected = LegacyMatchPlayerSelectionRules.selectS(candidates, random)

        assertEquals("keeper", selected?.value)
        assertEquals(listOf(100), random.bounds)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `S draw 85 moves to final nineteen-through-twenty-four bucket`() {
        val random = QueueRandomSource(85, 0)

        val selected = LegacyMatchPlayerSelectionRules.selectS(candidates, random)

        assertEquals("range-c-2", selected?.value)
        assertEquals(listOf(100, 2), random.bounds)
        assertEquals(2L, random.draws)
    }

    @Test
    fun `T zero draw selects keeper range and does not draw for one-element shuffle`() {
        val random = QueueRandomSource(0)

        val selected = LegacyMatchPlayerSelectionRules.selectT(candidates, random)

        assertEquals("keeper", selected?.value)
        assertEquals(listOf(500), random.bounds)
    }

    @Test
    fun `U boundary 190 selects final bucket`() {
        val random = QueueRandomSource(190, 0)

        val selected = LegacyMatchPlayerSelectionRules.selectU(candidates, random)

        assertEquals("range-c-2", selected?.value)
        assertEquals(listOf(200, 2), random.bounds)
    }

    @Test
    fun `V boundary 500 selects final bucket`() {
        val random = QueueRandomSource(500, 0)

        val selected = LegacyMatchPlayerSelectionRules.selectV(candidates, random)

        assertEquals("range-c-2", selected?.value)
        assertEquals(listOf(1000, 2), random.bounds)
    }

    @Test
    fun `W-equivalent filter returns null without shuffle draws when range is empty`() {
        val random = QueueRandomSource()

        val selected = LegacyMatchPlayerSelectionRules.selectWithinRange(
            candidates = candidates,
            range = LegacyMatchPlayerSelectionRules.PositionRange(30, 35),
            random = random,
        )

        assertNull(selected)
        assertEquals(emptyList<Int>(), random.bounds)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `W-equivalent shuffle is explicit and deterministic`() {
        val random = QueueRandomSource(0)

        val selected = LegacyMatchPlayerSelectionRules.selectWithinRange(
            candidates = candidates,
            range = LegacyMatchPlayerSelectionRules.PositionRange(10, 13),
            random = random,
        )

        assertEquals("range-a-2", selected?.value)
        assertEquals(listOf(2), random.bounds)
        assertEquals(1L, random.draws)
    }

    private fun candidate(value: String, position: Int) =
        LegacyMatchPlayerSelectionRules.Candidate(value, position)

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
