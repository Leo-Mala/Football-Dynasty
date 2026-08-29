package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot

/**
 * Read-only composition of the already-proven manager-facing state for the
 * club explicitly persisted by the career.
 *
 * The exact legacy source selection is carried with the view so later Marco B
 * reconstruction can consume one provenance boundary instead of independently
 * re-resolving club data. Visual identity is projected from that same exact
 * source. This still adds no lineup, tactics, transfer, finance, employment,
 * dismissal, reputation or progression semantics.
 */
data class ManagedClubManagerView(
    val overview: ManagedClubOverview,
    val legacyTeam: LegacyTeamSnapshot,
    val visualIdentity: LegacyManagedClubVisualIdentity,
    val coach: ManagedClubCoachView,
)

object ManagedClubManagerViews {
    fun from(
        career: CareerState,
        clubs: List<Club>,
        legacyTeams: List<LegacyTeamSnapshot>,
    ): ManagedClubManagerView? {
        val overview = ManagedClubOverviews.from(career, clubs) ?: return null
        val selection = ManagedClubLegacyTeamSelections.resolve(
            career = career,
            clubs = clubs,
            legacyTeams = legacyTeams,
        ) ?: return null
        if (overview.profile.clubId != selection.club.id) return null

        return ManagedClubManagerView(
            overview = overview,
            legacyTeam = selection.legacyTeam,
            visualIdentity = LegacyManagedClubVisualIdentityProjection.from(selection.legacyTeam),
            coach = ManagedClubCoachView(
                clubId = selection.club.id,
                coach = LegacyCoachProfileProjection.from(selection.legacyTeam),
            ),
        )
    }
}
