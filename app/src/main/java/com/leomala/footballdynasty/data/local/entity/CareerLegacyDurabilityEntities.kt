package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Serialized legacy `best.h0` owner from annual `best.k0.h` or round `konrent.t.j0`. */
@Entity(
    tableName = "career_competition_snapshots",
    primaryKeys = ["careerId", "competitionId", "snapshotKind", "snapshotOrdinal"],
    foreignKeys = [
        ForeignKey(
            entity = CareerCompetitionEntity::class,
            parentColumns = ["careerId", "competitionId"],
            childColumns = ["careerId", "competitionId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["careerId", "competitionId"])],
)
data class CareerCompetitionSnapshotEntity(
    val careerId: String,
    val competitionId: String,
    /** `ANNUAL` represents `best.k0.h`; `ROUND` represents `konrent.t.j0`. */
    val snapshotKind: String,
    val snapshotOrdinal: Int,
    /** Raw serialized `best.h0.a`. */
    val legacyA: Int,
    /** Raw serialized `best.h0.b`. */
    val legacyB: Int,
    /** Aligned serialized `best.k0.i` entry for annual snapshots; absent for round snapshots. */
    val topPlayerId: String?,
)

/** Ordered player + current-club pairs retained by serialized legacy `best.h0`. */
@Entity(
    tableName = "career_competition_snapshot_members",
    primaryKeys = ["careerId", "competitionId", "snapshotKind", "snapshotOrdinal", "memberOrdinal"],
    foreignKeys = [
        ForeignKey(
            entity = CareerCompetitionSnapshotEntity::class,
            parentColumns = ["careerId", "competitionId", "snapshotKind", "snapshotOrdinal"],
            childColumns = ["careerId", "competitionId", "snapshotKind", "snapshotOrdinal"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["careerId", "competitionId", "snapshotKind", "snapshotOrdinal"]),
        Index(value = ["careerId", "playerId"]),
    ],
)
data class CareerCompetitionSnapshotMemberEntity(
    val careerId: String,
    val competitionId: String,
    val snapshotKind: String,
    val snapshotOrdinal: Int,
    val memberOrdinal: Int,
    val playerId: String,
    /** Legacy `best.k0.c()` appends player.u0() without a null check. */
    val clubIdAtSnapshot: String?,
)

/** Serialized annual `best.o.V` / `components.s2` player rating history. */
@Entity(
    tableName = "career_player_match_rating_history",
    primaryKeys = ["careerId", "playerId", "legacySeasonIndex", "legacyMatchIndex"],
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
        Index(value = ["careerId", "playerId"]),
        Index(value = ["careerId", "legacySeasonIndex", "legacyMatchIndex"]),
    ],
)
data class CareerPlayerMatchRatingHistoryEntity(
    val careerId: String,
    val playerId: String,
    /** Raw `components.s2.b`, sourced from legacy `best.b.j0()`. */
    val legacySeasonIndex: Int,
    /** Raw `components.s2.c`, zero-based index inside legacy `best.b.q0()`. */
    val legacyMatchIndex: Int,
    /** Raw `components.s2.a`, the already-resolved `best.o.O/y0`. */
    val legacyRating: Double,
)
