package com.leomala.footballdynasty.domain.manager

/**
 * Exact raw club-division code mutation used by legacy `best.c0.s1(int)`.
 *
 * The field is career state (`best.c0.j`, read by `O()`). It is written by national-league
 * `konrent.t.f1()` and reset to zero when a club returns to the country pool. The setter has an
 * asymmetric legacy guard: values greater than four become zero, while negative values are kept.
 */
object LegacyClubDivisionCodeRule {
    fun write(rawValue: Int): Int = if (rawValue > 4) 0 else rawValue

    /** `konrent.t.f1()` forwards raw league `F` through `best.c0.s1(F)`. */
    fun assignFromLeague(rawLeagueF: Int): Int = write(rawLeagueF)

    /** `konrent.t.c1()` and the last-division relegation path explicitly write zero. */
    fun returnToCountryPool(): Int = 0
}
