package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot

/**
 * Consolidated persistence-independent view of every senior-player field that is
 * already proven by the legacy `.ban` snapshot and represented by the modern
 * [com.leomala.footballdynasty.domain.model.Player] model.
 *
 * Numeric position/status/side/CR values remain intentionally opaque. This view
 * does not decide starters, reserves, formation slots, eligibility, strength or
 * tactical meaning; those rules require separate Java/SMALI characterization.
 */
data class LegacySeniorSquadPlayerView(
    val playerId: String,
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
)

object LegacySeniorSquadPlayerViews {
    fun from(club: Club): List<LegacySeniorSquadPlayerView> =
        LegacySeniorSquad.players(club).map { player ->
            LegacySeniorSquadPlayerView(
                playerId = player.id,
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
            )
        }
}

/**
 * Read-only senior-player view taken directly from the exact legacy team source.
 *
 * [sourceIndex] is the zero-based position in the serialized `players` collection;
 * it is provenance only and must not be interpreted as starter/reserve priority or
 * tactical order. The `legacy*` values remain opaque. Keeping source order and raw
 * identifiers beside the already-proven player fields gives later Marco B
 * characterization a deterministic boundary without matching players by name,
 * position, ratings or other heuristics. Junior entries remain a separate legacy
 * collection and are intentionally excluded here.
 */
data class LegacySourceSeniorSquadPlayerView(
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

object LegacySourceSeniorSquadPlayerViews {
    fun from(team: LegacyTeamSnapshot): List<LegacySourceSeniorSquadPlayerView> =
        team.players.mapIndexed { sourceIndex, player ->
            LegacySourceSeniorSquadPlayerView(
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
