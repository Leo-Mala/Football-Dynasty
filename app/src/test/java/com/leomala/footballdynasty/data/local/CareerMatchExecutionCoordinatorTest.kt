package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.local.entity.PlayerEntity
import com.leomala.footballdynasty.data.repository.RoomCareerRepository
import com.leomala.footballdynasty.data.repository.RoomCareerStateRepository
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.LegacyCalendarRules
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.match.LegacyMatchEventType
import com.leomala.footballdynasty.domain.match.LegacyMatchModernResultMapper
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime
import com.leomala.footballdynasty.domain.model.Career
import com.leomala.footballdynasty.domain.model.Match
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerMatchExecutionCoordinatorTest {
    @Test
    fun `persisted match uses career rng and commits injury effects through database reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase9-execution-coordinator-reopen"
        context.deleteDatabase(name)
        var database = database(context, name)
        seed(database)

        val initial = CareerStateFactory.create("career-exec", 424242L)
        val scheduled = ScheduledCareerMatch(
            matchId = "match-1",
            dayIndex = initial.calendar.currentDayIndex,
            eventTypeCode = 1,
            homeClubId = "home",
            awayClubId = "away",
        )
        CareerMatchStore(database).initializeSchedule(initial, listOf(scheduled))

        var injuryDuration = -1
        var injuryShouldWrite = false
        var postInjuryOverall = -1
        val coordinator = CareerMatchExecutionCoordinator(database) { 77L }
        val result = coordinator.execute(
            careerId = "career-exec",
            matchId = "match-1",
            transientEvidence = transientEvidence(),
        ) { event, state, random ->
            val injured = state.home.active.single()
            val applied = LegacyMatchTransientRuntime.applyEvent(
                state = state,
                legacyType = LegacyMatchEventType.INJURY.legacyCode,
                legacySubtype = -1,
                eventClub = state.home,
                originalPrimary = injured,
                legacyPeriod = 1,
                legacyMinute = 12,
                random = random,
            )
            val injury = requireNotNull(applied.injuryResult)
            injuryDuration = injury.durationDays
            injuryShouldWrite = injury.shouldSetInjuryUntil
            postInjuryOverall = injury.updatedSkill
            LegacyMatchModernResultMapper.map(
                state = state,
                matchId = event.matchId,
                homeClubId = event.homeClubId,
                awayClubId = event.awayClubId,
            )
        }

        assertEquals(initial.random.draws + 3L, result.state.random.draws)
        assertEquals(0, result.match.homeGoals)
        assertEquals(0, result.match.awayGoals)
        database.close()

        database = database(context, name)
        val reopenedState = requireNotNull(RoomCareerStateRepository(database).findById("career-exec"))
        assertEquals(result.state, reopenedState)
        val store = CareerMatchStore(database)
        assertEquals(result.match, store.findResult("career-exec", "match-1"))

        val dao = database.careerPlayerRuntimeDao()
        val home = requireNotNull(dao.findRuntime("career-exec", "home-player"))
        assertEquals(postInjuryOverall, home.overall)
        val matchDate = LegacyCalendarRules.dateAt(
            initial.calendar.copy(currentDayIndex = scheduled.dayIndex)
        )
        val matchEpoch = LocalDate.of(matchDate.year, matchDate.month, matchDate.day).toEpochDay()
        val expectedDeadline = if (injuryShouldWrite) matchEpoch + injuryDuration else 0L
        assertEquals(expectedDeadline, home.injuryUntilEpochDay)
        val stats = dao.clubSeasonStatsForPlayer("career-exec", "home-player").single()
        assertEquals(initial.season.number, stats.legacySeasonId)
        assertEquals(101, stats.legacyClubId)
        assertEquals(1, stats.legacyH)
        assertTrue(store.loadSchedule("career-exec").single().processed)

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `type seven coach statistics commit with match and survive reopen without unrelated V11 rows`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase14-type7-coach-postmatch"
        context.deleteDatabase(name)
        var database = database(context, name)
        seed(database)

        val initial = CareerStateFactory.create("career-exec", 515151L)
        val scheduled = ScheduledCareerMatch(
            matchId = "match-7",
            dayIndex = initial.calendar.currentDayIndex,
            eventTypeCode = 1,
            homeClubId = "home",
            awayClubId = "away",
        )
        CareerMatchStore(database).initializeSchedule(initial, listOf(scheduled))
        materializeTypeSevenCompetition(database, scheduled)

        val ticketStore = CareerTicketRuntimeStore(database)
        ticketStore.materializeClubState(
            "career-exec", "home", CareerClubTicketRuntimeState(rawDivisionCode = 0, legacyManagerId = 100)
        )
        ticketStore.materializeClubState(
            "career-exec", "away", CareerClubTicketRuntimeState(rawDivisionCode = 0, legacyManagerId = 200)
        )
        ticketStore.materializeManagers(
            "career-exec",
            listOf(
                CareerManagerTicketRuntimeState(0, 100, 45),
                CareerManagerTicketRuntimeState(1, 200, 55),
                CareerManagerTicketRuntimeState(2, 300, 88),
            ),
        )
        val coachStore = CareerCoachRuntimeStore(database)
        val homeBefore = coachState(0, 100, "home", rawG = 44, rawH = 45)
        val awayBefore = coachState(1, 200, "away", rawG = 54, rawH = 55)
        coachStore.materialize("career-exec", homeBefore)
        coachStore.materialize("career-exec", awayBefore)
        // Manager ordinal 2 deliberately has no V11 row: only c0.y0()-resolved managers may block.

        val result = CareerMatchExecutionCoordinator(database) { 90L }.execute(
            careerId = "career-exec",
            matchId = scheduled.matchId,
            transientEvidence = transientEvidence(),
        ) { event, _, _ ->
            Match(event.matchId, event.homeClubId, event.awayClubId, 2, 1)
        }

        val homeAfter = requireNotNull(coachStore.find("career-exec", 0))
        val awayAfter = requireNotNull(coachStore.find("career-exec", 1))
        assertEquals(1, homeAfter.rawD)
        assertEquals(1, homeAfter.rawE)
        assertEquals(0, homeAfter.rawF)
        assertEquals(44, homeAfter.rawG)
        assertEquals(45, homeAfter.rawH)
        assertEquals(1, awayAfter.rawD)
        assertEquals(0, awayAfter.rawE)
        assertEquals(1, awayAfter.rawF)
        assertEquals(54, awayAfter.rawG)
        assertEquals(55, awayAfter.rawH)
        assertEquals(88, ticketStore.managersInWorldOrder("career-exec").single { it.sourceOrdinal == 2 }.rawH)
        assertEquals(result.match, CareerMatchStore(database).findResult("career-exec", scheduled.matchId))

        database.close()
        database = database(context, name)
        assertEquals(homeAfter, CareerCoachRuntimeStore(database).find("career-exec", 0))
        assertEquals(awayAfter, CareerCoachRuntimeStore(database).find("career-exec", 1))
        assertEquals(result.match, CareerMatchStore(database).findResult("career-exec", scheduled.matchId))

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `type seven missing resolved V11 coach fails closed before match persistence`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase14-type7-coach-fail-closed"
        context.deleteDatabase(name)
        val database = database(context, name)
        seed(database)

        val initial = CareerStateFactory.create("career-exec", 616161L)
        val scheduled = ScheduledCareerMatch(
            matchId = "match-7-missing-coach",
            dayIndex = initial.calendar.currentDayIndex,
            eventTypeCode = 1,
            homeClubId = "home",
            awayClubId = "away",
        )
        CareerMatchStore(database).initializeSchedule(initial, listOf(scheduled))
        materializeTypeSevenCompetition(database, scheduled)

        val ticketStore = CareerTicketRuntimeStore(database)
        ticketStore.materializeClubState(
            "career-exec", "home", CareerClubTicketRuntimeState(rawDivisionCode = 0, legacyManagerId = 100)
        )
        ticketStore.materializeClubState(
            "career-exec", "away", CareerClubTicketRuntimeState(rawDivisionCode = 0, legacyManagerId = -1)
        )
        ticketStore.materializeManagers(
            "career-exec",
            listOf(CareerManagerTicketRuntimeState(0, 100, 45)),
        )

        val failure = try {
            CareerMatchExecutionCoordinator(database) { 91L }.execute(
                careerId = "career-exec",
                matchId = scheduled.matchId,
                transientEvidence = transientEvidence(),
            ) { event, _, _ ->
                Match(event.matchId, event.homeClubId, event.awayClubId, 1, 0)
            }
            null
        } catch (throwable: Throwable) {
            throwable
        }

        assertNotNull(failure)
        assertNull(CareerMatchStore(database).findResult("career-exec", scheduled.matchId))
        assertEquals(initial, RoomCareerStateRepository(database).findById("career-exec"))
        assertEquals(false, CareerMatchStore(database).loadSchedule("career-exec").single().processed)
        assertEquals(1, requireNotNull(CareerCompetitionStore(database).load("career-exec", "type-7")).currentRoundNumber)

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    private fun database(context: Context, name: String): FootballDynastyDatabase =
        Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()

    private suspend fun materializeTypeSevenCompetition(
        database: FootballDynastyDatabase,
        scheduled: ScheduledCareerMatch,
    ) {
        CareerCompetitionStore(database).initializeLeague(
            careerId = "career-exec",
            competitionId = "type-7",
            legacyCompetitionType = 7,
            legacyFormatCode = 0,
            clubIds = listOf("home", "away"),
            roundMatchIds = listOf(listOf(scheduled.matchId)),
        )
    }

    private fun transientEvidence() = CareerMatchPersistedRuntimeResolver.TransientMatchEvidence(
        home = CareerMatchPersistedRuntimeResolver.TransientClubEvidence(
            active = listOf(
                CareerMatchPersistedRuntimeResolver.TransientPlayerEvidence("home-player", legacyG0 = 2)
            ),
            bench = emptyList(),
            substitutionsRemaining = 0,
            legacyModeFlag = false,
        ),
        away = CareerMatchPersistedRuntimeResolver.TransientClubEvidence(
            active = listOf(
                CareerMatchPersistedRuntimeResolver.TransientPlayerEvidence("away-player", legacyG0 = 2)
            ),
            bench = emptyList(),
            substitutionsRemaining = 0,
            legacyModeFlag = false,
        ),
    )

    private fun coachState(
        sourceOrdinal: Int,
        legacyManagerId: Int,
        currentClubId: String,
        rawG: Int,
        rawH: Int,
    ) = CareerCoachRuntimeState(
        sourceOrdinal = sourceOrdinal,
        legacyManagerId = legacyManagerId,
        isUserControlled = false,
        currentClubId = currentClubId,
        alternativeClubId = null,
        previousClubId = null,
        previousClubCountry = null,
        previousClubDivisionIndex = null,
        rawG = rawG,
        rawH = rawH,
        rawD = 0,
        rawE = 0,
        rawF = 0,
        rawO = 0,
        rawM = 0,
        records = emptyList(),
    )

    private suspend fun seed(database: FootballDynastyDatabase) {
        database.clubDao().upsertAll(listOf(club("home", 101), club("away", 202)))
        database.playerDao().upsertAll(
            listOf(player("home-player", 35, 80), player("away-player", 25, 78))
        )
        RoomCareerRepository(database) { 10L }.save(Career("career-exec", "Phase 9", null, null))
        val dao = database.careerPlayerRuntimeDao()
        dao.upsertRuntime(runtime("home-player", 35, 80))
        dao.upsertMembership(membership("home-player", "home", 0))
        dao.upsertRuntime(runtime("away-player", 25, 78))
        dao.upsertMembership(membership("away-player", "away", 0))
    }

    private fun runtime(playerId: String, age: Int, overall: Int) = CareerPlayerRuntimeEntity(
        careerId = "career-exec",
        playerId = playerId,
        sourceType = CareerPlayerRuntimeStore.SOURCE_CANONICAL,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = age,
        overall = overall,
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
        energy = 60,
        injuryUntilEpochDay = 0L,
    )

    private fun membership(playerId: String, clubId: String, ordinal: Int) =
        CareerSquadMembershipEntity(
            careerId = "career-exec",
            playerId = playerId,
            clubId = clubId,
            rosterKind = "SENIOR",
            sourceOrdinal = ordinal,
        )

    private fun player(id: String, age: Int, overall: Int) = PlayerEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        name = id,
        age = age,
        country = 1,
        position = 3,
        status = 2,
        side = 0,
        cr1 = overall,
        cr2 = overall,
        star = false,
        worldTop = false,
        legacyAid = 0,
        legacySid = 0,
        legacyTid = 0,
        legacyHash = id.hashCode(),
    )

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
        legacyId = legacyId,
        legacyValid = true,
    )
}
