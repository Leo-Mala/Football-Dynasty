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
class Migration15To16Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FootballDynastyDatabase::class.java,
    )

    @Test
    fun `migration 15 to 16 creates empty proven aggregate and survives reopen`() {
        val name = "phase16-migration-15-16"
        var db = helper.createDatabase(name, 15)
        db.execSQL(
            "INSERT INTO career_metadata " +
                "(id,dataVersion,displayName,legacyMetadataFingerprint,legacyCareerFingerprint,createdAtEpochMillis,updatedAtEpochMillis) " +
                "VALUES ('career-v16',1,'Rating V16 probe',NULL,NULL,10,10)"
        )
        db.execSQL(
            "INSERT INTO career_competitions " +
                "(careerId,competitionId,legacyCompetitionType,legacyFormatCode,currentRoundNumber,totalRounds,legacyRelegationCount,legacyLeagueSubtype) " +
                "VALUES ('career-v16','league-v16',1,-1,1,1,NULL,NULL)"
        )
        db.close()

        db = helper.runMigrationsAndValidate(
            name,
            16,
            true,
            Phase16CompetitionPlayerRatingMigration.MIGRATION_15_16,
        )
        db.query(
            "SELECT COUNT(*) FROM career_competition_player_ratings " +
                "WHERE careerId='career-v16' AND competitionId='league-v16'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            // No V15 source exists for historical k0.g: migration must not invent a backfill row.
            assertEquals(0, cursor.getInt(0))
        }
        db.execSQL(
            "INSERT INTO career_competition_player_ratings " +
                "(careerId,competitionId,playerId,legacyRatingSum,legacyRatingCount,legacyAverageRating,legacyCategory) " +
                "VALUES ('career-v16','league-v16','p1',13.5,2.0,6.75,3)"
        )
        db.close()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val reopened = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        val aggregate = kotlinx.coroutines.runBlocking {
            reopened.careerCompetitionDao().findPlayerRating("career-v16", "league-v16", "p1")
        }
        requireNotNull(aggregate)
        assertEquals(13.5, aggregate.legacyRatingSum, 0.0)
        assertEquals(2.0, aggregate.legacyRatingCount, 0.0)
        assertEquals(6.75, aggregate.legacyAverageRating, 0.0)
        assertEquals(3, aggregate.legacyCategory)
        reopened.close()
        context.deleteDatabase(name)
    }
}
