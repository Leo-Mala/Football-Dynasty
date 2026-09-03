package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Minimal persisted projection of serializable legacy `konrent.t` league runtime. */
@Entity(
    tableName = "career_competitions",
    primaryKeys = ["careerId", "competitionId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerMetadataEntity::class,
            parentColumns = ["id"],
            childColumns = ["careerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["careerId"])],
)
data class CareerCompetitionEntity(
    val careerId: String,
    val competitionId: String,
    val legacyCompetitionType: Int,
    val legacyFormatCode: Int,
    val currentRoundNumber: Int,
    val totalRounds: Int,
    /** Exact serialized `LoadLigaOptions.nRebaixados`; null means the previous row has no proven source. */
    val legacyRelegationCount: Int? = null,
    /** Exact serialized `konrent.t.x0()`; null means the previous row has no proven source. */
    val legacyLeagueSubtype: Int? = null,
)

/** Persisted `best.e0` counters plus the current stable ordering used by the legacy comparator. */
@Entity(
    tableName = "career_competition_standings",
    primaryKeys = ["careerId", "competitionId", "clubId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerCompetitionEntity::class,
            parentColumns = ["careerId", "competitionId"],
            childColumns = ["careerId", "competitionId"],
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
    indices = [
        Index(value = ["careerId", "competitionId"]),
        Index(value = ["clubId"]),
    ],
)
data class CareerCompetitionStandingEntity(
    val careerId: String,
    val competitionId: String,
    val clubId: String,
    val stableOrdinal: Int,
    val points: Int,
    val played: Int,
    val wins: Int,
    val losses: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
)

/** Links already-persisted career matches to the exact legacy competition round/order. */
@Entity(
    tableName = "career_competition_matches",
    primaryKeys = ["careerId", "competitionId", "matchId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerCompetitionEntity::class,
            parentColumns = ["careerId", "competitionId"],
            childColumns = ["careerId", "competitionId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = CareerScheduledMatchEntity::class,
            parentColumns = ["careerId", "matchId"],
            childColumns = ["careerId", "matchId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["careerId", "competitionId"]),
        Index(value = ["careerId", "matchId"]),
        Index(
            value = ["careerId", "competitionId", "roundNumber", "fixtureOrdinal"],
            unique = true,
        ),
    ],
)
data class CareerCompetitionMatchEntity(
    val careerId: String,
    val competitionId: String,
    val matchId: String,
    val roundNumber: Int,
    val fixtureOrdinal: Int,
)
