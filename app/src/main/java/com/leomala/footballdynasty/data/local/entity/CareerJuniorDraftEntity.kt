package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Durable pre-promotion state of legacy `best.p` inside a career club's junior list.
 *
 * This is intentionally separate from `career_procedural_players`: a `best.p` draft is not a
 * materialized senior player yet. Persisting the draft independently prevents promotion-only RNG
 * from being consumed early and preserves the legacy list order through [sourceOrdinal].
 */
@Entity(
    tableName = "career_junior_drafts",
    primaryKeys = ["careerId", "clubId", "sourceOrdinal"],
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
    indices = [
        Index(value = ["careerId"]),
        Index(value = ["clubId"]),
    ],
)
data class CareerJuniorDraftEntity(
    val careerId: String,
    val clubId: String,
    val sourceOrdinal: Int,
    val legacyN: Int,
    val legacyB: Boolean,
    val legacyC: Int,
    val legacyE: Int,
    val legacyJ: Int,
    val legacyL: Int,
    val legacyD: Int,
    val name: String,
    val legacyG: Int,
    val legacyF: Int,
    val legacyO: Int,
    val legacyM: Int,
    val legacyH: Int,
    val legacyI: Int,
    val developmentRemainder: Double,
)
