package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Persisted projection of serializable legacy `best.e` player x club x season counters.
 *
 * Legacy field `i` is transient and is intentionally not stored. The neutral counter names are
 * preserved because Phase 8 characterized their write behavior without inventing additional
 * sporting labels.
 */
@Entity(
    tableName = "career_player_club_season_stats",
    primaryKeys = ["careerId", "playerId", "legacySeasonId", "legacyClubId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerPlayerRuntimeEntity::class,
            parentColumns = ["careerId", "playerId"],
            childColumns = ["careerId", "playerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["careerId", "playerId"])],
)
data class CareerPlayerClubSeasonStatEntity(
    val careerId: String,
    val playerId: String,
    val legacySeasonId: Int,
    val legacyClubId: Int,
    val legacyC: Int = 0,
    val legacyD: Int = 0,
    val legacyE: Int = 0,
    val legacyF: Int = 0,
    val legacyG: Int = 0,
    val legacyH: Int = 0,
)
