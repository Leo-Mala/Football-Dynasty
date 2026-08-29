package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot

/**
 * Opaque source identity carried by the serialized legacy team object.
 *
 * These integer fields are preserved exactly as imported from the official
 * corpus. Their gameplay meaning is not yet certified, so this projection must
 * not interpret, normalize or use them as replacements for the proven
 * `fileRef` provenance boundary.
 */
data class LegacyManagedClubSourceIdentity(
    val legacyAid: Int,
    val legacySid: Int,
    val legacyTid: Int,
    val legacyVid: Int,
    val legacyId: Int,
    val legacyValid: Boolean,
)

object LegacyManagedClubSourceIdentityProjection {
    fun from(team: LegacyTeamSnapshot): LegacyManagedClubSourceIdentity =
        LegacyManagedClubSourceIdentity(
            legacyAid = team.legacyAid,
            legacySid = team.legacySid,
            legacyTid = team.legacyTid,
            legacyVid = team.legacyVid,
            legacyId = team.legacyId,
            legacyValid = team.legacyValid,
        )
}
