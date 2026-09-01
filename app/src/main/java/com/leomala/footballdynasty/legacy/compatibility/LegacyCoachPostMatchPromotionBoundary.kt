package com.leomala.footballdynasty.legacy.compatibility

/**
 * Fail-closed promotion boundary for the reachable legacy coach post-match lifecycle.
 *
 * The official Brasfoot 2026/27 corpus proves the caller chain
 * `best.s.f() -> best.f0.i(best.s) / best.f0.j(best.s)`. The H-only projection of
 * `best.f0.i(best.s)` has already been characterized, but the complete field/effect ordering of
 * both manager methods has not yet been reconstructed. Production persistence must therefore stay
 * blocked until the complete lifecycle is characterized together.
 *
 * This object is deliberately evidence-only: it does not invent missing coach semantics and it
 * does not execute gameplay.
 */
object LegacyCoachPostMatchPromotionBoundary {
    const val callerMethod: String = "best.s.f()"
    const val homeManagerMethod: String = "best.f0.i(best.s)"
    const val pairedManagerMethod: String = "best.f0.j(best.s)"

    /** Exact competition `E()` values for which `best.s.f()` reaches the characterized H path. */
    val characterizedHCompetitionTypes: Set<Int> = linkedSetOf(1, 2, 3, 4, 5, 6, 8)

    /** The H-only projection is characterized and tested by `LegacyCoachRawHRule`. */
    const val hProjectionCharacterized: Boolean = true

    /** Additional mutations/effects and their ordering in i/j are still an explicit evidence gap. */
    const val completeLifecycleCharacterized: Boolean = false

    /**
     * Never allow an H-only production post-match write to masquerade as the complete legacy
     * manager lifecycle.
     */
    fun productionPersistenceAllowed(): Boolean =
        hProjectionCharacterized && completeLifecycleCharacterized
}
