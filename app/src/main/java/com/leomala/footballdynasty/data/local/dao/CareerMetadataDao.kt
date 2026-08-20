package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity

@Dao
interface CareerMetadataDao {
    @Upsert
    suspend fun upsert(career: CareerMetadataEntity)

    @Query("SELECT * FROM career_metadata WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): CareerMetadataEntity?

    @Query("SELECT * FROM career_metadata ORDER BY updatedAtEpochMillis DESC")
    suspend fun all(): List<CareerMetadataEntity>
}
