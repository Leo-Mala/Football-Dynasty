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
class CareerSponsorPaymentStoreTest {
    @Test
    fun `annual sponsor cash and ledger survive Room reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "marco-b-sponsor-reopen"
        context.deleteDatabase(name)
        var database = fileDatabase(context, name)
        materialize(database, cash = 1_000L, sponsorIncome = 20)

        val after = CareerSponsorPaymentStore(database).applyAnnualSponsor(
            careerId = CAREER,
            clubId = CLUB,
            rawCountryCode = 10,
            rawDivisionCode = 2,
            playStateChampionship = true,
            seniorSalaryCodes = listOf(100, 50),
            youthSalaryCodes = listOf(25),
            recordFinanceLedger = true,
        )
        assertEquals(5_001_560L, after.cash)
        assertEquals(5_000_020, after.ledger.sponsorIncome)

        database.close()
        database = fileDatabase(context, name)
        val reopened = requireNotNull(CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        assertEquals(after, reopened)

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `ledger-disabled club persists fixed cash credit without sponsor bucket mutation`() = runBlocking {
        val database = inMemoryDatabase()
        materialize(database, cash = 100L, sponsorIncome = 7)
        val after = CareerSponsorPaymentStore(database).applyAnnualSponsor(
            careerId = CAREER,
            clubId = CLUB,
            rawCountryCode = 29,
            rawDivisionCode = 4,
            playStateChampionship = true,
            seniorSalaryCodes = listOf(500),
            youthSalaryCodes = listOf(500),
            recordFinanceLedger = false,
        )
        assertEquals(2_500_100L, after.cash)
        assertEquals(7, after.ledger.sponsorIncome)
        assertEquals(after, CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        database.close()
        Unit
    }

    private suspend fun materialize(
        database: FootballDynastyDatabase,
        cash: Long,
        sponsorIncome: Int,
    ) {
        database.clubDao().upsertAll(listOf(club()))
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Sponsor finance",
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
                ledger = LegacyFinanceLedgerState(sponsorIncome = sponsorIncome),
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
        const val CAREER = "career-sponsor"
        const val CLUB = "sponsor-club"
        const val LEGACY_CLUB = 404
    }
}
