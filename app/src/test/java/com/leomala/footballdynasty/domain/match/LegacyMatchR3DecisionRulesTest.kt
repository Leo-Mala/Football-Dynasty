package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyMatchR3DecisionRulesTest {
    @Test
    fun `difference uses eight below five and eleven from five onward`() {
        assertEquals(1.0, LegacyMatchR3DecisionRules.difference(8.0, 0.0, 4), 0.0)
        assertEquals(1.0, LegacyMatchR3DecisionRules.difference(11.0, 0.0, 5), 0.0)
        assertEquals(1.0, LegacyMatchR3DecisionRules.difference(11.0, 0.0, 9), 0.0)
    }

    @Test
    fun `legacy divisor twelve branch remains unreachable at nine`() {
        val value = LegacyMatchR3DecisionRules.difference(12.0, 0.0, 9)

        assertEquals(12.0 / 11.0, value, 1e-12)
    }

    @Test
    fun `J index zero returns current side and preserves mutation order`() {
        val random = DoubleQueueRandomSource(0.0)

        val result = LegacyMatchR3DecisionRules.resolveJ(
            currentSide = 0,
            metricYCurrent = 1.0,
            metricYOpposite = 1.0,
            legacyNeutralFlag = true,
            legacyGlobalJValue = 0,
            random = random,
        )

        assertEquals(0, result.weightedIndex)
        assertEquals(0, result.returnedValue)
        assertEquals(1.0, result.firstModifier, 0.0)
        assertEquals(1.0, result.secondModifier, 0.0)
        assertNull(result.storedLegacyG)
        assertEquals(
            listOf(
                LegacyMatchR3DecisionRules.Mutation.J_T_OPPOSITE,
                LegacyMatchR3DecisionRules.Mutation.J_S_CURRENT,
                LegacyMatchR3DecisionRules.Mutation.J_P_CURRENT,
                LegacyMatchR3DecisionRules.Mutation.J_APPLY_A_CURRENT,
            ),
            result.mutations,
        )
        assertEquals(1L, random.draws)
    }

    @Test
    fun `J exact fifty five percent boundary advances to index one due strict weighted comparison`() {
        val result = LegacyMatchR3DecisionRules.resolveJ(
            currentSide = 0,
            metricYCurrent = 1.0,
            metricYOpposite = 1.0,
            legacyNeutralFlag = true,
            legacyGlobalJValue = 0,
            random = DoubleQueueRandomSource(0.55),
        )

        assertEquals(1, result.weightedIndex)
        assertEquals(1, result.returnedValue)
        assertEquals(
            listOf(
                LegacyMatchR3DecisionRules.Mutation.J_T_CURRENT,
                LegacyMatchR3DecisionRules.Mutation.J_S_OPPOSITE,
                LegacyMatchR3DecisionRules.Mutation.J_Q_OPPOSITE,
                LegacyMatchR3DecisionRules.Mutation.J_APPLY_A_OPPOSITE,
            ),
            result.mutations,
        )
    }

    @Test
    fun `J side zero receives point three modifier only when legacy neutral flag is false`() {
        val boosted = LegacyMatchR3DecisionRules.resolveJ(
            0, 1.0, 1.0, false, 0, DoubleQueueRandomSource(0.0),
        )
        val neutral = LegacyMatchR3DecisionRules.resolveJ(
            0, 1.0, 1.0, true, 0, DoubleQueueRandomSource(0.0),
        )
        val sideOne = LegacyMatchR3DecisionRules.resolveJ(
            1, 1.0, 1.0, false, 0, DoubleQueueRandomSource(0.0),
        )

        assertEquals(1.3, boosted.firstModifier, 1e-12)
        assertEquals(1.0, neutral.firstModifier, 0.0)
        assertEquals(1.0, sideOne.firstModifier, 0.0)
    }

    @Test
    fun `J modifiers clamp independently at point two`() {
        val result = LegacyMatchR3DecisionRules.resolveJ(
            currentSide = 0,
            metricYCurrent = -100.0,
            metricYOpposite = 100.0,
            legacyNeutralFlag = true,
            legacyGlobalJValue = 0,
            random = DoubleQueueRandomSource(0.0),
        )

        assertEquals(0.2, result.firstModifier, 0.0)
        assertEquals(26.0, result.secondModifier, 0.0)
    }

    @Test
    fun `I stores opposite u metric and index zero mutates current o`() {
        val result = LegacyMatchR3DecisionRules.resolveI(
            currentSide = 0,
            metricUOpposite = 1.0,
            metricZCurrent = 1.0,
            legacyNeutralFlag = true,
            legacyGlobalJValue = 0,
            random = DoubleQueueRandomSource(0.0),
        )

        assertEquals(0, result.returnedValue)
        assertEquals(1.0, result.storedLegacyG!!, 0.0)
        assertEquals(listOf(LegacyMatchR3DecisionRules.Mutation.I_O_CURRENT), result.mutations)
    }

    @Test
    fun `I exact half boundary selects index one and opposite r mutation`() {
        val result = LegacyMatchR3DecisionRules.resolveI(
            1, 1.0, 1.0, true, 0, DoubleQueueRandomSource(0.5),
        )

        assertEquals(1, result.weightedIndex)
        assertEquals(1, result.returnedValue)
        assertEquals(listOf(LegacyMatchR3DecisionRules.Mutation.I_R_OPPOSITE), result.mutations)
    }

    @Test
    fun `I zero u resets second to point one then clamp makes point two`() {
        val result = LegacyMatchR3DecisionRules.resolveI(
            1, 0.0, 1.0, true, 0, DoubleQueueRandomSource(0.0),
        )

        assertEquals(0.2, result.secondModifier, 0.0)
        assertEquals(0.0, result.storedLegacyG!!, 0.0)
    }

    @Test
    fun `I zero z resets first after side zero bonus so final clamp is point two`() {
        val result = LegacyMatchR3DecisionRules.resolveI(
            currentSide = 0,
            metricUOpposite = 1.0,
            metricZCurrent = 0.0,
            legacyNeutralFlag = false,
            legacyGlobalJValue = 0,
            random = DoubleQueueRandomSource(0.0),
        )

        assertEquals(0.2, result.firstModifier, 0.0)
    }

    private class DoubleQueueRandomSource(vararg values: Double) : RandomSource {
        private val queue = values.toMutableList()
        override var draws: Long = 0
            private set

        override fun nextDouble(): Double {
            check(queue.isNotEmpty()) { "No queued nextDouble value" }
            val value = queue.removeAt(0)
            require(value >= 0.0 && value < 1.0) { "value=$value" }
            draws++
            return value
        }

        override fun nextInt(bound: Int): Int = error("not used")
        override fun nextBoolean(): Boolean = error("not used")
    }
}
