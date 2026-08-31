package com.leomala.footballdynasty.domain.manager

/**
 * Exact call ordering of `best.b.b4(f0,f0)`.
 *
 * Both club references are captured before either manager leaves. Then both `l(...)` departures run
 * before either `e(...)` arrival. Keeping this as an executable orchestration rule prevents a
 * tempting but behavior-changing implementation of two independent `G(...)` transfers.
 */
object LegacyManagerSwapRule {
    fun <M, C> execute(
        firstManager: M,
        secondManager: M,
        currentClubOf: (M) -> C?,
        depart: (manager: M, replacement: M) -> Unit,
        arrive: (manager: M, club: C?) -> Unit,
    ) {
        val firstClub = currentClubOf(firstManager)
        val secondClub = currentClubOf(secondManager)
        depart(firstManager, secondManager)
        depart(secondManager, firstManager)
        arrive(firstManager, secondClub)
        arrive(secondManager, firstClub)
    }
}
