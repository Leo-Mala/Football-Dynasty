package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.CareerClubTicketRuntimeState
import com.leomala.footballdynasty.data.local.CareerManagerRuntimeStore
import com.leomala.footballdynasty.data.local.CareerTicketRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.FootballDynastyMigrations
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.manager.LegacyFinanceLedgerState
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacyTransferClubRuntimeState
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
class CareerFinanceCommandStoreTest {
    @Test
    fun `borrow resolves persisted managed club and reports legacy acceptance then rejection`() = runBlocking {
        val database = inMemoryDatabase()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A, "Club A", LEGACY_CLUB_A)))
            createCareer(database, CAREER_A, CLUB_A)
            materializeFinance(
                database,
                CAREER_A,
                CLUB_A,
                LEGACY_CLUB_A,
                LegacyFinanceRuntimeState(
                    cash = 100L,
                    ledger = LegacyFinanceLedgerState(
                        borrowed = 1_000_000,
                        monthlyBorrowingCharge = 30_000,
                    ),
                ),
            )
            materializeTicket(database, CAREER_A, CLUB_A, rawDivisionCode = 4)

            val store = CareerFinanceCommandStore(database)
            val accepted = requireNotNull(store.borrow(CAREER_A))
            assertTrue(accepted.accepted)
            assertEquals(500_100L, accepted.snapshot.cash)
            assertEquals(1_500_000, accepted.snapshot.borrowed)
            assertEquals(45_000, accepted.snapshot.monthlyBorrowingCharge)
            assertEquals(0L, accepted.snapshot.totalIncome)

            val rejected = requireNotNull(store.borrow(CAREER_A))
            assertFalse(rejected.accepted)
            assertEquals(accepted.snapshot, rejected.snapshot)
        } finally {
            database.close()
        }
    }

    @Test
    fun `repay reports acceptance then rejection and preserves legacy miscellaneous expense routing`() = runBlocking {
        val database = inMemoryDatabase()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A, "Club A", LEGACY_CLUB_A)))
            createCareer(database, CAREER_A, CLUB_A)
            materializeFinance(
                database,
                CAREER_A,
                CLUB_A,
                LEGACY_CLUB_A,
                LegacyFinanceRuntimeState(
                    cash = 600_000L,
                    ledger = LegacyFinanceLedgerState(
                        miscellaneousExpense = 7,
                        borrowed = 500_000,
                        monthlyBorrowingCharge = 15_000,
                    ),
                ),
            )

            val store = CareerFinanceCommandStore(database)
            val accepted = requireNotNull(store.repay(CAREER_A))
            assertTrue(accepted.accepted)
            assertEquals(100_000L, accepted.snapshot.cash)
            assertEquals(0, accepted.snapshot.borrowed)
            assertEquals(0, accepted.snapshot.monthlyBorrowingCharge)
            assertEquals(500_007L, accepted.snapshot.totalExpense)

            val rejected = requireNotNull(store.repay(CAREER_A))
            assertFalse(rejected.accepted)
            assertEquals(accepted.snapshot, rejected.snapshot)
        } finally {
            database.close()
        }
    }

    @Test
    fun `accepted command survives database reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase17-finance-command-reopen"
        context.deleteDatabase(name)
        var database = fileDatabase(context, name)
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A, "Club A", LEGACY_CLUB_A)))
            createCareer(database, CAREER_A, CLUB_A)
            materializeFinance(
                database,
                CAREER_A,
                CLUB_A,
                LEGACY_CLUB_A,
                LegacyFinanceRuntimeState(cash = 100L, ledger = LegacyFinanceLedgerState()),
            )
            materializeTicket(database, CAREER_A, CLUB_A, rawDivisionCode = 4)

            val result = requireNotNull(CareerFinanceCommandStore(database).borrow(CAREER_A))
            assertTrue(result.accepted)
            database.close()

            database = fileDatabase(context, name)
            assertEquals(result.snapshot, CareerFinanceCatalogStore(database).loadFinances(CAREER_A))
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun `command is isolated between careers`() = runBlocking {
        val database = inMemoryDatabase()
        try {
            database.clubDao().upsertAll(
                listOf(
                    club(CLUB_A, "Club A", LEGACY_CLUB_A),
                    club(CLUB_B, "Club B", LEGACY_CLUB_B),
                )
            )
            createCareer(database, CAREER_A, CLUB_A)
            createCareer(database, CAREER_B, CLUB_B)
            materializeFinance(
                database,
                CAREER_A,
                CLUB_A,
                LEGACY_CLUB_A,
                LegacyFinanceRuntimeState(cash = 100L, ledger = LegacyFinanceLedgerState()),
            )
            materializeFinance(
                database,
                CAREER_B,
                CLUB_B,
                LEGACY_CLUB_B,
                LegacyFinanceRuntimeState(cash = 900L, ledger = LegacyFinanceLedgerState()),
            )
            materializeTicket(database, CAREER_A, CLUB_A, rawDivisionCode = 4)
            materializeTicket(database, CAREER_B, CLUB_B, rawDivisionCode = 4)

            val beforeB = requireNotNull(CareerFinanceCatalogStore(database).loadFinances(CAREER_B))
            val resultA = requireNotNull(CareerFinanceCommandStore(database).borrow(CAREER_A))
            assertTrue(resultA.accepted)
            assertEquals(beforeB, CareerFinanceCatalogStore(database).loadFinances(CAREER_B))
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing managed-club finance state fails closed`() = runBlocking {
        val database = inMemoryDatabase()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A, "Club A", LEGACY_CLUB_A)))
            createCareer(database, CAREER_A, CLUB_A)

            val store = CareerFinanceCommandStore(database)
            assertNull(store.borrow(CAREER_A))
            assertNull(store.repay(CAREER_A))
            assertNull(CareerManagerRuntimeStore(database).clubFinanceState(CAREER_A, CLUB_A))
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing persisted division fails closed without partial mutation`() = runBlocking {
        val database = inMemoryDatabase()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A, "Club A", LEGACY_CLUB_A)))
            createCareer(database, CAREER_A, CLUB_A)
            val finance = LegacyFinanceRuntimeState(cash = 100L, ledger = LegacyFinanceLedgerState())
            materializeFinance(database, CAREER_A, CLUB_A, LEGACY_CLUB_A, finance)

            assertNull(CareerFinanceCommandStore(database).borrow(CAREER_A))
            assertEquals(finance, CareerManagerRuntimeStore(database).clubFinanceState(CAREER_A, CLUB_A))
        } finally {
            database.close()
        }
    }

    @Test
    fun `command never falls through from managed club to another materialized club`() = runBlocking {
        val database = inMemoryDatabase()
        try {
            database.clubDao().upsertAll(
                listOf(
                    club(CLUB_A, "Club A", LEGACY_CLUB_A),
                    club(CLUB_B, "Club B", LEGACY_CLUB_B),
                )
            )
            createCareer(database, CAREER_A, CLUB_A)
            val otherFinance = LegacyFinanceRuntimeState(cash = 321L, ledger = LegacyFinanceLedgerState())
            materializeFinance(database, CAREER_A, CLUB_B, LEGACY_CLUB_B, otherFinance)
            materializeTicket(database, CAREER_A, CLUB_B, rawDivisionCode = 4)

            assertNull(CareerFinanceCommandStore(database).borrow(CAREER_A))
            assertEquals(otherFinance, CareerManagerRuntimeStore(database).clubFinanceState(CAREER_A, CLUB_B))
            assertNull(CareerManagerRuntimeStore(database).clubFinanceState(CAREER_A, CLUB_A))
        } finally {
            database.close()
        }
    }

    private suspend fun createCareer(
        database: FootballDynastyDatabase,
        careerId: String,
        managedClubId: String,
    ) {
        CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
            careerId = careerId,
            displayName = careerId,
            seed = 7L,
            managedClubId = managedClubId,
        )
    }

    private suspend fun materializeFinance(
        database: FootballDynastyDatabase,
        careerId: String,
        clubId: String,
        legacyClubCode: Int,
        finance: LegacyFinanceRuntimeState,
    ) {
        CareerManagerRuntimeStore(database).materializeClubState(
            careerId = careerId,
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

    private suspend fun materializeTicket(
        database: FootballDynastyDatabase,
        careerId: String,
        clubId: String,
        rawDivisionCode: Int,
    ) {
        CareerTicketRuntimeStore(database).materializeClubState(
            careerId = careerId,
            clubId = clubId,
            state = CareerClubTicketRuntimeState(rawDivisionCode = rawDivisionCode, legacyManagerId = -1),
        )
    }

    private fun club(id: String, name: String, legacyId: Int) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = "teams/$id.ban",
        name = name,
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
        const val CAREER_A = "career-a"
        const val CAREER_B = "career-b"
        const val CLUB_A = "club-a"
        const val CLUB_B = "club-b"
        const val LEGACY_CLUB_A = 701
        const val LEGACY_CLUB_B = 702
    }
}
