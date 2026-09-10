package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.CareerPlayerRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.CareerClubManagerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerProceduralPlayerEntity
import com.leomala.footballdynasty.data.local.entity.CareerScheduledMatchEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerLineupClubEligibilityProjectionTest {
    @Test
    fun `catalog projects persisted u0 and Q0 true and false without clearing aggregate eligibility blocker`() =
        runBlocking {
            listOf(true, false).forEach { activeQ0 ->
                val database = database()
                try {
                    seedPreparedManagedPlayer(database)
                    database.careerManagerRuntimeDao().upsertClubRuntime(clubRuntime(activeQ0))

                    val inputs = requireNotNull(
                        CareerLineupInputCatalogStore(database)
                            .loadManagedClubLineupInputs(CAREER_A)
                    )
                    val player = inputs.players.single()

                    assertEquals(true, player.hasClubForPreparedMatch)
                    assertEquals(activeQ0, player.clubActiveQ0ForPreparedMatch)
                    assertTrue(
                        CareerLineupInputCatalogStore.MatchPreparationBlocker
                            .LINEUP_ELIGIBILITY_OWNER_UNRESOLVED in inputs.matchPreparation.blockers
                    )
                } finally {
                    database.close()
                }
            }
        }

    @Test
    fun `catalog keeps u0 and Q0 unresolved when persisted club runtime is missing`() = runBlocking {
        val database = database()
        try {
            seedPreparedManagedPlayer(database)

            val inputs = requireNotNull(
                CareerLineupInputCatalogStore(database)
                    .loadManagedClubLineupInputs(CAREER_A)
            )
            val player = inputs.players.single()

            assertNull(player.hasClubForPreparedMatch)
            assertNull(player.clubActiveQ0ForPreparedMatch)
            assertTrue(
                CareerLineupInputCatalogStore.MatchPreparationBlocker
                    .LINEUP_ELIGIBILITY_OWNER_UNRESOLVED in inputs.matchPreparation.blockers
            )
        } finally {
            database.close()
        }
    }

    private suspend fun seedPreparedManagedPlayer(database: FootballDynastyDatabase) {
        database.clubDao().upsertAll(listOf(club(CLUB_A), club(CLUB_B)))
        CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
            careerId = CAREER_A,
            displayName = "Career A",
            seed = 7L,
            managedClubId = CLUB_A,
        )
        CareerPlayerRuntimeStore(database, clockMillis = { 100L }).saveProceduralPlayer(
            runtime = runtime(),
            procedural = procedural(),
            membership = membership(),
        )
        val currentDay = requireNotNull(database.careerCoreStateDao().findById(CAREER_A))
            .currentDayIndex
        database.careerScheduledMatchDao().upsert(
            CareerScheduledMatchEntity(
                careerId = CAREER_A,
                matchId = MATCH_A,
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

    private fun clubRuntime(active: Boolean) = CareerClubManagerRuntimeEntity(
        careerId = CAREER_A,
        clubId = CLUB_A,
        active = active,
        cash = 0L,
        primarySlotPlayerCode = null,
        secondarySlotPlayerCode = null,
        rawStateFlag = false,
        ticketIncome = 0,
        playerSaleIncome = 0L,
        prizeIncome = 0,
        sponsorIncome = 0,
        playerPurchaseExpense = 0L,
        stadiumExpense = 0,
        salaryExpense = 0L,
        borrowingChargeExpense = 0,
        fineExpense = 0,
        miscellaneousExpense = 0,
        borrowed = 0,
        monthlyBorrowingCharge = 0,
    )

    private fun runtime() = CareerPlayerRuntimeEntity(
        careerId = CAREER_A,
        playerId = PLAYER_A,
        sourceType = CareerPlayerRuntimeStore.SOURCE_PROCEDURAL,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = 24,
        overall = 80,
        marketValue = 7_100,
        star = false,
        worldTop = false,
        legacyHash = PLAYER_A.hashCode(),
        legacyGeneratedO = 0,
        legacyCreatedYear = 2026,
        contractEndEpochMillis = 2_222L,
        legacyPreviousMarketValue = 7_100,
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

    private fun procedural() = CareerProceduralPlayerEntity(
        careerId = CAREER_A,
        playerId = PLAYER_A,
        name = PLAYER_A,
        country = 11,
        position = 4,
        status = 0,
        side = 1,
        cr1 = 8,
        cr2 = 0,
    )

    private fun membership() = CareerSquadMembershipEntity(
        careerId = CAREER_A,
        playerId = PLAYER_A,
        clubId = CLUB_A,
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
        const val CAREER_A = "career-a"
        const val CLUB_A = "club-a"
        const val CLUB_B = "club-b"
        const val PLAYER_A = "player-a"
        const val MATCH_A = "match-a"
    }
}
