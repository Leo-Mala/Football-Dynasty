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
 *
 * [sourceFileRef] is part of the identity on purpose: the same collection/index
 * pair can exist in every legacy team, so a reference must not resolve against a
 * different `.ban` snapshot merely because its roster kind and index match.
 */
data class LegacySourcePlayerRef(
    val sourceFileRef: String,
    val rosterKind: LegacySourceRosterKind,
    val sourceIndex: Int,
)

/**
 * Typed result of resolving one exact legacy source reference.
 *
 * This wrapper preserves the proven senior/junior collection boundary. It carries
 * no starter, reserve, eligibility, promotion or tactical semantics.
 */
sealed interface LegacyResolvedSourcePlayer {
    val ref: LegacySourcePlayerRef

    data class Senior(
        override val ref: LegacySourcePlayerRef,
        val player: LegacySourceSeniorSquadPlayerView,
    ) : LegacyResolvedSourcePlayer

    data class Junior(
        override val ref: LegacySourcePlayerRef,
        val player: LegacySourceJuniorSquadPlayerView,
    ) : LegacyResolvedSourcePlayer
}

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
     * Creates a reference only when [sourceIndex] names an existing senior entry.
     * This is a bounds/provenance check only; it does not make that player eligible
     * or selected for any lineup role.
     */
    fun seniorRef(sourceIndex: Int): LegacySourcePlayerRef? =
        if (sourceIndex in senior.indices) {
            LegacySourcePlayerRef(sourceFileRef, LegacySourceRosterKind.SENIOR, sourceIndex)
        } else {
            null
        }

    /**
     * Creates a reference only when [sourceIndex] names an existing junior entry.
     * This is a bounds/provenance check only; it does not imply promotion or any
     * senior-team role.
     */
    fun juniorRef(sourceIndex: Int): LegacySourcePlayerRef? =
        if (sourceIndex in juniors.indices) {
            LegacySourcePlayerRef(sourceFileRef, LegacySourceRosterKind.JUNIOR, sourceIndex)
        } else {
            null
        }

    fun seniorRefs(): List<LegacySourcePlayerRef> = senior.indices.map { sourceIndex ->
        checkNotNull(seniorRef(sourceIndex))
    }

    fun juniorRefs(): List<LegacySourcePlayerRef> = juniors.indices.map { sourceIndex ->
        checkNotNull(juniorRef(sourceIndex))
    }

    /**
     * Resolves only a SENIOR reference owned by this exact legacy source file.
     * Wrong file, collection or absent index returns null; there is deliberately
     * no fallback to juniors, another team or fact matching.
     */
    fun seniorPlayer(ref: LegacySourcePlayerRef): LegacySourceSeniorSquadPlayerView? =
        if (
            ref.sourceFileRef == sourceFileRef &&
            ref.rosterKind == LegacySourceRosterKind.SENIOR
        ) {
            senior.getOrNull(ref.sourceIndex)
        } else {
            null
        }

    /**
     * Resolves only a JUNIOR reference owned by this exact legacy source file.
     * Wrong file, collection or absent index returns null; there is deliberately
     * no fallback to seniors, another team or fact matching.
     */
    fun juniorPlayer(ref: LegacySourcePlayerRef): LegacySourceJuniorSquadPlayerView? =
        if (
            ref.sourceFileRef == sourceFileRef &&
            ref.rosterKind == LegacySourceRosterKind.JUNIOR
        ) {
            juniors.getOrNull(ref.sourceIndex)
        } else {
            null
        }

    /**
     * Resolves an exact source reference while retaining its proven collection
     * type. This is the neutral boundary later lineup/tactics characterization can
     * consume without matching player facts or collapsing senior/junior sources.
     */
    fun player(ref: LegacySourcePlayerRef): LegacyResolvedSourcePlayer? = when (ref.rosterKind) {
        LegacySourceRosterKind.SENIOR -> seniorPlayer(ref)?.let { player ->
            LegacyResolvedSourcePlayer.Senior(ref = ref, player = player)
        }

        LegacySourceRosterKind.JUNIOR -> juniorPlayer(ref)?.let { player ->
            LegacyResolvedSourcePlayer.Junior(ref = ref, player = player)
        }
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
