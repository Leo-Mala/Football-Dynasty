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

    @Query(
        """
        SELECT squad_memberships.*
        FROM squad_memberships
        INNER JOIN clubs ON clubs.id = squad_memberships.clubId
        WHERE clubs.importScope = :scope
        ORDER BY clubs.sourceFileRef,
                 CASE squad_memberships.rosterKind WHEN 'SENIOR' THEN 0 ELSE 1 END,
                 squad_memberships.sourceOrdinal
        """
    )
    suspend fun allForImportScope(scope: String): List<SquadMembershipEntity>

    @Query("SELECT COUNT(*) FROM squad_memberships")
    suspend fun count(): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM squad_memberships
        INNER JOIN clubs ON clubs.id = squad_memberships.clubId
        WHERE clubs.importScope = :scope AND squad_memberships.rosterKind = 'JUNIOR'
        """
    )
    suspend fun juniorCountForImportScope(scope: String): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM squad_memberships
        INNER JOIN clubs ON clubs.id = squad_memberships.clubId
        WHERE clubs.importScope = :scope AND squad_memberships.rosterKind = 'SENIOR'
        """
    )
    suspend fun seniorCountForImportScope(scope: String): Int
}
