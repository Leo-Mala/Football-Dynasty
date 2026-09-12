package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.domain.manager.LegacyLineupEligibilityKnownInputsRule

/**
 * Application projection of the recovered `best.o.K0(...)` predicate using only owners already
 * exposed by [CareerLineupInputCatalogStore]. A null result means an unresolved owner or the exact
 * career clock can still change K0; it is never replaced by a modern default.
 */
object CareerLineupEligibilityProjection {
    fun resolveWithoutCareerClock(
        player: CareerLineupInputCatalogStore.PlayerInput,
        preparation: CareerLineupInputCatalogStore.MatchPreparation,
    ): Boolean? = LegacyLineupEligibilityKnownInputsRule.resolveWithoutCareerClock(
        blockedByM0 = player.blockedByM0ForPreparedMatch,
        competitionRestrictionActive = preparation.competitionRestrictionActive,
        excludedByCompetitionV0 = player.excludedByCompetitionV0ForPreparedMatch,
        lineupModeFlag = preparation.lineupModeFlag,
        hasClub = player.hasClubForPreparedMatch,
        clubActiveQ0 = player.clubActiveQ0ForPreparedMatch,
    )
}
