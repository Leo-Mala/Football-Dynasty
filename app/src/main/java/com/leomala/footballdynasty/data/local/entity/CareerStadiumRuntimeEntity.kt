package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Career-local four-sector stadium capacity state from legacy `best.k.b:[I`.
 *
 * The aggregate `ClubEntity.capacity` cannot reconstruct these four source values, so V7 -> V8
 * deliberately creates no rows. Callers must materialize the four proven capacities explicitly.
 */
@Entity(
    tableName = "career_stadium_runtime",
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
data class CareerStadiumRuntimeEntity(
    val careerId: String,
    val clubId: String,
    val sector0Capacity: Int,
    val sector1Capacity: Int,
    val sector2Capacity: Int,
    val sector3Capacity: Int,
)
