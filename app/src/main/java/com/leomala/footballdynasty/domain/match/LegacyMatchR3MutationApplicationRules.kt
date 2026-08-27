package com.leomala.footballdynasty.domain.match

/**
 * Pure application boundary for mutation plans already proven for reachable legacy `components.r3.b()/c()`.
 *
 * Field names deliberately retain the recovered legacy letters. Goal materialization remains a callback because
 * the complete mutable `best.l` runtime is a separate evidence boundary and must not be invented here.
 */
object LegacyMatchR3MutationApplicationRules {
    data class State(
        val legacyIBySide: List<Int>,
        val legacyYBySide: List<Int>,
        val legacyZBySide: List<Int>,
        val primaryLegacyR0P: Int? = null,
    )

    data class Result(
        val state: State,
        val goalMaterializationRequested: Boolean,
    )

    fun apply(
        currentSide: Int,
        plan: LegacyMatchR3EventRoutingRules.Result,
        state: State,
        materializeGoalCurrent: () -> Unit = {},
    ): Result {
        require(currentSide in state.legacyIBySide.indices) { "currentSide outside legacyIBySide" }
        require(currentSide in state.legacyYBySide.indices) { "currentSide outside legacyYBySide" }
        require(currentSide in state.legacyZBySide.indices) { "currentSide outside legacyZBySide" }

        var next = state
        var goalRequested = false

        plan.mutations.forEach { mutation ->
            next = when (mutation) {
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_S_CURRENT -> next.copy(
                    legacyIBySide = incrementAt(next.legacyIBySide, currentSide),
                )

                LegacyMatchR3EventRoutingRules.Mutation.MATERIALIZE_GOAL_CURRENT -> {
                    materializeGoalCurrent()
                    goalRequested = true
                    next
                }

                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_Y_CURRENT -> next.copy(
                    legacyYBySide = incrementAt(next.legacyYBySide, currentSide),
                )

                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_PRIMARY_R0_P -> next.copy(
                    primaryLegacyR0P = next.primaryLegacyR0P?.plus(1),
                )

                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_Z_CURRENT -> next.copy(
                    legacyZBySide = incrementAt(next.legacyZBySide, currentSide),
                )
            }
        }

        return Result(
            state = next,
            goalMaterializationRequested = goalRequested,
        )
    }

    private fun incrementAt(values: List<Int>, index: Int): List<Int> =
        values.toMutableList().also { it[index] = it[index] + 1 }.toList()
}
