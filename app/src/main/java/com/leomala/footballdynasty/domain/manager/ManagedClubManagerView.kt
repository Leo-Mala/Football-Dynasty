package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot

/**
 * Read-only composition of the already-proven manager-facing state for the
 * club explicitly persisted by the career.
 *
 * This only joins the certified club/senior-squad overview with the certified
 * legacy coach identity. It deliberately adds no lineup, tactics, transfer,
 * finance, employment, dismissal, reputation or progression semantics.
 */
data class ManagedClubManagerView(
    val overview: ManagedClubOverview,
    val coach: ManagedClubCoachView,
)

object ManagedClubManagerViews {
    fun from(
        career: CareerState,
        clubs: List<Club>,
        legacyTeams: List<LegacyTeamSnapshot>,
    ): ManagedClubManagerView? {
        val overview = ManagedClubOverviews.from(career, clubs) ?: return null
        val coach = ManagedClubCoachViews.from(career, clubs, legacyTeams) ?: return null
        if (overview.profile.clubId != coach.clubId) return null
        return ManagedClubManagerView(
            overview = overview,
            coach = coach,
        )
    }
}
