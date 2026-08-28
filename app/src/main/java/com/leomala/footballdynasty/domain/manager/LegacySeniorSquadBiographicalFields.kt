package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club

/**
 * Proven raw biographical fields for the club's legacy senior squad.
 *
 * `name`, `age` and `country` are serialized by the legacy player record and
 * already preserved by the modern import model. This projection deliberately
 * adds no age bands, nationality labels, eligibility rules or sorting.
 */
data class LegacySeniorPlayerBiographicalFields(
    val playerId: String,
    val name: String,
    val age: Int,
    val country: Int,
)

object LegacySeniorSquadBiographicalFields {
    fun from(club: Club): List<LegacySeniorPlayerBiographicalFields> =
        LegacySeniorSquad.players(club).map { player ->
            LegacySeniorPlayerBiographicalFields(
                playerId = player.id,
                name = player.name,
                age = player.age,
                country = player.country,
            )
        }
}
