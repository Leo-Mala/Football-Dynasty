package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyMatchTransitionRulesTest {
    @Test
    fun `r mode one threshold sixty scans from zero without RNG`() {
        val random = QueueRandomSource()
        val candidates = listOf(
            player("at-60", n = 60),
            player("eligible-59", n = 59),
        )

        val selected = LegacyMatchTransitionRules.selectR(
            mode = 1,
            legacyP2 = 1,
            legacyP4 = 40,
            candidates = candidates,
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals("eligible-59", selected?.value)
        assertEquals(emptyList<Int>(), random.bounds)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `r mode one threshold ninety uses random start and excludes ninety`() {
        val random = QueueRandomSource(0)
        val candidates = listOf(
            player("at-90", n = 90),
            player("eligible-89", n = 89),
        )

        val selected = LegacyMatchTransitionRules.selectR(
            mode = 1,
            legacyP2 = 1,
            legacyP4 = 41,
            candidates = candidates,
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals("eligible-89", selected?.value)
        assertEquals(listOf(2), random.bounds)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `r mode one random start never wraps around`() {
        val random = QueueRandomSource(1)
        val candidates = listOf(
            player("eligible-before-start", n = 20),
            player("blocked", g0 = 1, n = 20),
            player("too-high", n = 90),
        )

        val selected = LegacyMatchTransitionRules.selectR(
            mode = 1,
            legacyP2 = 1,
            legacyP4 = 41,
            candidates = candidates,
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertNull(selected)
        assertEquals(listOf(3), random.bounds)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `r mode one empty candidates short circuits before random start`() {
        val random = QueueRandomSource()

        val selected = LegacyMatchTransitionRules.selectR<String>(
            mode = 1,
            legacyP2 = 1,
            legacyP4 = 41,
            candidates = emptyList(),
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertNull(selected)
        assertEquals(emptyList<Int>(), random.bounds)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `r mode two rerolls exactly once when first candidate is already used`() {
        val random = QueueRandomSource(0, 1)
        val candidates = listOf(player("used"), player("fresh"))

        val selected = LegacyMatchTransitionRules.selectR(
            mode = 2,
            legacyP2 = 1,
            legacyP4 = 0,
            candidates = candidates,
            legacyG = setOf("used"),
            legacyH = emptySet(),
            random = random,
        )

        assertEquals("fresh", selected?.value)
        assertEquals(listOf(2, 2), random.bounds)
        assertEquals(2L, random.draws)
    }

    @Test
    fun `r mode two second used candidate returns null without a third draw`() {
        val random = QueueRandomSource(0, 1)
        val candidates = listOf(player("used-a"), player("used-b"), player("fresh"))

        val selected = LegacyMatchTransitionRules.selectR(
            mode = 2,
            legacyP2 = 1,
            legacyP4 = 0,
            candidates = candidates,
            legacyG = setOf("used-a", "used-b"),
            legacyH = emptySet(),
            random = random,
        )

        assertNull(selected)
        assertEquals(listOf(3, 3), random.bounds)
        assertEquals(2L, random.draws)
    }

    @Test
    fun `r mode two p2 two uses legacy H rather than legacy G`() {
        val random = QueueRandomSource(0)
        val candidates = listOf(player("in-g-only"), player("in-h-only"))

        val selected = LegacyMatchTransitionRules.selectR(
            mode = 2,
            legacyP2 = 2,
            legacyP4 = 0,
            candidates = candidates,
            legacyG = setOf("in-g-only"),
            legacyH = setOf("in-h-only"),
            random = random,
        )

        assertEquals("in-g-only", selected?.value)
        assertEquals(listOf(2), random.bounds)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `r mode two blocked player does not trigger an extra retry`() {
        val random = QueueRandomSource(0)
        val candidates = listOf(player("blocked", g0 = 1))

        val selected = LegacyMatchTransitionRules.selectR(
            mode = 2,
            legacyP2 = 1,
            legacyP4 = 0,
            candidates = candidates,
            legacyG = emptySet(),
            legacyH = emptySet(),
            random = random,
        )

        assertNull(selected)
        assertEquals(listOf(1), random.bounds)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `r0 p2 one threshold boundaries match smali`() {
        val cases = listOf(
            899 to 1,
            900 to 3,
            949 to 3,
            950 to 4,
            979 to 4,
            980 to 2,
            989 to 2,
            990 to 5,
            994 to 5,
            995 to 1,
            999 to 1,
        )

        for ((draw, expected) in cases) {
            val random = QueueRandomSource(draw)
            val result = LegacyMatchTransitionRules.resolveR0(
                player = player("source", l0 = 1),
                legacyP2 = 1,
                legacyP3 = 7,
                legacyEPlayers = emptyList(),
                legacyFPlayers = emptyList(),
                random = random,
            )
            assertEquals("draw=$draw", expected, result.code)
            assertNull("draw=$draw", result.followUp)
            assertEquals("draw=$draw", listOf(1000), random.bounds)
            assertEquals("draw=$draw", 1L, random.draws)
        }
    }

    @Test
    fun `r0 p2 two threshold boundaries match smali`() {
        val cases = listOf(
            799 to 1,
            800 to 3,
            849 to 3,
            850 to 4,
            979 to 4,
            980 to 2,
            989 to 2,
            990 to 5,
            994 to 5,
            995 to 1,
            999 to 1,
        )

        for ((draw, expected) in cases) {
            val random = QueueRandomSource(draw)
            val result = LegacyMatchTransitionRules.resolveR0(
                player = player("source", l0 = 1),
                legacyP2 = 2,
                legacyP3 = 7,
                legacyEPlayers = emptyList(),
                legacyFPlayers = emptyList(),
                random = random,
            )
            assertEquals("draw=$draw", expected, result.code)
            assertNull("draw=$draw", result.followUp)
            assertEquals("draw=$draw", listOf(1000), random.bounds)
            assertEquals("draw=$draw", 1L, random.draws)
        }
    }

    @Test
    fun `r0 code five is rewritten to one for legacy l0 zero or two`() {
        for (legacyL0 in listOf(0, 2)) {
            val random = QueueRandomSource(990)
            val result = LegacyMatchTransitionRules.resolveR0(
                player = player("source", l0 = legacyL0),
                legacyP2 = 1,
                legacyP3 = 0,
                legacyEPlayers = emptyList(),
                legacyFPlayers = emptyList(),
                random = random,
            )
            assertEquals("legacyL0=$legacyL0", 1, result.code)
            assertNull(result.followUp)
            assertEquals(listOf(1000), random.bounds)
        }
    }

    @Test
    fun `r0 code three follow up below fifty uses S then legacy C on E side`() {
        val random = QueueRandomSource(900, 49, 0)
        val target = player("target", position = 10, side = LegacyMatchTransitionRules.LegacyClubSide.LEGACY_E)
        val result = LegacyMatchTransitionRules.resolveR0(
            player = player("source", side = LegacyMatchTransitionRules.LegacyClubSide.LEGACY_E),
            legacyP2 = 1,
            legacyP3 = 17,
            legacyEPlayers = listOf(target),
            legacyFPlayers = emptyList(),
            random = random,
        )

        assertEquals(3, result.code)
        assertEquals(1, result.followUp?.sideFlag)
        assertEquals(LegacyMatchTransitionRules.FollowUpAction.LEGACY_C, result.followUp?.action)
        assertEquals("target", result.followUp?.player?.value)
        assertEquals(1, result.followUp?.legacyP2)
        assertEquals(17, result.followUp?.legacyP3)
        assertEquals(listOf(1000, 100, 100), random.bounds)
        assertEquals(3L, random.draws)
    }

    @Test
    fun `r0 code three follow up fifty through fifty nine uses U then legacy D on F side`() {
        val random = QueueRandomSource(900, 50, 1)
        val target = player("target", position = 10, side = LegacyMatchTransitionRules.LegacyClubSide.LEGACY_F)
        val result = LegacyMatchTransitionRules.resolveR0(
            player = player("source", side = LegacyMatchTransitionRules.LegacyClubSide.LEGACY_F),
            legacyP2 = 1,
            legacyP3 = 23,
            legacyEPlayers = emptyList(),
            legacyFPlayers = listOf(target),
            random = random,
        )

        assertEquals(3, result.code)
        assertEquals(0, result.followUp?.sideFlag)
        assertEquals(LegacyMatchTransitionRules.FollowUpAction.LEGACY_D, result.followUp?.action)
        assertEquals("target", result.followUp?.player?.value)
        assertEquals(listOf(1000, 100, 200), random.bounds)
        assertEquals(3L, random.draws)
    }

    @Test
    fun `r0 code three follow up sixty or above consumes no selector draw`() {
        val random = QueueRandomSource(900, 60)
        val result = LegacyMatchTransitionRules.resolveR0(
            player = player("source", side = LegacyMatchTransitionRules.LegacyClubSide.LEGACY_E),
            legacyP2 = 1,
            legacyP3 = 0,
            legacyEPlayers = listOf(player("target", position = 10)),
            legacyFPlayers = emptyList(),
            random = random,
        )

        assertEquals(3, result.code)
        assertNull(result.followUp)
        assertEquals(listOf(1000, 100), random.bounds)
        assertEquals(2L, random.draws)
    }

    @Test
    fun `r0 code three non match club side short circuits before follow up draw`() {
        val random = QueueRandomSource(900)
        val result = LegacyMatchTransitionRules.resolveR0(
            player = player("source", side = LegacyMatchTransitionRules.LegacyClubSide.OTHER),
            legacyP2 = 1,
            legacyP3 = 0,
            legacyEPlayers = listOf(player("target", position = 10)),
            legacyFPlayers = listOf(player("target-2", position = 10)),
            random = random,
        )

        assertEquals(3, result.code)
        assertNull(result.followUp)
        assertEquals(listOf(1000), random.bounds)
        assertEquals(1L, random.draws)
    }

    private fun player(
        value: String,
        position: Int = 10,
        g0: Int = 0,
        n: Int = 0,
        l0: Int = 1,
        side: LegacyMatchTransitionRules.LegacyClubSide = LegacyMatchTransitionRules.LegacyClubSide.OTHER,
    ) = LegacyMatchTransitionRules.Player(
        value = value,
        legacyPositionIndex = position,
        legacyG0 = g0,
        legacyN = n,
        legacyL0 = l0,
        clubSide = side,
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
