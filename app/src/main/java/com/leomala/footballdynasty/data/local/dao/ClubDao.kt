package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.ClubEntity

@Dao
interface ClubDao {
    @Upsert
    suspend fun upsertAll(clubs: List<ClubEntity>)

    @Query("SELECT * FROM clubs WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ClubEntity?

    @Query("SELECT * FROM clubs WHERE sourceFileRef = :fileRef LIMIT 1")
    suspend fun findBySourceFileRef(fileRef: String): ClubEntity?

    /** Legacy numeric ids are resolved fail-closed by callers; duplicates remain observable. */
    @Query("SELECT * FROM clubs WHERE legacyId = :legacyId ORDER BY sourceFileRef")
    suspend fun findByLegacyId(legacyId: Int): List<ClubEntity>

    @Query("SELECT * FROM clubs ORDER BY sourceFileRef")
    suspend fun all(): List<ClubEntity>

    @Query("SELECT * FROM clubs WHERE importScope = :scope ORDER BY sourceFileRef")
    suspend fun allForImportScope(scope: String): List<ClubEntity>

    @Query("SELECT COUNT(*) FROM clubs")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM clubs WHERE importScope = :scope")
    suspend fun countForImportScope(scope: String): Int

    @Query("DELETE FROM clubs WHERE importScope = :scope")
    suspend fun deleteForImportScope(scope: String)
}
