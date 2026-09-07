package com.leomala.footballdynasty.domain.career

/**
 * Transient lifecycle projection for legacy static `best.n.g`.
 *
 * Official SMALI proves `g` belongs to the non-serializable `best.n` utility class. Annual
 * `best.a.n(...)` overwrites it with each eligible `best.b.A(...)` result. `best.n.k()` reads it
 * only as an in-session branch: non-null/non-empty `g` scans `best.b.H0()` for the first `K()==true`
 * entry and then opens the corresponding invitation path. `best.n.j()` explicitly assigns
 * `g = null` after `best.b.m()` and before `best.b.j2(0)`.
 *
 * Therefore this boundary intentionally has no Room field or migration.
 */
object LegacyAnnualNEmploymentTransientRules {
    enum class KRoute {
        LEGACY_G_INVITATION,
        FALLBACK,
    }

    data class KResult(
        val route: KRoute,
        val firstLegacyKIndex: Int?,
    )

    enum class JLifecycleAction {
        CALL_B_M,
        CLEAR_LEGACY_G,
        CALL_B_J2_ZERO,
    }

    fun resolveK(
        legacyGSize: Int?,
        legacyKFlags: List<Boolean>,
    ): KResult {
        val hasLegacyG = legacyGSize != null && legacyGSize > 0
        if (!hasLegacyG) {
            return KResult(route = KRoute.FALLBACK, firstLegacyKIndex = null)
        }

        return KResult(
            route = KRoute.LEGACY_G_INVITATION,
            firstLegacyKIndex = legacyKFlags.indexOfFirst { it }.takeIf { it >= 0 },
        )
    }

    fun planJLifecycle(): List<JLifecycleAction> =
        listOf(
            JLifecycleAction.CALL_B_M,
            JLifecycleAction.CLEAR_LEGACY_G,
            JLifecycleAction.CALL_B_J2_ZERO,
        )
}
