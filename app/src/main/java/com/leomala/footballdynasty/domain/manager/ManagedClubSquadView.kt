package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.model.Club

/**
 * Resolves the club explicitly controlled by the persisted career and exposes
 * only its already-proven senior squad fields.
 *
 * This composes two certified boundaries without introducing lineup, reserve,
 * formation, eligibility, strength or tactical rules. A missing/stale managed
 * club id remains unresolved rather than falling back to another club.
 */
data class ManagedClubSquadView(
    val clubId: String,
    val players: List<LegacySeniorSquadPlayerView>,
)

object ManagedClubSquadViews {
    fun from(
        career: CareerState,
        clubs: List<Club>,
    ): ManagedClubSquadView? {
        val club = ManagedClubSelection.resolve(career, clubs) ?: return null
        return ManagedClubSquadView(
            clubId = club.id,
            players = LegacySeniorSquadPlayerViews.from(club),
        )
    }
}
