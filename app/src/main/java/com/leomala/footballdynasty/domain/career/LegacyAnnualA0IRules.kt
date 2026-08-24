package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Structural projection of the SMALI-only legacy `best.a0.i()` routine. */
object LegacyAnnualA0IRules {
    /**
     * Exact club gate from the bytecode. `legacyJ` and `legacyP0` retain obfuscated names because
     * the phase only needs their proven control behavior.
     */
    fun clubEligible(
        q0: Boolean,
        legacyJ: Int,
        legacyP0: Int,
    ): Boolean {
        if (q0) return false
        return if (legacyJ != 0) legacyP0 < 5 else legacyP0 < 4
    }

    /**
     * Candidate filter before constructing `best.f` mode 2. The RNG draw is deliberately last,
     * matching the SMALI short-circuit order, so failed structural predicates consume no draw.
     */
    fun playerEligible(
        random: RandomSource,
        overall: Int,
        legacyW: Int,
        o0: Boolean,
    ): Boolean {
        if (overall <= 50) return false
        if (legacyW >= 31) return false
        if (!o0) return false
        return LegacyAnnualRandomRules.bestA0IGate(random)
    }

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
