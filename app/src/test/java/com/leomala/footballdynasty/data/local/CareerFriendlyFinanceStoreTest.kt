package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.manager.LegacyFinanceLedgerState
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacyFriendlySchedulingResult
import com.leomala.footballdynasty.domain.manager.LegacyFriendlySchedulingRule
import com.leomala.footballdynasty.domain.manager.LegacyTransferClubRuntimeState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerFriendlyFinanceStoreTest {
    @Test
    fun `accepted paid friendly persists exact debit and miscellaneous ledger through reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "marco-b-friendly-finance-reopen"
        context.deleteDatabase(name)
        var database = fileDatabase(context, name)
        materialize(database, cash = 600_000L, miscellaneousExpense = 12_000)

        val after = requireNotNull(
            CareerFriendlyFinanceStore(database).applyAcceptedPaidFriendly(
                careerId = CAREER,
                clubId = CLUB,
                schedulingResult = LegacyFriendlySchedulingResult(
                    rawDecisionCode = LegacyFriendlySchedulingRule.REQUIRES_PAYMENT,
                    requestedPayment = 300_000,
                ),
            )
        )
        assertEquals(300_000L, after.cash)
        assertEquals(312_000, after.ledger.miscellaneousExpense)
        assertEquals(500_000, after.ledger.borrowed)
        assertEquals(15_000, after.ledger.monthlyBorrowingCharge)

        database.close()
        database = fileDatabase(context, name)
        assertEquals(after, CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `paid friendly preserves legacy negative cash behavior`() = runBlocking {
        val database = inMemoryDatabase()
        materialize(database, cash = 100_000L, miscellaneousExpense = 1_000)

        val after = requireNotNull(
            CareerFriendlyFinanceStore(database).applyAcceptedPaidFriendly(
                careerId = CAREER,
                clubId = CLUB,
                schedulingResult = LegacyFriendlySchedulingResult(
                    rawDecisionCode = LegacyFriendlySchedulingRule.REQUIRES_PAYMENT,
                    requestedPayment = 200_000,
                ),
            )
        )
        assertEquals(-100_000L, after.cash)
        assertEquals(201_000, after.ledger.miscellaneousExpense)
        database.close()
        Unit
    }

    @Test
    fun `non payment decision leaves persisted finance untouched`() = runBlocking {
        val database = inMemoryDatabase()
        materialize(database, cash = 500_000L, miscellaneousExpense = 7_000)
        val before = requireNotNull(CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))

        val result = CareerFriendlyFinanceStore(database).applyAcceptedPaidFriendly(
            careerId = CAREER,
            clubId = CLUB,
            schedulingResult = LegacyFriendlySchedulingResult(
                rawDecisionCode = LegacyFriendlySchedulingRule.SCHEDULE_DIRECTLY,
                requestedPayment = 0,
            ),
        )
        assertNull(result)
        assertEquals(before, CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        database.close()
        Unit
    }

    private suspend fun materialize(
        database: FootballDynastyDatabase,
        cash: Long,
        miscellaneousExpense: Int,
    ) {
        database.clubDao().upsertAll(listOf(club()))
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Friendly finance",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            )
        )
        CareerManagerRuntimeStore(database).materializeClubState(
            careerId = CAREER,
            clubId = CLUB,
            transfer = LegacyTransferClubRuntimeState(
                clubCode = LEGACY_CLUB,
                active = true,
                funds = cash,
                rosterPlayerCodes = emptyList(),
                primarySlotPlayerCode = null,
                secondarySlotPlayerCode = null,
                rawStateFlag = true,
            ),
            finance = LegacyFinanceRuntimeState(
                cash = cash,
                ledger = LegacyFinanceLedgerState(
                    miscellaneousExpense = miscellaneousExpense,
                    borrowed = 500_000,
                    monthlyBorrowingCharge = 15_000,
                ),
            ),
        )
    }

    private fun club() = ClubEntity(
        id = CLUB,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = CLUB,
        name = CLUB,
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
        legacyId = LEGACY_CLUB,
        legacyValid = true,
    )

    private fun inMemoryDatabase(): FootballDynastyDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private fun fileDatabase(context: Context, name: String): FootballDynastyDatabase =
        Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()

    private companion object {
        const val CAREER = "career-friendly-finance"
        const val CLUB = "friendly-finance-club"
        const val LEGACY_CLUB = 804
    }
}
