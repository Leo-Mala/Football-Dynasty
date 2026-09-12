package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.CareerCompetitionStore
import com.leomala.footballdynasty.data.local.CareerPlayerRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionDisciplineEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerProceduralPlayerEntity
import com.leomala.footballdynasty.data.local.entity.CareerScheduledMatchEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
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
class CareerLineupCompetitionDisciplineOwnerTest {
    @Test
    fun `restricted competition projects exact V19 V0 state for prepared lineup`() = runBlocking {
        val database = database()
        try {
            seedPreparedMatch(database, legacyCompetitionType = 1)
            database.careerCompetitionDisciplineDao().upsertAll(
                listOf(
                    discipline(PLAYER_A, threshold3 = 3, threshold1 = 0),
                    discipline(PLAYER_B, threshold3 = 0, threshold1 = 0),
                )
            )

            val inputs = requireNotNull(
                CareerLineupInputCatalogStore(database).loadManagedClubLineupInputs(CAREER)
            )
            val preparation = inputs.matchPreparation

            assertEquals(COMPETITION, preparation.competitionId)
            assertEquals(true, preparation.competitionRestrictionActive)
            assertEquals(true, preparation.competitionDisciplineOwnerResolved)
            assertEquals(true, inputs.players.single().excludedByCompetitionV0ForPreparedMatch)
            assertTrue(
                CareerLineupInputCatalogStore.MatchPreparationBlocker.LINEUP_ELIGIBILITY_OWNER_UNRESOLVED in
                    preparation.blockers
            )
            assertFalse(preparation.executable)
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing V19 row stays unknown instead of fabricating zero discipline`() = runBlocking {
        val database = database()
        try {
            seedPreparedMatch(database, legacyCompetitionType = 1)
            database.careerCompetitionDisciplineDao().upsert(
                discipline(PLAYER_B, threshold3 = 0, threshold1 = 0)
            )

            val inputs = requireNotNull(
                CareerLineupInputCatalogStore(database).loadManagedClubLineupInputs(CAREER)
            )

            assertEquals(false, inputs.matchPreparation.competitionDisciplineOwnerResolved)
            assertNull(inputs.players.single().excludedByCompetitionV0ForPreparedMatch)
            assertFalse(inputs.matchPreparation.executable)
        } finally {
            database.close()
        }
    }

    @Test
    fun `legacy competition type zero bypasses V0 without requiring discipline rows`() = runBlocking {
        val database = database()
        try {
            seedPreparedMatch(database, legacyCompetitionType = 0)

            val inputs = requireNotNull(
                CareerLineupInputCatalogStore(database).loadManagedClubLineupInputs(CAREER)
            )

            assertEquals(COMPETITION, inputs.matchPreparation.competitionId)
            assertEquals(false, inputs.matchPreparation.competitionRestrictionActive)
            assertEquals(true, inputs.matchPreparation.competitionDisciplineOwnerResolved)
            assertEquals(false, inputs.players.single().excludedByCompetitionV0ForPreparedMatch)
        } finally {
            database.close()
        }
    }

    private suspend fun seedPreparedMatch(
        database: FootballDynastyDatabase,
        legacyCompetitionType: Int,
    ) {
        database.clubDao().upsertAll(listOf(club(CLUB_A), club(CLUB_B)))
        CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
            careerId = CAREER,
            displayName = "Discipline owner",
            seed = 17L,
            managedClubId = CLUB_A,
        )
        val runtimeStore = CareerPlayerRuntimeStore(database, clockMillis = { 100L })
        runtimeStore.saveProceduralPlayer(
            runtime = runtime(PLAYER_A),
            procedural = procedural(PLAYER_A),
            membership = membership(PLAYER_A, CLUB_A),
        )
        runtimeStore.saveProceduralPlayer(
            runtime = runtime(PLAYER_B),
            procedural = procedural(PLAYER_B),
            membership = membership(PLAYER_B, CLUB_B),
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
        CareerCompetitionStore(database).initializeLeague(
            careerId = CAREER,
            competitionId = COMPETITION,
            legacyCompetitionType = legacyCompetitionType,
            legacyFormatCode = 0,
            clubIds = listOf(CLUB_A, CLUB_B),
            roundMatchIds = listOf(listOf(MATCH)),
        )
    }

    private fun discipline(
        playerId: String,
        threshold3: Int,
        threshold1: Int,
    ) = CareerCompetitionDisciplineEntity(
        careerId = CAREER,
        playerId = playerId,
        competitionId = COMPETITION,
        legacyThreshold3Counter = threshold3,
        legacyThreshold1Counter = threshold1,
    )

    private fun runtime(playerId: String) = CareerPlayerRuntimeEntity(
        careerId = CAREER,
        playerId = playerId,
        sourceType = CareerPlayerRuntimeStore.SOURCE_PROCEDURAL,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = 24,
        overall = 75,
        marketValue = 7_500,
        star = false,
        worldTop = false,
        legacyHash = playerId.hashCode(),
        legacyGeneratedO = 0,
        legacyCreatedYear = 2026,
        contractEndEpochMillis = 2_222L,
        legacyPreviousMarketValue = 7_500,
        legacyQ = false,
        legacyX = false,
        legacyY = false,
        legacyZ = false,
        legacyAnnualM = false,
        legacyAnnualN = 0.0,
        legacyRawPayrollN = 0,
        energy = 80,
        injuryUntilEpochDay = 0L,
    )

    private fun procedural(playerId: String) = CareerProceduralPlayerEntity(
        careerId = CAREER,
        playerId = playerId,
        name = playerId,
        country = 11,
        position = 2,
        status = 0,
        side = 0,
        cr1 = 10,
        cr2 = 0,
    )

    private fun membership(playerId: String, clubId: String) = CareerSquadMembershipEntity(
        careerId = CAREER,
        playerId = playerId,
        clubId = clubId,
        rosterKind = "SENIOR",
        sourceOrdinal = 0,
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
        const val CAREER = "career-discipline"
        const val CLUB_A = "club-a"
        const val CLUB_B = "club-b"
        const val PLAYER_A = "player-a"
        const val PLAYER_B = "player-b"
        const val MATCH = "match-a"
        const val COMPETITION = "competition-a"
    }
}
