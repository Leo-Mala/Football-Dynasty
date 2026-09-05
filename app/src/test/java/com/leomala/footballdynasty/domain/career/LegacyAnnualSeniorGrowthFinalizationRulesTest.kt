package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyAnnualSeniorGrowthFinalizationRulesTest {
    @Test
    fun `strict threshold does not grow at exactly one`() {
        val random = FixedIntRandomSource()
        val result = LegacyAnnualSeniorGrowthFinalizationRules.apply(
            LegacyAnnualSeniorGrowthFinalizationRules.Input(
                overall = 60,
                legacyN = 1.0,
                cappedTarget = 80,
                d0 = 59,
                m = 7,
            ),
            random,
        )

        assertEquals(60, result.overall)
        assertEquals(1.0, result.legacyN, 0.0)
        assertEquals(80, result.effectiveTarget)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `growth consumes exactly one accumulated point per invocation`() {
        val random = FixedIntRandomSource()
        val result = LegacyAnnualSeniorGrowthFinalizationRules.apply(
            LegacyAnnualSeniorGrowthFinalizationRules.Input(
                overall = 60,
                legacyN = 2.75,
                cappedTarget = 80,
                d0 = 0,
                m = 1,
            ),
            random,
        )

        assertEquals(61, result.overall)
        assertEquals(1.75, result.legacyN, 1e-12)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `cap block collapses excess accumulator to exactly one`() {
        val random = FixedIntRandomSource()
        val result = LegacyAnnualSeniorGrowthFinalizationRules.apply(
            LegacyAnnualSeniorGrowthFinalizationRules.Input(
                overall = 80,
                legacyN = 4.5,
                cappedTarget = 80,
                d0 = 0,
                m = 1,
            ),
            random,
        )

        assertEquals(80, result.overall)
        assertEquals(1.0, result.legacyN, 0.0)
    }

    @Test
    fun `overall one hundred guard preserves accumulator`() {
        val random = FixedIntRandomSource()
        val result = LegacyAnnualSeniorGrowthFinalizationRules.apply(
            LegacyAnnualSeniorGrowthFinalizationRules.Input(
                overall = 100,
                legacyN = 4.5,
                cappedTarget = 100,
                d0 = 0,
                m = 1,
            ),
            random,
        )

        assertEquals(100, result.overall)
        assertEquals(4.5, result.legacyN, 0.0)
    }

    @Test
    fun `high d0 adjustment is applied before cap comparison and consumes one draw`() {
        val random = FixedIntRandomSource(4)
        val result = LegacyAnnualSeniorGrowthFinalizationRules.apply(
            LegacyAnnualSeniorGrowthFinalizationRules.Input(
                overall = 80,
                legacyN = 1.25,
                cappedTarget = 80,
                d0 = 60,
                m = 7,
            ),
            random,
        )

        assertEquals(81, result.overall)
        assertEquals(0.25, result.legacyN, 1e-12)
        assertEquals(89, result.effectiveTarget)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `high d0 still consumes draw when m has no target bonus`() {
        val random = FixedIntRandomSource(4)
        val result = LegacyAnnualSeniorGrowthFinalizationRules.apply(
            LegacyAnnualSeniorGrowthFinalizationRules.Input(
                overall = 80,
                legacyN = 1.25,
                cappedTarget = 80,
                d0 = 60,
                m = 6,
            ),
            random,
        )

        assertEquals(80, result.overall)
        assertEquals(1.0, result.legacyN, 0.0)
        assertEquals(80, result.effectiveTarget)
        assertEquals(1L, random.draws)
    }

    private class FixedIntRandomSource(
        vararg values: Int,
    ) : RandomSource {
        private val iterator = values.iterator()

        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            val value = iterator.nextInt()
            require(value in 0 until bound) { "value=$value bound=$bound" }
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used by this characterization")

        override fun nextDouble(): Double = error("not used by this characterization")
    }
}
