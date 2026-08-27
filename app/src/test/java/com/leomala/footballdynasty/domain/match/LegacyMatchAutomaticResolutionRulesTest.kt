package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchAutomaticResolutionRulesTest {
    @Test
    fun `true post gate consumes O draws after both added time draws`() {
        val random = QueueRandomSource(0, 0, 3, 3)
        val trace = mutableListOf<String>()

        val result = LegacyMatchAutomaticResolutionRules.run<String>(
            random = random,
            runMinuteRule = { _, _ -> },
            advanceR3 = { _, _ -> null },
            halftimeTransition = { _, _ -> },
            legacyZFlag = true,
            legacyA0Flag = true,
            resolveP0 = { trace += "P0"; true },
        )

        assertEquals(listOf("P0"), trace)
        assertEquals(listOf(3, 5, 7, 7), random.bounds)
        assertEquals(4L, random.draws)
        assertNotNull(result.legacyO)
        assertEquals(LegacyMatchPostGateORules.SelectedSide.LEGACY_E, result.legacyO!!.selectedSide)
        assertEquals(
            listOf(
                LegacyMatchAutomaticPostSimulationRules.Operation.INVOKE_LEGACY_O,
                LegacyMatchAutomaticPostSimulationRules.Operation.CLEAR_BOTH_CLUB_FLAGS,
            ),
            result.flow.postSimulation.operations,
        )
    }

    @Test
    fun `false P0 does not consume O draws`() {
        val random = QueueRandomSource(0, 0)

        val result = LegacyMatchAutomaticResolutionRules.run<String>(
            random = random,
            runMinuteRule = { _, _ -> },
            advanceR3 = { _, _ -> null },
            halftimeTransition = { _, _ -> },
            legacyZFlag = true,
            legacyA0Flag = true,
            resolveP0 = { false },
        )

        assertNull(result.legacyO)
        assertEquals(listOf(3, 5), random.bounds)
        assertEquals(2L, random.draws)
        assertFalse(result.flow.postSimulation.invokeLegacyO)
    }

    @Test
    fun `short circuited flags do not evaluate P0 or consume O draws`() {
        val random = QueueRandomSource(2, 4)
        var p0Calls = 0

        val result = LegacyMatchAutomaticResolutionRules.run<String>(
            random = random,
            runMinuteRule = { _, _ -> },
            advanceR3 = { _, _ -> null },
            halftimeTransition = { _, _ -> },
            legacyZFlag = false,
            legacyA0Flag = true,
            resolveP0 = { p0Calls++; true },
        )

        assertEquals(0, p0Calls)
        assertNull(result.legacyO)
        assertEquals(listOf(3, 5), random.bounds)
        assertTrue(result.flow.postSimulation.clearBothClubFlags)
    }

    @Test
    fun `O branch retains second half loop result and can select legacy F`() {
        val random = QueueRandomSource(1, 2, 0, 6)

        val result = LegacyMatchAutomaticResolutionRules.run<String>(
            random = random,
            runMinuteRule = { _, _ -> },
            advanceR3 = { _, _ -> null },
            halftimeTransition = { _, _ -> },
            legacyZFlag = true,
            legacyA0Flag = true,
            resolveP0 = { true },
        )

        assertEquals(1, result.flow.simulation.firstHalfAddedMinutes)
        assertEquals(3, result.flow.simulation.secondHalfAddedMinutes)
        assertEquals(LegacyMatchPostGateORules.SelectedSide.LEGACY_F, result.legacyO!!.selectedSide)
        assertEquals(listOf(2, 3), result.legacyO!!.legacyD0Values)
        assertEquals(listOf(3, 5, 7, 7), random.bounds)
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
