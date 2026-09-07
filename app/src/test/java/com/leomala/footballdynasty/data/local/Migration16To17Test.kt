package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
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
class Migration16To17Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FootballDynastyDatabase::class.java,
    )

    @Test
    fun `migration 16 to 17 keeps unproved scalars null creates empty histories and survives reopen`() {
        val name = "phase17-migration-16-17"
        var db = helper.createDatabase(name, 16)
        db.execSQL(
            "INSERT INTO career_metadata " +
                "(id,dataVersion,displayName,legacyMetadataFingerprint,legacyCareerFingerprint,createdAtEpochMillis,updatedAtEpochMillis) " +
                "VALUES ('career-v17',1,'Durability V17 probe',NULL,NULL,10,10)"
        )
        db.execSQL(
            "INSERT INTO career_competitions " +
                "(careerId,competitionId,legacyCompetitionType,legacyFormatCode,currentRoundNumber,totalRounds,legacyRelegationCount,legacyLeagueSubtype) " +
                "VALUES ('career-v17','league-v17',1,-1,1,1,NULL,NULL)"
        )
        db.execSQL(
            "INSERT INTO career_competition_player_ratings " +
                "(careerId,competitionId,playerId,legacyRatingSum,legacyRatingCount,legacyAverageRating,legacyCategory) " +
                "VALUES ('career-v17','league-v17','p1',7.5,1.0,7.5,3)"
        )
        db.close()

        db = helper.runMigrationsAndValidate(
            name,
            17,
            true,
            Phase17LegacyDurabilityMigration.MIGRATION_16_17,
        )
        db.query(
            "SELECT legacyCompetitionIndex, legacyGroupCountA0 FROM career_competitions " +
                "WHERE careerId='career-v17' AND competitionId='league-v17'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.isNull(0))
            assertTrue(cursor.isNull(1))
        }
        db.query(
            "SELECT legacyStableOrdinal FROM career_competition_player_ratings " +
                "WHERE careerId='career-v17' AND competitionId='league-v17' AND playerId='p1'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.isNull(0))
        }
        for (table in listOf(
            "career_competition_snapshots",
            "career_competition_snapshot_members",
            "career_player_match_rating_history",
        )) {
            db.query("SELECT COUNT(*) FROM $table").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
            }
        }
        db.execSQL(
            "INSERT INTO career_competition_snapshots " +
                "(careerId,competitionId,snapshotKind,snapshotOrdinal,legacyA,legacyB,topPlayerId) " +
                "VALUES ('career-v17','league-v17','ANNUAL',0,1,0,'p1')"
        )
        db.execSQL(
            "INSERT INTO career_competition_snapshot_members " +
                "(careerId,competitionId,snapshotKind,snapshotOrdinal,memberOrdinal,playerId,clubIdAtSnapshot) " +
                "VALUES ('career-v17','league-v17','ANNUAL',0,0,'p1','club-historical')"
        )
        db.close()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val reopened = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        runBlocking {
            val competition = requireNotNull(
                reopened.careerCompetitionDao().findCompetition("career-v17", "league-v17")
            )
            assertNull(competition.legacyCompetitionIndex)
            assertNull(competition.legacyGroupCountA0)
            val rating = requireNotNull(
                reopened.careerCompetitionDao().findPlayerRating("career-v17", "league-v17", "p1")
            )
            assertNull(rating.legacyStableOrdinal)
            val snapshots = reopened.careerLegacyDurabilityDao().snapshots(
                "career-v17", "league-v17", "ANNUAL"
            )
            assertEquals(1, snapshots.size)
            assertEquals("p1", snapshots.single().topPlayerId)
            val members = reopened.careerLegacyDurabilityDao().snapshotMembers(
                "career-v17", "league-v17", "ANNUAL", 0
            )
            assertEquals(listOf("p1"), members.map { it.playerId })
            assertEquals(listOf("club-historical"), members.map { it.clubIdAtSnapshot })
        }
        reopened.close()
        context.deleteDatabase(name)
    }
}
