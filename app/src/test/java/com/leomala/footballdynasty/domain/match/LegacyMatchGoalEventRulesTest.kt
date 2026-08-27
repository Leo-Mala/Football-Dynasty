package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchGoalEventRulesTest {
    @Test
    fun `event type is one and threshold boundaries match legacy`() {
        val cases = listOf(
            0 to LegacyMatchGoalEventRules.GoalSubtype.NORMAL,
            899 to LegacyMatchGoalEventRules.GoalSubtype.NORMAL,
            900 to LegacyMatchGoalEventRules.GoalSubtype.PENALTY,
            949 to LegacyMatchGoalEventRules.GoalSubtype.PENALTY,
            950 to LegacyMatchGoalEventRules.GoalSubtype.FOUL,
            979 to LegacyMatchGoalEventRules.GoalSubtype.FOUL,
            980 to LegacyMatchGoalEventRules.GoalSubtype.AGAINST,
            989 to LegacyMatchGoalEventRules.GoalSubtype.AGAINST,
            990 to LegacyMatchGoalEventRules.GoalSubtype.CORNER,
            994 to LegacyMatchGoalEventRules.GoalSubtype.CORNER,
            995 to LegacyMatchGoalEventRules.GoalSubtype.NORMAL,
            999 to LegacyMatchGoalEventRules.GoalSubtype.NORMAL,
        )

        for ((draw, subtype) in cases) {
            val random = QueueRandomSource(draw)
            val result = LegacyMatchGoalEventRules.drawInitialGoalEvent(
                random = random,
                primaryPlayerLegacyL0 = 1,
            )

            assertEquals("draw=$draw", LegacyMatchGoalEventRules.LEGACY_EVENT_TYPE_GOAL, result.eventType)
            assertEquals("draw=$draw", subtype, result.subtype)
            assertEquals("draw=$draw", listOf(1000), random.bounds)
            assertEquals("draw=$draw", 1L, random.draws)
        }
    }

    @Test
    fun `corner subtype falls back to normal only for legacy l0 zero`() {
        val zero = LegacyMatchGoalEventRules.drawInitialGoalEvent(QueueRandomSource(990), 0)
        val nonZero = LegacyMatchGoalEventRules.drawInitialGoalEvent(QueueRandomSource(990), 2)
        val absent = LegacyMatchGoalEventRules.drawInitialGoalEvent(QueueRandomSource(990), null)

        assertEquals(LegacyMatchGoalEventRules.GoalSubtype.NORMAL, zero.subtype)
        assertEquals(LegacyMatchGoalEventRules.GoalSubtype.CORNER, nonZero.subtype)
        assertEquals(LegacyMatchGoalEventRules.GoalSubtype.CORNER, absent.subtype)
    }

    private class QueueRandomSource(vararg values: Int) : RandomSource {
        private val queue = values.toMutableList()
        val bounds = mutableListOf<Int>()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
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
