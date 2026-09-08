package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.CareerStadiumRuntimeState
import com.leomala.footballdynasty.data.local.CareerStadiumRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerStadiumCatalogStoreTest {
    @Test
    fun `stadium projection uses persisted four-sector runtime for managed club`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(
                listOf(
                    club(CLUB_A, "Club A", "Arena A", 50_000),
                    club(CLUB_B, "Club B", "Arena B", 60_000),
                )
            )
            CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
                careerId = CAREER_A,
                displayName = "Career A",
                seed = 7L,
                managedClubId = CLUB_A,
            )
            CareerStadiumRuntimeStore(database).materialize(
                careerId = CAREER_A,
                clubId = CLUB_A,
                state = CareerStadiumRuntimeState(listOf(1_500, 7_410, 900, 90)),
            )

            val stadium = CareerStadiumCatalogStore(database).loadStadium(CAREER_A)

            requireNotNull(stadium)
            assertEquals(CAREER_A, stadium.careerId)
            assertEquals(CLUB_A, stadium.clubId)
            assertEquals("Club A", stadium.clubName)
            assertEquals("Arena A", stadium.stadiumName)
            assertEquals(listOf(1_500, 7_410, 900, 90), stadium.sectorCapacities)
            assertEquals(9_900L, stadium.totalCapacity)
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing materialized runtime fails closed instead of rebuilding aggregate capacity`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A, "Club A", "Arena A", 50_000)))
            CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
                careerId = CAREER_A,
                displayName = "Career A",
                seed = 7L,
                managedClubId = CLUB_A,
            )

            assertNull(CareerStadiumCatalogStore(database).loadStadium(CAREER_A))
            assertNull(CareerStadiumCatalogStore(database).loadStadium("missing"))
        } finally {
            database.close()
        }
    }

    private fun club(id: String, name: String, stadium: String, capacity: Int) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = "teams/$id.ban",
        name = name,
        country = 11,
        state = 0,
        level = 1,
        stadium = stadium,
        capacity = capacity,
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
        const val CLUB_B = "club-b"
    }
}
