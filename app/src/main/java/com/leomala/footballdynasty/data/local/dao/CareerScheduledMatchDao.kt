package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.CareerScheduledMatchEntity

@Dao
interface CareerScheduledMatchDao {
    @Upsert
    suspend fun upsert(match: CareerScheduledMatchEntity)

    @Upsert
    suspend fun upsertAll(matches: List<CareerScheduledMatchEntity>)

    @Query(
        "SELECT * FROM career_scheduled_matches " +
            "WHERE careerId = :careerId ORDER BY dayIndex ASC, matchId ASC"
    )
    suspend fun findAll(careerId: String): List<CareerScheduledMatchEntity>

    @Query(
        "SELECT * FROM career_scheduled_matches " +
            "WHERE careerId = :careerId AND matchId = :matchId LIMIT 1"
    )
    suspend fun findById(careerId: String, matchId: String): CareerScheduledMatchEntity?
}
