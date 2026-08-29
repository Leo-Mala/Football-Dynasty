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
 * re-resolving club data. Visual identity, opaque serialized source identity,
 * and the raw legacy senior/junior player provenance are projected from that same
 * exact source while preserving the legacy separation between both collections.
 * This still adds no lineup, tactics, transfer, finance, youth-development,
 * employment, dismissal, reputation or progression semantics.
 */
data class ManagedClubManagerView(
    val overview: ManagedClubOverview,
    val legacyTeam: LegacyTeamSnapshot,
    val sourceIdentity: LegacyManagedClubSourceIdentity,
    val visualIdentity: LegacyManagedClubVisualIdentity,
    val sourceSquads: LegacyManagedClubSourceSquads,
    val sourceSeniorSquad: List<LegacySourceSeniorSquadPlayerView>,
    val sourceJuniorSquad: List<LegacySourceJuniorSquadPlayerView>,
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

        val sourceSquads = LegacyManagedClubSourceSquadProjection.from(selection.legacyTeam)

        return ManagedClubManagerView(
            overview = overview,
            legacyTeam = selection.legacyTeam,
            sourceIdentity = LegacyManagedClubSourceIdentityProjection.from(selection.legacyTeam),
            visualIdentity = LegacyManagedClubVisualIdentityProjection.from(selection.legacyTeam),
            sourceSquads = sourceSquads,
            sourceSeniorSquad = sourceSquads.senior,
            sourceJuniorSquad = sourceSquads.juniors,
            coach = ManagedClubCoachView(
                clubId = selection.club.id,
                coach = LegacyCoachProfileProjection.from(selection.legacyTeam),
            ),
        )
    }
}
