package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchRatingMetricRuntimeRulesTest {
    @Test
    fun `J apply-a mutations preserve B0 identity and recompute legacy E percentages`() {
        var state = LegacyMatchRatingMetricRuntimeRules.State()

        state = LegacyMatchRatingMetricRuntimeRules.applyJMutations(
            state = state,
            currentSide = 0,
            mutations = listOf(LegacyMatchR3DecisionRules.Mutation.J_APPLY_A_CURRENT),
        )
        assertEquals(listOf(1, 0), state.legacyB0BySide)
        assertEquals(listOf(100, 0), state.legacyEBySide)

        state = LegacyMatchRatingMetricRuntimeRules.applyJMutations(
            state = state,
            currentSide = 0,
            mutations = listOf(LegacyMatchR3DecisionRules.Mutation.J_APPLY_A_OPPOSITE),
        )
        assertEquals(listOf(1, 1), state.legacyB0BySide)
        assertEquals(listOf(50, 50), state.legacyEBySide)
    }

    @Test
    fun `K counter outcomes update only rating arrays proven by the selected branch`() {
        val initial = LegacyMatchRatingMetricRuntimeRules.State()
        val goal = LegacyMatchRatingMetricRuntimeRules.applyAdvanceCounter(
            state = initial,
            mutation = LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_W_CURRENT,
            counterSide = 0,
        )
        assertEquals(listOf(1, 0), goal.legacyWBySide)
        assertEquals(listOf(0, 0), goal.legacyQ0BySide)

        val noGoal = LegacyMatchRatingMetricRuntimeRules.applyAdvanceCounter(
            state = goal,
            mutation = LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_Q0_OPPOSITE,
            counterSide = 1,
        )
        assertEquals(listOf(1, 0), noGoal.legacyWBySide)
        assertEquals(listOf(0, 1), noGoal.legacyQ0BySide)

        val unrelatedA0 = LegacyMatchRatingMetricRuntimeRules.applyAdvanceCounter(
            state = noGoal,
            mutation = LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_A0_CURRENT,
            counterSide = 0,
        )
        assertEquals(noGoal, unrelatedA0)
    }

    @Test
    fun `b-c routing increments y and side-normalized rating view uses opponent arrays`() {
        val updated = LegacyMatchRatingMetricRuntimeRules.applyEventMutations(
            state = LegacyMatchRatingMetricRuntimeRules.State(
                legacyEBySide = listOf(60, 40),
                legacyQ0BySide = listOf(3, 5),
                legacyWBySide = listOf(2, 4),
                legacyYBySide = listOf(6, 8),
            ),
            currentSide = 1,
            mutations = listOf(
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_Y_CURRENT,
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_PRIMARY_R0_P,
            ),
        )

        assertEquals(listOf(6, 9), updated.legacyYBySide)
        assertEquals(
            LegacyMatchRatingMetricRuntimeRules.RatingInputs(
                currentLegacyE = 60,
                opponentLegacyE = 40,
                currentLegacyQ0 = 3,
                opponentLegacyQ0 = 5,
                opponentLegacyW = 4,
                opponentLegacyY = 9,
            ),
            LegacyMatchRatingMetricRuntimeRules.ratingInputs(updated, currentSide = 0),
        )
    }
}
