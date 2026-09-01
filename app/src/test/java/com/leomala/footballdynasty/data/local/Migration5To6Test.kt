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
class Migration5To6Test {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), FootballDynastyDatabase::class.java)

    @Test
    fun `explicit chain through V6 preserves competition runtime in current schema`() {
        val name = "phase10-migration-3-current-v6-contract"
        var db = helper.createDatabase(name, 3)
        db.execSQL("INSERT INTO career_metadata (id,dataVersion,displayName,legacyMetadataFingerprint,legacyCareerFingerprint,createdAtEpochMillis,updatedAtEpochMillis) VALUES ('career-v6',1,'Migration V6 probe',NULL,NULL,10,10)")
        db.close()
        db = helper.runMigrationsAndValidate(
            name, FootballDynastyDatabase.SCHEMA_VERSION, true,
            FootballDynastyMigrations.MIGRATION_3_4,
            FootballDynastyMigrations.MIGRATION_4_5,
            Phase10CompetitionMigration.MIGRATION_5_6,
            Phase12ManagerPersistenceMigration.MIGRATION_6_7,
            Phase13StadiumRuntimeMigration.MIGRATION_7_8,
            Phase13TicketRuntimeMigration.MIGRATION_8_9,
        )
        db.execSQL("PRAGMA foreign_keys=ON")
        db.execSQL("INSERT INTO career_competitions (careerId,competitionId,legacyCompetitionType,legacyFormatCode,currentRoundNumber,totalRounds) VALUES ('career-v6','league-1',1,11,1,12)")
        db.query("SELECT legacyCompetitionType,legacyFormatCode,currentRoundNumber,totalRounds FROM career_competitions WHERE careerId='career-v6' AND competitionId='league-1'").use { c ->
            assertTrue(c.moveToFirst()); assertEquals(1,c.getInt(0)); assertEquals(11,c.getInt(1)); assertEquals(1,c.getInt(2)); assertEquals(12,c.getInt(3))
        }
        db.query("PRAGMA table_info('career_competition_standings')").use { c ->
            val names=mutableSetOf<String>(); val i=c.getColumnIndex("name"); while(c.moveToNext()) names+=c.getString(i)
            assertEquals(setOf("careerId","competitionId","clubId","stableOrdinal","points","played","wins","losses","goalsFor","goalsAgainst"),names)
        }
        db.query("PRAGMA table_info('career_competition_matches')").use { c ->
            val names=mutableSetOf<String>(); val i=c.getColumnIndex("name"); while(c.moveToNext()) names+=c.getString(i)
            assertEquals(setOf("careerId","competitionId","matchId","roundNumber","fixtureOrdinal"),names)
        }
        db.query("SELECT COUNT(*) FROM career_player_commercial").use { c -> assertTrue(c.moveToFirst()); assertEquals(0,c.getInt(0)) }
        db.execSQL("DELETE FROM career_metadata WHERE id='career-v6'")
        db.query("SELECT COUNT(*) FROM career_competitions").use { c -> assertTrue(c.moveToFirst()); assertEquals(0,c.getInt(0)) }
        db.close()
    }
}
