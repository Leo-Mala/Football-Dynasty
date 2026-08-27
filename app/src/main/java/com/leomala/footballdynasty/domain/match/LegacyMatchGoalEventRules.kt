package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Direct `components.r3.f(...)` goal-event subtype routing proven by SMALI + `best.l` UI labels. */
object LegacyMatchGoalEventRules {
    const val LEGACY_EVENT_TYPE_GOAL: Int = 1

    enum class GoalSubtype(val legacyCode: Int) {
        NORMAL(1),
        AGAINST(2),
        PENALTY(3),
        FOUL(4),
        CORNER(5),
    }

    data class InitialGoalEvent(
        val eventType: Int = LEGACY_EVENT_TYPE_GOAL,
        val subtype: GoalSubtype,
    )

    fun drawInitialGoalEvent(
        random: RandomSource,
        primaryPlayerLegacyL0: Int?,
    ): InitialGoalEvent {
        val draw = random.nextInt(1000)
        var subtype = when {
            draw < 900 -> GoalSubtype.NORMAL
            draw < 950 -> GoalSubtype.PENALTY
            draw < 980 -> GoalSubtype.FOUL
            draw < 990 -> GoalSubtype.AGAINST
            draw < 995 -> GoalSubtype.CORNER
            else -> GoalSubtype.NORMAL
        }

        // Exact r3.f quirk: the type-5 branch falls back to normal when the selected player's
        // legacy l0 value is zero. A null player does not trigger this fallback at this site.
        if (subtype == GoalSubtype.CORNER && primaryPlayerLegacyL0 == 0) {
            subtype = GoalSubtype.NORMAL
        }

        return InitialGoalEvent(subtype = subtype)
    }
}
