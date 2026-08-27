package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchR3MutationApplicationRulesTest {
    @Test
    fun `goal outcome applies i then goal callback then y only to current side`() {
        var goalCalls = 0
        val result = LegacyMatchR3MutationApplicationRules.apply(
            currentSide = 1,
            plan = plan(
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_S_CURRENT,
                LegacyMatchR3EventRoutingRules.Mutation.MATERIALIZE_GOAL_CURRENT,
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_Y_CURRENT,
            ),
            state = state(),
            materializeGoalCurrent = { goalCalls++ },
        )

        assertEquals(listOf(3, 5), result.state.legacyIBySide)
        assertEquals(listOf(7, 12), result.state.legacyYBySide)
        assertEquals(listOf(13, 17), result.state.legacyZBySide)
        assertEquals(1, goalCalls)
        assertTrue(result.goalMaterializationRequested)
    }

    @Test
    fun `primary counter increments only when plan contains proven primary mutation`() {
        val result = LegacyMatchR3MutationApplicationRules.apply(
            currentSide = 0,
            plan = plan(
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_S_CURRENT,
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_Y_CURRENT,
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_PRIMARY_R0_P,
            ),
            state = state(primaryP = 19),
        )

        assertEquals(listOf(4, 4), result.state.legacyIBySide)
        assertEquals(listOf(8, 11), result.state.legacyYBySide)
        assertEquals(20, result.state.primaryLegacyR0P)
        assertFalse(result.goalMaterializationRequested)
    }

    @Test
    fun `null primary counter is not fabricated when applying primary mutation`() {
        val result = LegacyMatchR3MutationApplicationRules.apply(
            currentSide = 0,
            plan = plan(LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_PRIMARY_R0_P),
            state = state(primaryP = null),
        )

        assertEquals(null, result.state.primaryLegacyR0P)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `side outside recovered arrays is rejected instead of normalized`() {
        LegacyMatchR3MutationApplicationRules.apply(
            currentSide = 2,
            plan = plan(LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_S_CURRENT),
            state = state(),
        )
    }

    private fun state(primaryP: Int? = 23) = LegacyMatchR3MutationApplicationRules.State(
        legacyIBySide = listOf(3, 4),
        legacyYBySide = listOf(7, 11),
        legacyZBySide = listOf(13, 17),
        primaryLegacyR0P = primaryP,
    )

    private fun plan(vararg mutations: LegacyMatchR3EventRoutingRules.Mutation) =
        LegacyMatchR3EventRoutingRules.Result(
            selectedIndex = -1,
            weightTable = LegacyMatchR3EventRoutingRules.WeightTable.B0,
            multipliers = listOf(1.0, 1.0, 1.0),
            storedLegacyGAfter = 0.0,
            sIncrementTiming = LegacyMatchR3EventRoutingRules.SIncrementTiming.AFTER_WEIGHTED_DRAW,
            mutations = mutations.toList(),
        )
}
