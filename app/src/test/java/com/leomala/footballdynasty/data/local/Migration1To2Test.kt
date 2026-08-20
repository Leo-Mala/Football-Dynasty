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
class Migration1To2Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FootballDynastyDatabase::class.java,
    )

    @Test
    fun `migration 1 to 2 preserves metadata and creates core state table`() {
        val name = "phase4-migration-1-2"
        var db = helper.createDatabase(name, 1)
        db.execSQL(
            "INSERT INTO career_metadata " +
                "(id, dataVersion, displayName, legacyMetadataFingerprint, legacyCareerFingerprint, createdAtEpochMillis, updatedAtEpochMillis) " +
                "VALUES ('migration-career', 1, 'Migration probe', NULL, NULL, 10, 10)"
        )
        db.close()

        db = helper.runMigrationsAndValidate(
            name,
            2,
            true,
            FootballDynastyMigrations.MIGRATION_1_2,
        )

        db.query("SELECT displayName FROM career_metadata WHERE id = 'migration-career'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Migration probe", cursor.getString(0))
        }
        db.query("SELECT COUNT(*) FROM career_core_state").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        db.execSQL(
            "INSERT INTO career_core_state " +
                "(careerId,stateVersion,seasonNumber,seasonYear,calendarYear,currentDayIndex,startDayIndex,dayCount," +
                "rngInitialSeed,rngInternalState,rngDraws,managedClubId,transitionCount,updatedAtEpochMillis) " +
                "VALUES ('migration-career',1,1,2026,2026,3,3,365,7,11,0,NULL,0,20)"
        )
        db.query("SELECT seasonYear FROM career_core_state WHERE careerId = 'migration-career'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(2026, cursor.getInt(0))
        }
        db.close()
    }
}
