package com.leomala.footballdynasty.domain.match

/**
 * Exact pure state/mutation projection of legacy transient `components.n2`.
 *
 * `n2` is match-only state: eleven integer fields (`a..k`) initialized to zero. The deliberately
 * raw names below preserve bytecode ownership while Phase 16 connects the already-recovered match
 * events to the final `best.o.n(...) -> O/y0` rating boundary.
 */
object LegacyMatchN2CounterRules {
    data class State(
        val rawA: Int = 0,
        val rawB: Int = 0,
        val rawC: Int = 0,
        val rawD: Int = 0,
        val rawE: Int = 0,
        val rawF: Int = 0,
        val rawG: Int = 0,
        val rawH: Int = 0,
        val rawI: Int = 0,
        val rawJ: Int = 0,
        val rawK: Int = 0,
    )

    enum class Mutation {
        LEGACY_L,
        LEGACY_M,
        LEGACY_N,
        LEGACY_O,
        LEGACY_P,
        LEGACY_Q,
        LEGACY_R,
        LEGACY_S,
        LEGACY_T,
        LEGACY_U,
        LEGACY_V,
    }

    /** Legacy getter methods `a()..k()` are intentionally not field-order getters. */
    data class GetterSnapshot(
        val legacyA: Int,
        val legacyB: Int,
        val legacyC: Int,
        val legacyD: Int,
        val legacyE: Int,
        val legacyF: Int,
        val legacyG: Int,
        val legacyH: Int,
        val legacyI: Int,
        val legacyJ: Int,
        val legacyK: Int,
    )

    fun snapshot(state: State): GetterSnapshot = GetterSnapshot(
        legacyA = state.rawJ,
        legacyB = state.rawH,
        legacyC = state.rawI,
        legacyD = state.rawA,
        legacyE = state.rawB,
        legacyF = state.rawC,
        legacyG = state.rawD,
        legacyH = state.rawE,
        legacyI = state.rawF,
        legacyJ = state.rawK,
        legacyK = state.rawG,
    )

    fun apply(state: State, mutation: Mutation): State = when (mutation) {
        Mutation.LEGACY_L -> state.copy(rawJ = state.rawJ + 1)
        Mutation.LEGACY_M -> state.copy(rawH = state.rawH + 1)
        Mutation.LEGACY_N -> state.copy(rawI = state.rawI + 1)
        Mutation.LEGACY_O -> state.copy(rawA = state.rawA + 1)
        Mutation.LEGACY_P -> state.copy(rawB = state.rawB + 1)
        Mutation.LEGACY_Q -> state.copy(rawC = state.rawC + 1)
        Mutation.LEGACY_R -> state.copy(rawD = state.rawD + 1)
        // Legacy `s()` increments both field e and field a, in that order.
        Mutation.LEGACY_S -> state.copy(rawE = state.rawE + 1, rawA = state.rawA + 1)
        Mutation.LEGACY_T -> state.copy(rawF = state.rawF + 1)
        Mutation.LEGACY_U -> state.copy(rawK = state.rawK + 1)
        Mutation.LEGACY_V -> state.copy(rawG = state.rawG + 1)
    }

    fun applyAll(state: State, mutations: Iterable<Mutation>): State =
        mutations.fold(state, ::apply)
}
