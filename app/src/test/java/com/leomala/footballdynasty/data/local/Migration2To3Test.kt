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
class Migration2To3Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FootballDynastyDatabase::class.java,
    )

    @Test
    fun `migration 2 to 3 preserves career state and creates isolated player runtime tables`() {
        val name = "phase7-migration-2-3"
        var db = helper.createDatabase(name, 2)
        db.execSQL(
            "INSERT INTO career_metadata " +
                "(id, dataVersion, displayName, legacyMetadataFingerprint, legacyCareerFingerprint, createdAtEpochMillis, updatedAtEpochMillis) " +
                "VALUES ('migration-career', 1, 'Migration V3 probe', NULL, NULL, 10, 10)"
        )
        db.execSQL(
            "INSERT INTO career_core_state " +
                "(careerId,stateVersion,seasonNumber,seasonYear,calendarYear,currentDayIndex,startDayIndex,dayCount," +
                "rngInitialSeed,rngInternalState,rngDraws,managedClubId,transitionCount,updatedAtEpochMillis) " +
                "VALUES ('migration-career',1,2,2027,2027,3,3,365,7,11,12,NULL,1,20)"
        )
        db.close()

        db = helper.runMigrationsAndValidate(
            name,
            3,
            true,
            FootballDynastyMigrations.MIGRATION_2_3,
        )

        db.query("SELECT displayName FROM career_metadata WHERE id = 'migration-career'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Migration V3 probe", cursor.getString(0))
        }
        db.query("SELECT seasonYear, rngDraws FROM career_core_state WHERE careerId = 'migration-career'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(2027, cursor.getInt(0))
            assertEquals(12L, cursor.getLong(1))
        }
        listOf(
            "career_player_runtime",
            "career_procedural_players",
            "career_squad_memberships",
        ).forEach { table ->
            db.query("SELECT COUNT(*) FROM $table").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
            }
        }

        db.execSQL(
            "INSERT INTO career_player_runtime " +
                "(careerId,playerId,sourceType,stateVersion,age,overall,marketValue,star,worldTop,legacyHash," +
                "legacyGeneratedO,legacyCreatedYear,contractEndEpochMillis,legacyPreviousMarketValue,legacyQ,legacyX,legacyY,legacyZ) " +
                "VALUES ('migration-career','generated','PROCEDURAL',1,18,70,500000,0,0,7,50,2027,999000,0,0,0,0,0)"
        )
        db.execSQL(
            "INSERT INTO career_procedural_players " +
                "(careerId,playerId,name,country,position,status,side,cr1,cr2) " +
                "VALUES ('migration-career','generated','Generated',29,3,0,1,4,11)"
        )
        db.execSQL(
            "INSERT INTO career_squad_memberships " +
                "(careerId,playerId,clubId,rosterKind,sourceOrdinal) " +
                "VALUES ('migration-career','generated','target-club','SENIOR',0)"
        )
        db.query(
            "SELECT r.overall, p.name, m.clubId " +
                "FROM career_player_runtime r " +
                "JOIN career_procedural_players p USING(careerId,playerId) " +
                "JOIN career_squad_memberships m USING(careerId,playerId) " +
                "WHERE r.careerId='migration-career' AND r.playerId='generated'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(70, cursor.getInt(0))
            assertEquals("Generated", cursor.getString(1))
            assertEquals("target-club", cursor.getString(2))
        }

        db.execSQL("DELETE FROM career_metadata WHERE id = 'migration-career'")
        listOf(
            "career_core_state",
            "career_player_runtime",
            "career_procedural_players",
            "career_squad_memberships",
        ).forEach { table ->
            db.query("SELECT COUNT(*) FROM $table").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("cascade $table", 0, cursor.getInt(0))
            }
        }
        db.close()
    }
}
