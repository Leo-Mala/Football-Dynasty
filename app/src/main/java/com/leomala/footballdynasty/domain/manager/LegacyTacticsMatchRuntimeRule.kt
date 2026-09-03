package com.leomala.footballdynasty.domain.manager

/**
 * Exact bridge between the characterized `best.c0.S` tactics array and legacy `best.s.k(...)`.
 *
 * `DialogTatics.j()` writes the club array through `best.c0.x1(index,value)`. The match engine then
 * reads `best.c0.i0()[2]` for the selected side before applying its own legacy >=3 -> 0 fallback.
 * This rule therefore returns the raw slot-2 value without pre-normalizing or inventing defaults.
 */
object LegacyTacticsMatchRuntimeRule {
    const val MATCH_ENGINE_OPTION_SLOT: Int = 2

    fun matchEngineTacticIndex(state: LegacyTacticsRawState): Int =
        state.optionSlots[MATCH_ENGINE_OPTION_SLOT]
}
