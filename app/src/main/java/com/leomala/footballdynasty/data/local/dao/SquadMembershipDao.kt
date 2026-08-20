package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.SquadMembershipEntity

@Dao
interface SquadMembershipDao {
    @Upsert
    suspend fun upsertAll(memberships: List<SquadMembershipEntity>)

    @Query(
        """
        SELECT * FROM squad_memberships
        WHERE clubId = :clubId
        ORDER BY CASE rosterKind WHEN 'SENIOR' THEN 0 ELSE 1 END, sourceOrdinal
        """
    )
    suspend fun membershipsForClub(clubId: String): List<SquadMembershipEntity>

    @Query("SELECT COUNT(*) FROM squad_memberships")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM squad_memberships WHERE rosterKind = 'JUNIOR'")
    suspend fun juniorCount(): Int

    @Query("SELECT COUNT(*) FROM squad_memberships WHERE rosterKind = 'SENIOR'")
    suspend fun seniorCount(): Int
}
