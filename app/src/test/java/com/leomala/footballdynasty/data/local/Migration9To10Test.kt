package com.leomala.footballdynasty.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class Migration9To10Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FootballDynastyDatabase::class.java,
    )

    @Test
    fun `migration to 10 adds nullable construction owner without inventing backfill`() {
        val name = "phase13-migration-9-10"
        var db = helper.createDatabase(name, 9)
        db.execSQL(
            "INSERT INTO career_metadata " +
                "(id,dataVersion,displayName,legacyMetadataFingerprint,legacyCareerFingerprint,createdAtEpochMillis,updatedAtEpochMillis) " +
                "VALUES ('career-v10',1,'Migration V10 probe',NULL,NULL,10,10)"
        )
        db.execSQL(
            "INSERT INTO career_stadium_constructions " +
                "(careerId,sourceOrdinal,stadiumCode,endTimestampMillis,addition0,addition1,addition2,addition3) " +
                "VALUES ('career-v10',0,77,20000,100,20,30,40)"
        )
        db.close()

        db = helper.runMigrationsAndValidate(
            name,
            10,
            true,
            Phase13StadiumConstructionOwnershipMigration.MIGRATION_9_10,
        )
        db.query(
            "SELECT stadiumCode, ownerClubId FROM career_stadium_constructions " +
                "WHERE careerId='career-v10' AND sourceOrdinal=0"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(77, cursor.getInt(0))
            assertNull(cursor.getString(1))
        }
        db.query("SELECT displayName FROM career_metadata WHERE id='career-v10'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Migration V10 probe", cursor.getString(0))
        }
        db.execSQL("DELETE FROM career_metadata WHERE id='career-v10'")
        db.close()
    }
}
