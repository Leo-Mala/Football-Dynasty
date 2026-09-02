package com.leomala.footballdynasty.data.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class Migration4To5Test {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), FootballDynastyDatabase::class.java)

    @Test
    fun `explicit chain through V5 preserves V5 contract in current schema`() {
        val name = "phase9-migration-v5-contract"
        var db = helper.createDatabase(name, 3)
        db.execSQL("INSERT INTO career_metadata (id,dataVersion,displayName,legacyMetadataFingerprint,legacyCareerFingerprint,createdAtEpochMillis,updatedAtEpochMillis) VALUES ('career-v5',1,'Migration V5 probe',NULL,NULL,10,10)")
        db.execSQL("INSERT INTO career_player_runtime (careerId,playerId,sourceType,stateVersion,age,overall,marketValue,star,worldTop,legacyHash,legacyGeneratedO,legacyCreatedYear,contractEndEpochMillis,legacyPreviousMarketValue,legacyQ,legacyX,legacyY,legacyZ) VALUES ('career-v5','p1','CANONICAL',1,35,81,1000,0,0,44,0,0,0,0,0,0,0,0)")
        db.close()

        db = helper.runMigrationsAndValidate(
            name, FootballDynastyDatabase.SCHEMA_VERSION, true,
            FootballDynastyMigrations.MIGRATION_3_4,
            FootballDynastyMigrations.MIGRATION_4_5,
            Phase10CompetitionMigration.MIGRATION_5_6,
            Phase12ManagerPersistenceMigration.MIGRATION_6_7,
            Phase13StadiumRuntimeMigration.MIGRATION_7_8,
            Phase13TicketRuntimeMigration.MIGRATION_8_9,
            Phase13StadiumConstructionOwnershipMigration.MIGRATION_9_10,
            Phase14CoachRuntimeMigration.MIGRATION_10_11,
        )
        db.execSQL("PRAGMA foreign_keys=ON")
        db.query("SELECT age,overall,energy,injuryUntilEpochDay FROM career_player_runtime WHERE careerId='career-v5' AND playerId='p1'").use { c ->
            assertTrue(c.moveToFirst()); assertEquals(35,c.getInt(0)); assertEquals(81,c.getInt(1)); assertEquals(100,c.getInt(2)); assertEquals(0L,c.getLong(3))
        }
        db.query("SELECT COUNT(*) FROM career_player_club_season_stats").use { c -> assertTrue(c.moveToFirst()); assertEquals(0,c.getInt(0)) }
        db.execSQL("INSERT INTO career_player_club_season_stats (careerId,playerId,legacySeasonId,legacyClubId,legacyC,legacyD,legacyE,legacyF,legacyG,legacyH) VALUES ('career-v5','p1',1,101,2,3,4,5,6,7)")
        db.query("SELECT legacyC,legacyD,legacyE,legacyF,legacyG,legacyH FROM career_player_club_season_stats WHERE careerId='career-v5' AND playerId='p1'").use { c ->
            assertTrue(c.moveToFirst()); (0..5).forEach { assertEquals(it+2,c.getInt(it)) }
        }
        try {
            db.execSQL("INSERT INTO career_player_club_season_stats (careerId,playerId,legacySeasonId,legacyClubId,legacyC,legacyD,legacyE,legacyF,legacyG,legacyH) VALUES ('career-v5','missing',1,101,0,0,0,0,0,0)")
            fail("Expected player runtime foreign key rejection")
        } catch (_: SQLiteConstraintException) {}
        db.execSQL("DELETE FROM career_player_runtime WHERE careerId='career-v5' AND playerId='p1'")
        db.query("SELECT COUNT(*) FROM career_player_club_season_stats").use { c -> assertTrue(c.moveToFirst()); assertEquals(0,c.getInt(0)) }
        db.query("SELECT COUNT(*) FROM career_coach_runtime").use { c -> assertTrue(c.moveToFirst()); assertEquals("V11 must not invent coach state",0,c.getInt(0)) }
        db.close()
    }
}
