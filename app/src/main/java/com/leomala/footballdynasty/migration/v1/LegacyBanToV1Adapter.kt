package com.leomala.footballdynasty.migration.v1

import com.leomala.footballdynasty.domain.model.LegacyPlayerSnapshot
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import com.leomala.footballdynasty.foundation.error.DuplicateStableIdentityException
import com.leomala.footballdynasty.foundation.error.LegacyFormatException
import com.leomala.footballdynasty.foundation.identity.StableLegacyIdentity

/**
 * Explicit legacy compatibility model -> versioned migration DTO boundary.
 * No value is normalized, rebalanced, corrected, or inferred here.
 */
class LegacyBanToV1Adapter {
    fun adapt(snapshot: LegacyTeamSnapshot): ClubDataV1 {
        if (snapshot.fileRef.isBlank()) {
            throw LegacyFormatException("Legacy .ban team is missing its stable fileRef")
        }

        val clubId = StableLegacyIdentity.club(snapshot)
        val senior = snapshot.players.mapIndexed { index, player ->
            player.toV1(clubId, RosterKindV1.SENIOR, index)
        }
        val junior = snapshot.juniors.mapIndexed { index, player ->
            player.toV1(clubId, RosterKindV1.JUNIOR, index)
        }
        val roster = senior + junior

        val uniqueIds = roster.asSequence().map { it.id }.toSet()
        if (uniqueIds.size != roster.size) {
            throw DuplicateStableIdentityException(
                "Stable player identity collision inside legacy club ${snapshot.fileRef}"
            )
        }

        return ClubDataV1(
            id = clubId,
            sourceFileRef = snapshot.fileRef,
            name = snapshot.name,
            country = snapshot.country,
            state = snapshot.state,
            level = snapshot.level,
            stadium = snapshot.stadium,
            capacity = snapshot.capacity,
            reputation = snapshot.reputation,
            primaryColor = snapshot.primaryColor,
            secondaryColor = snapshot.secondaryColor,
            coach = snapshot.coach,
            coachCountry = snapshot.coachCountry,
            baseColor = snapshot.baseColor,
            legacyAid = snapshot.legacyAid,
            legacySid = snapshot.legacySid,
            legacyTid = snapshot.legacyTid,
            legacyVid = snapshot.legacyVid,
            legacyId = snapshot.legacyId,
            legacyValid = snapshot.legacyValid,
            players = roster,
        )
    }

    private fun LegacyPlayerSnapshot.toV1(
        clubId: String,
        rosterKind: RosterKindV1,
        sourceOrdinal: Int,
    ): PlayerDataV1 = PlayerDataV1(
        id = StableLegacyIdentity.player(clubId, rosterKind, sourceOrdinal, this),
        sourceClubId = clubId,
        rosterKind = rosterKind,
        sourceOrdinal = sourceOrdinal,
        name = name,
        age = age,
        country = country,
        position = position,
        status = status,
        side = side,
        cr1 = cr1,
        cr2 = cr2,
        star = star,
        worldTop = worldTop,
        legacyAid = legacyAid,
        legacySid = legacySid,
        legacyTid = legacyTid,
        legacyHash = legacyHash,
    )
}
