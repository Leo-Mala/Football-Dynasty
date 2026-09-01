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
class Migration7To8Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FootballDynastyDatabase::class.java,
    )

    @Test
    fun `migration chain to 8 adds empty fail closed stadium runtime without destroying career`() {
        val name = "marco-b-migration-3-8"
        var db = helper.createDatabase(name, 3)
        db.execSQL(
            "INSERT INTO career_metadata " +
                "(id,dataVersion,displayName,legacyMetadataFingerprint,legacyCareerFingerprint,createdAtEpochMillis,updatedAtEpochMillis) " +
                "VALUES ('career-v8',1,'Migration V8 probe',NULL,NULL,10,10)"
        )
        db.close()

        db = helper.runMigrationsAndValidate(
            name,
            8,
            true,
            FootballDynastyMigrations.MIGRATION_3_4,
            FootballDynastyMigrations.MIGRATION_4_5,
            Phase10CompetitionMigration.MIGRATION_5_6,
            Phase12ManagerPersistenceMigration.MIGRATION_6_7,
            Phase13StadiumRuntimeMigration.MIGRATION_7_8,
        )
        db.execSQL("PRAGMA foreign_keys=ON")

        db.query("SELECT COUNT(*) FROM career_stadium_runtime").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("V8 migration must not synthesize stadium sectors", 0, cursor.getInt(0))
        }
        db.query("SELECT displayName FROM career_metadata WHERE id='career-v8'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Migration V8 probe", cursor.getString(0))
        }

        db.execSQL("DELETE FROM career_metadata WHERE id='career-v8'")
        db.close()
    }
}
