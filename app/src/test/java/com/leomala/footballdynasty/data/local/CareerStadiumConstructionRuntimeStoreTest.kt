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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerStadiumConstructionRuntimeStoreTest {
    @Test
    fun `construction derives capacity and finance from Room and survives reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase13-stadium-start-reopen"
        context.deleteDatabase(name)
        var database = fileDatabase(context, name)
        materialize(database, cash = 500_000L, capacities = listOf(900, 0, 0, 0))

        val result = CareerStadiumConstructionRuntimeStore(database).startFromPersistedState(
            careerId = CAREER,
            clubId = CLUB,
            additions = listOf(100, 0, 0, 0),
            legacyJValue = 0,
            stadiumCode = 77,
            endTimestampMillis = 20_000L,
        )
        assertTrue(result.accepted)
        assertEquals(392_000L, result.state.cash)
        assertEquals(108_000, result.state.ledger.stadiumExpense)
        assertEquals(77, requireNotNull(result.recordToAppend).stadiumCode)

        database.close()
        database = fileDatabase(context, name)
        assertEquals(result.state, CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        val records = CareerManagerRuntimeStore(database).stadiumConstructionRecords(CAREER)
        assertEquals(1, records.size)
        assertEquals(listOf(100, 0, 0, 0), records.single().additions)
        assertEquals(77, records.single().stadiumCode)
        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `missing persisted stadium fails closed before finance mutation`() = runBlocking {
        val database = inMemoryDatabase()
        materializeClubAndFinance(database, cash = 500_000L)
        val before = requireNotNull(CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))

        val error = runCatching {
            CareerStadiumConstructionRuntimeStore(database).startFromPersistedState(
                careerId = CAREER,
                clubId = CLUB,
                additions = listOf(100, 0, 0, 0),
                legacyJValue = 0,
                stadiumCode = 77,
                endTimestampMillis = 20_000L,
            )
        }.exceptionOrNull()

        assertNotNull(error)
        assertEquals(before, CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        assertTrue(CareerManagerRuntimeStore(database).stadiumConstructionRecords(CAREER).isEmpty())
        database.close()
        Unit
    }

    @Test
    fun `rejected quote leaves persisted finance and construction list untouched`() = runBlocking {
        val database = inMemoryDatabase()
        materialize(database, cash = 500_000L, capacities = listOf(18_000, 0, 0, 0))
        val before = requireNotNull(CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))

        val result = CareerStadiumConstructionRuntimeStore(database).startFromPersistedState(
            careerId = CAREER,
            clubId = CLUB,
            additions = listOf(1, 0, 0, 0),
            legacyJValue = 0,
            stadiumCode = 77,
            endTimestampMillis = 20_000L,
        )

        assertFalse(result.accepted)
        assertEquals(before, CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        assertTrue(CareerManagerRuntimeStore(database).stadiumConstructionRecords(CAREER).isEmpty())
        database.close()
        Unit
    }

    private suspend fun materialize(
        database: FootballDynastyDatabase,
        cash: Long,
        capacities: List<Int>,
    ) {
        materializeClubAndFinance(database, cash)
        CareerStadiumRuntimeStore(database).materialize(
            careerId = CAREER,
            clubId = CLUB,
            state = CareerStadiumRuntimeState(capacities),
        )
    }

    private suspend fun materializeClubAndFinance(database: FootballDynastyDatabase, cash: Long) {
        database.clubDao().upsertAll(listOf(club()))
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Phase 13 stadium start",
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
                ledger = LegacyFinanceLedgerState(),
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
        capacity = 900,
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
        const val CAREER = "phase13-stadium-start"
        const val CLUB = "stadium-club"
        const val LEGACY_CLUB = 901
    }
}
