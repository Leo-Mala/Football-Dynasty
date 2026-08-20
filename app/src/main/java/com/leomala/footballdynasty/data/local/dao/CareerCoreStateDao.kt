package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.CareerCoreStateEntity

@Dao
interface CareerCoreStateDao {
    @Upsert
    suspend fun upsert(state: CareerCoreStateEntity)

    @Query("SELECT * FROM career_core_state WHERE careerId = :careerId LIMIT 1")
    suspend fun findById(careerId: String): CareerCoreStateEntity?
}
