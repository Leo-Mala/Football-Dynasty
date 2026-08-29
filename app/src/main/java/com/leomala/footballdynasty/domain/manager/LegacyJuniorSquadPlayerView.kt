package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot

/**
 * Read-only junior-player view taken directly from the exact legacy team source.
 *
 * The legacy snapshot proves that juniors are stored in a collection separate from
 * the senior players. Numeric position/status/side/CR values and the `legacy*`
 * identifiers remain intentionally opaque. This projection does not promote a
 * junior, merge juniors into the senior squad, decide eligibility, or add any
 * youth-development gameplay semantics.
 */
data class LegacySourceJuniorSquadPlayerView(
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
        team.juniors.map { player ->
            LegacySourceJuniorSquadPlayerView(
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
