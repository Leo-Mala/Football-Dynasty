package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class LegacyMatchGoalSecondarySelectionRulesTest {
    @Test
    fun `gate eighty one returns null without weighted double draw`() {
        val random = MixedQueueRandomSource(ints = listOf(81), doubles = emptyList())

        val result = LegacyMatchGoalSecondarySelectionRules.select(
            primary = null,
            active = listOf(player("p", 2)),
            legacyClubI0Index2 = 0,
            random = random,
        )

        assertNull(result)
        assertEquals(listOf(100), random.intBounds)
        assertEquals(1L, random.draws)
        assertEquals(0, random.doubleCalls)
    }

    @Test
    fun `gate eighty is accepted and consumes weighted double draw`() {
        val candidate = player("candidate", 2)
        val random = MixedQueueRandomSource(ints = listOf(80), doubles = listOf(0.5))

        val result = LegacyMatchGoalSecondarySelectionRules.select(null, listOf(candidate), 0, random)

        assertSame(candidate, result)
        assertEquals(2L, random.draws)
        assertEquals(1, random.doubleCalls)
    }

    @Test
    fun `base weights select heavier later player`() {
        val light = player("light", 3)
        val heavy = player("heavy", 14)

        val result = LegacyMatchGoalSecondarySelectionRules.select(
            primary = null,
            active = listOf(light, heavy),
            legacyClubI0Index2 = 0,
            random = MixedQueueRandomSource(listOf(0), listOf(0.5)),
        )

        assertSame(heavy, result)
    }

    @Test
    fun `trait eleven plus other trait four adds ten then five`() {
        val p = player("p", position = 2, g = 11, h = 4)
        assertEquals(25.0, LegacyMatchGoalSecondarySelectionRules.weight(p, 0), 0.0)
    }

    @Test
    fun `trait four branch can add second bonus when G is eight and H is four`() {
        val p = player("p", position = 2, g = 8, h = 4)
        assertEquals(14.0, LegacyMatchGoalSecondarySelectionRules.weight(p, 0), 0.0)
    }

    @Test
    fun `trait eight branch can add second bonus when G is thirteen and H is eight`() {
        val p = player("p", position = 2, g = 13, h = 8)
        assertEquals(14.0, LegacyMatchGoalSecondarySelectionRules.weight(p, 0), 0.0)
    }

    @Test
    fun `trait thirteen with l0 one adds one plus two`() {
        val p = player("p", position = 2, g = 13, h = 0, l0 = 1)
        assertEquals(13.0, LegacyMatchGoalSecondarySelectionRules.weight(p, 0), 0.0)
    }

    @Test
    fun `trait six with l0 one adds five plus two`() {
        val p = player("p", position = 2, g = 6, h = 0, l0 = 1)
        assertEquals(17.0, LegacyMatchGoalSecondarySelectionRules.weight(p, 0), 0.0)
    }

    @Test
    fun `club i0 index two exactly one adds twenty for l0 one`() {
        val p = player("p", position = 2, l0 = 1)
        assertEquals(30.0, LegacyMatchGoalSecondarySelectionRules.weight(p, 1), 0.0)
        assertEquals(10.0, LegacyMatchGoalSecondarySelectionRules.weight(p, 2), 0.0)
    }

    @Test
    fun `earlier trait branch prevents later trait six bonus`() {
        val p = player("p", position = 2, g = 11, h = 6, l0 = 1)
        assertEquals(20.0, LegacyMatchGoalSecondarySelectionRules.weight(p, 0), 0.0)
    }

    @Test
    fun `exact zero weighted target may return excluded primary because comparison is outside eligibility`() {
        val primary = player("primary", 18)
        val other = player("other", 18)

        val result = LegacyMatchGoalSecondarySelectionRules.select(
            primary = primary,
            active = listOf(primary, other),
            legacyClubI0Index2 = 0,
            random = MixedQueueRandomSource(listOf(0), listOf(0.0)),
        )

        assertSame(primary, result)
    }

    @Test
    fun `zero total with nonempty invalid list returns first item after accepted gate`() {
        val invalid = player("invalid", 0)

        val result = LegacyMatchGoalSecondarySelectionRules.select(
            primary = null,
            active = listOf(invalid),
            legacyClubI0Index2 = 0,
            random = MixedQueueRandomSource(listOf(0), listOf(0.75)),
        )

        assertSame(invalid, result)
    }

    @Test
    fun `empty active list consumes both accepted-gate draws and returns null`() {
        val random = MixedQueueRandomSource(listOf(0), listOf(0.4))

        val result = LegacyMatchGoalSecondarySelectionRules.select<String>(null, emptyList(), 0, random)

        assertNull(result)
        assertEquals(2L, random.draws)
    }

    private fun player(
        value: String,
        position: Int,
        g: Int = 0,
        h: Int = 0,
        l0: Int = 0,
    ) = LegacyMatchGoalSecondarySelectionRules.Player(value, position, g, h, l0)

    private class MixedQueueRandomSource(
        ints: List<Int>,
        doubles: List<Double>,
    ) : RandomSource {
        private val ints = ints.toMutableList()
        private val doubles = doubles.toMutableList()
        val intBounds = mutableListOf<Int>()
        var doubleCalls: Int = 0
            private set
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            check(ints.isNotEmpty()) { "No queued int for bound=$bound" }
            val value = ints.removeAt(0)
            require(value in 0 until bound)
            intBounds += bound
            draws++
            return value
        }

        override fun nextDouble(): Double {
            check(doubles.isNotEmpty()) { "No queued double" }
            val value = doubles.removeAt(0)
            require(value >= 0.0 && value < 1.0)
            doubleCalls++
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
    }
}
