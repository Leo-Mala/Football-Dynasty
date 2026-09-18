package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Exact durable `best.r` counter slice for one player inside one competition.
 *
 * The legacy object is created lazily by `best.o.o0(k0)` and its primitive counters start at zero.
 * V18 has no owner for this state, so the V18 -> V19 migration deliberately creates no rows: a
 * missing migrated row means "historical value not materialized", not permission to invent one.
 */
@Entity(
    tableName = "career_player_competition_discipline",
    primaryKeys = ["careerId", "playerId", "competitionId"],
    foreignKeys = [
        ForeignKey(
            entity = CareerPlayerRuntimeEntity::class,
            parentColumns = ["careerId", "playerId"],
            childColumns = ["careerId", "playerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = CareerCompetitionEntity::class,
            parentColumns = ["careerId", "competitionId"],
            childColumns = ["careerId", "competitionId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [
        Index(value = ["careerId", "playerId"]),
        Index(value = ["careerId", "competitionId"]),
    ],
)
data class CareerCompetitionDisciplineEntity(
    val careerId: String,
    val playerId: String,
    val competitionId: String,
    /** Raw `best.r.f4425c`; `best.r.n()` blocks at >= 3. */
    val legacyThreshold3Counter: Int,
    /** Raw `best.r.f4426d`; `best.r.n()` blocks at >= 1. */
    val legacyThreshold1Counter: Int,
)
