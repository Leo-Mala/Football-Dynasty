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

    /**
     * Exact constructor-owned state from every recovered `best.c0` constructor in the certified
     * 2026/27 corpus: `S = new int[]{0, 0, 0, 0}` and `T = false`/JVM false initialization.
     *
     * This is source evidence, not a modern fallback. It is valid only while no modern mutation of
     * `DialogTatics.j()` is exposed without persistence; once that mutation surface is wired, the
     * caller must load the persisted club-local state instead of recreating this constructor state.
     */
    fun constructorInitialState(): LegacyTacticsRawState = LegacyTacticsRawState(
        optionSlots = listOf(0, 0, 0, 0),
        checkboxT = false,
    )

    fun matchEngineTacticIndex(state: LegacyTacticsRawState): Int =
        state.optionSlots[MATCH_ENGINE_OPTION_SLOT]

    fun constructorInitialMatchEngineTacticIndex(): Int =
        matchEngineTacticIndex(constructorInitialState())
}
