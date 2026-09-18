package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.CareerCoreStateEntity
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerEntryCatalogStoreTest {
    @Test
    fun `career list preserves metadata order and exposes incomplete saves fail closed`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A, "teams/a.ban", "Club A")))
            database.careerMetadataDao().upsert(metadata(CAREER_A, "A", 10L))
            database.careerCoreStateDao().upsert(core(CAREER_A, CLUB_A, 10L))
            database.careerMetadataDao().upsert(metadata(CAREER_B, "B", 20L))

            val rows = store(database).listCareers()

            assertEquals(listOf(CAREER_B, CAREER_A), rows.map { it.careerId })
            assertFalse(rows[0].loadable)
            assertNull(rows[0].seasonYear)
            assertTrue(rows[1].loadable)
            assertEquals(2026, rows[1].seasonYear)
            assertEquals(CLUB_A, rows[1].managedClubId)
            assertEquals("Club A", rows[1].managedClubName)
        } finally {
            database.close()
        }
    }

    @Test
    fun `load career restores certified core state and rejects orphan core rows`() = runBlocking {
        val database = database()
        try {
            database.careerMetadataDao().upsert(metadata(CAREER_A, "A", 10L))
            database.careerCoreStateDao().upsert(core(CAREER_A, CLUB_A, 10L))

            val loaded = store(database).loadCareer(CAREER_A)

            assertEquals(CAREER_A, loaded?.id)
            assertEquals(CLUB_A, loaded?.managedClub?.clubId)
            assertEquals(3L, loaded?.random?.draws)
            assertNull(store(database).loadCareer("missing"))
        } finally {
            database.close()
        }
    }

    @Test
    fun `selectable clubs preserve canonical dao source ordering`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(
                listOf(
                    club("club-z", "teams/z.ban", "Z"),
                    club("club-a", "teams/a.ban", "A"),
                )
            )

            val clubs = store(database).selectableClubs()

            assertEquals(listOf("teams/a.ban", "teams/z.ban"), clubs.map { it.sourceFileRef })
            assertEquals(listOf("A", "Z"), clubs.map { it.name })
        } finally {
            database.close()
        }
    }

    private fun store(database: FootballDynastyDatabase) = CareerEntryCatalogStore(
        careerMetadataDao = database.careerMetadataDao(),
        careerCoreStateDao = database.careerCoreStateDao(),
        clubDao = database.clubDao(),
    )

    private fun metadata(id: String, name: String, updatedAt: Long) = CareerMetadataEntity(
        id = id,
        dataVersion = 1,
        displayName = name,
        legacyMetadataFingerprint = null,
        legacyCareerFingerprint = null,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = updatedAt,
    )

    private fun core(careerId: String, clubId: String?, updatedAt: Long) = CareerCoreStateEntity(
        careerId = careerId,
        stateVersion = 1,
        seasonNumber = 1,
        seasonYear = 2026,
        calendarYear = 2026,
        currentDayIndex = 2,
        startDayIndex = 1,
        dayCount = 365,
        rngInitialSeed = 7L,
        rngInternalState = 11L,
        rngDraws = 3L,
        managedClubId = clubId,
        transitionCount = 0L,
        updatedAtEpochMillis = updatedAt,
    )

    private fun club(id: String, sourceRef: String, name: String) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = sourceRef,
        name = name,
        country = 11,
        state = 0,
        level = 1,
        stadium = "",
        capacity = 0,
        reputation = 0,
        primaryColor = "",
        secondaryColor = "",
        coach = "",
        coachCountry = 0,
        baseColor = 0,
        legacyAid = 0,
        legacySid = 0,
        legacyTid = 0,
        legacyVid = 0,
        legacyId = 0,
        legacyValid = true,
    )

    private fun database(): FootballDynastyDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private companion object {
        const val CAREER_A = "career-a"
        const val CAREER_B = "career-b"
        const val CLUB_A = "club-a"
    }
}
