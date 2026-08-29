package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.model.Club

/**
 * Read-only manager overview for the club explicitly persisted by the career.
 *
 * This composes already-proven club administration and senior-squad projections.
 * It deliberately introduces no lineup, tactical, eligibility, finance, stadium
 * upgrade, reputation, transfer or coach-progression behavior.
 */
data class ManagedClubOverview(
    val profile: LegacyManagedClubProfile,
    val squad: ManagedClubSquadView,
)

object ManagedClubOverviews {
    fun from(
        career: CareerState,
        clubs: List<Club>,
    ): ManagedClubOverview? {
        val club = ManagedClubSelection.resolve(career, clubs) ?: return null
        return ManagedClubOverview(
            profile = LegacyManagedClubProfileProjection.from(club),
            squad = ManagedClubSquadView(
                clubId = club.id,
                players = LegacySeniorSquadPlayerViews.from(club),
            ),
        )
    }
}
