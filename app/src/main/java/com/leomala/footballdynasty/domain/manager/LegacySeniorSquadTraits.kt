package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club

/**
 * Opaque legacy player traits exposed for the senior squad without assigning
 * gameplay meaning that has not yet been proven from Java/SMALI call paths.
 */
data class LegacySeniorPlayerTraits(
    val playerId: String,
    val cr1: Int,
    val cr2: Int,
    val star: Boolean,
    val worldTop: Boolean,
)

object LegacySeniorSquadTraits {
    fun from(club: Club): List<LegacySeniorPlayerTraits> =
        LegacySeniorSquad.players(club).map { player ->
            LegacySeniorPlayerTraits(
                playerId = player.id,
                cr1 = player.cr1,
                cr2 = player.cr2,
                star = player.star,
                worldTop = player.worldTop,
            )
        }
}
