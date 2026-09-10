package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchR3DecisionRuntimeRulesTest {
    @Test
    fun `legacy h consumes one boolean and initializes recovered side`() {
        val home = LegacyMatchR3DecisionRuntimeRules.initialize(BooleanRandom(true))
        val away = LegacyMatchR3DecisionRuntimeRules.initialize(BooleanRandom(false))

        assertEquals(0, home.currentSide)
        assertEquals(1, away.currentSide)
    }

    @Test
    fun `J current branch applies internal counters and best s A mutation`() {
        val result = LegacyMatchR3DecisionRuntimeRules.applyJ(
            state = LegacyMatchR3DecisionRuntimeRules.State(currentSide = 0),
            decision = decision(
                returnedValue = 0,
                LegacyMatchR3DecisionRules.Mutation.J_T_OPPOSITE,
                LegacyMatchR3DecisionRules.Mutation.J_S_CURRENT,
                LegacyMatchR3DecisionRules.Mutation.J_P_CURRENT,
                LegacyMatchR3DecisionRules.Mutation.J_APPLY_A_CURRENT,
            ),
        )

        assertEquals(listOf(1, 0), result.legacyPBySide)
        assertEquals(listOf(1, 0), result.legacySBySide)
        assertEquals(listOf(0, 1), result.legacyTBySide)
        assertEquals(listOf(1, 0), result.legacyB0BySide)
        assertEquals(listOf(100, 0), result.legacyEBySide)
    }

    @Test
    fun `J opposite branch applies opposite counters and percentage recomputation`() {
        val seeded = LegacyMatchR3DecisionRuntimeRules.State(
            currentSide = 0,
            legacyB0BySide = listOf(1, 0),
            legacyEBySide = listOf(100, 0),
        )
        val result = LegacyMatchR3DecisionRuntimeRules.applyJ(
            state = seeded,
            decision = decision(
                returnedValue = 1,
                LegacyMatchR3DecisionRules.Mutation.J_T_CURRENT,
                LegacyMatchR3DecisionRules.Mutation.J_S_OPPOSITE,
                LegacyMatchR3DecisionRules.Mutation.J_Q_OPPOSITE,
                LegacyMatchR3DecisionRules.Mutation.J_APPLY_A_OPPOSITE,
            ),
        )

        assertEquals(listOf(0, 1), result.legacyQBySide)
        assertEquals(listOf(0, 1), result.legacySBySide)
        assertEquals(listOf(1, 0), result.legacyTBySide)
        assertEquals(listOf(1, 1), result.legacyB0BySide)
        assertEquals(listOf(50, 50), result.legacyEBySide)
    }

    @Test
    fun `I applies recovered counter and stored g then toggle mirrors L`() {
        val applied = LegacyMatchR3DecisionRuntimeRules.applyI(
            state = LegacyMatchR3DecisionRuntimeRules.State(currentSide = 1),
            decision = LegacyMatchR3DecisionRules.DecisionResult(
                weightedIndex = 0,
                returnedValue = 0,
                firstModifier = 1.0,
                secondModifier = 1.0,
                storedLegacyG = 2.75,
                mutations = listOf(LegacyMatchR3DecisionRules.Mutation.I_O_CURRENT),
            ),
        )

        assertEquals(listOf(0, 1), applied.legacyOBySide)
        assertEquals(2.75, applied.storedLegacyG, 0.0)
        assertEquals(0, LegacyMatchR3DecisionRuntimeRules.toggleSide(applied).currentSide)
    }

    @Test
    fun `I opposite branch increments recovered R counter`() {
        val applied = LegacyMatchR3DecisionRuntimeRules.applyI(
            state = LegacyMatchR3DecisionRuntimeRules.State(currentSide = 0),
            decision = LegacyMatchR3DecisionRules.DecisionResult(
                weightedIndex = 1,
                returnedValue = 1,
                firstModifier = 1.0,
                secondModifier = 1.0,
                storedLegacyG = 0.5,
                mutations = listOf(LegacyMatchR3DecisionRules.Mutation.I_R_OPPOSITE),
            ),
        )

        assertEquals(listOf(0, 1), applied.legacyRBySide)
        assertEquals(0.5, applied.storedLegacyG, 0.0)
    }

    private fun decision(
        returnedValue: Int,
        vararg mutations: LegacyMatchR3DecisionRules.Mutation,
    ) = LegacyMatchR3DecisionRules.DecisionResult(
        weightedIndex = if (returnedValue == 0) 0 else 1,
        returnedValue = returnedValue,
        firstModifier = 1.0,
        secondModifier = 1.0,
        storedLegacyG = null,
        mutations = mutations.toList(),
    )

    private class BooleanRandom(
        private val value: Boolean,
    ) : RandomSource {
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int = error("not used")

        override fun nextBoolean(): Boolean {
            draws++
            return value
        }

        override fun nextDouble(): Double = error("not used")
    }
}
