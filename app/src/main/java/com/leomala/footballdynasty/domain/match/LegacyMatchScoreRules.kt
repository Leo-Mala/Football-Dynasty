package com.leomala.footballdynasty.domain.match

object LegacyMatchScoreRules {
    enum class LegacySide {
        LEGACY_E,
        LEGACY_F,
    }

    data class Score(
        val legacyE: Int,
        val legacyF: Int,
    )

    fun applyMaterializedGoal(score: Score, side: LegacySide): Score = when (side) {
        LegacySide.LEGACY_E -> score.copy(legacyE = score.legacyE + 1)
        LegacySide.LEGACY_F -> score.copy(legacyF = score.legacyF + 1)
    }

    fun <TClub, TPlayer> rebuildFromEvents(
        events: List<LegacyMatchEventRecord<TClub, TPlayer>>,
        legacyEClub: TClub,
        legacyFClub: TClub,
    ): Score {
        var e = 0
        var f = 0
        for (event in events) {
            if (event.legacyType != LegacyMatchGoalEventRules.LEGACY_EVENT_TYPE_GOAL) continue
            when (event.legacyClub) {
                legacyEClub -> e++
                legacyFClub -> f++
            }
        }
        return Score(e, f)
    }
}
