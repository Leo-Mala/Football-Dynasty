package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot

/**
 * Compatibility projection for coach identity fields that are explicitly
 * serialized by the certified legacy team snapshot.
 *
 * The country code remains opaque: no nationality label, reputation,
 * employment rule or career progression behavior is inferred here.
 */
data class LegacyCoachProfile(
    val teamFileRef: String,
    val coachName: String,
    val coachCountry: Int,
)

object LegacyCoachProfileProjection {
    fun from(team: LegacyTeamSnapshot): LegacyCoachProfile = LegacyCoachProfile(
        teamFileRef = team.fileRef,
        coachName = team.coach,
        coachCountry = team.coachCountry,
    )
}
