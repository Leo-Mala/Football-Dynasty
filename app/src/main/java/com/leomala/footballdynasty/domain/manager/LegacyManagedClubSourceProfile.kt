package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot

/**
 * Read-only administrative identity projected directly from the exact legacy
 * `.ban` team source selected for the managed club.
 *
 * These values are preserved as source facts only. Numeric country/state/level
 * and reputation values remain opaque here, and stadium/capacity are not given
 * any upgrade, finance, competition or gameplay semantics by this projection.
 */
data class LegacyManagedClubSourceProfile(
    val sourceFileRef: String,
    val name: String,
    val country: Int,
    val state: Int,
    val level: Int,
    val stadium: String,
    val capacity: Int,
    val reputation: Int,
)

object LegacyManagedClubSourceProfileProjection {
    fun from(team: LegacyTeamSnapshot): LegacyManagedClubSourceProfile =
        LegacyManagedClubSourceProfile(
            sourceFileRef = team.fileRef,
            name = team.name,
            country = team.country,
            state = team.state,
            level = team.level,
            stadium = team.stadium,
            capacity = team.capacity,
            reputation = team.reputation,
        )
}
