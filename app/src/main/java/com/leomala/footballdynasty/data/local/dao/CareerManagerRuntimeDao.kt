package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.CareerActiveLoanEntity
import com.leomala.footballdynasty.data.local.entity.CareerClubManagerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerCommercialEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerTransferStateEntity
import com.leomala.footballdynasty.data.local.entity.CareerStadiumConstructionEntity

@Dao
interface CareerManagerRuntimeDao {
    @Upsert
    suspend fun upsertPlayerCommercial(entity: CareerPlayerCommercialEntity)

    @Upsert
    suspend fun upsertPlayerTransferState(entity: CareerPlayerTransferStateEntity)

    @Upsert
    suspend fun upsertClubRuntime(entity: CareerClubManagerRuntimeEntity)

    @Upsert
    suspend fun upsertActiveLoan(entity: CareerActiveLoanEntity)

    @Upsert
    suspend fun upsertStadiumConstruction(entity: CareerStadiumConstructionEntity)

    @Upsert
    suspend fun upsertStadiumConstructions(entities: List<CareerStadiumConstructionEntity>)

    @Query(
        "SELECT * FROM career_player_commercial " +
            "WHERE careerId = :careerId AND playerId = :playerId LIMIT 1"
    )
    suspend fun findPlayerCommercial(careerId: String, playerId: String): CareerPlayerCommercialEntity?

    @Query(
        "SELECT * FROM career_player_transfer_state " +
            "WHERE careerId = :careerId AND playerId = :playerId LIMIT 1"
    )
    suspend fun findPlayerTransferState(careerId: String, playerId: String): CareerPlayerTransferStateEntity?

    @Query("SELECT * FROM career_player_transfer_state WHERE careerId = :careerId ORDER BY playerId")
    suspend fun playerTransferStateForCareer(careerId: String): List<CareerPlayerTransferStateEntity>

    @Query(
        "SELECT * FROM career_club_manager_runtime " +
            "WHERE careerId = :careerId AND clubId = :clubId LIMIT 1"
    )
    suspend fun findClubRuntime(careerId: String, clubId: String): CareerClubManagerRuntimeEntity?

    @Query("SELECT * FROM career_club_manager_runtime WHERE careerId = :careerId ORDER BY clubId")
    suspend fun clubRuntimeForCareer(careerId: String): List<CareerClubManagerRuntimeEntity>

    @Query(
        "SELECT * FROM career_active_loans " +
            "WHERE careerId = :careerId AND playerId = :playerId LIMIT 1"
    )
    suspend fun findActiveLoan(careerId: String, playerId: String): CareerActiveLoanEntity?

    @Query("SELECT * FROM career_active_loans WHERE careerId = :careerId ORDER BY playerId")
    suspend fun activeLoansForCareer(careerId: String): List<CareerActiveLoanEntity>

    @Query("DELETE FROM career_active_loans WHERE careerId = :careerId AND playerId = :playerId")
    suspend fun deleteActiveLoan(careerId: String, playerId: String)

    @Query(
        "SELECT * FROM career_stadium_constructions " +
            "WHERE careerId = :careerId ORDER BY sourceOrdinal"
    )
    suspend fun stadiumConstructions(careerId: String): List<CareerStadiumConstructionEntity>

    @Query("SELECT MAX(sourceOrdinal) FROM career_stadium_constructions WHERE careerId = :careerId")
    suspend fun maxStadiumConstructionOrdinal(careerId: String): Int?

    @Query("DELETE FROM career_stadium_constructions WHERE careerId = :careerId")
    suspend fun deleteStadiumConstructions(careerId: String)
}
