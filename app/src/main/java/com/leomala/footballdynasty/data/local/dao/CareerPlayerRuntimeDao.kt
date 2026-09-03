package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.CareerPlayerClubSeasonStatEntity
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

    @Upsert
    suspend fun upsertClubSeasonStat(entity: CareerPlayerClubSeasonStatEntity)

    @Upsert
    suspend fun upsertClubSeasonStats(entities: List<CareerPlayerClubSeasonStatEntity>)

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

    @Query(
        "SELECT MAX(sourceOrdinal) FROM career_squad_memberships " +
            "WHERE careerId = :careerId AND clubId = :clubId AND rosterKind = :rosterKind"
    )
    suspend fun maxMembershipOrdinal(
        careerId: String,
        clubId: String,
        rosterKind: String,
    ): Int?

    @Query(
        "SELECT * FROM career_squad_memberships " +
            "WHERE careerId = :careerId AND clubId = :clubId AND rosterKind = :rosterKind " +
            "ORDER BY sourceOrdinal, playerId"
    )
    suspend fun membershipsForClub(
        careerId: String,
        clubId: String,
        rosterKind: String,
    ): List<CareerSquadMembershipEntity>

    @Query(
        "SELECT * FROM career_player_club_season_stats " +
            "WHERE careerId = :careerId AND playerId = :playerId " +
            "ORDER BY legacySeasonId, legacyClubId"
    )
    suspend fun clubSeasonStatsForPlayer(
        careerId: String,
        playerId: String,
    ): List<CareerPlayerClubSeasonStatEntity>

    @Query(
        "SELECT * FROM career_player_club_season_stats " +
            "WHERE careerId = :careerId ORDER BY playerId, legacySeasonId, legacyClubId"
    )
    suspend fun clubSeasonStatsForCareer(careerId: String): List<CareerPlayerClubSeasonStatEntity>

    @Query("DELETE FROM career_player_runtime WHERE careerId = :careerId AND playerId = :playerId")
    suspend fun deleteRuntime(careerId: String, playerId: String)
}
