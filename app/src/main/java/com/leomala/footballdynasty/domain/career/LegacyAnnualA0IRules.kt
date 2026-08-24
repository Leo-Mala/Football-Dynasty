package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Named facade for the SMALI-only legacy `best.a0.i()` routine.
 *
 * Predicate logic lives in [LegacyAnnualSelectionRules] so the selection implementation has one
 * source of truth; this facade preserves the subsystem boundary used by tests/documentation.
 */
object LegacyAnnualA0IRules {
    fun clubEligible(
        q0: Boolean,
        legacyJ: Int,
        legacyP0: Int,
    ): Boolean = LegacyAnnualSelectionRules.bestA0IClubEligible(q0, legacyJ, legacyP0)

    fun playerEligible(
        random: RandomSource,
        overall: Int,
        legacyW: Int,
        o0: Boolean,
    ): Boolean = LegacyAnnualSelectionRules.bestA0IPlayerEligible(
        random = random,
        subjectOverall = overall,
        legacyW = legacyW,
        subjectO0 = o0,
    )

    /** The legacy routine first tries `best.f.n(false)` and only if unresolved tries `o(false)`. */
    enum class RelocationAttempt {
        N_PRIMARY,
        O_FALLBACK,
    }

    val attemptOrder: List<RelocationAttempt> = listOf(
        RelocationAttempt.N_PRIMARY,
        RelocationAttempt.O_FALLBACK,
    )

    const val BEST_F_MODE: Int = 2
}
