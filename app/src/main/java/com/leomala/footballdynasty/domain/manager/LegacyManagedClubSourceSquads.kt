package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot

/**
 * Single read-only provenance boundary for the two player collections proven by
 * the exact legacy team snapshot.
 *
 * The legacy model stores senior players and juniors separately. This type keeps
 * that separation explicit and preserves each collection's source order. It does
 * not merge both lists, promote juniors, choose starters/reserves, infer
 * eligibility, or assign tactical meaning to opaque legacy fields.
 */
data class LegacyManagedClubSourceSquads(
    val sourceFileRef: String,
    val senior: List<LegacySourceSeniorSquadPlayerView>,
    val juniors: List<LegacySourceJuniorSquadPlayerView>,
)

object LegacyManagedClubSourceSquadProjection {
    fun from(team: LegacyTeamSnapshot): LegacyManagedClubSourceSquads =
        LegacyManagedClubSourceSquads(
            sourceFileRef = team.fileRef,
            senior = LegacySourceSeniorSquadPlayerViews.from(team),
            juniors = LegacySourceJuniorSquadPlayerViews.from(team),
        )
}
