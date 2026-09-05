package com.leomala.footballdynasty.domain.career

/**
 * Pure routing projection of reachable legacy `best.a.n(boolean)`.
 *
 * Executable SMALI proves that the method first requires `best.b.N1() == true`, then scans
 * `best.b.H0()` in source order. Entries with `best.f0.K() == false` are skipped. For the
 * `flag=false` path (`cS`), entries whose `best.f0.y()` is non-null are also skipped. For the
 * `flag=true` path (`cSempregado`), `y()` does not participate in eligibility.
 *
 * Every eligible entry calls `best.b.A(f0, false)` and immediately assigns that return value to
 * `best.n.g`; therefore later eligible entries overwrite earlier assignments. The callee `A(...)`
 * remains a separate Phase 15 boundary and is intentionally not modeled here.
 */
object LegacyAnnualNEmploymentRoutingRules {
    data class Entry(
        val legacyK: Boolean,
        val hasLegacyY: Boolean,
    )

    data class Call(
        val sourceIndex: Int,
        val legacyArgument: Boolean = false,
        val overwritesLegacyG: Boolean = true,
    )

    fun plan(
        legacyN1: Boolean,
        employedFlag: Boolean,
        entries: List<Entry>,
    ): List<Call> {
        if (!legacyN1) return emptyList()

        return buildList {
            entries.forEachIndexed { index, entry ->
                if (!entry.legacyK) return@forEachIndexed
                if (!employedFlag && entry.hasLegacyY) return@forEachIndexed

                add(Call(sourceIndex = index))
            }
        }
    }
}
