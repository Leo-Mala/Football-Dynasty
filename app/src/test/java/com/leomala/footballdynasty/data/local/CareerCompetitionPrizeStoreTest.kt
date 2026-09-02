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
class CareerCompetitionPrizeStoreTest {
    @Test
    fun `resolved winner prize survives Room reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "marco-b-prize-reopen"
        context.deleteDatabase(name)
        var database = fileDatabase(context, name)
        materialize(database, cash = 1_000L, prizeIncome = 25, active = true)

        val after = CareerCompetitionPrizeStore(database).applyResolvedWinnerPrize(
            careerId = CAREER,
            winnerClubId = CLUB,
            rawCompetitionType = 4,
            rawStageIndex = 3,
            rawCompetitionI0 = 0,
            rawCompetitionPCode = -1,
        )
        assertEquals(5_001_000L, after.cash)
        assertEquals(5_000_025, after.ledger.prizeIncome)

        database.close()
        database = fileDatabase(context, name)
        val reopened = requireNotNull(CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        assertEquals(after, reopened)

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `persisted winner without legacy Q0 receives neither cash nor prize ledger`() = runBlocking {
        val database = inMemoryDatabase()
        materialize(database, cash = 100L, prizeIncome = 8, active = false)
        val before = requireNotNull(CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        val after = CareerCompetitionPrizeStore(database).applyResolvedWinnerPrize(
            careerId = CAREER,
            winnerClubId = CLUB,
            rawCompetitionType = 8,
            rawStageIndex = 0,
            rawCompetitionI0 = 0,
            rawCompetitionPCode = 0,
        )
        assertEquals(before, after)
        assertEquals(before, CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        database.close()
        Unit
    }

    @Test(expected = IllegalArgumentException::class)
    fun `missing persisted winner runtime fails closed instead of accepting caller eligibility`() = runBlocking {
        val database = inMemoryDatabase()
        database.clubDao().upsertAll(listOf(club()))
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Competition prize",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            )
        )

        CareerCompetitionPrizeStore(database).applyResolvedWinnerPrize(
            careerId = CAREER,
            winnerClubId = CLUB,
            rawCompetitionType = 4,
            rawStageIndex = 3,
            rawCompetitionI0 = 0,
            rawCompetitionPCode = -1,
        )
    }

    private suspend fun materialize(
        database: FootballDynastyDatabase,
        cash: Long,
        prizeIncome: Int,
        active: Boolean,
    ) {
        database.clubDao().upsertAll(listOf(club()))
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Competition prize",
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
                active = active,
                funds = cash,
                rosterPlayerCodes = emptyList(),
                primarySlotPlayerCode = null,
                secondarySlotPlayerCode = null,
                rawStateFlag = true,
            ),
            finance = LegacyFinanceRuntimeState(
                cash = cash,
                ledger = LegacyFinanceLedgerState(prizeIncome = prizeIncome),
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
        const val CAREER = "career-prize"
        const val CLUB = "prize-club"
        const val LEGACY_CLUB = 505
    }
}
