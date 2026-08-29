package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot

/**
 * Read-only coach identity associated with the club explicitly controlled by the career.
 *
 * The association is deliberately limited to the exact legacy source-file reference that
 * produced the modern club. It does not infer employment, reputation, dismissal, offers,
 * progression, or any fallback by club/team name when the source identity is unavailable.
 */
data class ManagedClubCoachView(
    val clubId: String,
    val coach: LegacyCoachProfile,
)

object ManagedClubCoachViews {
    fun from(
        career: CareerState,
        clubs: List<Club>,
        legacyTeams: List<LegacyTeamSnapshot>,
    ): ManagedClubCoachView? {
        val club = ManagedClubSelection.resolve(career, clubs) ?: return null
        val legacyTeam = legacyTeams.firstOrNull { it.fileRef == club.sourceFileRef } ?: return null
        return ManagedClubCoachView(
            clubId = club.id,
            coach = LegacyCoachProfileProjection.from(legacyTeam),
        )
    }
}
