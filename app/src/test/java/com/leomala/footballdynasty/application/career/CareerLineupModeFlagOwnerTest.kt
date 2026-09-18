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
import com.leomala.footballdynasty.data.local.entity.CareerScheduledMatchEntity
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
class CareerLineupModeFlagOwnerTest {
    @Test
    fun `missing V9 club manager state keeps ActivityMainTeam D unresolved`() = runBlocking {
        val database = database()
        try {
            seedPreparedMatch(database)

            val preparation = loadPreparation(database)

            assertNull(preparation.lineupModeFlag)
            assertTrue(
                CareerLineupInputCatalogStore.MatchPreparationBlocker.LINEUP_MODE_FLAG_OWNER_UNRESOLVED in
                    preparation.blockers
            )
            assertFalse(preparation.executable)
        } finally {
            database.close()
        }
    }

    @Test
    fun `legacy absent manager id resolves ActivityMainTeam D false without fabrication`() = runBlocking {
        val database = database()
        try {
            seedPreparedMatch(database)
            CareerTicketRuntimeStore(database).materializeClubState(
                careerId = CAREER,
                clubId = CLUB_A,
                state = CareerClubTicketRuntimeState(
                    rawDivisionCode = 0,
                    legacyManagerId = LegacyManagerIdentityRule.clubStoredManagerId(null),
                ),
            )

            val preparation = loadPreparation(database)

            assertEquals(false, preparation.lineupModeFlag)
            assertFalse(
                CareerLineupInputCatalogStore.MatchPreparationBlocker.LINEUP_MODE_FLAG_OWNER_UNRESOLVED in
                    preparation.blockers
            )
        } finally {
            database.close()
        }
    }

    @Test
    fun `resolved first manager pointing to managed club resolves ActivityMainTeam D true`() = runBlocking {
        val database = database()
        try {
            seedPreparedMatch(database)
            val ticketStore = CareerTicketRuntimeStore(database)
            ticketStore.materializeManagers(
                careerId = CAREER,
                managersInWorldOrder = listOf(
                    CareerManagerTicketRuntimeState(
                        sourceOrdinal = 0,
                        legacyManagerId = MANAGER_ID,
                        rawH = 80,
                    )
                ),
            )
            ticketStore.materializeClubState(
                careerId = CAREER,
                clubId = CLUB_A,
                state = CareerClubTicketRuntimeState(
                    rawDivisionCode = 0,
                    legacyManagerId = MANAGER_ID,
                ),
            )
            CareerCoachRuntimeStore(database).materialize(
                careerId = CAREER,
                state = CareerCoachRuntimeState(
                    sourceOrdinal = 0,
                    legacyManagerId = MANAGER_ID,
                    isUserControlled = true,
                    currentClubId = CLUB_A,
                    alternativeClubId = null,
                    previousClubId = null,
                    previousClubCountry = null,
                    previousClubDivisionIndex = null,
                    rawG = 100,
                    rawH = 80,
                    rawD = 0,
                    rawE = 0,
                    rawF = 0,
                    rawO = 0,
                    rawM = 0,
                    records = emptyList(),
                ),
            )

            val preparation = loadPreparation(database)

            assertEquals(true, preparation.lineupModeFlag)
            assertFalse(
                CareerLineupInputCatalogStore.MatchPreparationBlocker.LINEUP_MODE_FLAG_OWNER_UNRESOLVED in
                    preparation.blockers
            )
        } finally {
            database.close()
        }
    }

    @Test
    fun `resolved manager pointing elsewhere resolves ActivityMainTeam D false`() = runBlocking {
        val database = database()
        try {
            seedPreparedMatch(database)
            val ticketStore = CareerTicketRuntimeStore(database)
            ticketStore.materializeManagers(
                careerId = CAREER,
                managersInWorldOrder = listOf(
                    CareerManagerTicketRuntimeState(
                        sourceOrdinal = 0,
                        legacyManagerId = MANAGER_ID,
                        rawH = 80,
                    )
                ),
            )
            ticketStore.materializeClubState(
                careerId = CAREER,
                clubId = CLUB_A,
                state = CareerClubTicketRuntimeState(
                    rawDivisionCode = 0,
                    legacyManagerId = MANAGER_ID,
                ),
            )
            CareerCoachRuntimeStore(database).materialize(
                careerId = CAREER,
                state = CareerCoachRuntimeState(
                    sourceOrdinal = 0,
                    legacyManagerId = MANAGER_ID,
                    isUserControlled = true,
                    currentClubId = CLUB_B,
                    alternativeClubId = null,
                    previousClubId = null,
                    previousClubCountry = null,
                    previousClubDivisionIndex = null,
                    rawG = 100,
                    rawH = 80,
                    rawD = 0,
                    rawE = 0,
                    rawF = 0,
                    rawO = 0,
                    rawM = 0,
                    records = emptyList(),
                ),
            )

            val preparation = loadPreparation(database)

            assertEquals(false, preparation.lineupModeFlag)
            assertFalse(
                CareerLineupInputCatalogStore.MatchPreparationBlocker.LINEUP_MODE_FLAG_OWNER_UNRESOLVED in
                    preparation.blockers
            )
        } finally {
            database.close()
        }
    }

    private suspend fun seedPreparedMatch(database: FootballDynastyDatabase) {
        database.clubDao().upsertAll(listOf(club(CLUB_A), club(CLUB_B)))
        CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
            careerId = CAREER,
            displayName = "Lineup mode owner",
            seed = 17L,
            managedClubId = CLUB_A,
        )
        val currentDay = requireNotNull(database.careerCoreStateDao().findById(CAREER)).currentDayIndex
        database.careerScheduledMatchDao().upsert(
            CareerScheduledMatchEntity(
                careerId = CAREER,
                matchId = MATCH,
                dayIndex = currentDay,
                eventTypeCode = 1,
                homeClubId = CLUB_A,
                awayClubId = CLUB_B,
                processed = false,
                homeGoals = null,
                awayGoals = null,
                legacyDayMatchOrdinal = 0,
            )
        )
    }

    private suspend fun loadPreparation(
        database: FootballDynastyDatabase,
    ): CareerLineupInputCatalogStore.MatchPreparation = requireNotNull(
        CareerLineupInputCatalogStore(database).loadManagedClubLineupInputs(CAREER)
    ).matchPreparation

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
        const val CAREER = "career-lineup-mode"
        const val CLUB_A = "club-a"
        const val CLUB_B = "club-b"
        const val MATCH = "match-a"
        const val MANAGER_ID = 73
    }
}
