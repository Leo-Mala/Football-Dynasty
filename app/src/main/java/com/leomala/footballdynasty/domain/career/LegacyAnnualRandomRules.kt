package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Small deterministic boundary for annual random decisions proven in the Brasfoot 2026/27
 * legacy corpus. Method names deliberately retain the legacy labels where sporting semantics
 * are not yet proven.
 */
object LegacyAnnualRandomRules {
    /**
     * Structural parity for the random predicate in legacy best.a0.a():
     * `new Random().nextInt(100) > 30`.
     *
     * This reproduces only the proven draw/bound/predicate. It does not model or name the
     * opaque side effect that follows the predicate in the legacy application.
     */
    fun bestA0AGate(random: RandomSource): Boolean = random.nextInt(100) > 30
}
