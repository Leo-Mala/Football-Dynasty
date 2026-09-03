package com.leomala.footballdynasty.domain.manager

/** Exact legacy construction source for full `best.s` matches. */
enum class LegacyMatchConstructionSource {
    /** `konrent.t.X(...)` passes the league itself as `best.s.A()`. */
    LEAGUE_T,

    /** `konrent.f0.e(...)` passes its explicit `konrent.a0` parent as `best.s.A()`. */
    KNOCKOUT_F0,

    /** `konrent.a.a0(...)` passes world `konrent.a` as both parent and competition. */
    FRIENDLY_A,
}

/**
 * Replays the class-identity test in `best.k.b(best.s)` without guessing from competition type.
 */
object LegacyTicketParentClassRule {
    fun parentCompetitionIsA0(source: LegacyMatchConstructionSource): Boolean =
        source == LegacyMatchConstructionSource.KNOCKOUT_F0
}
