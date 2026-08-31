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
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FootballDynastyDatabase::class.java,
    )

    @Test
    fun `explicit chain through V6 preserves competition runtime in current schema`() {
        val name = "phase10-migration-3-7-v6-contract"
        var db = helper.createDatabase(name, 3)
        db.execSQL(
            "INSERT INTO career_metadata " +
                "(id,dataVersion,displayName,legacyMetadataFingerprint,legacyCareerFingerprint,createdAtEpochMillis,updatedAtEpochMillis) " +
                "VALUES ('career-v6',1,'Migration V6 probe',NULL,NULL,10,10)"
        )
        db.close()

        // V6 JSON was never committed as a historical schema asset. Validate the V6 competition
        // contract after the complete explicit migration chain to the current V7 schema instead.
        db = helper.runMigrationsAndValidate(
            name,
            FootballDynastyDatabase.SCHEMA_VERSION,
            true,
            FootballDynastyMigrations.MIGRATION_3_4,
            FootballDynastyMigrations.MIGRATION_4_5,
            Phase10CompetitionMigration.MIGRATION_5_6,
            Phase12ManagerPersistenceMigration.MIGRATION_6_7,
        )
        db.execSQL("PRAGMA foreign_keys=ON")

        db.execSQL(
            "INSERT INTO career_competitions " +
                "(careerId,competitionId,legacyCompetitionType,legacyFormatCode,currentRoundNumber,totalRounds) " +
                "VALUES ('career-v6','league-1',1,11,1,12)"
        )
        db.query(
            "SELECT legacyCompetitionType,legacyFormatCode,currentRoundNumber,totalRounds " +
                "FROM career_competitions WHERE careerId='career-v6' AND competitionId='league-1'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
            assertEquals(11, cursor.getInt(1))
            assertEquals(1, cursor.getInt(2))
            assertEquals(12, cursor.getInt(3))
        }

        db.query("PRAGMA table_info('career_competition_standings')").use { cursor ->
            val names = mutableSetOf<String>()
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) names += cursor.getString(nameIndex)
            assertEquals(
                setOf(
                    "careerId", "competitionId", "clubId", "stableOrdinal", "points", "played",
                    "wins", "losses", "goalsFor", "goalsAgainst",
                ),
                names,
            )
        }
        db.query("PRAGMA table_info('career_competition_matches')").use { cursor ->
            val names = mutableSetOf<String>()
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) names += cursor.getString(nameIndex)
            assertEquals(
                setOf("careerId", "competitionId", "matchId", "roundNumber", "fixtureOrdinal"),
                names,
            )
        }

        // V7 is additive: the Phase 10 tables remain present while the new manager tables exist too.
        db.query("SELECT COUNT(*) FROM career_player_commercial").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }

        db.execSQL("DELETE FROM career_metadata WHERE id='career-v6'")
        db.query("SELECT COUNT(*) FROM career_competitions").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        db.close()
    }
}
