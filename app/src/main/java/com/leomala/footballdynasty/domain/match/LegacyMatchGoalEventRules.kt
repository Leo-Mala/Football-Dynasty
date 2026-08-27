package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Direct `components.r3.f(...)` initial goal-event subtype routing proven by SMALI + `best.l` UI labels. */
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

    /**
     * Preserve only the initial subtype draw performed near the start of legacy `r3.f(...)`.
     *
     * The later `CORNER -> NORMAL` fallback is intentionally not applied here. SMALI performs that
     * check only after a null primary player has been replaced through `n()` and after the normal
     * secondary-player branch has already been skipped. `LegacyMatchGoalMaterializationRules`
     * owns that later ordered transition.
     *
     * `primaryPlayerLegacyL0` remains in this boundary for source compatibility with the earlier
     * characterization API, but it cannot affect the initial draw itself.
     */
    @Suppress("UNUSED_PARAMETER")
    fun drawInitialGoalEvent(
        random: RandomSource,
        primaryPlayerLegacyL0: Int?,
    ): InitialGoalEvent {
        val draw = random.nextInt(1000)
        val subtype = when {
            draw < 900 -> GoalSubtype.NORMAL
            draw < 950 -> GoalSubtype.PENALTY
            draw < 980 -> GoalSubtype.FOUL
            draw < 990 -> GoalSubtype.AGAINST
            draw < 995 -> GoalSubtype.CORNER
            else -> GoalSubtype.NORMAL
        }

        return InitialGoalEvent(subtype = subtype)
    }
}
