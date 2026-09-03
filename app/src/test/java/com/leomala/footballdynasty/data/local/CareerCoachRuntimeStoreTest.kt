package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.domain.manager.LegacyCoachSeasonClubRecord
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerCoachRuntimeStoreTest {
    @Test
    fun `V11 coach state duplicate records and first duplicate manager survive reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase14-coach-v11-reopen"
        context.deleteDatabase(name)
        var database = database(context, name)
        seed(database)
        var ticket = CareerTicketRuntimeStore(database)
        var store = CareerCoachRuntimeStore(database)
        ticket.materializeManagers(
            CAREER,
            listOf(
                CareerManagerTicketRuntimeState(0, 7, 61),
                CareerManagerTicketRuntimeState(1, 7, 99),
                CareerManagerTicketRuntimeState(2, 8, 80),
            ),
        )
        store.materialize(CAREER, coachState(0, 7, 61))
        store.materialize(CAREER, coachState(1, 7, 99, rawG = 91))

        val first = requireNotNull(store.resolveFirstCoachState(CAREER, 7))
        assertEquals(0, first.sourceOrdinal)
        assertEquals(61, first.rawH)
        assertEquals(listOf(10, 10), first.records.map { it.legacyClubId })
        assertNull(store.resolveFirstCoachState(CAREER, -1))
        assertNull(store.resolveFirstCoachState(CAREER, 999))

        database.close()
        database = database(context, name)
        ticket = CareerTicketRuntimeStore(database)
        store = CareerCoachRuntimeStore(database)
        assertEquals(61, ticket.resolveCoachRawH(CAREER, 7))
        assertEquals(coachState(0, 7, 61), store.find(CAREER, 0))
        assertEquals(91, requireNotNull(store.find(CAREER, 1)).rawG)

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `post match commit mutates only characterized fields updates shared H and preserves record order`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        seed(database)
        val ticket = CareerTicketRuntimeStore(database)
        val store = CareerCoachRuntimeStore(database)
        ticket.materializeManagers(CAREER, listOf(CareerManagerTicketRuntimeState(0, 7, 61)))
        val before = coachState(0, 7, 61)
        store.materialize(CAREER, before)

        val after = before.copy(
            rawG = 55,
            rawH = 64,
            rawD = 11,
            rawE = 5,
            rawO = 24,
            records = listOf(
                before.records[0].copy(rawMatches = 5, rawWins = 3, rawPoints = 13),
                before.records[1],
                LegacyCoachSeasonClubRecord(2027, 10, rawMatches = 1, rawWins = 1, rawPoints = 4),
            ),
        )
        store.commitPostMatch(CAREER, before, after)

        assertEquals(after, store.find(CAREER, 0))
        assertEquals(64, ticket.resolveCoachRawH(CAREER, 7))
        assertEquals(listOf(0, 1, 2), database.careerCoachRuntimeDao().seasonClubRecords(CAREER, 0).map { it.sourceOrdinal })
        database.close()
        Unit
    }

    @Test
    fun `post match stale state and unrelated lifecycle mutation fail closed`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        seed(database)
        val ticket = CareerTicketRuntimeStore(database)
        val store = CareerCoachRuntimeStore(database)
        ticket.materializeManagers(CAREER, listOf(CareerManagerTicketRuntimeState(0, 7, 61)))
        val before = coachState(0, 7, 61)
        store.materialize(CAREER, before)

        assertNotNull(
            runCatching {
                store.commitPostMatch(CAREER, before.copy(rawG = 99), before.copy(rawG = 70))
            }.exceptionOrNull()
        )
        assertNotNull(
            runCatching {
                store.commitPostMatch(CAREER, before, before.copy(currentClubId = "other", rawG = 70))
            }.exceptionOrNull()
        )
        assertEquals(before, store.find(CAREER, 0))
        database.close()
        Unit
    }

    @Test
    fun `materialization requires exact parent identity and H and missing V11 state is blocker`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        seed(database)
        val ticket = CareerTicketRuntimeStore(database)
        val store = CareerCoachRuntimeStore(database)
        ticket.materializeManagers(CAREER, listOf(CareerManagerTicketRuntimeState(0, 7, 61)))

        assertNotNull(runCatching { store.materialize(CAREER, coachState(0, 8, 61)) }.exceptionOrNull())
        assertNotNull(runCatching { store.materialize(CAREER, coachState(0, 7, 62)) }.exceptionOrNull())
        assertNotNull(runCatching { store.resolveFirstCoachState(CAREER, 7) }.exceptionOrNull())
        database.close()
        Unit
    }

    private fun database(context: Context, name: String): FootballDynastyDatabase =
        Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()

    private suspend fun seed(database: FootballDynastyDatabase) {
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Coach V11",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            )
        )
    }

    private fun coachState(
        ordinal: Int,
        managerId: Int,
        rawH: Int,
        rawG: Int = 60,
    ) = CareerCoachRuntimeState(
        sourceOrdinal = ordinal,
        legacyManagerId = managerId,
        isUserControlled = true,
        currentClubId = "club-a",
        alternativeClubId = "selection-a",
        previousClubId = "club-old",
        previousClubCountry = 29,
        previousClubDivisionIndex = 1,
        rawG = rawG,
        rawH = rawH,
        rawD = 10,
        rawE = 4,
        rawF = 3,
        rawO = 20,
        rawM = 0,
        records = listOf(
            LegacyCoachSeasonClubRecord(2026, 10, rawMatches = 4, rawWins = 2, rawLosses = 1, rawPoints = 9),
            LegacyCoachSeasonClubRecord(2026, 10, rawMatches = 100, rawWins = 100, rawLosses = 100, rawPoints = 100),
        ),
    )

    private companion object {
        const val CAREER = "career-coach-v11"
    }
}
