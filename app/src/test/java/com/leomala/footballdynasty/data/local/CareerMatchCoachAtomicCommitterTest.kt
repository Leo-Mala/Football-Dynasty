package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.repository.RoomCareerRepository
import com.leomala.footballdynasty.data.repository.RoomCareerStateRepository
import com.leomala.footballdynasty.domain.career.CareerMatchRuntimeBridge
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.manager.LegacyCoachSeasonClubRecord
import com.leomala.footballdynasty.domain.model.Career
import com.leomala.footballdynasty.domain.model.Match
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
class CareerMatchCoachAtomicCommitterTest {
    @Test
    fun `coach post match state commits with score rng and shared H`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = database(context)
        val initial = seed(database, 717171L)
        val scheduled = schedule(database, initial)
        val ticket = CareerTicketRuntimeStore(database)
        val coach = CareerCoachRuntimeStore(database)
        ticket.materializeManagers(CAREER, listOf(CareerManagerTicketRuntimeState(0, 7, 61)))
        val before = coachState(0, 7, 61, rawG = 60)
        coach.materialize(CAREER, before)

        val after = before.copy(
            rawG = 55,
            rawH = 64,
            rawD = 11,
            rawE = 5,
            rawO = 24,
            records = listOf(before.records.single().copy(rawMatches = 5, rawWins = 3, rawPoints = 13)),
        )
        val resolved = resolved(initial, scheduled)

        CareerMatchAtomicCommitter(database).commit(
            result = resolved,
            coachUpdatesInLegacyOrder = listOf(CareerMatchCoachUpdate(HOME, before, after)),
        )

        assertEquals(resolved.match, CareerMatchStore(database).findResult(CAREER, MATCH))
        assertEquals(resolved.state.random, requireNotNull(RoomCareerStateRepository(database).findById(CAREER)).random)
        assertEquals(after, coach.find(CAREER, 0))
        assertEquals(64, ticket.resolveCoachRawH(CAREER, 7))
        database.close()
        Unit
    }

    @Test
    fun `stale away coach rolls back match rng and prior home coach update`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = database(context)
        val initial = seed(database, 727272L)
        val scheduled = schedule(database, initial)
        val ticket = CareerTicketRuntimeStore(database)
        val coach = CareerCoachRuntimeStore(database)
        ticket.materializeManagers(
            CAREER,
            listOf(
                CareerManagerTicketRuntimeState(0, 7, 61),
                CareerManagerTicketRuntimeState(1, 8, 70),
            ),
        )
        val homeBefore = coachState(0, 7, 61, rawG = 60)
        val awayBefore = coachState(1, 8, 70, rawG = 65)
        coach.materialize(CAREER, homeBefore)
        coach.materialize(CAREER, awayBefore)
        val homeAfter = homeBefore.copy(rawG = 63, rawH = 66, rawD = 11)
        val staleAwayExpected = awayBefore.copy(rawG = 99)
        val resolved = resolved(initial, scheduled)

        try {
            CareerMatchAtomicCommitter(database).commit(
                result = resolved,
                coachUpdatesInLegacyOrder = listOf(
                    CareerMatchCoachUpdate(HOME, homeBefore, homeAfter),
                    CareerMatchCoachUpdate(AWAY, staleAwayExpected, staleAwayExpected.copy(rawG = 98)),
                ),
            )
            fail("Expected stale away coach rejection")
        } catch (_: IllegalArgumentException) {
            // Expected: match/core and the prior home-coach write must share the rollback.
        }

        assertNull(CareerMatchStore(database).findResult(CAREER, MATCH))
        assertEquals(initial.random, requireNotNull(RoomCareerStateRepository(database).findById(CAREER)).random)
        assertEquals(homeBefore, coach.find(CAREER, 0))
        assertEquals(awayBefore, coach.find(CAREER, 1))
        assertEquals(61, ticket.resolveCoachRawH(CAREER, 7))
        assertEquals(70, ticket.resolveCoachRawH(CAREER, 8))
        database.close()
        Unit
    }

    @Test
    fun `coach updates reject away before home without partial writes`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = database(context)
        val initial = seed(database, 737373L)
        val scheduled = schedule(database, initial)
        val ticket = CareerTicketRuntimeStore(database)
        val coach = CareerCoachRuntimeStore(database)
        ticket.materializeManagers(
            CAREER,
            listOf(
                CareerManagerTicketRuntimeState(0, 7, 61),
                CareerManagerTicketRuntimeState(1, 8, 70),
            ),
        )
        val homeBefore = coachState(0, 7, 61, rawG = 60)
        val awayBefore = coachState(1, 8, 70, rawG = 65)
        coach.materialize(CAREER, homeBefore)
        coach.materialize(CAREER, awayBefore)
        val resolved = resolved(initial, scheduled)

        assertNotNull(
            runCatching {
                CareerMatchAtomicCommitter(database).commit(
                    result = resolved,
                    coachUpdatesInLegacyOrder = listOf(
                        CareerMatchCoachUpdate(AWAY, awayBefore, awayBefore.copy(rawG = 66)),
                        CareerMatchCoachUpdate(HOME, homeBefore, homeBefore.copy(rawG = 61)),
                    ),
                )
            }.exceptionOrNull()
        )
        assertNull(CareerMatchStore(database).findResult(CAREER, MATCH))
        assertEquals(homeBefore, coach.find(CAREER, 0))
        assertEquals(awayBefore, coach.find(CAREER, 1))
        database.close()
        Unit
    }

    private fun database(context: Context): FootballDynastyDatabase =
        Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    private suspend fun seed(database: FootballDynastyDatabase, seed: Long): CareerState =
        CareerStateFactory.create(CAREER, seed).also {
            database.clubDao().upsertAll(listOf(club(HOME, 101), club(AWAY, 202)))
            RoomCareerRepository(database) { 10L }.save(Career(CAREER, "Atomic coach", null, null))
        }

    private suspend fun schedule(
        database: FootballDynastyDatabase,
        initial: CareerState,
    ): ScheduledCareerMatch = ScheduledCareerMatch(
        MATCH,
        initial.calendar.currentDayIndex,
        1,
        HOME,
        AWAY,
    ).also { CareerMatchStore(database).initializeSchedule(initial, listOf(it)) }

    private fun resolved(
        initial: CareerState,
        scheduled: ScheduledCareerMatch,
    ) = CareerMatchRuntimeBridge.run(initial, listOf(scheduled), scheduled.matchId) { event, random ->
        Match(event.matchId, event.homeClubId, event.awayClubId, random.nextInt(4), random.nextInt(4))
    }

    private fun coachState(
        ordinal: Int,
        managerId: Int,
        rawH: Int,
        rawG: Int,
    ) = CareerCoachRuntimeState(
        sourceOrdinal = ordinal,
        legacyManagerId = managerId,
        isUserControlled = true,
        currentClubId = if (ordinal == 0) HOME else AWAY,
        alternativeClubId = null,
        previousClubId = null,
        previousClubCountry = null,
        previousClubDivisionIndex = null,
        rawG = rawG,
        rawH = rawH,
        rawD = 10,
        rawE = 4,
        rawF = 3,
        rawO = 20,
        rawM = 0,
        records = listOf(
            LegacyCoachSeasonClubRecord(2026, if (ordinal == 0) 101 else 202, rawMatches = 4, rawWins = 2, rawLosses = 1, rawPoints = 9)
        ),
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

    private companion object {
        const val CAREER = "career-atomic-coach"
        const val HOME = "home"
        const val AWAY = "away"
        const val MATCH = "match-atomic-coach"
    }
}
