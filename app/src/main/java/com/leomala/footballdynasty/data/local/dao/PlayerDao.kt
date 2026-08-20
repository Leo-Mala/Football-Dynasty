package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.PlayerEntity

@Dao
interface PlayerDao {
    @Upsert
    suspend fun upsertAll(players: List<PlayerEntity>)

    @Query("SELECT * FROM players WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): PlayerEntity?

    @Query(
        """
        SELECT players.*
        FROM players
        INNER JOIN squad_memberships ON squad_memberships.playerId = players.id
        WHERE squad_memberships.clubId = :clubId
        ORDER BY CASE squad_memberships.rosterKind WHEN 'SENIOR' THEN 0 ELSE 1 END,
                 squad_memberships.sourceOrdinal
        """
    )
    suspend fun playersForClub(clubId: String): List<PlayerEntity>

    @Query("SELECT COUNT(*) FROM players")
    suspend fun count(): Int
}
