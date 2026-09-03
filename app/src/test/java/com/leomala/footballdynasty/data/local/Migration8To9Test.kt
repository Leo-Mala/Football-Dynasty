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
class Migration8To9Test {
    @get:Rule val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), FootballDynastyDatabase::class.java)
    @Test fun `migration through V9 preserves empty fail closed ticket runtime in current schema`() {
        val name="marco-b-migration-3-current-v9-contract"
        var db=helper.createDatabase(name,3)
        db.execSQL("INSERT INTO career_metadata (id,dataVersion,displayName,legacyMetadataFingerprint,legacyCareerFingerprint,createdAtEpochMillis,updatedAtEpochMillis) VALUES ('career-v9',1,'Migration V9 probe',NULL,NULL,10,10)")
        db.close()
        db=helper.runMigrationsAndValidate(name,FootballDynastyDatabase.SCHEMA_VERSION,true,
            FootballDynastyMigrations.MIGRATION_3_4,FootballDynastyMigrations.MIGRATION_4_5,
            Phase10CompetitionMigration.MIGRATION_5_6,Phase12ManagerPersistenceMigration.MIGRATION_6_7,
            Phase13StadiumRuntimeMigration.MIGRATION_7_8,Phase13TicketRuntimeMigration.MIGRATION_8_9,
            Phase13StadiumConstructionOwnershipMigration.MIGRATION_9_10,Phase14CoachRuntimeMigration.MIGRATION_10_11,
            Phase14CompetitionInputsMigration.MIGRATION_11_12,Phase14CompetitionInputsMigration.MIGRATION_12_13)
        db.execSQL("PRAGMA foreign_keys=ON")
        for(table in listOf("career_club_ticket_runtime","career_manager_ticket_runtime","career_match_construction_source")) db.query("SELECT COUNT(*) FROM $table").use { c -> assertTrue(c.moveToFirst()); assertEquals("V9 migration must not synthesize $table rows",0,c.getInt(0)) }
        for(table in listOf("career_coach_runtime","career_coach_season_club_records")) db.query("SELECT COUNT(*) FROM $table").use { c -> assertTrue(c.moveToFirst()); assertEquals("V11 migration must not synthesize $table rows",0,c.getInt(0)) }
        db.query("SELECT legacyRelegationCount,legacyLeagueSubtype FROM career_competitions").use { c -> assertTrue(!c.moveToFirst()) }
        db.query("SELECT displayName FROM career_metadata WHERE id='career-v9'").use { c -> assertTrue(c.moveToFirst()); assertEquals("Migration V9 probe",c.getString(0)) }
        db.execSQL("DELETE FROM career_metadata WHERE id='career-v9'")
        db.close()
    }
}
