package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Durable projection of serialized legacy `best.k0.g` / `components.n1`.
 *
 * The legacy row retains a player reference plus rating sum, count, average and positional category.
 * We intentionally do not add a Room foreign key to career_player_runtime: legacy `k0.g` owns these
 * rows for the lifetime of the competition and the recovered source does not prove cascade semantics
 * when a player later leaves a career squad. Competition ownership itself is proven and cascades.
 */
@Entity(
    tableName = "career_competition_player_ratings",
    primaryKeys = ["careerId", "competitionId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerCompetitionEntity::class,
            parentColumns = ["careerId", "competitionId"],
            childColumns = ["careerId", "competitionId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["careerId", "competitionId"]),
        Index(value = ["careerId", "playerId"]),
    ],
)
data class CareerCompetitionPlayerRatingEntity(
    val careerId: String,
    val competitionId: String,
    val playerId: String,
    val legacyRatingSum: Double,
    val legacyRatingCount: Double,
    val legacyAverageRating: Double,
    val legacyCategory: Int,
)
