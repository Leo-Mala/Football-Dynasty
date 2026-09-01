package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerClubManagerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.repository.RoomCareerRepository
import com.leomala.footballdynasty.domain.career.CareerMatchRuntimeBridge
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.manager.LegacyFinanceLedgerState
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState
import com.leomala.footballdynasty.domain.model.Career
import com.leomala.footballdynasty.domain.model.Match
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerMatchAtomicCommitterTest {
    @Test
    fun `match rng and ticket finance commit together`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = database(context)
        seed(database)
        val initial = CareerStateFactory.create("career-atomic-finance", 919191L)
        val scheduled = ScheduledCareerMatch(
            "match-atomic-finance",
            initial.calendar.currentDayIndex,
            1,
            "home",
            "away",
        )
        val matchStore = CareerMatchStore(database)
        matchStore.initializeSchedule(initial, listOf(scheduled))

        val before = LegacyFinanceRuntimeState(
            cash = 1_000L,
            ledger = LegacyFinanceLedgerState(),
        )
        database.careerManagerRuntimeDao().upsertClubRuntime(clubRuntime(before))
        val after = before.copy(
            cash = 1_450L,
            ledger = before.ledger.copy(ticketIncome = 450),
        )
        val resolved = CareerMatchRuntimeBridge.run(initial, listOf(scheduled), scheduled.matchId) { event, random ->
            Match(event.matchId, event.homeClubId, event.awayClubId, random.nextInt(4), random.nextInt(4))
        }

        CareerMatchAtomicCommitter(database).commit(
            result = resolved,
            financeUpdate = CareerMatchFinanceUpdate("home", before, after),
        )

        assertEquals(resolved.match, matchStore.findResult(initial.id, scheduled.matchId))
        assertEquals(resolved.state.random, requireNotNull(
            com.leomala.footballdynasty.data.repository.RoomCareerStateRepository(database).findById(initial.id)
        ).random)
        assertEquals(after, CareerManagerRuntimeStore(database).clubFinanceState(initial.id, "home"))
        database.close()
        Unit
    }

    @Test
    fun `stale ticket finance rolls back score and rng`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = database(context)
        seed(database)
        val initial = CareerStateFactory.create("career-atomic-finance", 818181L)
        val scheduled = ScheduledCareerMatch(
            "match-atomic-finance",
            initial.calendar.currentDayIndex,
            1,
            "home",
            "away",
        )
        val matchStore = CareerMatchStore(database)
        matchStore.initializeSchedule(initial, listOf(scheduled))

        val persisted = LegacyFinanceRuntimeState(1_000L, LegacyFinanceLedgerState())
        database.careerManagerRuntimeDao().upsertClubRuntime(clubRuntime(persisted))
        val staleExpected = LegacyFinanceRuntimeState(999L, LegacyFinanceLedgerState())
        val resolved = CareerMatchRuntimeBridge.run(initial, listOf(scheduled), scheduled.matchId) { event, random ->
            Match(event.matchId, event.homeClubId, event.awayClubId, random.nextInt(4), random.nextInt(4))
        }

        try {
            CareerMatchAtomicCommitter(database).commit(
                result = resolved,
                financeUpdate = CareerMatchFinanceUpdate(
                    clubId = "home",
                    expectedBefore = staleExpected,
                    after = staleExpected.copy(cash = 1_200L),
                ),
            )
            fail("Expected stale finance rejection")
        } catch (_: IllegalArgumentException) {
            // Expected: the nested match writes must roll back with the rejected finance state.
        }

        assertNull(matchStore.findResult(initial.id, scheduled.matchId))
        val reopenedState = requireNotNull(
            com.leomala.footballdynasty.data.repository.RoomCareerStateRepository(database).findById(initial.id)
        )
        assertEquals(initial.random, reopenedState.random)
        assertEquals(persisted, CareerManagerRuntimeStore(database).clubFinanceState(initial.id, "home"))
        database.close()
        Unit
    }

    private fun database(context: Context): FootballDynastyDatabase =
        Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    private suspend fun seed(database: FootballDynastyDatabase) {
        database.clubDao().upsertAll(listOf(club("home", 101), club("away", 202)))
        RoomCareerRepository(database) { 10L }.save(
            Career("career-atomic-finance", "Atomic finance", null, null)
        )
    }

    private fun clubRuntime(finance: LegacyFinanceRuntimeState) = CareerClubManagerRuntimeEntity(
        careerId = "career-atomic-finance",
        clubId = "home",
        active = true,
        cash = finance.cash,
        primarySlotPlayerCode = null,
        secondarySlotPlayerCode = null,
        rawStateFlag = false,
        ticketIncome = finance.ledger.ticketIncome,
        playerSaleIncome = finance.ledger.playerSaleIncome,
        prizeIncome = finance.ledger.prizeIncome,
        sponsorIncome = finance.ledger.sponsorIncome,
        playerPurchaseExpense = finance.ledger.playerPurchaseExpense,
        stadiumExpense = finance.ledger.stadiumExpense,
        salaryExpense = finance.ledger.salaryExpense,
        borrowingChargeExpense = finance.ledger.borrowingChargeExpense,
        fineExpense = finance.ledger.fineExpense,
        miscellaneousExpense = finance.ledger.miscellaneousExpense,
        borrowed = finance.ledger.borrowed,
        monthlyBorrowingCharge = finance.ledger.monthlyBorrowingCharge,
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
