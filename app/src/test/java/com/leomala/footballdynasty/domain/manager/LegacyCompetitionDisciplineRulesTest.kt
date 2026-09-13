package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.match.LegacyMatchEventType
import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCompetitionDisciplineRulesTest {
    @Test
    fun `best r n blocks on exact recovered thresholds`() {
        assertFalse(LegacyCompetitionDisciplineRules.isExcluded(LegacyCompetitionDisciplineState()))
        assertFalse(LegacyCompetitionDisciplineRules.isExcluded(LegacyCompetitionDisciplineState(2, 0)))
        assertTrue(LegacyCompetitionDisciplineRules.isExcluded(LegacyCompetitionDisciplineState(3, 0)))
        assertTrue(LegacyCompetitionDisciplineRules.isExcluded(LegacyCompetitionDisciplineState(0, 1)))
    }

    @Test
    fun `best r a resets threshold three before touching threshold one`() {
        assertEquals(
            LegacyCompetitionDisciplineState(0, 2),
            LegacyCompetitionDisciplineRules.consumeExistingSuspension(
                LegacyCompetitionDisciplineState(4, 2)
            ),
        )
        assertEquals(
            LegacyCompetitionDisciplineState(2, 1),
            LegacyCompetitionDisciplineRules.consumeExistingSuspension(
                LegacyCompetitionDisciplineState(2, 2)
            ),
        )
        assertEquals(
            LegacyCompetitionDisciplineState(2, 0),
            LegacyCompetitionDisciplineRules.consumeExistingSuspension(
                LegacyCompetitionDisciplineState(2, 0)
            ),
        )
    }

    @Test
    fun `yellow and second yellow mutate exact best r counters`() {
        val start = LegacyCompetitionDisciplineState(1, 0)
        assertEquals(
            LegacyCompetitionDisciplineState(2, 0),
            LegacyCompetitionDisciplineRules.applyEvent(
                start,
                LegacyMatchEventType.YELLOW_CARD.legacyCode,
            ),
        )
        assertEquals(
            LegacyCompetitionDisciplineState(2, 1),
            LegacyCompetitionDisciplineRules.applyEvent(
                start,
                LegacyMatchEventType.SECOND_YELLOW_RED.legacyCode,
            ),
        )
    }

    @Test
    fun `direct red preserves recovered unreachable greater than 950 branch`() {
        assertEquals(
            LegacyCompetitionDisciplineState(0, 1),
            LegacyCompetitionDisciplineRules.applyEvent(
                LegacyCompetitionDisciplineState(),
                LegacyMatchEventType.RED_CARD.legacyCode,
                FixedIntRandom(800),
            ),
        )
        assertEquals(
            LegacyCompetitionDisciplineState(0, 2),
            LegacyCompetitionDisciplineRules.applyEvent(
                LegacyCompetitionDisciplineState(),
                LegacyMatchEventType.RED_CARD.legacyCode,
                FixedIntRandom(801),
            ),
        )
        assertEquals(
            LegacyCompetitionDisciplineState(0, 2),
            LegacyCompetitionDisciplineRules.applyEvent(
                LegacyCompetitionDisciplineState(),
                LegacyMatchEventType.RED_CARD.legacyCode,
                FixedIntRandom(999),
            ),
        )
    }

    private class FixedIntRandom(private val value: Int) : RandomSource {
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            require(value in 0 until bound)
            draws += 1
            return value
        }

        override fun nextBoolean(): Boolean = error("unused")
        override fun nextDouble(): Double = error("unused")
    }
}
