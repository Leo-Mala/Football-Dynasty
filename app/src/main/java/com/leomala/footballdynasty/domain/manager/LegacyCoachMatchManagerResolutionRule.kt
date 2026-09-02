package com.leomala.footballdynasty.domain.manager

/** Club-side manager reference as stored by legacy `best.c0.G1/y0()`. */
data class LegacyCoachMatchClubManagerRef(
    val clubId: String,
    val storedManagerId: Int,
)

data class LegacyResolvedCoachMatchManager(
    val clubId: String,
    val manager: LegacyManagerIdentityRef,
)

/**
 * Exact manager resolution used by reachable `best.s.f()` after each club's `c0.k(match)` call.
 *
 * `best.c0.y0()` resolves its stored manager id through `best.b.b1(int)`, whose ArrayList scan
 * returns the first matching manager. `best.s.f()` then handles the home club first and the away
 * club second. Missing managers are skipped; the two sides are not deduplicated if corrupt/source
 * state makes both ids resolve to the same manager.
 */
object LegacyCoachMatchManagerResolutionRule {
    fun resolveFirst(
        club: LegacyCoachMatchClubManagerRef,
        managersInWorldOrder: List<LegacyManagerIdentityRef>,
    ): LegacyResolvedCoachMatchManager? {
        if (club.storedManagerId == LegacyManagerIdentityRule.clubStoredManagerId(null)) return null
        val ordinal = LegacyManagerIdentityRule.resolveFirstOrdinal(
            managersInWorldOrder = managersInWorldOrder,
            storedManagerId = club.storedManagerId,
        ) ?: return null
        val manager = managersInWorldOrder.firstOrNull { it.sourceOrdinal == ordinal } ?: return null
        return LegacyResolvedCoachMatchManager(club.clubId, manager)
    }

    fun orderedForMatch(
        home: LegacyCoachMatchClubManagerRef,
        away: LegacyCoachMatchClubManagerRef,
        managersInWorldOrder: List<LegacyManagerIdentityRef>,
    ): List<LegacyResolvedCoachMatchManager> = buildList {
        resolveFirst(home, managersInWorldOrder)?.let(::add)
        resolveFirst(away, managersInWorldOrder)?.let(::add)
    }
}
