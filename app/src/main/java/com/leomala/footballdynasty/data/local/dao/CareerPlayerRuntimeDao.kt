package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerProceduralPlayerEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity

@Dao
interface CareerPlayerRuntimeDao {
    @Upsert
    suspend fun upsertRuntime(entity: CareerPlayerRuntimeEntity)

    @Upsert
    suspend fun upsertRuntime(entities: List<CareerPlayerRuntimeEntity>)

    @Upsert
    suspend fun upsertProceduralPlayer(entity: CareerProceduralPlayerEntity)

    @Upsert
    suspend fun upsertMembership(entity: CareerSquadMembershipEntity)

    @Query(
        "SELECT * FROM career_player_runtime " +
            "WHERE careerId = :careerId AND playerId = :playerId LIMIT 1"
    )
    suspend fun findRuntime(careerId: String, playerId: String): CareerPlayerRuntimeEntity?

    @Query(
        "SELECT * FROM career_procedural_players " +
            "WHERE careerId = :careerId AND playerId = :playerId LIMIT 1"
    )
    suspend fun findProceduralPlayer(careerId: String, playerId: String): CareerProceduralPlayerEntity?

    @Query("SELECT * FROM career_player_runtime WHERE careerId = :careerId ORDER BY playerId")
    suspend fun runtimeForCareer(careerId: String): List<CareerPlayerRuntimeEntity>

    @Query("SELECT * FROM career_procedural_players WHERE careerId = :careerId ORDER BY playerId")
    suspend fun proceduralPlayersForCareer(careerId: String): List<CareerProceduralPlayerEntity>

    @Query("SELECT * FROM career_squad_memberships WHERE careerId = :careerId ORDER BY clubId, rosterKind, sourceOrdinal")
    suspend fun membershipsForCareer(careerId: String): List<CareerSquadMembershipEntity>

    @Query(
        "SELECT * FROM career_squad_memberships " +
            "WHERE careerId = :careerId AND playerId = :playerId LIMIT 1"
    )
    suspend fun findMembership(careerId: String, playerId: String): CareerSquadMembershipEntity?

    @Query("DELETE FROM career_player_runtime WHERE careerId = :careerId AND playerId = :playerId")
    suspend fun deleteRuntime(careerId: String, playerId: String)
}
