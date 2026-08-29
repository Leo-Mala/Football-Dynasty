package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot

/**
 * Identifies which serialized legacy player collection owns a source index.
 *
 * This is provenance only. It does not imply senior-team eligibility, promotion,
 * starter/reserve status or tactical meaning.
 */
enum class LegacySourceRosterKind {
    SENIOR,
    JUNIOR,
}

/**
 * Stable read-only reference to one player position in the exact legacy source
 * collections. The index is zero-based and is never inferred from player facts.
 */
data class LegacySourcePlayerRef(
    val rosterKind: LegacySourceRosterKind,
    val sourceIndex: Int,
)

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
) {
    /**
     * Returns the exact source entry addressed by [ref], or null when that exact
     * serialized collection/index pair is absent. No cross-collection fallback,
     * name matching or player-property heuristic is attempted.
     */
    fun player(ref: LegacySourcePlayerRef): Any? = when (ref.rosterKind) {
        LegacySourceRosterKind.SENIOR -> senior.getOrNull(ref.sourceIndex)
        LegacySourceRosterKind.JUNIOR -> juniors.getOrNull(ref.sourceIndex)
    }

    fun seniorRefs(): List<LegacySourcePlayerRef> = senior.indices.map { sourceIndex ->
        LegacySourcePlayerRef(LegacySourceRosterKind.SENIOR, sourceIndex)
    }

    fun juniorRefs(): List<LegacySourcePlayerRef> = juniors.indices.map { sourceIndex ->
        LegacySourcePlayerRef(LegacySourceRosterKind.JUNIOR, sourceIndex)
    }
}

object LegacyManagedClubSourceSquadProjection {
    fun from(team: LegacyTeamSnapshot): LegacyManagedClubSourceSquads =
        LegacyManagedClubSourceSquads(
            sourceFileRef = team.fileRef,
            senior = LegacySourceSeniorSquadPlayerViews.from(team),
            juniors = LegacySourceJuniorSquadPlayerViews.from(team),
        )
}
