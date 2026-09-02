package com.leomala.footballdynasty.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.CareerStadiumConstructionEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class Migration9To10Test {
    @Test
    fun `migration to 10 adds nullable construction owner without inventing backfill`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase13-migration-9-10"
        context.deleteDatabase(name)

        // Materialize a structurally valid current database first. V10 changed only the
        // construction table, so the test then rewinds that single table to its certified V9 shape
        // and stamps the certified V9 Room identity before reopening through the real migration.
        val current = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .build()
        current.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Migration V10 probe",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
                createdAtEpochMillis = 10L,
                updatedAtEpochMillis = 10L,
            )
        )
        current.careerManagerRuntimeDao().upsertStadiumConstruction(
            CareerStadiumConstructionEntity(
                careerId = CAREER,
                sourceOrdinal = 0,
                stadiumCode = 77,
                endTimestampMillis = 20_000L,
                addition0 = 100,
                addition1 = 20,
                addition2 = 30,
                addition3 = 40,
                ownerClubId = null,
            )
        )
        current.close()

        val path = context.getDatabasePath(name).absolutePath
        val raw = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READWRITE)
        raw.execSQL("PRAGMA foreign_keys=OFF")
        raw.execSQL("ALTER TABLE `career_stadium_constructions` RENAME TO `career_stadium_constructions_v10`")
        raw.execSQL(
            """CREATE TABLE IF NOT EXISTS `career_stadium_constructions` (`careerId` TEXT NOT NULL, `sourceOrdinal` INTEGER NOT NULL, `stadiumCode` INTEGER NOT NULL, `endTimestampMillis` INTEGER NOT NULL, `addition0` INTEGER NOT NULL, `addition1` INTEGER NOT NULL, `addition2` INTEGER NOT NULL, `addition3` INTEGER NOT NULL, PRIMARY KEY(`careerId`, `sourceOrdinal`), FOREIGN KEY(`careerId`) REFERENCES `career_metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"""
        )
        raw.execSQL(
            """INSERT INTO `career_stadium_constructions` (`careerId`,`sourceOrdinal`,`stadiumCode`,`endTimestampMillis`,`addition0`,`addition1`,`addition2`,`addition3`) SELECT `careerId`,`sourceOrdinal`,`stadiumCode`,`endTimestampMillis`,`addition0`,`addition1`,`addition2`,`addition3` FROM `career_stadium_constructions_v10`"""
        )
        raw.execSQL("DROP TABLE `career_stadium_constructions_v10`")
        raw.execSQL("PRAGMA user_version=9")
        raw.execSQL(
            "UPDATE room_master_table SET identity_hash=? WHERE id=42",
            arrayOf(V9_IDENTITY),
        )
        raw.close()

        val migrated = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(Phase13StadiumConstructionOwnershipMigration.MIGRATION_9_10)
            .build()
        val row = migrated.careerManagerRuntimeDao().stadiumConstructions(CAREER).single()
        assertEquals(77, row.stadiumCode)
        assertEquals(listOf(100, 20, 30, 40), listOf(row.addition0, row.addition1, row.addition2, row.addition3))
        assertNull(row.ownerClubId)
        assertEquals("Migration V10 probe", migrated.careerMetadataDao().findById(CAREER)?.displayName)
        migrated.close()
        context.deleteDatabase(name)
        Unit
    }

    private companion object {
        const val CAREER = "career-v10"
        const val V9_IDENTITY = "e246b6749364b3d2f7891177c2179fb4"
    }
}
