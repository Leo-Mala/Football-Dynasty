package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.CareerClubManagerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerScheduledMatchEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.match.LegacyMatchSubstitutionRules
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerLineupPersistedTacticsOwnersTest {
    @Test
    fun `match preparation reads persisted raw tactics Q0 and proved substitution budget for both clubs`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_HOME), club(CLUB_AWAY)))
            CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
                careerId = CAREER,
                displayName = "Persisted tactics owners",
                seed = 7L,
                managedClubId = CLUB_HOME,
            )
            val currentDay = requireNotNull(
                database.careerCoreStateDao().findById(CAREER)
            ).currentDayIndex
            database.careerScheduledMatchDao().upsert(
                CareerScheduledMatchEntity(
                    careerId = CAREER,
                    matchId = MATCH,
                    dayIndex = currentDay,
                    eventTypeCode = 1,
                    homeClubId = CLUB_HOME,
                    awayClubId = CLUB_AWAY,
                    processed = false,
                    homeGoals = null,
                    awayGoals = null,
                    legacyDayMatchOrdinal = 0,
                )
            )
            database.careerManagerRuntimeDao().upsertClubRuntime(
                managerRuntime(
                    clubId = CLUB_HOME,
                    active = true,
                    tacticOption2 = 9,
                )
            )
            database.careerManagerRuntimeDao().upsertClubRuntime(
                managerRuntime(
                    clubId = CLUB_AWAY,
                    active = false,
                    tacticOption2 = -4,
                )
            )

            val preparation = requireNotNull(
                CareerLineupInputCatalogStore(database)
                    .loadManagedClubLineupInputs(CAREER)
            ).matchPreparation

            assertEquals(MATCH, preparation.matchId)
            assertEquals(9, preparation.homeTacticIndex)
            assertEquals(-4, preparation.awayTacticIndex)
            assertEquals(
                LegacyMatchSubstitutionRules.INITIAL_SUBSTITUTIONS_PER_SIDE,
                preparation.homeSubstitutionsRemaining,
            )
            assertEquals(
                LegacyMatchSubstitutionRules.INITIAL_SUBSTITUTIONS_PER_SIDE,
                preparation.awaySubstitutionsRemaining,
            )
            assertEquals(true, preparation.homeLegacyModeFlag)
            assertEquals(false, preparation.awayLegacyModeFlag)
            assertFalse(
                CareerLineupInputCatalogStore.MatchPreparationBlocker.TACTICS_STATE_OWNER_UNRESOLVED in
                    preparation.blockers
            )
            assertFalse(
                CareerLineupInputCatalogStore.MatchPreparationBlocker.SUBSTITUTION_BUDGET_OWNER_UNRESOLVED in
                    preparation.blockers
            )
            assertFalse(
                CareerLineupInputCatalogStore.MatchPreparationBlocker.LEGACY_MODE_FLAG_OWNER_UNRESOLVED in
                    preparation.blockers
            )
        } finally {
            database.close()
        }
    }

    private fun managerRuntime(
        clubId: String,
        active: Boolean,
        tacticOption2: Int,
    ) = CareerClubManagerRuntimeEntity(
        careerId = CAREER,
        clubId = clubId,
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
        legacyTacticOption2 = tacticOption2,
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
        const val CAREER = "career-persisted-tactics"
        const val CLUB_HOME = "club-home"
        const val CLUB_AWAY = "club-away"
        const val MATCH = "match-tactics"
    }
}
