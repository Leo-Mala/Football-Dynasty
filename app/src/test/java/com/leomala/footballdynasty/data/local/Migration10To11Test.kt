package com.leomala.footballdynasty.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class Migration10To11Test {
    @Test fun `migration to 11 preserves V10 H identity and creates no invented coach state`() = runBlocking {
        val context=ApplicationProvider.getApplicationContext<Context>(); val name="phase14-migration-10-11"; context.deleteDatabase(name)
        val current=Room.databaseBuilder(context,FootballDynastyDatabase::class.java,name).allowMainThreadQueries().build()
        current.careerMetadataDao().upsert(CareerMetadataEntity(CAREER,1,"Migration V11 probe",null,null,10L,10L))
        CareerTicketRuntimeStore(current).materializeManagers(CAREER,listOf(CareerManagerTicketRuntimeState(0,7,61))); current.close()
        val path=context.getDatabasePath(name).absolutePath; val raw=SQLiteDatabase.openDatabase(path,null,SQLiteDatabase.OPEN_READWRITE)
        raw.execSQL("PRAGMA foreign_keys=OFF")
        raw.execSQL("ALTER TABLE `career_competitions` DROP COLUMN `legacyLeagueSubtype`")
        raw.execSQL("ALTER TABLE `career_competitions` DROP COLUMN `legacyRelegationCount`")
        raw.execSQL("DROP TABLE `career_coach_season_club_records`")
        raw.execSQL("DROP TABLE `career_coach_runtime`")
        raw.execSQL("PRAGMA user_version=10")
        raw.execSQL("UPDATE room_master_table SET identity_hash=? WHERE id=42",arrayOf(V10_IDENTITY)); raw.close()
        val migrated=Room.databaseBuilder(context,FootballDynastyDatabase::class.java,name).allowMainThreadQueries().addMigrations(
            Phase14CoachRuntimeMigration.MIGRATION_10_11,
            Phase14CompetitionInputsMigration.MIGRATION_11_12,
            Phase14CompetitionInputsMigration.MIGRATION_12_13,
        ).build()
        val ticket=CareerTicketRuntimeStore(migrated); val coach=CareerCoachRuntimeStore(migrated)
        assertEquals(61,ticket.resolveCoachRawH(CAREER,7)); assertNull(coach.find(CAREER,0)); assertNull(migrated.careerCoachRuntimeDao().findCoachRuntime(CAREER,0)); assertTrue(migrated.careerCoachRuntimeDao().seasonClubRecords(CAREER,0).isEmpty()); assertEquals("Migration V11 probe",migrated.careerMetadataDao().findById(CAREER)?.displayName)
        migrated.close(); context.deleteDatabase(name); Unit
    }
    private companion object { const val CAREER="career-v11"; const val V10_IDENTITY="086ae14fb0b6a625e6f22446d47df0b7" }
}
