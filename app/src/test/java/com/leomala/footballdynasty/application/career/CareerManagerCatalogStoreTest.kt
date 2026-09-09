package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.CareerClubTicketRuntimeState
import com.leomala.footballdynasty.data.local.CareerCoachRuntimeState
import com.leomala.footballdynasty.data.local.CareerCoachRuntimeStore
import com.leomala.footballdynasty.data.local.CareerManagerTicketRuntimeState
import com.leomala.footballdynasty.data.local.CareerTicketRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.manager.LegacyManagerIdentityRule
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
class CareerManagerCatalogStoreTest {
    @Test
    fun `manager projection preserves persisted opaque runtime for managed club`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A, "Club A")))
            createCareer(database)
            materializeManager(database, currentClubId = CLUB_A, userControlled = true)

            val snapshot = CareerManagerCatalogStore(database).loadManager(CAREER_A)

            requireNotNull(snapshot)
            assertEquals(CAREER_A, snapshot.careerId)
            assertEquals(CLUB_A, snapshot.clubId)
            assertEquals("Club A", snapshot.clubName)
            assertEquals(0, snapshot.sourceOrdinal)
            assertEquals(MANAGER_ID, snapshot.legacyManagerId)
            assertTrue(snapshot.isUserControlled)
            assertEquals(91, snapshot.rawG)
            assertEquals(79, snapshot.rawH)
            assertEquals(12, snapshot.rawD)
            assertEquals(13, snapshot.rawE)
            assertEquals(14, snapshot.rawF)
            assertEquals(15, snapshot.rawO)
            assertEquals(16, snapshot.rawM)
            assertTrue(snapshot.records.isEmpty())
        } finally {
            database.close()
        }
    }

    @Test
    fun `absent persisted club manager fails closed without synthesizing one`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A, "Club A")))
            createCareer(database)
            CareerTicketRuntimeStore(database).materializeClubState(
                careerId = CAREER_A,
                clubId = CLUB_A,
                state = CareerClubTicketRuntimeState(
                    rawDivisionCode = 3,
                    legacyManagerId = LegacyManagerIdentityRule.clubStoredManagerId(null),
                ),
            )

            assertNull(CareerManagerCatalogStore(database).loadManager(CAREER_A))
        } finally {
            database.close()
        }
    }

    @Test
    fun `stale employment link is not repaired by presentation projection`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A, "Club A")))
            createCareer(database)
            materializeManager(database, currentClubId = "another-club", userControlled = false)

            val snapshot = CareerManagerCatalogStore(database).loadManager(CAREER_A)

            assertNull(snapshot)
        } finally {
            database.close()
        }
    }

    @Test
    fun `unknown career or missing ticket slice remains unavailable`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A, "Club A")))
            createCareer(database)

            assertNull(CareerManagerCatalogStore(database).loadManager(CAREER_A))
            assertNull(CareerManagerCatalogStore(database).loadManager("missing"))
        } finally {
            database.close()
        }
    }

    private suspend fun materializeManager(
        database: FootballDynastyDatabase,
        currentClubId: String,
        userControlled: Boolean,
    ) {
        val ticketStore = CareerTicketRuntimeStore(database)
        ticketStore.materializeClubState(
            careerId = CAREER_A,
            clubId = CLUB_A,
            state = CareerClubTicketRuntimeState(rawDivisionCode = 3, legacyManagerId = MANAGER_ID),
        )
        ticketStore.materializeManagers(
            careerId = CAREER_A,
            managersInWorldOrder = listOf(
                CareerManagerTicketRuntimeState(
                    sourceOrdinal = 0,
                    legacyManagerId = MANAGER_ID,
                    rawH = 79,
                )
            ),
        )
        CareerCoachRuntimeStore(database).materialize(
            careerId = CAREER_A,
            state = CareerCoachRuntimeState(
                sourceOrdinal = 0,
                legacyManagerId = MANAGER_ID,
                isUserControlled = userControlled,
                currentClubId = currentClubId,
                alternativeClubId = null,
                previousClubId = null,
                previousClubCountry = null,
                previousClubDivisionIndex = null,
                rawG = 91,
                rawH = 79,
                rawD = 12,
                rawE = 13,
                rawF = 14,
                rawO = 15,
                rawM = 16,
                records = emptyList(),
            ),
        )
    }

    private suspend fun createCareer(database: FootballDynastyDatabase) {
        CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
            careerId = CAREER_A,
            displayName = "Career A",
            seed = 7L,
            managedClubId = CLUB_A,
        )
    }

    private fun club(id: String, name: String) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = "teams/$id.ban",
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
        const val CLUB_A = "club-a"
        const val MANAGER_ID = 41
    }
}
