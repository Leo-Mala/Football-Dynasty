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
class CareerSalaryCalendarStoreTest {
    @Test
    fun `day two salary event debits senior plus youth payroll and survives reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "marco-b-salary-calendar-reopen"
        context.deleteDatabase(name)
        var database = fileDatabase(context, name)
        materialize(database, cash = 1_000L, existingSalaryExpense = 50L)
        var store = CareerSalaryCalendarStore(database)

        val after = store.applyCalendarDay(
            careerId = CAREER,
            clubId = CLUB,
            useDayOfMonthTwoSchedule = true,
            dayOfMonth = 2,
            dayOfWeek = 4,
            currentMonthCode = 7,
            participatingCalendarMonthCodes = listOf(1, 7, 10),
            seniorSalaryCodes = listOf(300, 200),
            youthSalaryCodes = listOf(100),
        )
        assertEquals(400L, after.cash)
        assertEquals(650L, after.ledger.salaryExpense)

        database.close()
        database = fileDatabase(context, name)
        store = CareerSalaryCalendarStore(database)
        val reopened = requireNotNull(CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        assertEquals(400L, reopened.cash)
        assertEquals(650L, reopened.ledger.salaryExpense)

        // Sunday is not a second salary event while the raw flag uses day-of-month two scheduling.
        val unchanged = store.applyCalendarDay(
            careerId = CAREER,
            clubId = CLUB,
            useDayOfMonthTwoSchedule = true,
            dayOfMonth = 9,
            dayOfWeek = 1,
            currentMonthCode = 7,
            participatingCalendarMonthCodes = listOf(7),
            seniorSalaryCodes = listOf(999),
            youthSalaryCodes = listOf(999),
        )
        assertEquals(reopened, unchanged)

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `Sunday schedule charges when raw flag is false and can make cash negative`() = runBlocking {
        val database = inMemoryDatabase()
        materialize(database, cash = 100L, existingSalaryExpense = 0L)
        val after = CareerSalaryCalendarStore(database).applyCalendarDay(
            careerId = CAREER,
            clubId = CLUB,
            useDayOfMonthTwoSchedule = false,
            dayOfMonth = 10,
            dayOfWeek = 1,
            currentMonthCode = 2,
            participatingCalendarMonthCodes = listOf(2),
            seniorSalaryCodes = listOf(150),
            youthSalaryCodes = listOf(25),
        )
        assertEquals(-75L, after.cash)
        assertEquals(175L, after.ledger.salaryExpense)
        database.close()
        Unit
    }

    @Test
    fun `club without a participating event in current month is not charged`() = runBlocking {
        val database = inMemoryDatabase()
        materialize(database, cash = 500L, existingSalaryExpense = 10L)
        val before = requireNotNull(CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        val after = CareerSalaryCalendarStore(database).applyCalendarDay(
            careerId = CAREER,
            clubId = CLUB,
            useDayOfMonthTwoSchedule = true,
            dayOfMonth = 2,
            dayOfWeek = 2,
            currentMonthCode = 5,
            participatingCalendarMonthCodes = listOf(4, 6),
            seniorSalaryCodes = listOf(300),
            youthSalaryCodes = listOf(200),
        )
        assertEquals(before, after)
        assertEquals(before, CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        database.close()
        Unit
    }

    private suspend fun materialize(
        database: FootballDynastyDatabase,
        cash: Long,
        existingSalaryExpense: Long,
    ) {
        database.clubDao().upsertAll(listOf(club()))
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Salary calendar",
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
                ledger = LegacyFinanceLedgerState(salaryExpense = existingSalaryExpense),
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
        const val CAREER = "career-salary-calendar"
        const val CLUB = "salary-club"
        const val LEGACY_CLUB = 303
    }
}
