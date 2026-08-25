package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchMinuteRulesTest {
    @Test
    fun `home first-half legacy C short-circuits after primary gate`() {
        val random = QueueRandomSource(56, 1)

        val decision = LegacyMatchMinuteRules.decide(
            random = random,
            half = 1,
            minute = 0,
            homeTacticIndex = 0,
            awayTacticIndex = 2,
            primaryCounter = 0,
            secondaryCounter = 0,
            tertiaryCounter = 0,
        )

        assertEquals(LegacyMatchMinuteRules.Side.HOME, decision.side)
        assertEquals(LegacyMatchMinuteRules.Action.LEGACY_C, decision.action)
        assertTrue(decision.refreshPlayerState)
        assertEquals(100, decision.primaryBound)
        assertEquals(1200, decision.secondaryBound)
        assertEquals(2000, decision.tertiaryBound)
        assertEquals(listOf(100, 100), random.bounds)
        assertEquals(2L, random.draws)
    }

    @Test
    fun `away second-half legacy D uses segment and tactic bounds`() {
        val random = QueueRandomSource(55, 0, 1)

        val decision = LegacyMatchMinuteRules.decide(
            random = random,
            half = 2,
            minute = 20,
            homeTacticIndex = 0,
            awayTacticIndex = 2,
            primaryCounter = 0,
            secondaryCounter = 0,
            tertiaryCounter = 0,
        )

        assertEquals(LegacyMatchMinuteRules.Side.AWAY, decision.side)
        assertEquals(LegacyMatchMinuteRules.Action.LEGACY_D, decision.action)
        assertFalse(decision.refreshPlayerState)
        assertEquals(40, decision.primaryBound)
        assertEquals(700, decision.secondaryBound)
        assertEquals(1500, decision.tertiaryBound)
        assertEquals(listOf(100, 40, 700), random.bounds)
        assertEquals(3L, random.draws)
    }

    @Test
    fun `tertiary event consumes all four direct best s k draws`() {
        val random = QueueRandomSource(55, 0, 0, 1)

        val decision = LegacyMatchMinuteRules.decide(
            random = random,
            half = 2,
            minute = 4,
            homeTacticIndex = 1,
            awayTacticIndex = 2,
            primaryCounter = 0,
            secondaryCounter = 0,
            tertiaryCounter = 0,
        )

        assertEquals(LegacyMatchMinuteRules.Action.LEGACY_TYPE_5, decision.action)
        assertEquals(listOf(100, 45, 800, 2000), random.bounds)
        assertEquals(4L, random.draws)
    }

    @Test
    fun `second-half follow-up starts only at legacy minute five after all direct gates miss`() {
        val before = QueueRandomSource(55, 0, 0, 0)
        val atFive = QueueRandomSource(55, 0, 0, 0)

        val minuteFour = LegacyMatchMinuteRules.decide(
            before, 2, 4, 0, 2, 0, 0, 0,
        )
        val minuteFive = LegacyMatchMinuteRules.decide(
            atFive, 2, 5, 0, 2, 0, 0, 0,
        )

        assertEquals(LegacyMatchMinuteRules.Action.NONE, minuteFour.action)
        assertEquals(LegacyMatchMinuteRules.Action.SECOND_HALF_J, minuteFive.action)
        assertEquals(4L, before.draws)
        assertEquals(4L, atFive.draws)
    }

    @Test
    fun `primary counter greater than ten preserves unreachable legacy else-if branch`() {
        val random = QueueRandomSource(56, 0, 0, 0)

        val decision = LegacyMatchMinuteRules.decide(
            random = random,
            half = 1,
            minute = 30,
            homeTacticIndex = 0,
            awayTacticIndex = 0,
            primaryCounter = 11,
            secondaryCounter = 0,
            tertiaryCounter = 0,
        )

        assertEquals(120, decision.primaryBound)
        assertEquals(listOf(100, 120, 800, 1100), random.bounds)
    }

    @Test
    fun `secondary then tertiary counters override primary bound in legacy order`() {
        val random = QueueRandomSource(56, 1)

        val decision = LegacyMatchMinuteRules.decide(
            random = random,
            half = 1,
            minute = 30,
            homeTacticIndex = 0,
            awayTacticIndex = 0,
            primaryCounter = 11,
            secondaryCounter = 2,
            tertiaryCounter = 1,
        )

        assertEquals(5500, decision.primaryBound)
        assertEquals(LegacyMatchMinuteRules.Action.LEGACY_C, decision.action)
        assertEquals(listOf(100, 5500), random.bounds)
    }

    @Test
    fun `legacy tactic indexes three and above collapse to first offset bucket`() {
        val random = QueueRandomSource(56, 1)

        val decision = LegacyMatchMinuteRules.decide(
            random = random,
            half = 1,
            minute = 15,
            homeTacticIndex = 9,
            awayTacticIndex = 0,
            primaryCounter = 0,
            secondaryCounter = 0,
            tertiaryCounter = 0,
        )

        assertEquals(70, decision.primaryBound)
        assertEquals(listOf(100, 70), random.bounds)
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
