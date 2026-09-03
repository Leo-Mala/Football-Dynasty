package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.manager.LegacyFinanceLedgerState
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacyTransferClubRuntimeState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerFinanceBorrowingStoreTest {
    @Test
    fun `borrow resolves persisted division and survives reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "marco-b-borrow-reopen"
        context.deleteDatabase(name)
        var database = fileDatabase(context, name)
        materializeCareer(database, listOf(club(CLUB, "a", LEGACY_CLUB)))
        materializeFinance(
            database = database,
            clubId = CLUB,
            legacyClubCode = LEGACY_CLUB,
            finance = LegacyFinanceRuntimeState(
                cash = 100L,
                ledger = LegacyFinanceLedgerState(
                    borrowed = 1_000_000,
                    monthlyBorrowingCharge = 30_000,
                ),
            ),
        )
        CareerTicketRuntimeStore(database).materializeClubState(
            CAREER,
            CLUB,
            CareerClubTicketRuntimeState(rawDivisionCode = 4, legacyManagerId = -1),
        )

        val store = CareerFinanceBorrowingStore(database)
        val after = store.borrowFromPersistedClubState(CAREER, CLUB)
        assertEquals(500_100L, after.cash)
        assertEquals(1_500_000, after.ledger.borrowed)
        assertEquals(45_000, after.ledger.monthlyBorrowingCharge)
        assertEquals(0L, after.ledger.totalIncome())

        // Division 4 is capped at 1_500_000, so a second request is rejected without mutation.
        assertEquals(after, store.borrowFromPersistedClubState(CAREER, CLUB))

        database.close()
        database = fileDatabase(context, name)
        assertEquals(after, CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `repay uses unknown expense category miscellaneous exactly like legacy dispatcher`() = runBlocking {
        val database = inMemoryDatabase()
        materializeCareer(database, listOf(club(CLUB, "a", LEGACY_CLUB)))
        materializeFinance(
            database = database,
            clubId = CLUB,
            legacyClubCode = LEGACY_CLUB,
            finance = LegacyFinanceRuntimeState(
                cash = 600_000L,
                ledger = LegacyFinanceLedgerState(
                    miscellaneousExpense = 7,
                    borrowed = 500_000,
                    monthlyBorrowingCharge = 15_000,
                ),
            ),
        )

        val after = CareerFinanceBorrowingStore(database).repayFromPersistedClubState(CAREER, CLUB)
        assertEquals(100_000L, after.cash)
        assertEquals(0, after.ledger.borrowed)
        assertEquals(0, after.ledger.monthlyBorrowingCharge)
        assertEquals(500_007, after.ledger.miscellaneousExpense)
        database.close()
        Unit
    }

    @Test
    fun `monthly borrowing pass follows immutable club source order and can make cash negative`() = runBlocking {
        val database = inMemoryDatabase()
        val sourceFirst = club(CLUB_B, "a-source", LEGACY_CLUB_B)
        val sourceSecond = club(CLUB_A, "b-source", LEGACY_CLUB_A)
        materializeCareer(database, listOf(sourceSecond, sourceFirst))
        materializeFinance(
            database,
            CLUB_A,
            LEGACY_CLUB_A,
            LegacyFinanceRuntimeState(
                cash = 10_000L,
                ledger = LegacyFinanceLedgerState(borrowed = 500_000, monthlyBorrowingCharge = 15_000),
            ),
        )
        materializeFinance(
            database,
            CLUB_B,
            LEGACY_CLUB_B,
            LegacyFinanceRuntimeState(
                cash = 40_000L,
                ledger = LegacyFinanceLedgerState(borrowed = 1_000_000, monthlyBorrowingCharge = 30_000),
            ),
        )

        val applied = CareerFinanceBorrowingStore(database).applyMonthlyBorrowingCharges(CAREER)
        assertEquals(listOf(CLUB_B, CLUB_A), applied.map { it.first })

        val first = requireNotNull(CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB_B))
        assertEquals(10_000L, first.cash)
        assertEquals(30_000, first.ledger.borrowingChargeExpense)

        val second = requireNotNull(CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB_A))
        assertEquals(-5_000L, second.cash)
        assertEquals(15_000, second.ledger.borrowingChargeExpense)
        database.close()
        Unit
    }

    private suspend fun materializeCareer(database: FootballDynastyDatabase, clubs: List<ClubEntity>) {
        database.clubDao().upsertAll(clubs)
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Borrowing",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            )
        )
    }

    private suspend fun materializeFinance(
        database: FootballDynastyDatabase,
        clubId: String,
        legacyClubCode: Int,
        finance: LegacyFinanceRuntimeState,
    ) {
        CareerManagerRuntimeStore(database).materializeClubState(
            careerId = CAREER,
            clubId = clubId,
            transfer = LegacyTransferClubRuntimeState(
                clubCode = legacyClubCode,
                active = true,
                funds = finance.cash,
                rosterPlayerCodes = emptyList(),
                primarySlotPlayerCode = null,
                secondarySlotPlayerCode = null,
                rawStateFlag = true,
            ),
            finance = finance,
        )
    }

    private fun club(id: String, sourceFileRef: String, legacyId: Int) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = sourceFileRef,
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
        const val CAREER = "career-borrow"
        const val CLUB = "borrow-club"
        const val LEGACY_CLUB = 701
        const val CLUB_A = "club-a"
        const val CLUB_B = "club-b"
        const val LEGACY_CLUB_A = 702
        const val LEGACY_CLUB_B = 703
    }
}
