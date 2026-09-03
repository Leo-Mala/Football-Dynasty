package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionStandingEntity
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.CareerScheduledMatchEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerCoachPostMatchPersistedResolverTest {
    @Test
    fun `type one with x0 but missing relegation input still fails closed`() = runBlocking {
        val database = database()
        seed(database, legacyLeagueSubtype = 1)
        materializeManager(database)

        val failure = runCatching {
            CareerCoachPostMatchPersistedResolver(database).resolveReachable(
                careerId = CAREER,
                scheduled = scheduled(),
                seasonId = 1,
                homeGoals = 1,
                awayGoals = 0,
            )
        }.exceptionOrNull()

        assertNotNull(failure)
        assertTrue(requireNotNull(failure).message.orEmpty().contains("nRebaixados"))
        database.close()
    }

    @Test
    fun `legacy format code is not accepted as unproven x0 substitute`() = runBlocking {
        val database = database()
        seed(database, legacyFormatCode = 73, legacyRelegationCount = 1)
        materializeManager(database)

        assertEquals(
            73,
            requireNotNull(database.careerCompetitionDao().findCompetition(CAREER, COMPETITION)).legacyFormatCode,
        )
        val failure = runCatching {
            CareerCoachPostMatchPersistedResolver(database).resolveReachable(
                careerId = CAREER,
                scheduled = scheduled(),
                seasonId = 1,
                homeGoals = 1,
                awayGoals = 0,
            )
        }.exceptionOrNull()

        assertNotNull(failure)
        assertTrue(requireNotNull(failure).message.orEmpty().contains("konrent.t.x0()"))
        database.close()
    }

    @Test
    fun `type one exact persisted inputs resolve j then i from pre match table`() = runBlocking {
        val database = database()
        seed(database, legacyLeagueSubtype = 1, legacyRelegationCount = 1)
        val before = materializeManager(database)

        val updates = CareerCoachPostMatchPersistedResolver(database).resolveReachable(
            careerId = CAREER,
            scheduled = scheduled(),
            seasonId = 1,
            homeGoals = 1,
            awayGoals = 0,
        )

        assertEquals(1, updates.size)
        val update = updates.single()
        assertEquals(HOME, update.resolvedClubId)
        assertEquals(before, update.expectedBefore)
        assertEquals(1, update.after.rawD)
        assertEquals(1, update.after.rawE)
        assertEquals(0, update.after.rawF)
        assertEquals(4, update.after.rawO)
        assertEquals(55, update.after.rawG)
        assertEquals(1, update.after.records.single().rawMatches)
        assertEquals(1, update.after.records.single().rawWins)
        assertEquals(4, update.after.records.single().rawPoints)
        database.close()
    }

    @Test
    fun `reachable competition without attached manager remains no op without exact inputs`() = runBlocking {
        val database = database()
        seed(database)
        val ticketStore = CareerTicketRuntimeStore(database)
        ticketStore.materializeClubState(CAREER, HOME, CareerClubTicketRuntimeState(0, -1))
        ticketStore.materializeClubState(CAREER, AWAY, CareerClubTicketRuntimeState(0, -1))
        ticketStore.materializeManagers(CAREER, emptyList())

        val updates = CareerCoachPostMatchPersistedResolver(database).resolveReachable(
            careerId = CAREER,
            scheduled = scheduled(),
            seasonId = 1,
            homeGoals = 1,
            awayGoals = 0,
        )

        assertTrue(updates.isEmpty())
        database.close()
    }

    private suspend fun materializeManager(database: FootballDynastyDatabase): CareerCoachRuntimeState {
        val ticketStore = CareerTicketRuntimeStore(database)
        ticketStore.materializeClubState(CAREER, HOME, CareerClubTicketRuntimeState(0, 100))
        ticketStore.materializeClubState(CAREER, AWAY, CareerClubTicketRuntimeState(0, -1))
        ticketStore.materializeManagers(CAREER, listOf(CareerManagerTicketRuntimeState(0, 100, 80)))
        val state = CareerCoachRuntimeState(
            sourceOrdinal = 0,
            legacyManagerId = 100,
            isUserControlled = false,
            currentClubId = HOME,
            alternativeClubId = null,
            previousClubId = null,
            previousClubCountry = null,
            previousClubDivisionIndex = null,
            rawG = 50,
            rawH = 80,
            rawD = 0,
            rawE = 0,
            rawF = 0,
            rawO = 0,
            rawM = 0,
            records = emptyList(),
        )
        CareerCoachRuntimeStore(database).materialize(CAREER, state)
        return state
    }

    private suspend fun seed(
        database: FootballDynastyDatabase,
        legacyFormatCode: Int = 0,
        legacyLeagueSubtype: Int? = null,
        legacyRelegationCount: Int? = null,
    ) {
        database.clubDao().upsertAll(listOf(club(HOME, 101, 5), club(AWAY, 202, 10)))
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(CAREER, 1, "Coach resolver", null, null, 1L, 1L)
        )
        database.careerScheduledMatchDao().upsert(
            CareerScheduledMatchEntity(CAREER, MATCH, 0, 1, HOME, AWAY, false, null, null)
        )
        database.careerCompetitionDao().upsertCompetition(
            com.leomala.footballdynasty.data.local.entity.CareerCompetitionEntity(
                careerId = CAREER,
                competitionId = COMPETITION,
                legacyCompetitionType = 1,
                legacyFormatCode = legacyFormatCode,
                currentRoundNumber = 1,
                totalRounds = 1,
                legacyRelegationCount = legacyRelegationCount,
                legacyLeagueSubtype = legacyLeagueSubtype,
            )
        )
        database.careerCompetitionDao().upsertStandings(
            listOf(standing(HOME, 0), standing(AWAY, 1))
        )
        database.careerCompetitionDao().upsertMatches(
            listOf(
                com.leomala.footballdynasty.data.local.entity.CareerCompetitionMatchEntity(
                    careerId = CAREER,
                    competitionId = COMPETITION,
                    matchId = MATCH,
                    roundNumber = 1,
                    fixtureOrdinal = 0,
                )
            )
        )
    }

    private fun standing(clubId: String, ordinal: Int) = CareerCompetitionStandingEntity(
        careerId = CAREER,
        competitionId = COMPETITION,
        clubId = clubId,
        stableOrdinal = ordinal,
        points = 0,
        played = 0,
        wins = 0,
        losses = 0,
        goalsFor = 0,
        goalsAgainst = 0,
    )

    private fun scheduled() = ScheduledCareerMatch(MATCH, 0, 1, HOME, AWAY)

    private fun database(): FootballDynastyDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private fun club(id: String, legacyId: Int, level: Int) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = id,
        name = id,
        country = 0,
        state = 0,
        level = level,
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
        const val CAREER = "career-coach-resolver"
        const val COMPETITION = "type-1"
        const val MATCH = "match-1"
        const val HOME = "home"
        const val AWAY = "away"
    }
}
