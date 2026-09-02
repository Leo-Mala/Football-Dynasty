package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.CareerStadiumConstructionEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.manager.LegacyFinanceLedgerState
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacyTransferClubRuntimeState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerStadiumConstructionCompletionStoreTest {
    @Test
    fun `new construction owner survives reopen and completion applies four capacities atomically`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase13-stadium-completion-reopen"
        context.deleteDatabase(name)
        var database = fileDatabase(context, name)
        materialize(database, listOf(900, 100, 200, 300))
        CareerStadiumConstructionRuntimeStore(database).startFromPersistedState(
            careerId = CAREER,
            clubId = CLUB,
            additions = listOf(100, 20, 30, 40),
            legacyJValue = 0,
            stadiumCode = 77,
            endTimestampMillis = 20_000L,
        )
        assertEquals(CLUB, database.careerManagerRuntimeDao().stadiumConstructions(CAREER).single().ownerClubId)

        database.close()
        database = fileDatabase(context, name)
        val equality = CareerStadiumConstructionCompletionStore(database).sweepAndApply(CAREER, 20_000L)
        assertTrue(equality.completed.isEmpty())
        assertEquals(listOf(900, 100, 200, 300), capacities(database))

        val completed = CareerStadiumConstructionCompletionStore(database).sweepAndApply(CAREER, 20_001L)
        assertEquals(1, completed.completed.size)
        assertEquals(listOf(1_000, 120, 230, 340), capacities(database))
        assertTrue(database.careerManagerRuntimeDao().stadiumConstructions(CAREER).isEmpty())
        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `migrated construction without owner fails closed before deletion or stadium mutation`() = runBlocking {
        val database = inMemoryDatabase()
        materialize(database, listOf(900, 100, 200, 300))
        database.careerManagerRuntimeDao().upsertStadiumConstruction(
            CareerStadiumConstructionEntity(
                careerId = CAREER,
                sourceOrdinal = 0,
                stadiumCode = 77,
                endTimestampMillis = 10L,
                addition0 = 100,
                addition1 = 20,
                addition2 = 30,
                addition3 = 40,
                ownerClubId = null,
            )
        )

        val error = runCatching {
            CareerStadiumConstructionCompletionStore(database).sweepAndApply(CAREER, 11L)
        }.exceptionOrNull()
        assertNotNull(error)
        assertEquals(listOf(900, 100, 200, 300), capacities(database))
        assertEquals(1, database.careerManagerRuntimeDao().stadiumConstructions(CAREER).size)
        database.close()
        Unit
    }

    @Test
    fun `missing owner stadium fails closed before deleting completed record`() = runBlocking {
        val database = inMemoryDatabase()
        seedClubAndFinance(database)
        database.careerManagerRuntimeDao().upsertStadiumConstruction(
            CareerStadiumConstructionEntity(
                careerId = CAREER,
                sourceOrdinal = 0,
                stadiumCode = 77,
                endTimestampMillis = 10L,
                addition0 = 100,
                addition1 = 0,
                addition2 = 0,
                addition3 = 0,
                ownerClubId = CLUB,
            )
        )
        val error = runCatching {
            CareerStadiumConstructionCompletionStore(database).sweepAndApply(CAREER, 11L)
        }.exceptionOrNull()
        assertNotNull(error)
        assertEquals(1, database.careerManagerRuntimeDao().stadiumConstructions(CAREER).size)
        database.close()
        Unit
    }

    private suspend fun materialize(database: FootballDynastyDatabase, capacities: List<Int>) {
        seedClubAndFinance(database)
        CareerStadiumRuntimeStore(database).materialize(
            CAREER,
            CLUB,
            CareerStadiumRuntimeState(capacities),
        )
    }

    private suspend fun seedClubAndFinance(database: FootballDynastyDatabase) {
        database.clubDao().upsertAll(listOf(club()))
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Phase 13 completion",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            )
        )
        CareerManagerRuntimeStore(database).materializeClubState(
            CAREER,
            CLUB,
            LegacyTransferClubRuntimeState(
                clubCode = 901,
                active = true,
                funds = 1_000_000L,
                rosterPlayerCodes = emptyList(),
                primarySlotPlayerCode = null,
                secondarySlotPlayerCode = null,
                rawStateFlag = true,
            ),
            LegacyFinanceRuntimeState(1_000_000L, LegacyFinanceLedgerState()),
        )
    }

    private suspend fun capacities(database: FootballDynastyDatabase): List<Int> {
        val state = requireNotNull(database.careerManagerRuntimeDao().findStadiumRuntime(CAREER, CLUB))
        return listOf(state.sector0Capacity, state.sector1Capacity, state.sector2Capacity, state.sector3Capacity)
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
        legacyId = 901,
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
        const val CAREER = "phase13-stadium-completion"
        const val CLUB = "stadium-club"
    }
}
