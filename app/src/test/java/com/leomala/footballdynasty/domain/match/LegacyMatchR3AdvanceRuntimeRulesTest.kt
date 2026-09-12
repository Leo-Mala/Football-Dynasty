package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyMatchR3AdvanceRuntimeRulesTest {
    @Test
    fun `goal path increments tick and legacy w while preserving event and next side`() {
        val result = LegacyMatchR3AdvanceRuntimeRules.apply(
            state = LegacyMatchR3AdvanceRuntimeRules.State(),
            advance = advance(
                mutation = LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_W_CURRENT,
                side = 0,
                event = "goal",
                nextSide = 1,
            ),
        )

        assertEquals(1, result.state.legacyTick)
        assertEquals(listOf(1, 0), result.state.legacyWBySide)
        assertEquals(listOf(0, 0), result.state.legacyQ0BySide)
        assertEquals(listOf(0, 0), result.state.legacyA0BySide)
        assertEquals("goal", result.event)
        assertEquals(1, result.nextSide)
    }

    @Test
    fun `direct lower draw increments opposite legacy q0 only`() {
        val result = LegacyMatchR3AdvanceRuntimeRules.apply(
            state = LegacyMatchR3AdvanceRuntimeRules.State(
                legacyTick = 4,
                legacyQ0BySide = listOf(2, 3),
            ),
            advance = advance<String>(
                mutation = LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_Q0_OPPOSITE,
                side = 1,
                event = null,
                nextSide = 1,
            ),
        )

        assertEquals(5, result.state.legacyTick)
        assertEquals(listOf(2, 4), result.state.legacyQ0BySide)
        assertEquals(listOf(0, 0), result.state.legacyWBySide)
        assertEquals(listOf(0, 0), result.state.legacyA0BySide)
        assertNull(result.event)
    }

    @Test
    fun `direct upper draw increments current legacy a0 only`() {
        val result = LegacyMatchR3AdvanceRuntimeRules.apply(
            state = LegacyMatchR3AdvanceRuntimeRules.State(
                legacyTick = 9,
                legacyA0BySide = listOf(5, 7),
            ),
            advance = advance<String>(
                mutation = LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_A0_CURRENT,
                side = 1,
                event = null,
                nextSide = 0,
            ),
        )

        assertEquals(10, result.state.legacyTick)
        assertEquals(listOf(5, 8), result.state.legacyA0BySide)
        assertEquals(listOf(0, 0), result.state.legacyWBySide)
        assertEquals(listOf(0, 0), result.state.legacyQ0BySide)
        assertEquals(0, result.nextSide)
    }

    @Test
    fun `non incrementing plan preserves tick`() {
        val result = LegacyMatchR3AdvanceRuntimeRules.apply(
            state = LegacyMatchR3AdvanceRuntimeRules.State(legacyTick = 6),
            advance = LegacyMatchR3AdvanceRules.Result<String>(
                incrementTick = false,
                counterMutation = LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_A0_CURRENT,
                counterSide = 0,
                event = null,
                nextSide = 1,
                consumedDirectBound100Draw = true,
            ),
        )

        assertEquals(6, result.state.legacyTick)
        assertEquals(listOf(1, 0), result.state.legacyA0BySide)
    }

    private fun <T> advance(
        mutation: LegacyMatchR3AdvanceRules.CounterMutation,
        side: Int,
        event: T?,
        nextSide: Int,
    ) = LegacyMatchR3AdvanceRules.Result(
        incrementTick = true,
        counterMutation = mutation,
        counterSide = side,
        event = event,
        nextSide = nextSide,
        consumedDirectBound100Draw = mutation != LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_W_CURRENT,
    )
}
