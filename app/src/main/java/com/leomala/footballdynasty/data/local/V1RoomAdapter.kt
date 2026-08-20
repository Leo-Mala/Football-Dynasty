package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.local.entity.PlayerEntity
import com.leomala.footballdynasty.data.local.entity.SquadMembershipEntity
import com.leomala.footballdynasty.foundation.error.ImportVersionException
import com.leomala.footballdynasty.foundation.error.IntegrityMismatchException
import com.leomala.footballdynasty.migration.v1.CareerDataV1
import com.leomala.footballdynasty.migration.v1.ClubDataV1
import com.leomala.footballdynasty.migration.v1.DATA_SCHEMA_V1
import com.leomala.footballdynasty.migration.v1.PlayerDataV1
import com.leomala.footballdynasty.migration.v1.RosterKindV1

object V1RoomAdapter {
    fun clubEntity(data: ClubDataV1, importScope: String?): ClubEntity {
        requireV1(data.schemaVersion, "club", data.id)
        return ClubEntity(
            id = data.id,
            dataVersion = data.schemaVersion,
            importScope = importScope,
            sourceFileRef = data.sourceFileRef,
            name = data.name,
            country = data.country,
            state = data.state,
            level = data.level,
            stadium = data.stadium,
            capacity = data.capacity,
            reputation = data.reputation,
            primaryColor = data.primaryColor,
            secondaryColor = data.secondaryColor,
            coach = data.coach,
            coachCountry = data.coachCountry,
            baseColor = data.baseColor,
            legacyAid = data.legacyAid,
            legacySid = data.legacySid,
            legacyTid = data.legacyTid,
            legacyVid = data.legacyVid,
            legacyId = data.legacyId,
            legacyValid = data.legacyValid,
        )
    }

    fun playerEntity(data: PlayerDataV1, importScope: String?): PlayerEntity {
        requireV1(data.schemaVersion, "player", data.id)
        return PlayerEntity(
            id = data.id,
            dataVersion = data.schemaVersion,
            importScope = importScope,
            name = data.name,
            age = data.age,
            country = data.country,
            position = data.position,
            status = data.status,
            side = data.side,
            cr1 = data.cr1,
            cr2 = data.cr2,
            star = data.star,
            worldTop = data.worldTop,
            legacyAid = data.legacyAid,
            legacySid = data.legacySid,
            legacyTid = data.legacyTid,
            legacyHash = data.legacyHash,
        )
    }

    fun membershipEntity(data: PlayerDataV1): SquadMembershipEntity {
        requireV1(data.schemaVersion, "player", data.id)
        return SquadMembershipEntity(
            playerId = data.id,
            clubId = data.sourceClubId,
            rosterKind = data.rosterKind.name,
            sourceOrdinal = data.sourceOrdinal,
        )
    }

    fun careerEntity(
        data: CareerDataV1,
        createdAtEpochMillis: Long,
        updatedAtEpochMillis: Long,
    ): CareerMetadataEntity {
        requireV1(data.schemaVersion, "career", data.id)
        return CareerMetadataEntity(
            id = data.id,
            dataVersion = data.schemaVersion,
            displayName = data.displayName,
            legacyMetadataFingerprint = data.legacyMetadataFingerprint,
            legacyCareerFingerprint = data.legacyCareerFingerprint,
            createdAtEpochMillis = createdAtEpochMillis,
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }

    fun careerData(entity: CareerMetadataEntity): CareerDataV1 {
        requireV1(entity.dataVersion, "career entity", entity.id)
        return CareerDataV1(
            schemaVersion = entity.dataVersion,
            id = entity.id,
            displayName = entity.displayName,
            legacyMetadataFingerprint = entity.legacyMetadataFingerprint,
            legacyCareerFingerprint = entity.legacyCareerFingerprint,
        )
    }

    fun clubData(
        club: ClubEntity,
        players: List<PlayerEntity>,
        memberships: List<SquadMembershipEntity>,
    ): ClubDataV1 {
        requireV1(club.dataVersion, "club entity", club.id)
        if (players.size != memberships.size) {
            throw IntegrityMismatchException(
                "Room roster mismatch for ${club.id}: players=${players.size}, memberships=${memberships.size}"
            )
        }

        val playerById = players.associateBy { it.id }
        if (playerById.size != players.size) {
            throw IntegrityMismatchException("Duplicate player id returned for club ${club.id}")
        }

        val roster = memberships.map { membership ->
            if (membership.clubId != club.id) {
                throw IntegrityMismatchException(
                    "Membership ${membership.playerId} points to ${membership.clubId}, expected ${club.id}"
                )
            }
            val player = playerById[membership.playerId]
                ?: throw IntegrityMismatchException(
                    "Membership ${membership.playerId} has no matching player row"
                )
            playerData(player, membership)
        }

        return ClubDataV1(
            schemaVersion = club.dataVersion,
            id = club.id,
            sourceFileRef = club.sourceFileRef,
            name = club.name,
            country = club.country,
            state = club.state,
            level = club.level,
            stadium = club.stadium,
            capacity = club.capacity,
            reputation = club.reputation,
            primaryColor = club.primaryColor,
            secondaryColor = club.secondaryColor,
            coach = club.coach,
            coachCountry = club.coachCountry,
            baseColor = club.baseColor,
            legacyAid = club.legacyAid,
            legacySid = club.legacySid,
            legacyTid = club.legacyTid,
            legacyVid = club.legacyVid,
            legacyId = club.legacyId,
            legacyValid = club.legacyValid,
            players = roster,
        )
    }

    private fun playerData(
        player: PlayerEntity,
        membership: SquadMembershipEntity,
    ): PlayerDataV1 {
        requireV1(player.dataVersion, "player entity", player.id)
        val rosterKind = try {
            RosterKindV1.valueOf(membership.rosterKind)
        } catch (error: IllegalArgumentException) {
            throw IntegrityMismatchException(
                "Unknown roster kind '${membership.rosterKind}' for ${player.id}"
            )
        }

        return PlayerDataV1(
            schemaVersion = player.dataVersion,
            id = player.id,
            sourceClubId = membership.clubId,
            rosterKind = rosterKind,
            sourceOrdinal = membership.sourceOrdinal,
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

    private fun requireV1(version: Int, type: String, id: String) {
        if (version != DATA_SCHEMA_V1) {
            throw ImportVersionException(
                "Unsupported $type data version $version for $id; expected $DATA_SCHEMA_V1"
            )
        }
    }
}
