package com.leomala.footballdynasty.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class Migration6To7Test {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), FootballDynastyDatabase::class.java)

    @Test
    fun `explicit migration chain preserves V7 manager state contract through current schema`() {
        val name = "marco-b-migration-3-current-v7-contract"
        var db = helper.createDatabase(name, 3)
        db.execSQL("INSERT INTO career_metadata (id,dataVersion,displayName,legacyMetadataFingerprint,legacyCareerFingerprint,createdAtEpochMillis,updatedAtEpochMillis) VALUES ('career-v7',1,'Migration V7 probe',NULL,NULL,10,10)")
        db.close()
        db = helper.runMigrationsAndValidate(
            name, FootballDynastyDatabase.SCHEMA_VERSION, true,
            FootballDynastyMigrations.MIGRATION_3_4,
            FootballDynastyMigrations.MIGRATION_4_5,
            Phase10CompetitionMigration.MIGRATION_5_6,
            Phase12ManagerPersistenceMigration.MIGRATION_6_7,
            Phase13StadiumRuntimeMigration.MIGRATION_7_8,
        )
        db.execSQL("PRAGMA foreign_keys=ON")
        val expectedTables = setOf(
            "career_player_commercial", "career_player_transfer_state", "career_club_manager_runtime",
            "career_active_loans", "career_stadium_constructions",
        )
        db.query("SELECT name FROM sqlite_master WHERE type='table'").use { c ->
            val actual=mutableSetOf<String>(); while(c.moveToNext()) actual+=c.getString(0); assertTrue(actual.containsAll(expectedTables)); assertTrue(actual.contains("career_stadium_runtime"))
        }
        expectedTables.forEach { table ->
            db.query("SELECT COUNT(*) FROM `$table`").use { c -> assertTrue(c.moveToFirst()); assertEquals("Migration must not synthesize $table rows",0,c.getInt(0)) }
        }
        db.query("SELECT COUNT(*) FROM career_stadium_runtime").use { c -> assertTrue(c.moveToFirst()); assertEquals("V8 must not synthesize stadium sectors",0,c.getInt(0)) }
        db.query("SELECT displayName FROM career_metadata WHERE id='career-v7'").use { c -> assertTrue(c.moveToFirst()); assertEquals("Migration V7 probe",c.getString(0)) }
        db.execSQL("DELETE FROM career_metadata WHERE id='career-v7'")
        db.close()
    }
}
