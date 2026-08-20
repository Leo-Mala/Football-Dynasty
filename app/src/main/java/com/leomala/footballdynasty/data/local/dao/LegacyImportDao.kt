package com.leomala.footballdynasty.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leomala.footballdynasty.data.local.entity.LegacyImportManifestEntity
import com.leomala.footballdynasty.data.local.entity.LegacyImportStateEntity

@Dao
interface LegacyImportDao {
    @Query("SELECT * FROM legacy_import_state WHERE scope = :scope LIMIT 1")
    suspend fun state(scope: String): LegacyImportStateEntity?

    @Upsert
    suspend fun upsertState(state: LegacyImportStateEntity)

    @Query("SELECT * FROM legacy_import_manifest WHERE scope = :scope LIMIT 1")
    suspend fun manifest(scope: String): LegacyImportManifestEntity?

    @Upsert
    suspend fun upsertManifest(manifest: LegacyImportManifestEntity)

    @Query("DELETE FROM legacy_import_manifest WHERE scope = :scope")
    suspend fun deleteManifest(scope: String)

    @Query("DELETE FROM legacy_import_state WHERE scope = :scope")
    suspend fun deleteState(scope: String)
}
