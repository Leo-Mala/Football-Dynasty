package com.leomala.footballdynasty.domain.career

/**
 * Pure control-flow projection of reachable legacy `best.o.e()`.
 *
 * Executable authority: official `best/o.smali` from the pinned Brasfoot 2026/27 corpus.
 * This rule deliberately does not implement `best.o.s()` or `best.o.t()` themselves; it freezes
 * only their caller routing and the exact `M` clear boundary.
 */
object LegacyAnnualSeniorProgressionRoutingRules {
    enum class Step {
        APPLY_GROWTH,
        APPLY_DECLINE,
        CLEAR_LEGACY_M,
    }

    /**
     * Legacy behavior:
     * - no current club: return immediately and DO NOT clear M;
     * - age field e < 32: call s(), then clear M;
     * - age field e >= 32: call t(), then clear M.
     */
    fun steps(hasCurrentClub: Boolean, age: Int): List<Step> {
        if (!hasCurrentClub) return emptyList()

        val progression = if (age < 32) Step.APPLY_GROWTH else Step.APPLY_DECLINE
        return listOf(progression, Step.CLEAR_LEGACY_M)
    }
}
