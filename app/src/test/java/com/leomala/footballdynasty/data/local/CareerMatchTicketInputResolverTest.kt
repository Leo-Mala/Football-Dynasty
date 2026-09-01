package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerClubManagerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionEntity
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionMatchEntity
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.CareerScheduledMatchEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerMatchTicketInputResolverTest {
    @Test
    fun `persisted competition source clubs and Q0 replace caller supplied ticket fields`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            database.clubDao().upsertAll(
                listOf(
                    club("home", country = 29, reputation = 9),
                    club("away", country = 11, reputation = -2),
                )
            )
            database.careerMetadataDao().upsert(
                CareerMetadataEntity("career", 1, "Career", null, null, 1L, 1L)
            )
            database.careerScheduledMatchDao().upsert(
                CareerScheduledMatchEntity(
                    careerId = "career",
                    matchId = "match",
                    dayIndex = 5,
                    eventTypeCode = 1,
                    homeClubId = "home",
                    awayClubId = "away",
                    processed = false,
                    homeGoals = null,
                    awayGoals = null,
                )
            )
            database.careerCompetitionDao().upsertCompetition(
                CareerCompetitionEntity("career", "competition", 6, 44, 1, 1)
            )
            database.careerCompetitionDao().upsertMatches(
                listOf(CareerCompetitionMatchEntity("career", "competition", "match", 1, 0))
            )
            database.careerManagerRuntimeDao().upsertClubRuntime(
                CareerClubManagerRuntimeEntity(
                    careerId = "career",
                    clubId = "home",
                    active = true,
                    cash = 1_000L,
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

            val resolved = CareerMatchTicketInputResolver(database).resolve(
                careerId = "career",
                scheduled = ScheduledCareerMatch("match", 5, 1, "home", "away"),
                evidence = CareerMatchTicketUnpersistedEvidence(
                    homeRawO = 2,
                    homeLegacyCoachHOrNull = 73,
                    parentCompetitionIsA0 = true,
                ),
            )

            assertTrue(resolved.calculation.capacities.isEmpty())
            assertEquals(6, resolved.calculation.rawCompetitionType)
            assertEquals(2, resolved.calculation.homeRawO)
            assertEquals(5, resolved.calculation.homeRawP0)
            assertEquals(0, resolved.calculation.awayRawP0)
            assertEquals(1, resolved.calculation.homeRawJ)
            assertEquals(73, resolved.calculation.homeRegionalPercent)
            assertTrue(resolved.calculation.rawCompetitionAIsKonrentA0)
            assertTrue(resolved.homeLegacyQ0)
        } finally {
            database.close()
        }
    }

    private fun club(id: String, country: Int, reputation: Int) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = id,
        name = id,
        country = country,
        state = 0,
        level = 1,
        stadium = "",
        capacity = 10_000,
        reputation = reputation,
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
}
