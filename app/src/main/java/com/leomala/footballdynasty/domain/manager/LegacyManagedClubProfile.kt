package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club

/**
 * Persistence-independent projection of club administration fields already
 * carried by the certified legacy import model.
 *
 * No financial formula, stadium upgrade rule, competition rule or reputation
 * effect is inferred here. Country, state and level remain raw source-backed
 * integer codes; their behavioural semantics are not interpreted by this
 * projection.
 */
data class LegacyManagedClubProfile(
    val clubId: String,
    val country: Int,
    val state: Int,
    val level: Int,
    val stadium: String,
    val capacity: Int,
    val reputation: Int,
)

object LegacyManagedClubProfileProjection {
    fun from(club: Club): LegacyManagedClubProfile = LegacyManagedClubProfile(
        clubId = club.id,
        country = club.country,
        state = club.state,
        level = club.level,
        stadium = club.stadium,
        capacity = club.capacity,
        reputation = club.reputation,
    )
}
