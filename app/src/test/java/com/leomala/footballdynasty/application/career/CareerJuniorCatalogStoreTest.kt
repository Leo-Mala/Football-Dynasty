package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.CareerCoreStateRoomAdapter
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.CareerJuniorDraftEntity
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.career.CareerRandomState
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.LegacyCalendarRules
import com.leomala.footballdynasty.domain.career.SeasonState
import com.leomala.footballdynasty.foundation.random.StatefulJavaRandomSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerJuniorCatalogStoreTest {
    @Test
    fun `junior squad follows persisted order and managed career club`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A), club(CLUB_B)))
            CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
                careerId = CAREER_A,
                displayName = "Career A",
                seed = 7L,
                managedClubId = CLUB_A,
            )
            CareerEntryCommandStore(database, clockMillis = { 101L }).createCareer(
                careerId = CAREER_B,
                displayName = "Career B",
                seed = 9L,
                managedClubId = CLUB_B,
            )
            database.careerJuniorDraftDao().upsertAll(
                listOf(
                    draft(CAREER_A, CLUB_A, 1, "Junior A2", age = 18, position = 3),
                    draft(CAREER_A, CLUB_A, 0, "Junior A1", age = 17, position = 1),
                    draft(CAREER_A, CLUB_B, 0, "Wrong club", age = 19, position = 4),
                    draft(CAREER_B, CLUB_B, 0, "Other career", age = 16, position = 2),
                )
            )

            val squad = CareerJuniorCatalogStore(database).loadJuniorSquad(CAREER_A)

            assertEquals(CAREER_A, squad?.careerId)
            assertEquals(CLUB_A, squad?.clubId)
            assertEquals(listOf("Junior A1", "Junior A2"), squad?.juniors?.map { it.name })
            assertEquals(listOf(0, 1), squad?.juniors?.map { it.sourceOrdinal })
            assertEquals(listOf(17, 18), squad?.juniors?.map { it.age })
            assertEquals(listOf(1, 3), squad?.juniors?.map { it.position })
        } finally {
            database.close()
        }
    }

    @Test
    fun `valid managed club with no drafts returns empty persisted junior squad`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A)))
            CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
                careerId = CAREER_A,
                displayName = "Career A",
                seed = 7L,
                managedClubId = CLUB_A,
            )

            val squad = CareerJuniorCatalogStore(database).loadJuniorSquad(CAREER_A)

            assertNotNull(squad)
            assertEquals(CLUB_A, squad?.clubId)
            assertEquals(emptyList<CareerJuniorCatalogStore.JuniorRow>(), squad?.juniors)
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing core state fails closed instead of synthesizing juniors`() = runBlocking {
        val database = database()
        try {
            database.careerMetadataDao().upsert(metadata(CAREER_A))

            assertNull(CareerJuniorCatalogStore(database).loadJuniorSquad(CAREER_A))
            assertNull(CareerJuniorCatalogStore(database).loadJuniorSquad("missing"))
        } finally {
            database.close()
        }
    }

    @Test
    fun `career without managed club fails closed`() = runBlocking {
        val database = database()
        try {
            database.careerMetadataDao().upsert(metadata(CAREER_A))
            database.careerCoreStateDao().upsert(
                CareerCoreStateRoomAdapter.entity(careerWithoutManagedClub(), 100L)
            )

            assertNull(CareerJuniorCatalogStore(database).loadJuniorSquad(CAREER_A))
        } finally {
            database.close()
        }
    }

    private fun careerWithoutManagedClub(): CareerState {
        val snapshot = StatefulJavaRandomSource(7L).snapshot()
        val seasonNumber = 1
        return CareerState(
            id = CAREER_A,
            season = SeasonState(seasonNumber, LegacyCalendarRules.seasonYear(seasonNumber)),
            calendar = LegacyCalendarRules.calendarForSeason(seasonNumber),
            managedClub = null,
            random = CareerRandomState(
                initialSeed = snapshot.initialSeed,
                internalState = snapshot.internalState,
                draws = snapshot.draws,
            ),
        )
    }

    private fun metadata(careerId: String) = CareerMetadataEntity(
        id = careerId,
        dataVersion = 1,
        displayName = "Incomplete",
        legacyMetadataFingerprint = null,
        legacyCareerFingerprint = null,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L,
    )

    private fun draft(
        careerId: String,
        clubId: String,
        sourceOrdinal: Int,
        name: String,
        age: Int,
        position: Int,
    ) = CareerJuniorDraftEntity(
        careerId = careerId,
        clubId = clubId,
        sourceOrdinal = sourceOrdinal,
        legacyN = 1,
        legacyB = false,
        legacyC = age,
        legacyE = position,
        legacyJ = 0,
        legacyL = 0,
        legacyD = 0,
        name = name,
        legacyG = 0,
        legacyF = 1,
        legacyO = 50,
        legacyM = 0,
        legacyH = 500,
        legacyI = 500,
        developmentRemainder = 0.0,
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
        const val CAREER_B = "career-b"
        const val CLUB_A = "club-a"
        const val CLUB_B = "club-b"
    }
}
