package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Mutable player state that belongs to one career only.
 *
 * Canonical player facts stay in `players`. Procedural player facts stay in
 * `career_procedural_players`. This table contains only runtime values that the legacy career graph
 * persists and mutates independently for each save.
 */
@Entity(
    tableName = "career_player_runtime",
    primaryKeys = ["careerId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerMetadataEntity::class,
            parentColumns = ["id"],
            childColumns = ["careerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["careerId"]),
        Index(value = ["playerId"]),
    ],
)
data class CareerPlayerRuntimeEntity(
    val careerId: String,
    val playerId: String,
    val sourceType: String,
    val stateVersion: Int,
    val age: Int,
    val overall: Int,
    val marketValue: Int,
    val star: Boolean,
    val worldTop: Boolean,
    val legacyHash: Int,
    val legacyGeneratedO: Int,
    val legacyCreatedYear: Int,
    val contractEndEpochMillis: Long,
    val legacyPreviousMarketValue: Int,
    val legacyQ: Boolean,
    val legacyX: Boolean,
    val legacyY: Boolean,
    val legacyZ: Boolean,
)

/** Static facts for a player created inside a career through the legacy procedural generator. */
@Entity(
    tableName = "career_procedural_players",
    primaryKeys = ["careerId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerPlayerRuntimeEntity::class,
            parentColumns = ["careerId", "playerId"],
            childColumns = ["careerId", "playerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["playerId"])],
)
data class CareerProceduralPlayerEntity(
    val careerId: String,
    val playerId: String,
    val name: String,
    val country: Int,
    val position: Int,
    val status: Int,
    val side: Int,
    val cr1: Int,
    val cr2: Int,
)

/** Career-local squad ownership. It intentionally does not mutate global `squad_memberships`. */
@Entity(
    tableName = "career_squad_memberships",
    primaryKeys = ["careerId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerPlayerRuntimeEntity::class,
            parentColumns = ["careerId", "playerId"],
            childColumns = ["careerId", "playerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["clubId"]),
        Index(value = ["careerId", "clubId", "rosterKind", "sourceOrdinal"], unique = true),
    ],
)
data class CareerSquadMembershipEntity(
    val careerId: String,
    val playerId: String,
    val clubId: String,
    val rosterKind: String,
    val sourceOrdinal: Int,
)
