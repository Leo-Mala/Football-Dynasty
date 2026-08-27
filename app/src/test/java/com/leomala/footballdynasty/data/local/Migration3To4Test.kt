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
class Migration3To4Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FootballDynastyDatabase::class.java,
    )

    @Test
    fun `migration 3 to 4 preserves career and adds isolated scheduled match persistence`() {
        val name = "phase9-migration-3-4"
        var db = helper.createDatabase(name, 3)
        db.execSQL(
            "INSERT INTO career_metadata " +
                "(id,dataVersion,displayName,legacyMetadataFingerprint,legacyCareerFingerprint,createdAtEpochMillis,updatedAtEpochMillis) " +
                "VALUES ('career-v4',1,'Migration V4 probe',NULL,NULL,10,10)"
        )
        listOf("home", "away").forEach { id ->
            db.execSQL(
                "INSERT INTO clubs " +
                    "(id,dataVersion,importScope,sourceFileRef,name,country,state,level,stadium,capacity,reputation," +
                    "primaryColor,secondaryColor,coach,coachCountry,baseColor,legacyAid,legacySid,legacyTid,legacyVid,legacyId,legacyValid) " +
                    "VALUES ('$id',1,NULL,'$id','$id',0,0,1,'',0,0,'','','',0,0,0,0,0,0,0,1)"
            )
        }
        db.execSQL(
            "INSERT INTO career_core_state " +
                "(careerId,stateVersion,seasonNumber,seasonYear,calendarYear,currentDayIndex,startDayIndex,dayCount," +
                "rngInitialSeed,rngInternalState,rngDraws,managedClubId,transitionCount,updatedAtEpochMillis) " +
                "VALUES ('career-v4',1,1,2026,2026,3,3,365,7,11,12,NULL,0,20)"
        )
        db.close()

        db = helper.runMigrationsAndValidate(
            name,
            4,
            true,
            FootballDynastyMigrations.MIGRATION_3_4,
        )
        db.execSQL("PRAGMA foreign_keys=ON")

        db.query("SELECT rngDraws FROM career_core_state WHERE careerId='career-v4'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(12L, cursor.getLong(0))
        }
        db.query("SELECT COUNT(*) FROM career_scheduled_matches").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }

        db.execSQL(
            "INSERT INTO career_scheduled_matches " +
                "(careerId,matchId,dayIndex,eventTypeCode,homeClubId,awayClubId,processed,homeGoals,awayGoals) " +
                "VALUES ('career-v4','m1',3,1,'home','away',1,2,1)"
        )
        db.query(
            "SELECT dayIndex,processed,homeGoals,awayGoals FROM career_scheduled_matches " +
                "WHERE careerId='career-v4' AND matchId='m1'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(3, cursor.getInt(0))
            assertEquals(1, cursor.getInt(1))
            assertEquals(2, cursor.getInt(2))
            assertEquals(1, cursor.getInt(3))
        }

        try {
            db.execSQL(
                "INSERT INTO career_scheduled_matches " +
                    "(careerId,matchId,dayIndex,eventTypeCode,homeClubId,awayClubId,processed,homeGoals,awayGoals) " +
                    "VALUES ('career-v4','bad',4,1,'home','missing',0,NULL,NULL)"
            )
            fail("Expected away club foreign key rejection")
        } catch (_: SQLiteConstraintException) {
            // Expected.
        }

        db.execSQL("DELETE FROM career_metadata WHERE id='career-v4'")
        db.query("SELECT COUNT(*) FROM career_scheduled_matches").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        db.close()
    }
}
