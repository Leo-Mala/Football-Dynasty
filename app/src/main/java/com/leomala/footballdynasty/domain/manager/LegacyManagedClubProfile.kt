package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club

/**
 * Persistence-independent projection of club administration fields already
 * carried by the certified legacy import model.
 *
 * No financial formula, stadium upgrade rule or reputation effect is inferred
 * here. The projection only exposes source-backed values that are required by
 * Marco B / Phase 13 and already have direct legacy representation.
 */
data class LegacyManagedClubProfile(
    val clubId: String,
    val stadium: String,
    val capacity: Int,
    val reputation: Int,
)

object LegacyManagedClubProfileProjection {
    fun from(club: Club): LegacyManagedClubProfile = LegacyManagedClubProfile(
        clubId = club.id,
        stadium = club.stadium,
        capacity = club.capacity,
        reputation = club.reputation,
    )
}
