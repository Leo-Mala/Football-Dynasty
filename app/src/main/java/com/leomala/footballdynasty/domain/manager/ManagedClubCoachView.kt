package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot

/**
 * Exact provenance pair between the club persisted by the career and the
 * legacy team snapshot that produced that modern club.
 *
 * Resolution is intentionally limited to the modern club's source `.ban`
 * reference. No fallback by team name, country, stadium, reputation, roster
 * similarity, or list position is allowed.
 */
data class ManagedClubLegacyTeamSelection(
    val club: Club,
    val legacyTeam: LegacyTeamSnapshot,
)

object ManagedClubLegacyTeamSelections {
    fun resolve(
        career: CareerState,
        clubs: List<Club>,
        legacyTeams: List<LegacyTeamSnapshot>,
    ): ManagedClubLegacyTeamSelection? {
        val club = ManagedClubSelection.resolve(career, clubs) ?: return null
        val legacyTeam = legacyTeams.firstOrNull { team ->
            team.fileRef == club.sourceFileRef
        } ?: return null
        return ManagedClubLegacyTeamSelection(
            club = club,
            legacyTeam = legacyTeam,
        )
    }
}

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
        val selection = ManagedClubLegacyTeamSelections.resolve(
            career = career,
            clubs = clubs,
            legacyTeams = legacyTeams,
        ) ?: return null
        return ManagedClubCoachView(
            clubId = selection.club.id,
            coach = LegacyCoachProfileProjection.from(selection.legacyTeam),
        )
    }
}
