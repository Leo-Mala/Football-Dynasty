package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class LegacyMatchGoalPlayerSelectionRulesTest {
    @Test
    fun `primary selector uses exact position weights with one nextDouble draw`() {
        val light = player("light", g0 = 2, l0 = 1)
        val heavy = player("heavy", g0 = 18, l0 = 1)
        val random = DoubleQueueRandomSource(0.50)

        val selected = LegacyMatchGoalPlayerSelectionRules.selectPrimary(listOf(light, heavy), random)

        assertSame(heavy, selected)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `primary selector trait nine adds four to weight`() {
        val boosted = player("boosted", g0 = 2, l0 = 1, g = 9)
        val normal = player("normal", g0 = 2, l0 = 1)

        val selected = LegacyMatchGoalPlayerSelectionRules.selectPrimary(
            listOf(boosted, normal),
            DoubleQueueRandomSource(0.75),
        )

        assertSame(boosted, selected)
    }

    @Test
    fun `primary selector trait five adds two plus another two for l0 two`() {
        val boosted = player("boosted", g0 = 2, l0 = 2, h = 5)
        val normal = player("normal", g0 = 2, l0 = 1)

        val selected = LegacyMatchGoalPlayerSelectionRules.selectPrimary(
            listOf(boosted, normal),
            DoubleQueueRandomSource(0.75),
        )

        assertSame(boosted, selected)
    }

    @Test
    fun `trait nine takes precedence over trait five branch`() {
        val both = player("both", g0 = 2, l0 = 2, g = 9, h = 5)
        val normal = player("normal", g0 = 2, l0 = 1)

        val selected = LegacyMatchGoalPlayerSelectionRules.selectPrimary(
            listOf(both, normal),
            DoubleQueueRandomSource(0.80),
        )

        assertSame(both, selected)
    }

    @Test
    fun `primary exact zero draw may return first ineligible player due legacy comparison placement`() {
        val ineligible = player("ineligible", g0 = 1, l0 = 0)
        val eligible = player("eligible", g0 = 18, l0 = 1)

        val selected = LegacyMatchGoalPlayerSelectionRules.selectPrimary(
            listOf(ineligible, eligible),
            DoubleQueueRandomSource(0.0),
        )

        assertSame(ineligible, selected)
    }

    @Test
    fun `primary selector ignores goalkeeper g0 one and zero l0 for positive draw`() {
        val goalkeeper = player("keeper", g0 = 1, l0 = 1)
        val zeroL0 = player("zero", g0 = 18, l0 = 0)
        val eligible = player("eligible", g0 = 18, l0 = 1)

        val selected = LegacyMatchGoalPlayerSelectionRules.selectPrimary(
            listOf(goalkeeper, zeroL0, eligible),
            DoubleQueueRandomSource(0.5),
        )

        assertSame(eligible, selected)
    }

    @Test
    fun `empty primary list still consumes one nextDouble and returns null`() {
        val random = DoubleQueueRandomSource(0.25)

        val selected = LegacyMatchGoalPlayerSelectionRules.selectPrimary(
            emptyList<LegacyMatchGoalPlayerSelectionRules.Player<String>>(),
            random,
        )

        assertNull(selected)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `own goal selector uses opposing position weights`() {
        val heavy = player("heavy", g0 = 3, l0 = 1)
        val light = player("light", g0 = 1, l0 = 1)

        val selected = LegacyMatchGoalPlayerSelectionRules.selectOwnGoalAuthor(
            listOf(heavy, light),
            DoubleQueueRandomSource(0.50),
        )

        assertSame(heavy, selected)
    }

    @Test
    fun `own goal exact zero draw may return invalid first player`() {
        val invalid = player("invalid", g0 = -1, l0 = 0)
        val valid = player("valid", g0 = 3, l0 = 1)

        val selected = LegacyMatchGoalPlayerSelectionRules.selectOwnGoalAuthor(
            listOf(invalid, valid),
            DoubleQueueRandomSource(0.0),
        )

        assertSame(invalid, selected)
    }

    @Test
    fun `own goal selector includes legacy negative weight at g0 zero`() {
        val zero = player("zero", g0 = 0, l0 = 1)
        val heavy = player("heavy", g0 = 3, l0 = 1)

        val selected = LegacyMatchGoalPlayerSelectionRules.selectOwnGoalAuthor(
            listOf(zero, heavy),
            DoubleQueueRandomSource(0.10),
        )

        assertSame(heavy, selected)
    }

    @Test
    fun `empty own goal list consumes one nextDouble and returns null`() {
        val random = DoubleQueueRandomSource(0.5)

        val selected = LegacyMatchGoalPlayerSelectionRules.selectOwnGoalAuthor(
            emptyList<LegacyMatchGoalPlayerSelectionRules.Player<String>>(),
            random,
        )

        assertNull(selected)
        assertEquals(1L, random.draws)
    }

    private fun player(
        value: String,
        g0: Int,
        l0: Int,
        g: Int = 0,
        h: Int = 0,
    ) = LegacyMatchGoalPlayerSelectionRules.Player(value, g0, l0, g, h)

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
