package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.foundation.error.CareerIntegrityException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerEntryCommandStoreTest {
    @Test
    fun `create career atomically persists metadata and certified core state`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A)))

            val created = store(database).createCareer(
                careerId = CAREER_A,
                displayName = "Career A",
                seed = 1234L,
                managedClubId = CLUB_A,
            )

            val metadata = database.careerMetadataDao().findById(CAREER_A)
            val core = database.careerCoreStateDao().findById(CAREER_A)

            assertNotNull(metadata)
            assertEquals("Career A", metadata?.displayName)
            assertNotNull(core)
            assertEquals(CLUB_A, core?.managedClubId)
            assertEquals(CAREER_A, created.id)
            assertEquals(CLUB_A, created.managedClub?.clubId)
            assertEquals(1234L, created.random.initialSeed)
            assertEquals(0L, created.random.draws)
        } finally {
            database.close()
        }
    }

    @Test
    fun `unknown club fails closed without leaving metadata`() = runBlocking {
        val database = database()
        try {
            expectIntegrityFailure {
                store(database).createCareer(
                    careerId = CAREER_A,
                    displayName = null,
                    seed = 7L,
                    managedClubId = "missing-club",
                )
            }

            assertNull(database.careerMetadataDao().findById(CAREER_A))
            assertNull(database.careerCoreStateDao().findById(CAREER_A))
        } finally {
            database.close()
        }
    }

    @Test
    fun `duplicate career fails closed without mutating existing state`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A)))
            val commandStore = store(database)
            commandStore.createCareer(CAREER_A, "Original", 11L, CLUB_A)
            val before = database.careerCoreStateDao().findById(CAREER_A)

            expectIntegrityFailure {
                commandStore.createCareer(CAREER_A, "Replacement", 99L, CLUB_A)
            }

            assertEquals("Original", database.careerMetadataDao().findById(CAREER_A)?.displayName)
            assertEquals(before, database.careerCoreStateDao().findById(CAREER_A))
        } finally {
            database.close()
        }
    }

    private suspend fun expectIntegrityFailure(block: suspend () -> Unit) {
        try {
            block()
            fail("Expected CareerIntegrityException")
        } catch (_: CareerIntegrityException) {
            // Expected fail-closed path.
        }
    }

    private fun store(database: FootballDynastyDatabase) = CareerEntryCommandStore(
        database = database,
        clockMillis = { 100L },
    )

    private fun club(id: String) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = "teams/$id.ban",
        name = id,
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
        const val CLUB_A = "club-a"
    }
}
