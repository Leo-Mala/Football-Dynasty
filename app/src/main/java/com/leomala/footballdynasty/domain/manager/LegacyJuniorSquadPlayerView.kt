package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot

/**
 * Read-only junior-player view taken directly from the exact legacy team source.
 *
 * [sourceIndex] is the zero-based position in the serialized `juniors` collection;
 * it is provenance only and must not be interpreted as promotion, development,
 * eligibility or tactical priority. The legacy snapshot proves that juniors are
 * stored separately from senior players. Numeric position/status/side/CR values
 * and the `legacy*` identifiers remain intentionally opaque.
 */
data class LegacySourceJuniorSquadPlayerView(
    val sourceIndex: Int,
    val name: String,
    val age: Int,
    val country: Int,
    val position: Int,
    val status: Int,
    val side: Int,
    val cr1: Int,
    val cr2: Int,
    val star: Boolean,
    val worldTop: Boolean,
    val legacyAid: Int,
    val legacySid: Int,
    val legacyTid: Int,
    val legacyHash: Int,
)

object LegacySourceJuniorSquadPlayerViews {
    fun from(team: LegacyTeamSnapshot): List<LegacySourceJuniorSquadPlayerView> =
        team.juniors.mapIndexed { sourceIndex, player ->
            LegacySourceJuniorSquadPlayerView(
                sourceIndex = sourceIndex,
                name = player.name,
                age = player.age,
                country = player.country,
                position = player.position,
                status = player.status,
                side = player.side,
                cr1 = player.cr1,
                cr2 = player.cr2,
                star = player.star,
                worldTop = player.worldTop,
                legacyAid = player.legacyAid,
                legacySid = player.legacySid,
                legacyTid = player.legacyTid,
                legacyHash = player.legacyHash,
            )
        }
}
