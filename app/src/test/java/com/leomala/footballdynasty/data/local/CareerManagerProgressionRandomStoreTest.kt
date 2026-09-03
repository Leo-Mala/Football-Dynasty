package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.domain.career.CareerRandomState
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.LegacyCalendarRules
import com.leomala.footballdynasty.domain.career.SeasonState
import com.leomala.footballdynasty.domain.manager.LegacyPostDismissalContinuationRule
import com.leomala.footballdynasty.domain.manager.LegacyReplacementManagerCandidate
import com.leomala.footballdynasty.domain.manager.LegacyReplacementManagerCandidateRule
import com.leomala.footballdynasty.domain.manager.LegacyReplacementTargetClub
import com.leomala.footballdynasty.foundation.random.StatefulJavaRandomSource
import com.leomala.footballdynasty.foundation.random.StatefulRandomSnapshot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerManagerProgressionRandomStoreTest {
    @Test
    fun `post dismissal draw advances persisted career RNG and survives reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase14-manager-progression-rng"
        context.deleteDatabase(name)
        var database = database(context, name)
        val before = careerState(seed = 7_701L)
        seedCareer(database, before)

        val expectedRandom = restore(before.random)
        val expectedDraw = expectedRandom.nextInt(100)
        val expectedSnapshot = expectedRandom.snapshot()

        val result = CareerManagerProgressionRandomStore(database, clockMillis = { 22L }).run(before) { random ->
            LegacyPostDismissalContinuationRule.execute(
                random = random,
                p0 = { 1 },
                o0 = { emptyList<Any>() },
                runD4 = {},
                runG4 = {},
                g0 = { emptyList<Any>() },
                runE4 = {},
                setJ2 = {},
                setF2 = {},
                worldV0 = { false },
                worldE1 = { false },
                runWorldF = {},
                dispatchContinuationI = {},
                openEndYear = {},
            )
        }

        assertEquals(expectedDraw, result.value.g4GateDraw)
        assertEquals(expectedSnapshot.toCareerRandomState(), result.stateAfter.random)
        assertEquals(
            result.stateAfter,
            CareerCoreStateRoomAdapter.state(requireNotNull(database.careerCoreStateDao().findById(CAREER))),
        )

        database.close()
        database = database(context, name)
        assertEquals(
            result.stateAfter,
            CareerCoreStateRoomAdapter.state(requireNotNull(database.careerCoreStateDao().findById(CAREER))),
        )
        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `replacement candidate selection continues the exact persisted random stream`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val before = careerState(seed = 9_911L)
        seedCareer(database, before)
        val candidates = listOf(
            candidate("a", sortV = 5, sortH = 30, sortS = 3),
            candidate("b", sortV = 8, sortH = 20, sortS = 2),
            candidate("c", sortV = 7, sortH = 40, sortS = 1),
        )
        val target = LegacyReplacementTargetClub(countryCode = 1, levelCode = 3)

        val expectedRandom = restore(before.random)
        val expectedSelected = LegacyReplacementManagerCandidateRule.select(
            managers = candidates,
            targetClub = target,
            mode = 2,
            random = expectedRandom,
        )
        val expectedSnapshot = expectedRandom.snapshot()

        val result = CareerManagerProgressionRandomStore(database).run(before) { random ->
            LegacyReplacementManagerCandidateRule.select(
                managers = candidates,
                targetClub = target,
                mode = 2,
                random = random,
            )
        }

        assertEquals(expectedSelected, result.value)
        assertEquals(expectedSnapshot.toCareerRandomState(), result.stateAfter.random)
        database.close()
        Unit
    }

    @Test
    fun `failed nested manager mutation rolls back both RNG and manager H`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val before = careerState(seed = 5_101L)
        seedCareer(database, before)
        val tickets = CareerTicketRuntimeStore(database)
        tickets.materializeManagers(
            CAREER,
            listOf(CareerManagerTicketRuntimeState(0, MANAGER_ID, 61)),
        )

        val error = runCatching {
            CareerManagerProgressionRandomStore(database).run<Unit>(before) { random ->
                random.nextInt(100)
                tickets.applyCoachAnnualRecovery(CAREER, MANAGER_ID)
                error("abort progression")
            }
        }.exceptionOrNull()

        assertNotNull(error)
        assertEquals(
            before,
            CareerCoreStateRoomAdapter.state(requireNotNull(database.careerCoreStateDao().findById(CAREER))),
        )
        assertEquals(61, tickets.resolveCoachRawH(CAREER, MANAGER_ID))
        database.close()
        Unit
    }

    @Test
    fun `manager rematerialization cannot cascade-delete V11 coach state`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val before = careerState(seed = 4_401L)
        seedCareer(database, before)
        val tickets = CareerTicketRuntimeStore(database)
        val coaches = CareerCoachRuntimeStore(database)
        val managerParents = listOf(CareerManagerTicketRuntimeState(0, MANAGER_ID, 61))
        tickets.materializeManagers(CAREER, managerParents)
        val coach = CareerCoachRuntimeState(
            sourceOrdinal = 0,
            legacyManagerId = MANAGER_ID,
            isUserControlled = true,
            currentClubId = null,
            alternativeClubId = null,
            previousClubId = null,
            previousClubCountry = null,
            previousClubDivisionIndex = null,
            rawG = 70,
            rawH = 61,
            rawD = 0,
            rawE = 0,
            rawF = 0,
            rawO = 0,
            rawM = 0,
            records = emptyList(),
        )
        coaches.materialize(CAREER, coach)

        tickets.materializeManagers(CAREER, managerParents)
        assertEquals(coach, coaches.find(CAREER, 0))

        val error = runCatching {
            tickets.materializeManagers(
                CAREER,
                listOf(CareerManagerTicketRuntimeState(0, MANAGER_ID, 62)),
            )
        }.exceptionOrNull()
        assertNotNull(error)
        assertEquals(coach, coaches.find(CAREER, 0))
        assertEquals(61, tickets.resolveCoachRawH(CAREER, MANAGER_ID))
        database.close()
        Unit
    }

    private fun database(context: Context, name: String): FootballDynastyDatabase =
        Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()

    private suspend fun seedCareer(database: FootballDynastyDatabase, state: CareerState) {
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Phase 14 progression RNG",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            )
        )
        database.careerCoreStateDao().upsert(CareerCoreStateRoomAdapter.entity(state, 1L))
    }

    private fun careerState(seed: Long): CareerState {
        val snapshot = StatefulJavaRandomSource(seed).snapshot()
        val seasonNumber = 1
        val seasonYear = LegacyCalendarRules.seasonYear(seasonNumber)
        return CareerState(
            id = CAREER,
            season = SeasonState(number = seasonNumber, year = seasonYear),
            calendar = LegacyCalendarRules.calendarForSeason(seasonNumber),
            managedClub = null,
            random = snapshot.toCareerRandomState(),
        )
    }

    private fun restore(state: CareerRandomState) = StatefulJavaRandomSource.restore(
        StatefulRandomSnapshot(
            initialSeed = state.initialSeed,
            internalState = state.internalState,
            draws = state.draws,
        )
    )

    private fun StatefulRandomSnapshot.toCareerRandomState() = CareerRandomState(
        initialSeed = initialSeed,
        internalState = internalState,
        draws = draws,
    )

    private fun candidate(
        value: String,
        sortV: Int,
        sortH: Int,
        sortS: Int,
    ) = LegacyReplacementManagerCandidate(
        value = value,
        currentClubId = null,
        userControlled = false,
        primaryCountryCode = 1,
        secondaryCountryCode = 0,
        levelCode = 3,
        sortV = sortV,
        sortH = sortH,
        sortS = sortS,
    )

    private companion object {
        const val CAREER = "career-phase14-rng"
        const val MANAGER_ID = 7
    }
}
