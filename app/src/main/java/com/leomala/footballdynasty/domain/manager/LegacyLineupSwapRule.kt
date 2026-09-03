package com.leomala.footballdynasty.domain.manager

/**
 * Exact player-slot mutations from the readable `ActivityEscalacao.U/V/W` methods in the official
 * legacy corpus.
 *
 * `lineupSnapshotWriteCount` preserves calls to legacy `Q()`: U writes zero snapshots, V writes
 * one through `T()`, and W writes twice because `T()` writes once and W calls Q again. The duplicate
 * write is intentionally retained as observable legacy ordering rather than optimized away.
 */
data class LegacyLineupSlotState<T>(
    val starters: List<T?>,
    val bench: List<T?>,
)

data class LegacyLineupSwapResult<T>(
    val state: LegacyLineupSlotState<T>,
    val lineupSnapshotWriteCount: Int,
)

object LegacyLineupSwapRule {
    fun <T> reorderBenchU(
        state: LegacyLineupSlotState<T>,
        firstBenchIndex: Int,
        secondBenchIndex: Int,
    ): LegacyLineupSwapResult<T> {
        val bench = state.bench.toMutableList()
        swap(bench, firstBenchIndex, secondBenchIndex)
        return LegacyLineupSwapResult(
            state = state.copy(bench = bench),
            lineupSnapshotWriteCount = 0,
        )
    }

    fun <T> swapStarterWithBenchV(
        state: LegacyLineupSlotState<T>,
        starterIndex: Int,
        benchIndex: Int,
    ): LegacyLineupSwapResult<T> {
        val starters = state.starters.toMutableList()
        val bench = state.bench.toMutableList()
        val starter = starters[starterIndex]
        starters[starterIndex] = bench[benchIndex]
        bench[benchIndex] = starter
        return LegacyLineupSwapResult(
            state = LegacyLineupSlotState(starters = starters, bench = bench),
            lineupSnapshotWriteCount = 1,
        )
    }

    fun <T> swapStartersW(
        state: LegacyLineupSlotState<T>,
        firstStarterIndex: Int,
        secondStarterIndex: Int,
    ): LegacyLineupSwapResult<T> {
        val starters = state.starters.toMutableList()
        swap(starters, firstStarterIndex, secondStarterIndex)
        return LegacyLineupSwapResult(
            state = state.copy(starters = starters),
            lineupSnapshotWriteCount = 2,
        )
    }

    private fun <T> swap(list: MutableList<T?>, firstIndex: Int, secondIndex: Int) {
        val first = list[firstIndex]
        list[firstIndex] = list[secondIndex]
        list[secondIndex] = first
    }
}
