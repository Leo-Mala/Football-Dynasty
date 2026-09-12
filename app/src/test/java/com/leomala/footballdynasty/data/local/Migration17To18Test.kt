package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.leomala.footballdynasty.domain.manager.LegacyTacticsRawState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class Migration17To18Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FootballDynastyDatabase::class.java,
    )

    @Test
    fun `migration 17 to 18 materializes proved constructor tactics then preserves mutations on reopen`() {
        val name = "phase17-migration-17-18-tactics"
        var db = helper.createDatabase(name, 17)
        db.execSQL(
            "INSERT INTO clubs " +
                "(id,dataVersion,importScope,sourceFileRef,name,country,state,level,stadium,capacity,reputation," +
                "primaryColor,secondaryColor,coach,coachCountry,baseColor,legacyAid,legacySid,legacyTid,legacyVid,legacyId,legacyValid) " +
                "VALUES ('club-v18',1,NULL,'teams/club-v18.ban','Club V18',11,0,1,'',0,0,'','','',0,0,0,0,0,0,0,1)"
        )
        db.execSQL(
            "INSERT INTO career_metadata " +
                "(id,dataVersion,displayName,legacyMetadataFingerprint,legacyCareerFingerprint,createdAtEpochMillis,updatedAtEpochMillis) " +
                "VALUES ('career-v18',1,'Tactics migration',NULL,NULL,10,10)"
        )
        db.execSQL(
            "INSERT INTO career_club_manager_runtime " +
                "(careerId,clubId,active,cash,primarySlotPlayerCode,secondarySlotPlayerCode,rawStateFlag," +
                "ticketIncome,playerSaleIncome,prizeIncome,sponsorIncome,playerPurchaseExpense,stadiumExpense," +
                "salaryExpense,borrowingChargeExpense,fineExpense,miscellaneousExpense,borrowed,monthlyBorrowingCharge) " +
                "VALUES ('career-v18','club-v18',1,123,NULL,NULL,0,0,0,0,0,0,0,0,0,0,0,0,0)"
        )
        db.close()

        db = helper.runMigrationsAndValidate(
            name,
            18,
            true,
            Phase17ClubTacticsPersistenceMigration.MIGRATION_17_18,
        )
        db.query(
            "SELECT legacyTacticOption0,legacyTacticOption1,legacyTacticOption2,legacyTacticOption3,legacyTacticCheckboxT " +
                "FROM career_club_manager_runtime WHERE careerId='career-v18' AND clubId='club-v18'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            for (index in 0..4) assertEquals(0, cursor.getInt(index))
        }
        db.execSQL(
            "UPDATE career_club_manager_runtime SET " +
                "legacyTacticOption0=5,legacyTacticOption1=2,legacyTacticOption2=9," +
                "legacyTacticOption3=-3,legacyTacticCheckboxT=1 " +
                "WHERE careerId='career-v18' AND clubId='club-v18'"
        )
        db.close()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val reopened = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        runBlocking {
            assertEquals(
                LegacyTacticsRawState(
                    optionSlots = listOf(5, 2, 9, -3),
                    checkboxT = true,
                ),
                CareerClubTacticsStore(reopened).load("career-v18", "club-v18"),
            )
        }
        reopened.close()
        context.deleteDatabase(name)
    }
}
