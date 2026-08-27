package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchR3StepRulesTest {
    @Test
    fun `goal branch returns J and I plans and preserves two weighted draws`() {
        val random = TraceRandomSource(doubles = listOf(0.0, 0.0))

        val result = LegacyMatchR3StepRules.advance(
            currentSide = 0,
            metricYCurrent = 1.0,
            metricYOpposite = 1.0,
            metricUOpposite = 1.0,
            metricZCurrent = 1.0,
            legacyNeutralFlag = true,
            legacyGlobalJValue = 0,
            random = random,
            produceGoalEvent = {
                random.trace += "goal"
                "event"
            },
        )

        assertEquals(0, result.jDecision.returnedValue)
        assertEquals(0, result.iDecision?.returnedValue)
        assertEquals(
            listOf(
                LegacyMatchR3DecisionRules.Mutation.J_T_OPPOSITE,
                LegacyMatchR3DecisionRules.Mutation.J_S_CURRENT,
                LegacyMatchR3DecisionRules.Mutation.J_P_CURRENT,
                LegacyMatchR3DecisionRules.Mutation.J_APPLY_A_CURRENT,
            ),
            result.jDecision.mutations,
        )
        assertEquals(
            listOf(LegacyMatchR3DecisionRules.Mutation.I_O_CURRENT),
            result.iDecision?.mutations,
        )
        assertEquals("event", result.advance.event)
        assertEquals(LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_W_CURRENT, result.advance.counterMutation)
        assertFalse(result.advance.consumedDirectBound100Draw)
        assertEquals(listOf("double", "double", "goal"), random.trace)
        assertEquals(2L, random.draws)
    }

    @Test
    fun `J mismatch returns no I plan and uses direct bound one hundred after one weighted draw`() {
        val random = TraceRandomSource(
            doubles = listOf(0.99),
            ints = listOf(49),
        )

        val result = LegacyMatchR3StepRules.advance(
            currentSide = 0,
            metricYCurrent = 1.0,
            metricYOpposite = 1.0,
            metricUOpposite = 1.0,
            metricZCurrent = 1.0,
            legacyNeutralFlag = true,
            legacyGlobalJValue = 0,
            random = random,
            produceGoalEvent = { error("must not produce a goal event") },
        )

        assertEquals(1, result.jDecision.returnedValue)
        assertNull(result.iDecision)
        assertEquals(
            listOf(
                LegacyMatchR3DecisionRules.Mutation.J_T_CURRENT,
                LegacyMatchR3DecisionRules.Mutation.J_S_OPPOSITE,
                LegacyMatchR3DecisionRules.Mutation.J_Q_OPPOSITE,
                LegacyMatchR3DecisionRules.Mutation.J_APPLY_A_OPPOSITE,
            ),
            result.jDecision.mutations,
        )
        assertEquals(LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_Q0_OPPOSITE, result.advance.counterMutation)
        assertEquals(1, result.advance.counterSide)
        assertTrue(result.advance.consumedDirectBound100Draw)
        assertEquals(listOf("double", "int:100"), random.trace)
        assertEquals(2L, random.draws)
    }

    @Test
    fun `nonzero I plan is retained before direct current A0 branch`() {
        val random = TraceRandomSource(
            doubles = listOf(0.0, 0.75),
            ints = listOf(50),
        )

        val result = LegacyMatchR3StepRules.advance(
            currentSide = 0,
            metricYCurrent = 1.0,
            metricYOpposite = 1.0,
            metricUOpposite = 1.0,
            metricZCurrent = 1.0,
            legacyNeutralFlag = true,
            legacyGlobalJValue = 0,
            random = random,
            produceGoalEvent = { error("must not produce a goal event") },
        )

        assertEquals(0, result.jDecision.returnedValue)
        assertEquals(1, result.iDecision?.returnedValue)
        assertEquals(
            listOf(LegacyMatchR3DecisionRules.Mutation.I_R_OPPOSITE),
            result.iDecision?.mutations,
        )
        assertEquals(LegacyMatchR3AdvanceRules.CounterMutation.LEGACY_A0_CURRENT, result.advance.counterMutation)
        assertEquals(0, result.advance.counterSide)
        assertTrue(result.advance.consumedDirectBound100Draw)
        assertEquals(listOf("double", "double", "int:100"), random.trace)
        assertEquals(3L, random.draws)
    }

    private class TraceRandomSource(
        doubles: List<Double> = emptyList(),
        ints: List<Int> = emptyList(),
    ) : RandomSource {
        private val doubleQueue = doubles.toMutableList()
        private val intQueue = ints.toMutableList()
        val trace = mutableListOf<String>()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            check(intQueue.isNotEmpty()) { "No queued integer RNG value for bound=$bound" }
            val value = intQueue.removeAt(0)
            require(value in 0 until bound) { "value=$value bound=$bound" }
            trace += "int:$bound"
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")

        override fun nextDouble(): Double {
            check(doubleQueue.isNotEmpty()) { "No queued double RNG value" }
            val value = doubleQueue.removeAt(0)
            require(value >= 0.0 && value < 1.0) { "value=$value" }
            trace += "double"
            draws++
            return value
        }
    }
}
