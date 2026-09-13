package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Mutable-state boundary for the already-characterized internal mutations of legacy
 * `components.r3.J()` and `I()`.
 *
 * The pure decision rules deliberately return mutation plans. This boundary applies only the
 * counters that belong to `components.r3` plus the proven `best.s.B0/E` update performed by
 * `components.r3.a(side)`. External `best.s` counters changed later by `K()` and goal
 * materialization remain outside this boundary until their owners are connected.
 */
object LegacyMatchR3DecisionRuntimeRules {
    data class State(
        val currentSide: Int,
        val legacyOBySide: List<Int> = listOf(0, 0),
        val legacyPBySide: List<Int> = listOf(0, 0),
        val legacyQBySide: List<Int> = listOf(0, 0),
        val legacyRBySide: List<Int> = listOf(0, 0),
        val legacySBySide: List<Int> = listOf(0, 0),
        val legacyTBySide: List<Int> = listOf(0, 0),
        val legacyB0BySide: List<Int> = listOf(0, 0),
        val legacyEBySide: List<Int> = listOf(0, 0),
        val storedLegacyG: Double = 0.0,
    ) {
        init {
            require(currentSide == 0 || currentSide == 1) { "Legacy r3 side must be 0 or 1" }
            listOf(
                legacyOBySide,
                legacyPBySide,
                legacyQBySide,
                legacyRBySide,
                legacySBySide,
                legacyTBySide,
                legacyB0BySide,
                legacyEBySide,
            ).forEach { values ->
                require(values.size == 2) { "Legacy r3 side counters must contain exactly two values" }
            }
        }
    }

    /** Legacy `components.r3.h()`: one shared RNG boolean chooses the initial side. */
    fun initialize(random: RandomSource): State =
        State(currentSide = if (random.nextBoolean()) 0 else 1)

    fun applyJ(
        state: State,
        decision: LegacyMatchR3DecisionRules.DecisionResult,
    ): State {
        val current = state.currentSide
        val opposite = opposite(current)
        var legacyP = state.legacyPBySide
        var legacyQ = state.legacyQBySide
        var legacyS = state.legacySBySide
        var legacyT = state.legacyTBySide
        var legacyB0 = state.legacyB0BySide
        var legacyE = state.legacyEBySide

        decision.mutations.forEach { mutation ->
            when (mutation) {
                LegacyMatchR3DecisionRules.Mutation.J_T_OPPOSITE -> legacyT = increment(legacyT, opposite)
                LegacyMatchR3DecisionRules.Mutation.J_S_CURRENT -> legacyS = increment(legacyS, current)
                LegacyMatchR3DecisionRules.Mutation.J_P_CURRENT -> legacyP = increment(legacyP, current)
                LegacyMatchR3DecisionRules.Mutation.J_T_CURRENT -> legacyT = increment(legacyT, current)
                LegacyMatchR3DecisionRules.Mutation.J_S_OPPOSITE -> legacyS = increment(legacyS, opposite)
                LegacyMatchR3DecisionRules.Mutation.J_Q_OPPOSITE -> legacyQ = increment(legacyQ, opposite)
                LegacyMatchR3DecisionRules.Mutation.J_APPLY_A_CURRENT -> {
                    val applied = LegacyMatchR3ApplyARules.apply(legacyB0, current)
                    legacyB0 = applied.updatedLegacyB0
                    legacyE = applied.updatedLegacyE
                }
                LegacyMatchR3DecisionRules.Mutation.J_APPLY_A_OPPOSITE -> {
                    val applied = LegacyMatchR3ApplyARules.apply(legacyB0, opposite)
                    legacyB0 = applied.updatedLegacyB0
                    legacyE = applied.updatedLegacyE
                }
                LegacyMatchR3DecisionRules.Mutation.I_O_CURRENT,
                LegacyMatchR3DecisionRules.Mutation.I_R_OPPOSITE,
                -> error("I() mutation cannot be applied through the J() boundary: $mutation")
            }
        }

        return state.copy(
            legacyPBySide = legacyP,
            legacyQBySide = legacyQ,
            legacySBySide = legacyS,
            legacyTBySide = legacyT,
            legacyB0BySide = legacyB0,
            legacyEBySide = legacyE,
        )
    }

    fun applyI(
        state: State,
        decision: LegacyMatchR3DecisionRules.DecisionResult,
    ): State {
        val current = state.currentSide
        val opposite = opposite(current)
        var legacyO = state.legacyOBySide
        var legacyR = state.legacyRBySide

        decision.mutations.forEach { mutation ->
            when (mutation) {
                LegacyMatchR3DecisionRules.Mutation.I_O_CURRENT -> legacyO = increment(legacyO, current)
                LegacyMatchR3DecisionRules.Mutation.I_R_OPPOSITE -> legacyR = increment(legacyR, opposite)
                else -> error("J() mutation cannot be applied through the I() boundary: $mutation")
            }
        }

        return state.copy(
            legacyOBySide = legacyO,
            legacyRBySide = legacyR,
            storedLegacyG = requireNotNull(decision.storedLegacyG) {
                "Legacy I() must expose its recovered stored-g value"
            },
        )
    }

    /** Legacy `components.r3.L()` toggles the side after every `K()` step. */
    fun toggleSide(state: State): State = state.copy(currentSide = opposite(state.currentSide))

    private fun opposite(side: Int): Int = if (side == 1) 0 else 1

    private fun increment(values: List<Int>, side: Int): List<Int> =
        values.toMutableList().also { it[side] = it[side] + 1 }
}
