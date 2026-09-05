package com.leomala.footballdynasty.domain.career

/**
 * Pure routing projection of reachable legacy `best.a.s()`.
 *
 * Executable SMALI proves that `s()` scans every global club in source order, computes the
 * competition `Calendar.MONTH` through `best.a.D()`, asks `club.Y0(month)`, and calls `club.z()`
 * only when that predicate is true. The financial calculation inside `z()` (`E(q())`) remains a
 * separate Phase 15 boundary and is intentionally not approximated here.
 */
object LegacyAnnualClubPayrollRoutingRules {
    data class ClubEntry(
        val matchesLegacyMonthPredicate: Boolean,
    )

    data class Call(
        val sourceIndex: Int,
        val legacyCalendarMonth: Int,
    )

    fun plan(
        legacyCalendarMonth: Int,
        clubs: List<ClubEntry>,
    ): List<Call> = buildList {
        clubs.forEachIndexed { index, club ->
            if (club.matchesLegacyMonthPredicate) {
                add(
                    Call(
                        sourceIndex = index,
                        legacyCalendarMonth = legacyCalendarMonth,
                    )
                )
            }
        }
    }
}
