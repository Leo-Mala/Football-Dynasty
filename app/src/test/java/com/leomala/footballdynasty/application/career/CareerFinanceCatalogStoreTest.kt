package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.CareerManagerRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.manager.LegacyFinanceLedgerState
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState
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
class CareerFinanceCatalogStoreTest {
    @Test
    fun `finance projection uses persisted managed-club runtime and ledger totals`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(
                listOf(
                    club(CLUB_A, "Club A", LEGACY_CLUB_A),
                    club(CLUB_B, "Club B", LEGACY_CLUB_B),
                )
            )
            CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
                careerId = CAREER_A,
                displayName = "Career A",
                seed = 7L,
                managedClubId = CLUB_A,
            )
            val finance = LegacyFinanceRuntimeState(
                cash = 1_250_000L,
                ledger = LegacyFinanceLedgerState(
                    ticketIncome = 100,
                    sponsorIncome = 200,
                    salaryExpense = 50L,
                    borrowed = 500_000,
                    monthlyBorrowingCharge = 15_000,
                ),
            )
            CareerManagerRuntimeStore(database).materializeClubState(
                careerId = CAREER_A,
                clubId = CLUB_A,
                transfer = LegacyTransferClubRuntimeState(
                    clubCode = LEGACY_CLUB_A,
                    active = true,
                    funds = finance.cash,
                    rosterPlayerCodes = emptyList(),
                    primarySlotPlayerCode = null,
                    secondarySlotPlayerCode = null,
                    rawStateFlag = true,
                ),
                finance = finance,
            )

            val snapshot = CareerFinanceCatalogStore(database).loadFinances(CAREER_A)

            requireNotNull(snapshot)
            assertEquals(CAREER_A, snapshot.careerId)
            assertEquals(CLUB_A, snapshot.clubId)
            assertEquals("Club A", snapshot.clubName)
            assertEquals(1_250_000L, snapshot.cash)
            assertEquals(300L, snapshot.totalIncome)
            assertEquals(50L, snapshot.totalExpense)
            assertEquals(250L, snapshot.periodBalance)
            assertEquals(500_000, snapshot.borrowed)
            assertEquals(15_000, snapshot.monthlyBorrowingCharge)
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing materialized finance runtime fails closed`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A, "Club A", LEGACY_CLUB_A)))
            CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
                careerId = CAREER_A,
                displayName = "Career A",
                seed = 7L,
                managedClubId = CLUB_A,
            )

            assertNull(CareerFinanceCatalogStore(database).loadFinances(CAREER_A))
            assertNull(CareerFinanceCatalogStore(database).loadFinances("missing"))
        } finally {
            database.close()
        }
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
        const val LEGACY_CLUB_A = 701
        const val LEGACY_CLUB_B = 702
    }
}
