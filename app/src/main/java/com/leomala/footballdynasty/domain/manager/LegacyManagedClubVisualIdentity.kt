package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot

/**
 * Read-only projection of visual identity fields already proven on the legacy
 * team snapshot.
 *
 * These values are preserved exactly as imported. No color parsing, palette
 * normalization, UI theme selection or gameplay meaning is inferred here.
 */
data class LegacyManagedClubVisualIdentity(
    val primaryColor: String,
    val secondaryColor: String,
    val baseColor: Int,
)

object LegacyManagedClubVisualIdentityProjection {
    fun from(team: LegacyTeamSnapshot): LegacyManagedClubVisualIdentity =
        LegacyManagedClubVisualIdentity(
            primaryColor = team.primaryColor,
            secondaryColor = team.secondaryColor,
            baseColor = team.baseColor,
        )
}
