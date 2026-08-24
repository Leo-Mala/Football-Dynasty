package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyCareerPlayerInitializationRulesTest {
    @Test
    fun `q1 preserves target bands and f0 remap`() {
        assertEquals(20, LegacyCareerPlayerInitializationRules.targetBand(true, 1, 0))
        assertEquals(15, LegacyCareerPlayerInitializationRules.targetBand(true, 2, 0))
        assertEquals(5, LegacyCareerPlayerInitializationRules.targetBand(true, 3, 0))
        assertEquals(1, LegacyCareerPlayerInitializationRules.targetBand(true, 4, 0))
        assertEquals(5, LegacyCareerPlayerInitializationRules.targetBand(false, 0, 3))
        assertEquals(15, LegacyCareerPlayerInitializationRules.targetBand(false, 0, 4))
        assertEquals(22, LegacyCareerPlayerInitializationRules.targetBand(false, 0, 5))
        assertEquals(17, LegacyCareerPlayerInitializationRules.mappedTargetF0(16))
        assertEquals(30, LegacyCareerPlayerInitializationRules.mappedTargetF0(25))
        assertEquals(0, LegacyCareerPlayerInitializationRules.mappedTargetF0(26))
    }

    @Test
    fun `q1 base path consumes rating and contract draws only`() {
        val random = QueueRandomSource(2, 29)
        val result = LegacyCareerPlayerInitializationRules.initialize(
            random,
            LegacyCareerPlayerInitializationRules.Input(
                targetR0 = false,
                targetO = 0,
                targetP0 = 4,
                targetF0 = 20,
                playerLegacyE = 3,
                playerStar = false,
                playerWorldTop = false,
            ),
        )
        assertEquals(42, result.overall)
        assertEquals(239L, result.contractDays)
        assertEquals(listOf(3, 30), random.bounds)
    }

    @Test
    fun `q1 legacy E one consumes its conditional draw before star draw`() {
        val random = QueueRandomSource(1, 1, 2, 0)
        val result = LegacyCareerPlayerInitializationRules.initialize(
            random,
            LegacyCareerPlayerInitializationRules.Input(
                targetR0 = true,
                targetO = 1,
                targetP0 = 0,
                targetF0 = 25,
                playerLegacyE = 1,
                playerStar = true,
                playerWorldTop = false,
            ),
        )
        assertEquals(71, result.overall)
        assertEquals(210L, result.contractDays)
        assertEquals(listOf(3, 2, 3, 30), random.bounds)
    }

    @Test
    fun `q1 world top shares the same bonus branch as star`() {
        val random = QueueRandomSource(0, 0, 0)
        val result = LegacyCareerPlayerInitializationRules.initialize(
            random,
            LegacyCareerPlayerInitializationRules.Input(
                targetR0 = false,
                targetO = 0,
                targetP0 = 5,
                targetF0 = 25,
                playerLegacyE = 0,
                playerStar = false,
                playerWorldTop = true,
            ),
        )
        assertEquals(61, result.overall)
        assertEquals(listOf(3, 3, 30), random.bounds)
    }

    private class QueueRandomSource(vararg values: Int) : RandomSource {
        private val values = values.toMutableList()
        val bounds = mutableListOf<Int>()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            val value = values.removeAt(0)
            require(value in 0 until bound) { "value=$value bound=$bound" }
            bounds += bound
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}
