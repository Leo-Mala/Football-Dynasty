package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.CareerCoachRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerCoachSeasonClubRecordEntity

@Dao
interface CareerCoachRuntimeDao {
    @Upsert suspend fun upsertCoachRuntime(entity: CareerCoachRuntimeEntity)
    @Upsert suspend fun upsertSeasonClubRecords(entities: List<CareerCoachSeasonClubRecordEntity>)

    @Query(
        "SELECT * FROM career_coach_runtime " +
            "WHERE careerId = :careerId AND managerSourceOrdinal = :managerSourceOrdinal LIMIT 1"
    )
    suspend fun findCoachRuntime(careerId: String, managerSourceOrdinal: Int): CareerCoachRuntimeEntity?

    @Query("SELECT * FROM career_coach_runtime WHERE careerId = :careerId ORDER BY managerSourceOrdinal")
    suspend fun coachRuntimeForCareer(careerId: String): List<CareerCoachRuntimeEntity>

    @Query(
        "SELECT * FROM career_coach_season_club_records " +
            "WHERE careerId = :careerId AND managerSourceOrdinal = :managerSourceOrdinal ORDER BY sourceOrdinal"
    )
    suspend fun seasonClubRecords(
        careerId: String,
        managerSourceOrdinal: Int,
    ): List<CareerCoachSeasonClubRecordEntity>

    @Query(
        "DELETE FROM career_coach_season_club_records " +
            "WHERE careerId = :careerId AND managerSourceOrdinal = :managerSourceOrdinal"
    )
    suspend fun deleteSeasonClubRecords(careerId: String, managerSourceOrdinal: Int)
}
