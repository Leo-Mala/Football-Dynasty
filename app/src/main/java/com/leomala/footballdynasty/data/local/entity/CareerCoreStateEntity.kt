package com.leomala.footballdynasty.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "career_core_state",
    foreignKeys = [
        ForeignKey(
            entity = CareerMetadataEntity::class,
            parentColumns = ["id"],
            childColumns = ["careerId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION,
        ),
    ],
    indices = [Index(value = ["managedClubId"])],
)
data class CareerCoreStateEntity(
    @PrimaryKey val careerId: String,
    val stateVersion: Int,
    val seasonNumber: Int,
    val seasonYear: Int,
    val calendarYear: Int,
    val currentDayIndex: Int,
    val startDayIndex: Int,
    val dayCount: Int,
    val rngInitialSeed: Long,
    val rngInternalState: Long,
    val rngDraws: Long,
    val managedClubId: String?,
    val transitionCount: Long,
    val updatedAtEpochMillis: Long,
)
