package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.CareerScheduledMatchEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import kotlinx.coroutines.runBlocking
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
    fun `reachable non type seven with attached manager fails closed until raw inputs are persisted`() = runBlocking {
        val database = database()
        seed(database)
        val ticketStore = CareerTicketRuntimeStore(database)
        ticketStore.materializeClubState(CAREER, HOME, CareerClubTicketRuntimeState(0, 100))
        ticketStore.materializeClubState(CAREER, AWAY, CareerClubTicketRuntimeState(0, -1))
        ticketStore.materializeManagers(CAREER, listOf(CareerManagerTicketRuntimeState(0, 100, 80)))

        val failure = runCatching {
            CareerCoachPostMatchPersistedResolver(database).resolveTypeSeven(
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
    fun `reachable non type seven without attached manager remains no op`() = runBlocking {
        val database = database()
        seed(database)
        val ticketStore = CareerTicketRuntimeStore(database)
        ticketStore.materializeClubState(CAREER, HOME, CareerClubTicketRuntimeState(0, -1))
        ticketStore.materializeClubState(CAREER, AWAY, CareerClubTicketRuntimeState(0, -1))
        ticketStore.materializeManagers(CAREER, emptyList())

        val updates = CareerCoachPostMatchPersistedResolver(database).resolveTypeSeven(
            careerId = CAREER,
            scheduled = scheduled(),
            seasonId = 1,
            homeGoals = 1,
            awayGoals = 0,
        )

        assertTrue(updates.isEmpty())
        database.close()
    }

    private suspend fun seed(database: FootballDynastyDatabase) {
        database.clubDao().upsertAll(listOf(club(HOME, 101), club(AWAY, 202)))
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
                legacyFormatCode = 0,
                currentRoundNumber = 1,
                totalRounds = 1,
            )
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

    private fun scheduled() = ScheduledCareerMatch(MATCH, 0, 1, HOME, AWAY)

    private fun database(): FootballDynastyDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
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
