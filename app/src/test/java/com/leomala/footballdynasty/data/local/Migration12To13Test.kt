package com.leomala.footballdynasty.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class Migration12To13Test {
    @Test
    fun `migration to 13 preserves V12 identity and does not invent x0`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase14-migration-12-13"
        context.deleteDatabase(name)

        val current = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .build()
        current.careerMetadataDao().upsert(
            CareerMetadataEntity(CAREER, 1, "Migration V13 probe", null, null, 10L, 10L)
        )
        current.careerCompetitionDao().upsertCompetition(
            com.leomala.footballdynasty.data.local.entity.CareerCompetitionEntity(
                careerId = CAREER,
                competitionId = "league-1",
                legacyCompetitionType = 1,
                legacyFormatCode = 73,
                currentRoundNumber = 2,
                totalRounds = 12,
                legacyRelegationCount = 3,
                legacyLeagueSubtype = null,
            )
        )
        current.close()

        val path = context.getDatabasePath(name).absolutePath
        val raw = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)
        raw.execSQL("PRAGMA foreign_keys=OFF")
        raw.execSQL("ALTER TABLE `career_competitions` DROP COLUMN `legacyLeagueSubtype`")
        raw.execSQL("PRAGMA user_version=12")
        raw.execSQL(
            "UPDATE room_master_table SET identity_hash=? WHERE id=42",
            arrayOf(V12_IDENTITY),
        )
        raw.close()

        val migrated = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(Phase14CompetitionInputsMigration.MIGRATION_12_13)
            .build()
        val row = requireNotNull(migrated.careerCompetitionDao().findCompetition(CAREER, "league-1"))
        assertEquals(1, row.legacyCompetitionType)
        assertEquals(73, row.legacyFormatCode)
        assertEquals(2, row.currentRoundNumber)
        assertEquals(12, row.totalRounds)
        assertEquals(3, row.legacyRelegationCount)
        assertNull(row.legacyLeagueSubtype)
        assertEquals("Migration V13 probe", migrated.careerMetadataDao().findById(CAREER)?.displayName)

        migrated.close()
        context.deleteDatabase(name)
        Unit
    }

    private companion object {
        const val CAREER = "career-v13"
        const val V12_IDENTITY = "6b87dfc792ccf982f9e1a83cfcc8e8b6"
    }
}
