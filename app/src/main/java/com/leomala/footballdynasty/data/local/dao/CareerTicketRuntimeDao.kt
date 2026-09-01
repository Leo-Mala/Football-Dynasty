package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.CareerClubTicketRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerManagerTicketRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerMatchConstructionSourceEntity

@Dao
interface CareerTicketRuntimeDao {
    @Upsert suspend fun upsertClubState(entity: CareerClubTicketRuntimeEntity)
    @Upsert suspend fun upsertManagerStates(entities: List<CareerManagerTicketRuntimeEntity>)
    @Upsert suspend fun upsertMatchConstructionSource(entity: CareerMatchConstructionSourceEntity)

    @Query("SELECT * FROM career_club_ticket_runtime WHERE careerId = :careerId AND clubId = :clubId LIMIT 1")
    suspend fun findClubState(careerId: String, clubId: String): CareerClubTicketRuntimeEntity?

    @Query("SELECT * FROM career_manager_ticket_runtime WHERE careerId = :careerId ORDER BY sourceOrdinal")
    suspend fun managerStates(careerId: String): List<CareerManagerTicketRuntimeEntity>

    @Query("DELETE FROM career_manager_ticket_runtime WHERE careerId = :careerId")
    suspend fun deleteManagerStates(careerId: String)

    @Query("SELECT * FROM career_match_construction_source WHERE careerId = :careerId AND matchId = :matchId LIMIT 1")
    suspend fun findMatchConstructionSource(careerId: String, matchId: String): CareerMatchConstructionSourceEntity?
}
