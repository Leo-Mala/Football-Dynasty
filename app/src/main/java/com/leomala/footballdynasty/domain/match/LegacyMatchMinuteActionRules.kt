package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Downstream mutation/callback parity for one already-resolved legacy `best.s.k(...)` decision.
 *
 * `LegacyMatchMinuteRules` owns the direct event-gate RNG. This rule owns the reachable work after
 * that gate: player selector choice, O/P/Q counter timing, event callback, and the fallback `j` call.
 */
object LegacyMatchMinuteActionRules {
    data class Counters(
        val legacyO: Int,
        val legacyP: Int,
        val legacyQ: Int,
    )

    enum class Operation {
        REFRESH_PLAYER_STATE,
        SELECT_S,
        INCREMENT_O,
        APPLY_C,
        SELECT_U,
        APPLY_D,
        INCREMENT_P,
        INCREMENT_Q,
        SELECT_T,
        APPLY_TYPE_5,
        APPLY_SECOND_HALF_J,
    }

    data class Result<T>(
        val counters: Counters,
        val selectedPlayer: T?,
        val operations: List<Operation>,
    )

    fun <T> apply(
        decision: LegacyMatchMinuteRules.Decision,
        counters: Counters,
        random: RandomSource,
        activeCandidates: () -> List<LegacyMatchPlayerSelectionRules.Candidate<T>>,
        refreshPlayerState: () -> Unit = {},
        applyLegacyC: (T) -> Unit = {},
        applyLegacyD: (T) -> Unit = {},
        applyLegacyType5: (T) -> Unit = {},
        applySecondHalfJ: () -> Unit = {},
    ): Result<T> {
        val operations = mutableListOf<Operation>()
        if (decision.refreshPlayerState) {
            refreshPlayerState()
            operations += Operation.REFRESH_PLAYER_STATE
        }

        var nextCounters = counters
        var selected: T? = null
        when (decision.action) {
            LegacyMatchMinuteRules.Action.LEGACY_C -> {
                operations += Operation.SELECT_S
                selected = LegacyMatchPlayerSelectionRules.selectS(activeCandidates(), random)?.value
                nextCounters = nextCounters.copy(legacyO = nextCounters.legacyO + 1)
                operations += Operation.INCREMENT_O
                if (selected != null) {
                    applyLegacyC(selected)
                    operations += Operation.APPLY_C
                }
            }

            LegacyMatchMinuteRules.Action.LEGACY_D -> {
                operations += Operation.SELECT_U
                selected = LegacyMatchPlayerSelectionRules.selectU(activeCandidates(), random)?.value
                if (selected != null) {
                    applyLegacyD(selected)
                    operations += Operation.APPLY_D
                }
                nextCounters = nextCounters.copy(legacyP = nextCounters.legacyP + 1)
                operations += Operation.INCREMENT_P
            }

            LegacyMatchMinuteRules.Action.LEGACY_TYPE_5 -> {
                nextCounters = nextCounters.copy(legacyQ = nextCounters.legacyQ + 1)
                operations += Operation.INCREMENT_Q
                operations += Operation.SELECT_T
                selected = LegacyMatchPlayerSelectionRules.selectT(activeCandidates(), random)?.value
                if (selected != null) {
                    applyLegacyType5(selected)
                    operations += Operation.APPLY_TYPE_5
                }
            }

            LegacyMatchMinuteRules.Action.SECOND_HALF_J -> {
                applySecondHalfJ()
                operations += Operation.APPLY_SECOND_HALF_J
            }

            LegacyMatchMinuteRules.Action.NONE -> Unit
        }

        return Result(
            counters = nextCounters,
            selectedPlayer = selected,
            operations = operations.toList(),
        )
    }
}
