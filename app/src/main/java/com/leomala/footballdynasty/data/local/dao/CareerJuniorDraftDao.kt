package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.CareerJuniorDraftEntity

@Dao
interface CareerJuniorDraftDao {
    @Query(
        """SELECT * FROM career_junior_drafts
            WHERE careerId = :careerId AND clubId = :clubId
            ORDER BY sourceOrdinal ASC"""
    )
    suspend fun listForClub(careerId: String, clubId: String): List<CareerJuniorDraftEntity>

    @Query(
        """SELECT COUNT(*) FROM career_junior_drafts
            WHERE careerId = :careerId AND clubId = :clubId"""
    )
    suspend fun countForClub(careerId: String, clubId: String): Int

    @Upsert
    suspend fun upsert(draft: CareerJuniorDraftEntity)

    @Upsert
    suspend fun upsertAll(drafts: List<CareerJuniorDraftEntity>)

    @Query(
        """DELETE FROM career_junior_drafts
            WHERE careerId = :careerId AND clubId = :clubId AND sourceOrdinal = :sourceOrdinal"""
    )
    suspend fun delete(careerId: String, clubId: String, sourceOrdinal: Int): Int

    @Query("DELETE FROM career_junior_drafts WHERE careerId = :careerId AND clubId = :clubId")
    suspend fun deleteForClub(careerId: String, clubId: String): Int
}
