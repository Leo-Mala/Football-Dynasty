package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyMatchJRulesTest {
    @Test
    fun `j ignores every half except legacy half two`() {
        val random = QueueRandomSource()
        val result = LegacyMatchJRules.resolve(
            legacyP1 = 1,
            legacyP2 = 30,
            legacyScoreE = 0,
            legacyScoreF = 0,
            legacyE = state(h0 = listOf(30), candidates = listOf(player("e"))),
            legacyF = state(h0 = listOf(30), candidates = listOf(player("f"))),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals(emptyList<LegacyMatchJRules.Attempt<String>>(), result.attempts)
        assertEquals(emptyList<Int>(), random.bounds)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `j E halftime trailing by one uses strict greater than fifty gate`() {
        val random = QueueRandomSource(51, 0)
        val result = LegacyMatchJRules.resolve(
            legacyP1 = 2,
            legacyP2 = 0,
            legacyScoreE = 0,
            legacyScoreF = 1,
            legacyE = state(candidates = listOf(player("e"))),
            legacyF = state(),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals(1, result.attempts.size)
        assertEquals(LegacyMatchJRules.LegacySide.LEGACY_E, result.attempts.single().side)
        assertEquals(2, result.attempts.single().mode)
        assertEquals("e", result.attempts.single().selected?.value)
        assertEquals(listOf(100, 1), random.bounds)
        assertEquals(2L, random.draws)
    }

    @Test
    fun `j E halftime draw fifty does not fall through to scheduled arrays`() {
        val random = QueueRandomSource(50)
        val result = LegacyMatchJRules.resolve(
            legacyP1 = 2,
            legacyP2 = 0,
            legacyScoreE = 0,
            legacyScoreF = 1,
            legacyE = state(i0 = listOf(0), h0 = listOf(0), candidates = listOf(player("e"))),
            legacyF = state(),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals(0, result.attempts.size)
        assertEquals(listOf(100), random.bounds)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `j E i0 minute allows mode two when scores are tied`() {
        val random = QueueRandomSource(0)
        val result = LegacyMatchJRules.resolve(
            legacyP1 = 2,
            legacyP2 = 20,
            legacyScoreE = 1,
            legacyScoreF = 1,
            legacyE = state(i0 = listOf(20), candidates = listOf(player("e"))),
            legacyF = state(i0 = listOf(20), candidates = listOf(player("f"))),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals(1, result.attempts.size)
        assertEquals(LegacyMatchJRules.LegacySide.LEGACY_E, result.attempts.single().side)
        assertEquals(2, result.attempts.single().mode)
        assertEquals(listOf(1), random.bounds)
    }

    @Test
    fun `j E i0 minute is suppressed while E leads`() {
        val random = QueueRandomSource()
        val result = LegacyMatchJRules.resolve(
            legacyP1 = 2,
            legacyP2 = 20,
            legacyScoreE = 2,
            legacyScoreF = 1,
            legacyE = state(i0 = listOf(20), candidates = listOf(player("e"))),
            legacyF = state(),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals(0, result.attempts.size)
        assertEquals(emptyList<Int>(), random.bounds)
    }

    @Test
    fun `j E h0 minute uses mode one regardless of score`() {
        val random = QueueRandomSource()
        val result = LegacyMatchJRules.resolve(
            legacyP1 = 2,
            legacyP2 = 30,
            legacyScoreE = 3,
            legacyScoreF = 0,
            legacyE = state(h0 = listOf(30), candidates = listOf(player("e", n = 59))),
            legacyF = state(),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals(1, result.attempts.size)
        assertEquals(1, result.attempts.single().mode)
        assertEquals("e", result.attempts.single().selected?.value)
        assertEquals(emptyList<Int>(), random.bounds)
    }

    @Test
    fun `j successful E attempt short circuits F even when F is scheduled`() {
        val random = QueueRandomSource()
        val result = LegacyMatchJRules.resolve(
            legacyP1 = 2,
            legacyP2 = 30,
            legacyScoreE = 0,
            legacyScoreF = 0,
            legacyE = state(h0 = listOf(30), candidates = listOf(player("e"))),
            legacyF = state(h0 = listOf(30), candidates = listOf(player("f"))),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals(1, result.attempts.size)
        assertEquals(LegacyMatchJRules.LegacySide.LEGACY_E, result.attempts.single().side)
        assertEquals("e", result.attempts.single().selected?.value)
    }

    @Test
    fun `j failed E attempt allows F attempt in the same minute`() {
        val random = QueueRandomSource()
        val result = LegacyMatchJRules.resolve(
            legacyP1 = 2,
            legacyP2 = 30,
            legacyScoreE = 0,
            legacyScoreF = 0,
            legacyE = state(h0 = listOf(30), candidates = listOf(player("blocked-e", g0 = 1))),
            legacyF = state(h0 = listOf(30), candidates = listOf(player("f"))),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals(2, result.attempts.size)
        assertEquals(LegacyMatchJRules.LegacySide.LEGACY_E, result.attempts[0].side)
        assertNull(result.attempts[0].selected)
        assertEquals(LegacyMatchJRules.LegacySide.LEGACY_F, result.attempts[1].side)
        assertEquals("f", result.attempts[1].selected?.value)
        assertEquals(emptyList<Int>(), random.bounds)
    }

    @Test
    fun `j F halftime requires trailing by two and strict greater than fifty`() {
        val random = QueueRandomSource(51, 0)
        val result = LegacyMatchJRules.resolve(
            legacyP1 = 2,
            legacyP2 = 0,
            legacyScoreE = 2,
            legacyScoreF = 0,
            legacyE = state(),
            legacyF = state(candidates = listOf(player("f"))),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals(1, result.attempts.size)
        assertEquals(LegacyMatchJRules.LegacySide.LEGACY_F, result.attempts.single().side)
        assertEquals(2, result.attempts.single().mode)
        assertEquals(1, result.attempts.single().legacyP2)
        assertEquals("f", result.attempts.single().selected?.value)
        assertEquals(listOf(100, 1), random.bounds)
    }

    @Test
    fun `j F halftime trailing by only one consumes no gate draw`() {
        val random = QueueRandomSource()
        val result = LegacyMatchJRules.resolve(
            legacyP1 = 2,
            legacyP2 = 0,
            legacyScoreE = 1,
            legacyScoreF = 0,
            legacyE = state(),
            legacyF = state(),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals(0, result.attempts.size)
        assertEquals(emptyList<Int>(), random.bounds)
    }

    @Test
    fun `j F i0 minute requires F to trail and has no tie fallback`() {
        val tiedRandom = QueueRandomSource()
        val tied = LegacyMatchJRules.resolve(
            legacyP1 = 2,
            legacyP2 = 25,
            legacyScoreE = 1,
            legacyScoreF = 1,
            legacyE = state(),
            legacyF = state(i0 = listOf(25), candidates = listOf(player("f"))),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = tiedRandom,
        )
        assertEquals(0, tied.attempts.size)
        assertEquals(emptyList<Int>(), tiedRandom.bounds)

        val trailingRandom = QueueRandomSource(0)
        val trailing = LegacyMatchJRules.resolve(
            legacyP1 = 2,
            legacyP2 = 25,
            legacyScoreE = 2,
            legacyScoreF = 1,
            legacyE = state(),
            legacyF = state(i0 = listOf(25), candidates = listOf(player("f"))),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = trailingRandom,
        )
        assertEquals(1, trailing.attempts.size)
        assertEquals(2, trailing.attempts.single().mode)
        assertEquals(listOf(1), trailingRandom.bounds)
    }

    @Test
    fun `j F h0 minute uses mode one`() {
        val random = QueueRandomSource()
        val result = LegacyMatchJRules.resolve(
            legacyP1 = 2,
            legacyP2 = 35,
            legacyScoreE = 0,
            legacyScoreF = 4,
            legacyE = state(),
            legacyF = state(h0 = listOf(35), candidates = listOf(player("f"))),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals(1, result.attempts.size)
        assertEquals(LegacyMatchJRules.LegacySide.LEGACY_F, result.attempts.single().side)
        assertEquals(1, result.attempts.single().mode)
        assertEquals("f", result.attempts.single().selected?.value)
    }

    @Test
    fun `j blocked club or zero remaining short circuits its side`() {
        val random = QueueRandomSource()
        val result = LegacyMatchJRules.resolve(
            legacyP1 = 2,
            legacyP2 = 30,
            legacyScoreE = 0,
            legacyScoreF = 0,
            legacyE = state(blocked = true, h0 = listOf(30), candidates = listOf(player("e"))),
            legacyF = state(remaining = 0, h0 = listOf(30), candidates = listOf(player("f"))),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals(0, result.attempts.size)
        assertEquals(emptyList<Int>(), random.bounds)
        assertEquals(0L, random.draws)
    }

    private fun state(
        blocked: Boolean = false,
        remaining: Int = 5,
        i0: List<Int> = listOf(-1, -1, -1),
        h0: List<Int> = listOf(-1, -1, -1, -1),
        candidates: List<LegacyMatchTransitionRules.Player<String>> = emptyList(),
    ) = LegacyMatchJRules.SideState(
        blocked = blocked,
        remaining = remaining,
        legacyI0 = i0,
        legacyH0 = h0,
        candidates = candidates,
    )

    private fun player(
        value: String,
        position: Int = 10,
        g0: Int = 0,
        n: Int = 0,
        l0: Int = 1,
    ) = LegacyMatchTransitionRules.Player(
        value = value,
        legacyPositionIndex = position,
        legacyG0 = g0,
        legacyN = n,
        legacyL0 = l0,
    )

    private class QueueRandomSource(vararg values: Int) : RandomSource {
        private val values = values.toMutableList()
        val bounds = mutableListOf<Int>()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            check(values.isNotEmpty()) { "unexpected draw with bound=$bound" }
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
