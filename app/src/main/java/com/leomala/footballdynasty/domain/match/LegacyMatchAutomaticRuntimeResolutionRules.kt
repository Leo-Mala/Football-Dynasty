package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Applies the fully characterized automatic `best.s.Q0()` tail to the mutable match runtime.
 *
 * The minute loops and direct `k()` mutations are delegated to [LegacyMatchAutomaticRuntimeRules].
 * After those loops, this boundary preserves the proven `Z && a0 && P0()` short circuit, executes
 * the already-recovered reachable `best.s.o()` result when required, then clears both persisted
 * club-mode flags exactly where the legacy automatic flow performs that mutation.
 *
 * The returned `legacyO` result intentionally remains explicit because legacy `o()` also writes its
 * `f0/d0` transient fields; callers that own those fields can consume the exact recovered values
 * without inventing additional persistent match state here.
 */
object LegacyMatchAutomaticRuntimeResolutionRules {
    data class Result<TEvent>(
        val runtime: LegacyMatchAutomaticRuntimeRules.Result<TEvent>,
        val postSimulation: LegacyMatchAutomaticPostSimulationRules.Result,
        val legacyO: LegacyMatchPostGateORules.Result?,
    )

    fun <TClub, TPlayer, TEvent> run(
        state: LegacyMatchTransientRuntime.State<TClub, TPlayer>,
        random: RandomSource,
        homeTacticIndex: Int,
        awayTacticIndex: Int,
        initialCounters: LegacyMatchMinuteActionRules.Counters,
        advanceR3: (half: Int, minute: Int) -> TEvent?,
        halftimeTransition: (half: Int, minute: Int) -> Unit,
        legacyZFlag: Boolean,
        legacyA0Flag: Boolean,
        resolveP0: () -> Boolean,
        refreshPlayerState: () -> Unit = {},
        applySecondHalfJ: () -> Unit = {},
    ): Result<TEvent> {
        val runtime = LegacyMatchAutomaticRuntimeRules.run(
            state = state,
            random = random,
            homeTacticIndex = homeTacticIndex,
            awayTacticIndex = awayTacticIndex,
            initialCounters = initialCounters,
            advanceR3 = advanceR3,
            halftimeTransition = halftimeTransition,
            refreshPlayerState = refreshPlayerState,
            applySecondHalfJ = applySecondHalfJ,
        )

        val postSimulation = LegacyMatchAutomaticPostSimulationRules.resolve(
            legacyZFlag = legacyZFlag,
            legacyA0Flag = legacyA0Flag,
            resolveP0 = resolveP0,
        )
        val legacyO = if (postSimulation.invokeLegacyO) {
            LegacyMatchPostGateORules.resolve(random)
        } else {
            null
        }

        if (postSimulation.clearBothClubFlags) {
            state.home.legacyModeFlag = false
            state.away.legacyModeFlag = false
        }

        return Result(
            runtime = runtime,
            postSimulation = postSimulation,
            legacyO = legacyO,
        )
    }
}
