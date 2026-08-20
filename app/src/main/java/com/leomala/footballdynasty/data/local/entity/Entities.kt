package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clubs",
    indices = [
        Index(value = ["sourceFileRef"], unique = true),
        Index(value = ["importScope"]),
    ],
)
data class ClubEntity(
    @PrimaryKey val id: String,
    val dataVersion: Int,
    val importScope: String?,
    val sourceFileRef: String,
    val name: String,
    val country: Int,
    val state: Int,
    val level: Int,
    val stadium: String,
    val capacity: Int,
    val reputation: Int,
    val primaryColor: String,
    val secondaryColor: String,
    val coach: String,
    val coachCountry: Int,
    val baseColor: Int,
    val legacyAid: Int,
    val legacySid: Int,
    val legacyTid: Int,
    val legacyVid: Int,
    val legacyId: Int,
    val legacyValid: Boolean,
)

@Entity(
    tableName = "players",
    indices = [Index(value = ["importScope"])],
)
data class PlayerEntity(
    @PrimaryKey val id: String,
    val dataVersion: Int,
    val importScope: String?,
    val name: String,
    val age: Int,
    val country: Int,
    val position: Int,
    val status: Int,
    val side: Int,
    val cr1: Int,
    val cr2: Int,
    val star: Boolean,
    val worldTop: Boolean,
    val legacyAid: Int,
    val legacySid: Int,
    val legacyTid: Int,
    val legacyHash: Int,
)

@Entity(
    tableName = "squad_memberships",
    primaryKeys = ["playerId"],
    foreignKeys = [
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = ClubEntity::class,
            parentColumns = ["id"],
            childColumns = ["clubId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["clubId"]),
        Index(value = ["clubId", "rosterKind", "sourceOrdinal"], unique = true),
    ],
)
data class SquadMembershipEntity(
    val playerId: String,
    val clubId: String,
    val rosterKind: String,
    val sourceOrdinal: Int,
)

@Entity(tableName = "legacy_import_state")
data class LegacyImportStateEntity(
    @PrimaryKey val scope: String,
    val status: String,
    val adapterVersion: Int,
    val schemaVersion: Int,
    val sourceManifestSha256: String?,
    val semanticFingerprint: String?,
    val updatedAtEpochMillis: Long,
    val lastError: String?,
)

@Entity(tableName = "legacy_import_manifest")
data class LegacyImportManifestEntity(
    @PrimaryKey val scope: String,
    val adapterVersion: Int,
    val schemaVersion: Int,
    val sourceCount: Int,
    val clubCount: Int,
    val seniorCount: Int,
    val juniorCount: Int,
    val sourceManifestSha256: String,
    val semanticFingerprint: String,
    val importedAtEpochMillis: Long,
)

/**
 * Initial modern save envelope only. It does not represent or claim migration
 * of the legacy Kryo career graph.
 */
@Entity(tableName = "career_metadata")
data class CareerMetadataEntity(
    @PrimaryKey val id: String,
    val dataVersion: Int,
    val displayName: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
