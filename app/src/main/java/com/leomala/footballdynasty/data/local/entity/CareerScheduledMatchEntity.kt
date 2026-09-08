package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Career-local scheduled match event and its resolved score.
 *
 * V17 also retains only recovered non-reconstructible legacy match identity/tie state. All added
 * values remain nullable for pre-V17 saves because those older rows have no proven source.
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
    /** Exact legacy `best.b.d/j0()` index into the one-best.a-per-day calendar. */
    val dayIndex: Int,
    val eventTypeCode: Int,
    val homeClubId: String,
    val awayClubId: String,
    val processed: Boolean,
    val homeGoals: Int?,
    val awayGoals: Int?,
    /** Zero-based index inside that day's `best.a.A()` match ArrayList: raw `components.s2.c`. */
    val legacyDayMatchOrdinal: Int? = null,
    /** Raw serialized `best.s.N0()` / `a0`; null means the previous row has no proven value. */
    val legacyTieBreakActive: Boolean? = null,
    /** Raw serialized `best.s.A0()` / `f0` explicit winner; never reconstructed from a tied score. */
    val legacyTieBreakWinnerClubId: String? = null,
)
