package com.leomala.footballdynasty.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionEntity
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class Migration13To14Test {
    @Test
    fun `migration to 14 preserves V13 data and does not invent junior drafts`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase15-migration-13-14"
        context.deleteDatabase(name)

        val current = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .build()
        current.careerMetadataDao().upsert(
            CareerMetadataEntity(CAREER, 1, "Migration V14 probe", null, null, 10L, 10L)
        )
        current.careerCompetitionDao().upsertCompetition(
            CareerCompetitionEntity(
                careerId = CAREER,
                competitionId = "league-1",
                legacyCompetitionType = 1,
                legacyFormatCode = 73,
                currentRoundNumber = 2,
                totalRounds = 12,
                legacyRelegationCount = 3,
                legacyLeagueSubtype = 5,
            )
        )
        current.close()

        val path = context.getDatabasePath(name).absolutePath
        val raw = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)
        raw.execSQL("PRAGMA foreign_keys=OFF")
        raw.execSQL("DROP TABLE `career_junior_drafts`")
        raw.execSQL("PRAGMA user_version=13")
        raw.execSQL(
            "UPDATE room_master_table SET identity_hash=? WHERE id=42",
            arrayOf(V13_IDENTITY),
        )
        raw.close()

        val migrated = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(Phase15JuniorDraftMigration.MIGRATION_13_14)
            .build()

        assertEquals("Migration V14 probe", migrated.careerMetadataDao().findById(CAREER)?.displayName)
        val competition = requireNotNull(migrated.careerCompetitionDao().findCompetition(CAREER, "league-1"))
        assertEquals(1, competition.legacyCompetitionType)
        assertEquals(73, competition.legacyFormatCode)
        assertEquals(2, competition.currentRoundNumber)
        assertEquals(12, competition.totalRounds)
        assertEquals(3, competition.legacyRelegationCount)
        assertEquals(5, competition.legacyLeagueSubtype)

        val drafts = migrated.careerJuniorDraftDao().listForClub(CAREER, "any-club")
        assertEquals(0, drafts.size)
        assertEquals(0, migrated.careerJuniorDraftDao().countForClub(CAREER, "any-club"))

        migrated.close()
        context.deleteDatabase(name)
        Unit
    }

    private companion object {
        const val CAREER = "career-v14"
        const val V13_IDENTITY = "6575d77e3eef9ea84d059c2aa2bdf14b"
    }
}
