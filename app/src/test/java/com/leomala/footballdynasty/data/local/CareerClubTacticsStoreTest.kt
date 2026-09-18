package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerClubManagerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.manager.LegacyTacticsMatchRuntimeRule
import com.leomala.footballdynasty.domain.manager.LegacyTacticsRawState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerClubTacticsStoreTest {
    @Test
    fun `raw tactics save and load preserve all serialized S slots and T`() = runBlocking {
        val database = database()
        try {
            seed(database)
            val store = CareerClubTacticsStore(database)
            val raw = LegacyTacticsRawState(
                optionSlots = listOf(7, 2, 9, -4),
                checkboxT = true,
            )

            assertEquals(raw, store.save(CAREER, CLUB, raw))
            assertEquals(raw, store.load(CAREER, CLUB))
            assertEquals(9, LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(requireNotNull(store.load(CAREER, CLUB))))
        } finally {
            database.close()
        }
    }

    @Test
    fun `new manager runtime exposes exact legacy constructor tactics`() = runBlocking {
        val database = database()
        try {
            seed(database)
            val raw = requireNotNull(CareerClubTacticsStore(database).load(CAREER, CLUB))
            assertEquals(LegacyTacticsMatchRuntimeRule.constructorInitialState(), raw)
            assertEquals(0, LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(raw))
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing club runtime stays absent instead of synthesizing tactics`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club()))
            database.careerMetadataDao().upsert(metadata())
            assertNull(CareerClubTacticsStore(database).load(CAREER, CLUB))
        } finally {
            database.close()
        }
    }

    private suspend fun seed(database: FootballDynastyDatabase) {
        database.clubDao().upsertAll(listOf(club()))
        database.careerMetadataDao().upsert(metadata())
        database.careerManagerRuntimeDao().upsertClubRuntime(
            CareerClubManagerRuntimeEntity(
                careerId = CAREER,
                clubId = CLUB,
                active = true,
                cash = 123L,
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
        )
    }

    private fun metadata() = CareerMetadataEntity(
        id = CAREER,
        dataVersion = 1,
        displayName = "Tactics",
        legacyMetadataFingerprint = null,
        legacyCareerFingerprint = null,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L,
    )

    private fun club() = ClubEntity(
        id = CLUB,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = "teams/tactics.ban",
        name = "Tactics Club",
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
        const val CAREER = "career-tactics"
        const val CLUB = "club-tactics"
    }
}
