package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyLineupSwapRuleTest {
    private val state = LegacyLineupSlotState(
        starters = listOf<String?>("S0", "S1", "S2"),
        bench = listOf<String?>("B0", "B1", "B2"),
    )

    @Test
    fun UReordersOnlyTheBenchAndDoesNotWriteLineupSnapshot() {
        val result = LegacyLineupSwapRule.reorderBenchU(state, 0, 2)

        assertEquals(listOf("S0", "S1", "S2"), result.state.starters)
        assertEquals(listOf("B2", "B1", "B0"), result.state.bench)
        assertEquals(0, result.lineupSnapshotWriteCount)
    }

    @Test
    fun VSwapsStarterAndBenchPlayerAndWritesSnapshotOnce() {
        val result = LegacyLineupSwapRule.swapStarterWithBenchV(state, 1, 2)

        assertEquals(listOf("S0", "B2", "S2"), result.state.starters)
        assertEquals(listOf("B0", "B1", "S1"), result.state.bench)
        assertEquals(1, result.lineupSnapshotWriteCount)
    }

    @Test
    fun WSwapsOnlyStarterPlayersAndPreservesTheLegacyDuplicateSnapshotWrite() {
        val result = LegacyLineupSwapRule.swapStartersW(state, 0, 2)

        assertEquals(listOf("S2", "S1", "S0"), result.state.starters)
        assertEquals(listOf("B0", "B1", "B2"), result.state.bench)
        assertEquals(2, result.lineupSnapshotWriteCount)
    }

    @Test
    fun nullSlotsAreMovedWithoutInventingEligibilityOrFallbackPlayers() {
        val withNull = LegacyLineupSlotState(
            starters = listOf<String?>(null, "S1"),
            bench = listOf<String?>("B0", null),
        )

        val result = LegacyLineupSwapRule.swapStarterWithBenchV(withNull, 0, 0)

        assertEquals(listOf("B0", "S1"), result.state.starters)
        assertEquals(listOf(null, null), result.state.bench)
    }

    @Test
    fun sameIndexSwapIsAStateNoOpButPreservesLegacySnapshotWriteCount() {
        val result = LegacyLineupSwapRule.swapStartersW(state, 1, 1)

        assertEquals(state, result.state)
        assertEquals(2, result.lineupSnapshotWriteCount)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun invalidIndexIsNotSilentlyClamped() {
        LegacyLineupSwapRule.reorderBenchU(state, 0, 99)
    }
}
