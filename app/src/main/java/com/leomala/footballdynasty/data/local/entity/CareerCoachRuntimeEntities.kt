package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * V11 career-local manager fields whose exact legacy semantics are characterized in Phase 14.
 *
 * H deliberately remains in `career_manager_ticket_runtime`: that ordered V9 row is already the
 * certified source for `best.b.b1(id)` first-match identity plus `best.f0.H`. This table hangs off
 * the exact manager source ordinal and adds no duplicate H copy.
 */
@Entity(
    tableName = "career_coach_runtime",
    primaryKeys = ["careerId", "managerSourceOrdinal"],
    foreignKeys = [
        ForeignKey(
            entity = CareerManagerTicketRuntimeEntity::class,
            parentColumns = ["careerId", "sourceOrdinal"],
            childColumns = ["careerId", "managerSourceOrdinal"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
)
data class CareerCoachRuntimeEntity(
    val careerId: String,
    val managerSourceOrdinal: Int,
    val isUserControlled: Boolean,
    val currentClubId: String?,
    val alternativeClubId: String?,
    val previousClubId: String?,
    val previousClubCountry: Int?,
    val previousClubDivisionIndex: Int?,
    val rawG: Int,
    val rawD: Int,
    val rawE: Int,
    val rawF: Int,
    val rawO: Int,
    val rawM: Int,
)

/**
 * Ordered legacy `best.f0.n` / record-list state. Duplicate season+club records are preserved
 * because `best.f0.q(c0)` observes the first matching ArrayList entry rather than enforcing a key.
 */
@Entity(
    tableName = "career_coach_season_club_records",
    primaryKeys = ["careerId", "managerSourceOrdinal", "sourceOrdinal"],
    foreignKeys = [
        ForeignKey(
            entity = CareerCoachRuntimeEntity::class,
            parentColumns = ["careerId", "managerSourceOrdinal"],
            childColumns = ["careerId", "managerSourceOrdinal"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["careerId", "managerSourceOrdinal"])],
)
data class CareerCoachSeasonClubRecordEntity(
    val careerId: String,
    val managerSourceOrdinal: Int,
    val sourceOrdinal: Int,
    val legacySeasonId: Int,
    val legacyClubId: Int,
    val rawMatches: Int,
    val rawWins: Int,
    val rawLosses: Int,
    val rawPoints: Int,
    val rawOtherCount: Int,
)
