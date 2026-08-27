package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Structural parity for legacy `best.s.j(int, int)` recovered from SMALI. */
object LegacyMatchJRules {
    enum class LegacySide {
        LEGACY_E,
        LEGACY_F,
    }

    data class SideState<T>(
        val blocked: Boolean,
        val remaining: Int,
        val legacyI0: List<Int>,
        val legacyH0: List<Int>,
        val candidates: List<LegacyMatchTransitionRules.Player<T>>,
    )

    data class Attempt<T>(
        val side: LegacySide,
        val mode: Int,
        val legacyP2: Int,
        val legacyP3: Int,
        val legacyP4: Int,
        val selected: LegacyMatchTransitionRules.Player<T>?,
    )

    data class Result<T>(
        val attempts: List<Attempt<T>>,
    )

    fun <T> resolve(
        legacyP1: Int,
        legacyP2: Int,
        legacyScoreE: Int,
        legacyScoreF: Int,
        legacyE: SideState<T>,
        legacyF: SideState<T>,
        legacyG: Set<T>,
        legacyH: Set<T>,
        random: RandomSource,
    ): Result<T> {
        if (legacyP1 != 2) return Result(emptyList())

        val attempts = mutableListOf<Attempt<T>>()

        val eMode = resolveEMode(
            legacyMinute = legacyP2,
            legacyScoreE = legacyScoreE,
            legacyScoreF = legacyScoreF,
            state = legacyE,
            random = random,
        )
        if (eMode != null) {
            val selected = LegacyMatchTransitionRules.selectR(
                mode = eMode,
                legacyP2 = 0,
                legacyP4 = legacyP2,
                candidates = legacyE.candidates,
                legacyG = legacyG,
                legacyH = legacyH,
                random = random,
            )
            attempts += Attempt(
                side = LegacySide.LEGACY_E,
                mode = eMode,
                legacyP2 = 0,
                legacyP3 = legacyP1,
                legacyP4 = legacyP2,
                selected = selected,
            )
            if (selected != null) return Result(attempts)
        }

        val fMode = resolveFMode(
            legacyMinute = legacyP2,
            legacyScoreE = legacyScoreE,
            legacyScoreF = legacyScoreF,
            state = legacyF,
            random = random,
        )
        if (fMode != null) {
            val selected = LegacyMatchTransitionRules.selectR(
                mode = fMode,
                legacyP2 = 1,
                legacyP4 = legacyP2,
                candidates = legacyF.candidates,
                legacyG = legacyG,
                legacyH = legacyH,
                random = random,
            )
            attempts += Attempt(
                side = LegacySide.LEGACY_F,
                mode = fMode,
                legacyP2 = 1,
                legacyP3 = legacyP1,
                legacyP4 = legacyP2,
                selected = selected,
            )
        }

        return Result(attempts)
    }

    private fun <T> resolveEMode(
        legacyMinute: Int,
        legacyScoreE: Int,
        legacyScoreF: Int,
        state: SideState<T>,
        random: RandomSource,
    ): Int? {
        if (state.blocked || state.remaining <= 0) return null

        val eTrailsByOne = legacyScoreF - legacyScoreE >= 1
        if (legacyMinute == 0 && eTrailsByOne) {
            return if (random.nextInt(100) > 50) 2 else null
        }

        if (legacyMinute in state.legacyI0) {
            val tied = legacyScoreE == legacyScoreF
            if (eTrailsByOne || tied) return 2
            return null
        }

        if (legacyMinute in state.legacyH0) return 1
        return null
    }

    private fun <T> resolveFMode(
        legacyMinute: Int,
        legacyScoreE: Int,
        legacyScoreF: Int,
        state: SideState<T>,
        random: RandomSource,
    ): Int? {
        if (state.blocked || state.remaining <= 0) return null

        val fTrailsByTwo = legacyScoreE - legacyScoreF >= 2
        if (legacyMinute == 0 && fTrailsByTwo) {
            return if (random.nextInt(100) > 50) 2 else null
        }

        if (legacyMinute in state.legacyI0) {
            val fTrailsByOne = legacyScoreE - legacyScoreF >= 1
            if (fTrailsByOne) return 2
            return null
        }

        if (legacyMinute in state.legacyH0) return 1
        return null
    }
}
