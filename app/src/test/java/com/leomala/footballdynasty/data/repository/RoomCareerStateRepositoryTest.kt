package com.leomala.footballdynasty.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.application.career.CareerSimulationCoordinator
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.FootballDynastyMigrations
import com.leomala.footballdynasty.data.local.entity.CareerClubManagerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.career.CareerCommand
import com.leomala.footballdynasty.domain.career.CareerFingerprint
import com.leomala.footballdynasty.domain.career.CareerSimulationEngine
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.LegacyCalendarRules
import com.leomala.footballdynasty.domain.model.Career
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomCareerStateRepositoryTest {
    private lateinit var database: FootballDynastyDatabase
    private lateinit var repository: RoomCareerStateRepository

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        RoomCareerRepository(database) { 100L }.save(
            Career(
                id = "career-core",
                displayName = "Technical Phase 4 career",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
            )
        )
        repository = RoomCareerStateRepository(database) { 200L }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `career state round trip preserves exact fingerprint`() = runBlocking {
        val initial = CareerStateFactory.create("career-core", seed = 42L)
        val saved = repository.save(initial)
        val loaded = requireNotNull(repository.findById(initial.id))

        assertEquals(initial, saved)
        assertEquals(initial, loaded)
        assertEquals(CareerFingerprint.of(initial), CareerFingerprint.of(loaded))
    }

    @Test
    fun `season transition persists exact state`() = runBlocking {
        repository.save(CareerStateFactory.create("career-core", seed = 42L))
        val coordinator = CareerSimulationCoordinator(repository)
        val transition = coordinator.apply("career-core", CareerCommand.TransitionSeason)
        val loaded = requireNotNull(repository.findById("career-core"))

        assertEquals(2, loaded.season.number)
        assertEquals(2027, loaded.season.year)
        assertEquals(LegacyCalendarRules.firstSundayOfJanuaryIndex(2027), loaded.calendar.currentDayIndex)
        assertEquals(transition.state, loaded)
        assertEquals(CareerFingerprint.of(transition.state), CareerFingerprint.of(loaded))
    }

    @Test
    fun `season transition resets every materialized finance period atomically and survives reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase15-finance-reset-reopen"
        context.deleteDatabase(name)
        var fileDatabase = fileDatabase(context, name)
        try {
            RoomCareerRepository(fileDatabase) { 100L }.save(
                Career(
                    id = PHASE15_CAREER,
                    displayName = "Phase 15 finance reset",
                    legacyMetadataFingerprint = null,
                    legacyCareerFingerprint = null,
                )
            )
            fileDatabase.clubDao().upsertAll(
                listOf(
                    club("phase15-a", 101),
                    club("phase15-b", 202),
                    club("phase15-no-ledger", 303),
                )
            )

            val initial = CareerStateFactory.create(PHASE15_CAREER, seed = 4_242L)
            val stateRepository = RoomCareerStateRepository(fileDatabase) { 200L }
            stateRepository.save(initial)

            val firstBefore = managerRuntime("phase15-a", 10)
            val secondBefore = managerRuntime("phase15-b", 30)
            val managerDao = fileDatabase.careerManagerRuntimeDao()
            managerDao.upsertClubRuntime(firstBefore)
            managerDao.upsertClubRuntime(secondBefore)

            val expectedCore = CareerSimulationEngine()
                .apply(initial, CareerCommand.TransitionSeason)
                .state
            val transition = CareerSimulationCoordinator(stateRepository)
                .apply(PHASE15_CAREER, CareerCommand.TransitionSeason)

            assertEquals(expectedCore, transition.state)
            assertEquals(initial.random, transition.state.random)
            assertEquals(resetExpected(firstBefore), requireNotNull(managerDao.findClubRuntime(PHASE15_CAREER, "phase15-a")))
            assertEquals(resetExpected(secondBefore), requireNotNull(managerDao.findClubRuntime(PHASE15_CAREER, "phase15-b")))
            assertNull(managerDao.findClubRuntime(PHASE15_CAREER, "phase15-no-ledger"))

            fileDatabase.close()
            fileDatabase = fileDatabase(context, name)

            val reopenedRepository = RoomCareerStateRepository(fileDatabase) { 300L }
            val reopenedDao = fileDatabase.careerManagerRuntimeDao()
            assertEquals(expectedCore, requireNotNull(reopenedRepository.findById(PHASE15_CAREER)))
            assertEquals(resetExpected(firstBefore), requireNotNull(reopenedDao.findClubRuntime(PHASE15_CAREER, "phase15-a")))
            assertEquals(resetExpected(secondBefore), requireNotNull(reopenedDao.findClubRuntime(PHASE15_CAREER, "phase15-b")))
            assertNull(reopenedDao.findClubRuntime(PHASE15_CAREER, "phase15-no-ledger"))
        } finally {
            fileDatabase.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun `concurrent advances are serialized without lost update`() = runBlocking {
        val initial = CareerStateFactory.create("career-core", seed = 42L)
        repository.save(initial)
        val coordinator = CareerSimulationCoordinator(repository)

        listOf(
            async { coordinator.apply("career-core", CareerCommand.AdvanceOneDay) },
            async { coordinator.apply("career-core", CareerCommand.AdvanceOneDay) },
        ).awaitAll()

        val loaded = requireNotNull(repository.findById("career-core"))
        assertEquals(initial.calendar.currentDayIndex + 2, loaded.calendar.currentDayIndex)
    }

    private fun managerRuntime(clubId: String, base: Int) = CareerClubManagerRuntimeEntity(
        careerId = PHASE15_CAREER,
        clubId = clubId,
        active = base % 20 == 10,
        cash = 1_000_000L + base,
        primarySlotPlayerCode = base + 100,
        secondarySlotPlayerCode = base + 200,
        rawStateFlag = base % 20 == 10,
        ticketIncome = base + 1,
        playerSaleIncome = (base + 2).toLong(),
        prizeIncome = base + 3,
        sponsorIncome = base + 4,
        playerPurchaseExpense = (base + 5).toLong(),
        stadiumExpense = base + 6,
        salaryExpense = (base + 7).toLong(),
        borrowingChargeExpense = base + 8,
        fineExpense = base + 9,
        miscellaneousExpense = base + 10,
        borrowed = base + 11,
        monthlyBorrowingCharge = base + 12,
    )

    private fun resetExpected(entity: CareerClubManagerRuntimeEntity) = entity.copy(
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
    )

    private fun club(id: String, legacyId: Int) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = id,
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

    private fun fileDatabase(context: Context, name: String): FootballDynastyDatabase =
        Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()

    private companion object {
        const val PHASE15_CAREER = "phase15-finance-reset"
    }
}
