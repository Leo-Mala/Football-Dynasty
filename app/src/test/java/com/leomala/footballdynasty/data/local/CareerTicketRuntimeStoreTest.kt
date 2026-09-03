package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.CareerScheduledMatchEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.manager.LegacyMatchConstructionSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerTicketRuntimeStoreTest {
    @Test
    fun `raw division duplicate manager identity and construction source survive database reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase13-ticket-v9-reopen"
        context.deleteDatabase(name)
        var database = database(context, name)
        seed(database)
        var store = CareerTicketRuntimeStore(database)

        store.materializeClubState(CAREER, HOME, CareerClubTicketRuntimeState(-2, 7))
        store.materializeManagers(
            CAREER,
            listOf(
                CareerManagerTicketRuntimeState(0, 7, 61),
                CareerManagerTicketRuntimeState(1, 7, 99),
            ),
        )
        store.materializeMatchConstructionSource(
            CAREER,
            MATCH,
            LegacyMatchConstructionSource.KNOCKOUT_F0,
        )

        assertEquals(-2, requireNotNull(store.findClubState(CAREER, HOME)).rawDivisionCode)
        assertEquals(61, store.resolveCoachRawH(CAREER, 7))
        assertEquals(
            LegacyMatchConstructionSource.KNOCKOUT_F0,
            store.findMatchConstructionSource(CAREER, MATCH),
        )

        database.close()
        database = database(context, name)
        store = CareerTicketRuntimeStore(database)
        assertEquals(CareerClubTicketRuntimeState(-2, 7), store.findClubState(CAREER, HOME))
        assertEquals(listOf(61, 99), store.managersInWorldOrder(CAREER).map { it.rawH })
        assertEquals(61, store.resolveCoachRawH(CAREER, 7))
        assertEquals(
            LegacyMatchConstructionSource.KNOCKOUT_F0,
            store.findMatchConstructionSource(CAREER, MATCH),
        )

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `employment annual recovery and main team floor mutate only first duplicate manager and survive reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase14-coach-h-v9-reopen"
        context.deleteDatabase(name)
        var database = database(context, name)
        seed(database)
        var store = CareerTicketRuntimeStore(database)
        store.materializeManagers(
            CAREER,
            listOf(
                CareerManagerTicketRuntimeState(0, 7, 20),
                CareerManagerTicketRuntimeState(1, 7, 91),
                CareerManagerTicketRuntimeState(2, 8, 70),
            ),
        )

        assertEquals(30, store.applyCoachMainTeamRefresh(CAREER, 7, legacyFloorEnabled = true))
        assertEquals(listOf(30, 91, 70), store.managersInWorldOrder(CAREER).map { it.rawH })
        assertEquals(80, store.applyCoachEmployment(CAREER, 7))
        assertEquals(listOf(80, 91, 70), store.managersInWorldOrder(CAREER).map { it.rawH })
        assertEquals(100, store.applyCoachAnnualRecovery(CAREER, 7))
        assertEquals(listOf(100, 91, 70), store.managersInWorldOrder(CAREER).map { it.rawH })
        assertEquals(100, store.applyCoachAnnualRecovery(CAREER, 8))

        database.close()
        database = database(context, name)
        store = CareerTicketRuntimeStore(database)
        assertEquals(listOf(100, 91, 100), store.managersInWorldOrder(CAREER).map { it.rawH })
        assertEquals(100, store.resolveCoachRawH(CAREER, 7))
        assertEquals(100, store.resolveCoachRawH(CAREER, 8))

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `coach H mutations are fail closed and disabled floor is exact no op`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        seed(database)
        val store = CareerTicketRuntimeStore(database)
        store.materializeManagers(CAREER, listOf(CareerManagerTicketRuntimeState(0, 7, 12)))

        assertEquals(12, store.applyCoachMainTeamRefresh(CAREER, 7, legacyFloorEnabled = false))
        assertEquals(12, store.resolveCoachRawH(CAREER, 7))
        assertNotNull(runCatching { store.applyCoachEmployment(CAREER, 77) }.exceptionOrNull())
        assertNotNull(runCatching { store.applyCoachEmployment(CAREER, -1) }.exceptionOrNull())
        assertNotNull(runCatching { store.applyCoachAnnualRecovery(CAREER, 77) }.exceptionOrNull())
        assertNotNull(runCatching { store.applyCoachAnnualRecovery(CAREER, -1) }.exceptionOrNull())

        database.close()
        Unit
    }

    @Test
    fun `non absent dangling manager id fails closed and invalid manager order is rejected`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        seed(database)
        val store = CareerTicketRuntimeStore(database)

        store.materializeManagers(CAREER, emptyList())
        assertNotNull(runCatching { store.resolveCoachRawH(CAREER, 77) }.exceptionOrNull())
        assertNotNull(
            runCatching {
                store.materializeManagers(CAREER, listOf(CareerManagerTicketRuntimeState(1, 7, 80)))
            }.exceptionOrNull()
        )
        database.close()
        Unit
    }

    private fun database(context: Context, name: String): FootballDynastyDatabase =
        Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()

    private suspend fun seed(database: FootballDynastyDatabase) {
        database.clubDao().upsertAll(listOf(club(HOME, 101), club(AWAY, 202)))
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Ticket V9",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            )
        )
        database.careerScheduledMatchDao().upsert(
            CareerScheduledMatchEntity(
                careerId = CAREER,
                matchId = MATCH,
                dayIndex = 0,
                eventTypeCode = 1,
                homeClubId = HOME,
                awayClubId = AWAY,
                processed = false,
                homeGoals = null,
                awayGoals = null,
            )
        )
    }

    private fun club(id: String, legacyId: Int) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = id,
        name = id,
        country = 0,
        state = 0,
        level = 1,
        stadium = "",
        capacity = 10_000,
        reputation = 3,
        primaryColor = "",
        secondaryColor = "",
        coach = "",
        coachCountry = 0,
        baseColor = 0,
        legacyAid = 0,
        legacySid = 0,
        legacyTid = 0,
        legacyVid = 0,
        legacyId = legacyId,
        legacyValid = true,
    )

    private companion object {
        const val CAREER = "career-ticket-v9"
        const val HOME = "home"
        const val AWAY = "away"
        const val MATCH = "match-ticket-v9"
    }
}
