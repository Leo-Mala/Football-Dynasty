package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Career-local mutable club fields consumed by legacy ticket calculation. */
@Entity(
    tableName = "career_club_ticket_runtime",
    primaryKeys = ["careerId", "clubId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerMetadataEntity::class,
            parentColumns = ["id"],
            childColumns = ["careerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = ClubEntity::class,
            parentColumns = ["id"],
            childColumns = ["clubId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["clubId"])],
)
data class CareerClubTicketRuntimeEntity(
    val careerId: String,
    val clubId: String,
    val rawDivisionCode: Int,
    val legacyManagerId: Int,
)

/**
 * Ordered legacy manager list slice needed by `best.b.b1(id)` and ticket `best.f0.o()`.
 *
 * `legacyManagerId` is intentionally not unique: the legacy ArrayList lookup returns the first
 * matching manager, so duplicate ids remain observable state.
 */
@Entity(
    tableName = "career_manager_ticket_runtime",
    primaryKeys = ["careerId", "sourceOrdinal"],
    foreignKeys = [
        ForeignKey(
            entity = CareerMetadataEntity::class,
            parentColumns = ["id"],
            childColumns = ["careerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["careerId", "legacyManagerId"])],
)
data class CareerManagerTicketRuntimeEntity(
    val careerId: String,
    val sourceOrdinal: Int,
    val legacyManagerId: Int,
    val rawH: Int,
)

/** Exact constructor origin needed for the legacy `match.A() instanceof konrent.a0` branch. */
@Entity(
    tableName = "career_match_construction_source",
    primaryKeys = ["careerId", "matchId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerScheduledMatchEntity::class,
            parentColumns = ["careerId", "matchId"],
            childColumns = ["careerId", "matchId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["matchId"])],
)
data class CareerMatchConstructionSourceEntity(
    val careerId: String,
    val matchId: String,
    val sourceCode: String,
)
