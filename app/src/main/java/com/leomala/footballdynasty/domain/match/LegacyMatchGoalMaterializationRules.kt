package com.leomala.footballdynasty.domain.match

/** Structural routing inside reachable legacy `components.r3.f(best.l, best.o)`. */
object LegacyMatchGoalMaterializationRules {
    class Player<T>(
        val value: T,
        val legacyL0: Int,
    )

    data class Designated<T>(
        val player: Player<T>,
        val isActive: Boolean,
    )

    enum class StatOperation {
        PRIMARY_S,
        SECONDARY_L,
        SECONDARY_COMPETITION_SIDE_EFFECT,
        OWN_GOAL_T,
    }

    data class StatMutation<T>(
        val player: Player<T>,
        val operation: StatOperation,
    )

    data class Result<T>(
        val finalSubtype: LegacyMatchGoalEventRules.GoalSubtype,
        val eventPrimary: Player<T>?,
        val eventSecondary: Player<T>?,
        val penaltyFlag: Boolean,
        val statMutations: List<StatMutation<T>>,
        val incrementInternalGoalCounter: Boolean = true,
        val incrementScoreForCurrentSide: Boolean = true,
    )

    fun <T> resolve(
        initialSubtype: LegacyMatchGoalEventRules.GoalSubtype,
        initialPrimary: Player<T>?,
        fallbackPrimary: () -> Player<T>?,
        normalSecondary: (Player<T>?) -> Player<T>?,
        ownGoalAuthor: () -> Player<T>?,
        designatedPenaltyOrFoul: Designated<T>?,
        designatedCorner: Designated<T>?,
    ): Result<T> {
        var subtype = initialSubtype
        val primaryForStats = initialPrimary ?: fallbackPrimary()
        var eventPrimary = primaryForStats
        var eventSecondary: Player<T>? = null
        val stats = mutableListOf<StatMutation<T>>()

        if (
            subtype != LegacyMatchGoalEventRules.GoalSubtype.PENALTY &&
            subtype != LegacyMatchGoalEventRules.GoalSubtype.AGAINST &&
            primaryForStats != null
        ) {
            stats += StatMutation(primaryForStats, StatOperation.PRIMARY_S)
        }

        // Only a goal that is already NORMAL at this point enters the secondary-player helper.
        // A corner that later falls back to NORMAL does not come back through this branch.
        if (subtype == LegacyMatchGoalEventRules.GoalSubtype.NORMAL) {
            val secondary = normalSecondary(primaryForStats)
            if (secondary != null && secondary !== primaryForStats) {
                stats += StatMutation(secondary, StatOperation.SECONDARY_L)
                stats += StatMutation(secondary, StatOperation.SECONDARY_COMPETITION_SIDE_EFFECT)
                eventSecondary = secondary
            }
        }

        // This check occurs after the normal-secondary branch and after null-primary fallback.
        if (
            primaryForStats != null &&
            subtype == LegacyMatchGoalEventRules.GoalSubtype.CORNER &&
            primaryForStats.legacyL0 == 0
        ) {
            subtype = LegacyMatchGoalEventRules.GoalSubtype.NORMAL
        }

        if (subtype == LegacyMatchGoalEventRules.GoalSubtype.AGAINST) {
            val ownAuthor = ownGoalAuthor()
            if (ownAuthor != null) {
                eventPrimary = ownAuthor
                stats += StatMutation(ownAuthor, StatOperation.OWN_GOAL_T)
            } else {
                // Legacy falls back to the NORMAL subtype but does not rewind to the secondary branch.
                subtype = LegacyMatchGoalEventRules.GoalSubtype.NORMAL
            }
        }

        when (subtype) {
            LegacyMatchGoalEventRules.GoalSubtype.PENALTY,
            LegacyMatchGoalEventRules.GoalSubtype.FOUL,
            -> if (designatedPenaltyOrFoul?.isActive == true) {
                eventPrimary = designatedPenaltyOrFoul.player
            }

            LegacyMatchGoalEventRules.GoalSubtype.CORNER ->
                if (designatedCorner?.isActive == true) {
                    eventPrimary = designatedCorner.player
                }

            else -> Unit
        }

        // The legacy register holding p2 is not replaced when the displayed event author changes.
        // Its final stat mutation therefore still targets the original/fallback primary player.
        if (primaryForStats != null) {
            stats += StatMutation(primaryForStats, StatOperation.PRIMARY_S)
        }

        return Result(
            finalSubtype = subtype,
            eventPrimary = eventPrimary,
            eventSecondary = eventSecondary,
            penaltyFlag = subtype == LegacyMatchGoalEventRules.GoalSubtype.PENALTY,
            statMutations = stats,
        )
    }
}
