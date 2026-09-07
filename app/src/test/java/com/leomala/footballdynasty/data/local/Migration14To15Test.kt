package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
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
class Migration14To15Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FootballDynastyDatabase::class.java,
    )

    @Test
    fun `migration 14 to 15 preserves player and leaves unknown senior fields null`() {
        val name = "phase15-migration-14-15"
        var db = helper.createDatabase(name, 14)
        db.execSQL(
            "INSERT INTO career_metadata " +
                "(id,dataVersion,displayName,legacyMetadataFingerprint,legacyCareerFingerprint,createdAtEpochMillis,updatedAtEpochMillis) " +
                "VALUES ('career-v15',1,'Senior V15 probe',NULL,NULL,10,10)"
        )
        db.execSQL(
            "INSERT INTO career_player_runtime " +
                "(careerId,playerId,sourceType,stateVersion,age,overall,marketValue,star,worldTop,legacyHash," +
                "legacyGeneratedO,legacyCreatedYear,contractEndEpochMillis,legacyPreviousMarketValue,legacyQ,legacyX,legacyY,legacyZ,energy,injuryUntilEpochDay) " +
                "VALUES ('career-v15','p1','PROCEDURAL',1,27,73,1234,1,1,17,0,2026,9000,111,0,0,0,0,88,42)"
        )
        db.close()

        db = helper.runMigrationsAndValidate(
            name,
            15,
            true,
            Phase15SeniorRuntimeMigration.MIGRATION_14_15,
        )
        db.query(
            "SELECT age,overall,marketValue,worldTop,energy,injuryUntilEpochDay," +
                "legacyAnnualM,legacyAnnualN,legacyRawPayrollN FROM career_player_runtime " +
                "WHERE careerId='career-v15' AND playerId='p1'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(27, cursor.getInt(0))
            assertEquals(73, cursor.getInt(1))
            assertEquals(1234, cursor.getInt(2))
            assertEquals(1, cursor.getInt(3))
            assertEquals(88, cursor.getInt(4))
            assertEquals(42L, cursor.getLong(5))
            assertTrue(cursor.isNull(6))
            assertTrue(cursor.isNull(7))
            assertTrue(cursor.isNull(8))
        }
        db.execSQL(
            "UPDATE career_player_runtime SET legacyAnnualM=1, legacyAnnualN=0.75, legacyRawPayrollN=4321 " +
                "WHERE careerId='career-v15' AND playerId='p1'"
        )
        db.close()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val reopened = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        val runtime = kotlinx.coroutines.runBlocking {
            reopened.careerPlayerRuntimeDao().findRuntime("career-v15", "p1")
        }
        requireNotNull(runtime)
        assertEquals(true, runtime.legacyAnnualM)
        assertEquals(0.75, runtime.legacyAnnualN ?: Double.NaN, 0.0)
        assertEquals(4321, runtime.legacyRawPayrollN)
        assertEquals(true, runtime.worldTop)
        reopened.close()
        context.deleteDatabase(name)
    }
}
