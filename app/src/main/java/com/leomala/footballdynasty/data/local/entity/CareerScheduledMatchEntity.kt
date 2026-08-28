package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Career-local scheduled match event and its resolved score.
 *
 * Competition/table semantics deliberately remain outside this Phase 9 persistence boundary.
 */
@Entity(
    tableName = "career_scheduled_matches",
    primaryKeys = ["careerId", "matchId"],
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
            childColumns = ["homeClubId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = ClubEntity::class,
            parentColumns = ["id"],
            childColumns = ["awayClubId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["careerId", "dayIndex"]),
        Index(value = ["homeClubId"]),
        Index(value = ["awayClubId"]),
    ],
)
data class CareerScheduledMatchEntity(
    val careerId: String,
    val matchId: String,
    val dayIndex: Int,
    val eventTypeCode: Int,
    val homeClubId: String,
    val awayClubId: String,
    val processed: Boolean,
    val homeGoals: Int?,
    val awayGoals: Int?,
)
