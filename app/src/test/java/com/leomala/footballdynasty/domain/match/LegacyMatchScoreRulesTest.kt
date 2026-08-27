package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchScoreRulesTest {
    @Test
    fun `materialized goal increments exactly one selected side`() {
        val initial = LegacyMatchScoreRules.Score(2, 3)

        assertEquals(
            LegacyMatchScoreRules.Score(3, 3),
            LegacyMatchScoreRules.applyMaterializedGoal(
                initial,
                LegacyMatchScoreRules.LegacySide.LEGACY_E,
            ),
        )
        assertEquals(
            LegacyMatchScoreRules.Score(2, 4),
            LegacyMatchScoreRules.applyMaterializedGoal(
                initial,
                LegacyMatchScoreRules.LegacySide.LEGACY_F,
            ),
        )
    }

    @Test
    fun `rebuild counts only type one events by stored club`() {
        val events = listOf(
            event("E", type = 1, subtype = 1),
            event("F", type = 1, subtype = 3),
            event("E", type = 2, subtype = -1),
            event("other", type = 1, subtype = 1),
            event("E", type = 1, subtype = 2),
        )

        assertEquals(
            LegacyMatchScoreRules.Score(2, 1),
            LegacyMatchScoreRules.rebuildFromEvents(events, "E", "F"),
        )
    }

    @Test
    fun `goal subtype never changes score counting`() {
        val events = LegacyMatchGoalEventRules.GoalSubtype.values().map { subtype ->
            event("E", type = 1, subtype = subtype.legacyCode)
        }

        assertEquals(
            LegacyMatchScoreRules.Score(5, 0),
            LegacyMatchScoreRules.rebuildFromEvents(events, "E", "F"),
        )
    }

    @Test
    fun `empty or unrecognized event list rebuilds zero zero`() {
        assertEquals(
            LegacyMatchScoreRules.Score(0, 0),
            LegacyMatchScoreRules.rebuildFromEvents<String, String>(emptyList(), "E", "F"),
        )
        assertEquals(
            LegacyMatchScoreRules.Score(0, 0),
            LegacyMatchScoreRules.rebuildFromEvents(
                listOf(event("other", 1, 1)),
                "E",
                "F",
            ),
        )
    }

    private fun event(club: String, type: Int, subtype: Int) =
        LegacyMatchEventRecord<String, String>(
            legacyClub = club,
            legacyType = type,
            legacySubtype = subtype,
        )
}
