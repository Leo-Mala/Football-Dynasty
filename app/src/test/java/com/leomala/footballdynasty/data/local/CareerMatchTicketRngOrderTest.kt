package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.local.entity.PlayerEntity
import com.leomala.footballdynasty.data.repository.RoomCareerRepository
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.manager.LegacyFinanceLedgerState
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacyMatchConstructionSource
import com.leomala.footballdynasty.domain.manager.LegacyTransferClubRuntimeState
import com.leomala.footballdynasty.domain.model.Career
import com.leomala.footballdynasty.domain.model.Match
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerMatchTicketRngOrderTest {
    @Test
    fun `V9 ticket state consumes career rng before match simulation and final state persists both`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase13-ticket-rng-order"
        context.deleteDatabase(name)
        val database = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        seed(database)

        val initial = CareerStateFactory.create(CAREER, 424242L)
        val scheduled = ScheduledCareerMatch(
            matchId = MATCH,
            dayIndex = initial.calendar.currentDayIndex,
            eventTypeCode = 1,
            homeClubId = HOME,
            awayClubId = AWAY,
        )
        CareerMatchStore(database).initializeSchedule(initial, listOf(scheduled))
        CareerCompetitionStore(database).initializeLeague(
            careerId = CAREER,
            competitionId = COMPETITION,
            legacyCompetitionType = 1,
            legacyFormatCode = -1,
            clubIds = listOf(HOME, AWAY),
            roundMatchIds = listOf(listOf(MATCH)),
        )

        val managerStore = CareerManagerRuntimeStore(database)
        val financeBefore = LegacyFinanceRuntimeState(1_000L, LegacyFinanceLedgerState())
        managerStore.materializeClubState(
            careerId = CAREER,
            clubId = HOME,
            transfer = LegacyTransferClubRuntimeState(
                clubCode = 101,
                active = true,
                funds = financeBefore.cash,
                rosterPlayerCodes = emptyList(),
                primarySlotPlayerCode = null,
                secondarySlotPlayerCode = null,
                rawStateFlag = true,
            ),
            finance = financeBefore,
        )
        CareerStadiumRuntimeStore(database).materialize(
            CAREER,
            HOME,
            CareerStadiumRuntimeState(listOf(1_000, 5_000, 1_200, 20)),
        )
        val ticketStore = CareerTicketRuntimeStore(database)
        ticketStore.materializeClubState(
            CAREER,
            HOME,
            CareerClubTicketRuntimeState(rawDivisionCode = 0, legacyManagerId = -1),
        )
        ticketStore.materializeManagers(CAREER, emptyList())
        ticketStore.materializeMatchConstructionSource(
            CAREER,
            MATCH,
            LegacyMatchConstructionSource.LEAGUE_T,
        )

        var drawsAtSimulationEntry = -1L
        val coordinator = CareerMatchExecutionCoordinator(database) { 77L }
        val result = coordinator.execute(
            careerId = CAREER,
            matchId = MATCH,
            transientEvidence = transientEvidence(),
            includeTicketFinance = true,
        ) { event, _, random ->
            drawsAtSimulationEntry = random.draws
            random.nextInt(100)
            Match(event.matchId, event.homeClubId, event.awayClubId, 0, 0)
        }

        assertEquals(initial.random.draws + 3L, drawsAtSimulationEntry)
        assertEquals(initial.random.draws + 4L, result.state.random.draws)
        val financeAfter = requireNotNull(managerStore.clubFinanceState(CAREER, HOME))
        assertTrue(financeAfter.cash > financeBefore.cash)
        assertTrue(financeAfter.ledger.ticketIncome > financeBefore.ledger.ticketIncome)

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    private fun transientEvidence() = CareerMatchPersistedRuntimeResolver.TransientMatchEvidence(
        home = CareerMatchPersistedRuntimeResolver.TransientClubEvidence(
            active = listOf(
                CareerMatchPersistedRuntimeResolver.TransientPlayerEvidence(HOME_PLAYER, legacyG0 = 2)
            ),
            bench = emptyList(),
            substitutionsRemaining = 0,
            legacyModeFlag = false,
        ),
        away = CareerMatchPersistedRuntimeResolver.TransientClubEvidence(
            active = listOf(
                CareerMatchPersistedRuntimeResolver.TransientPlayerEvidence(AWAY_PLAYER, legacyG0 = 2)
            ),
            bench = emptyList(),
            substitutionsRemaining = 0,
            legacyModeFlag = false,
        ),
    )

    private suspend fun seed(database: FootballDynastyDatabase) {
        database.clubDao().upsertAll(
            listOf(
                club(HOME, 101, reputation = 1),
                club(AWAY, 202, reputation = 1),
            )
        )
        database.playerDao().upsertAll(listOf(player(HOME_PLAYER), player(AWAY_PLAYER)))
        RoomCareerRepository(database) { 10L }.save(Career(CAREER, "Ticket RNG", null, null))
        val dao = database.careerPlayerRuntimeDao()
        dao.upsertRuntime(runtime(HOME_PLAYER))
        dao.upsertMembership(membership(HOME_PLAYER, HOME, 0))
        dao.upsertRuntime(runtime(AWAY_PLAYER))
        dao.upsertMembership(membership(AWAY_PLAYER, AWAY, 0))
    }

    private fun runtime(playerId: String) = CareerPlayerRuntimeEntity(
        careerId = CAREER,
        playerId = playerId,
        sourceType = CareerPlayerRuntimeStore.SOURCE_CANONICAL,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = 25,
        overall = 80,
        marketValue = 1_000,
        star = false,
        worldTop = false,
        legacyHash = playerId.hashCode(),
        legacyGeneratedO = 0,
        legacyCreatedYear = 0,
        contractEndEpochMillis = 0L,
        legacyPreviousMarketValue = 0,
        legacyQ = false,
        legacyX = false,
        legacyY = false,
        legacyZ = false,
        energy = 100,
        injuryUntilEpochDay = 0L,
    )

    private fun membership(playerId: String, clubId: String, ordinal: Int) =
        CareerSquadMembershipEntity(CAREER, playerId, clubId, "SENIOR", ordinal)

    private fun player(id: String) = PlayerEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        name = id,
        age = 25,
        country = 1,
        position = 3,
        status = 2,
        side = 0,
        cr1 = 80,
        cr2 = 80,
        star = false,
        worldTop = false,
        legacyAid = 0,
        legacySid = 0,
        legacyTid = 0,
        legacyHash = id.hashCode(),
    )

    private fun club(id: String, legacyId: Int, reputation: Int) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = id,
        name = id,
        country = 0,
        state = 0,
        level = 1,
        stadium = "$id stadium",
        capacity = 10_000,
        reputation = reputation,
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
        const val CAREER = "career-ticket-order"
        const val MATCH = "match-ticket-order"
        const val COMPETITION = "league-ticket-order"
        const val HOME = "home"
        const val AWAY = "away"
        const val HOME_PLAYER = "home-player"
        const val AWAY_PLAYER = "away-player"
    }
}
