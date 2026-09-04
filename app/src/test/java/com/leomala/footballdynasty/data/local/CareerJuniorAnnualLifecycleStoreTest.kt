package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerJuniorDraftEntity
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.career.CareerRandomState
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.LegacyCalendarRules
import com.leomala.footballdynasty.domain.career.SeasonState
import com.leomala.footballdynasty.foundation.random.StatefulJavaRandomSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerJuniorAnnualLifecycleStoreTest {
    @Test
    fun `annual promotion removes original only after snapshot and persists immediate replacement with RNG`() = runBlocking {
        val database = database()
        val before = careerState(701L)
        seed(database, before)
        database.careerJuniorDraftDao().upsert(draftEntity(draft(age = 19, name = "Original"), 0))

        var replacementCalls = 0
        val result = CareerJuniorAnnualLifecycleStore(database, clockMillis = { 44L }).run(
            expectedBefore = before,
            clubs = listOf(target()),
            refreshDraft = { current, _ -> current },
            generateReplacement = { clubId, random ->
                assertEquals(CLUB, clubId)
                replacementCalls++
                val marker = random.nextInt(1000)
                draft(age = 17, name = "Replacement-$marker")
            },
        )

        assertEquals(1, replacementCalls)
        assertEquals(1, result.promoted.size)
        val drafts = database.careerJuniorDraftDao().listForClub(CAREER, CLUB)
        assertEquals(1, drafts.size)
        assertEquals(1, drafts.single().sourceOrdinal)
        assertEquals(true, drafts.single().name.startsWith("Replacement-"))
        assertEquals(17, drafts.single().legacyC)
        val promoted = result.promoted.single()
        assertEquals(SENIOR, promoted.membership?.rosterKind)
        assertNotNull(database.careerPlayerRuntimeDao().findRuntime(CAREER, promoted.runtime.playerId))
        assertNotNull(database.careerPlayerRuntimeDao().findMembership(CAREER, promoted.runtime.playerId))
        assertEquals(
            result.stateAfter,
            CareerCoreStateRoomAdapter.state(requireNotNull(database.careerCoreStateDao().findById(CAREER))),
        )
        assertEquals(true, result.stateAfter.random.draws > before.random.draws)
        database.close()
    }

    @Test
    fun `annual lifecycle failure after materialization rolls back draft player membership and RNG`() = runBlocking {
        val database = database()
        val before = careerState(702L)
        seed(database, before)
        val original = draftEntity(draft(age = 19, name = "Rollback"), 0)
        database.careerJuniorDraftDao().upsert(original)

        val error = runCatching {
            CareerJuniorAnnualLifecycleStore(database).run(
                expectedBefore = before,
                clubs = listOf(target(currentGameEpochMillis = Long.MAX_VALUE)),
                refreshDraft = { current, _ -> current },
                generateReplacement = { _, _ -> draft(age = 17, name = "Should rollback") },
            )
        }.exceptionOrNull()

        assertNotNull(error)
        assertEquals(listOf(original), database.careerJuniorDraftDao().listForClub(CAREER, CLUB))
        assertEquals(emptyList<Any>(), database.careerPlayerRuntimeDao().runtimeForCareer(CAREER))
        assertEquals(emptyList<Any>(), database.careerPlayerRuntimeDao().proceduralPlayersForCareer(CAREER))
        assertEquals(emptyList<Any>(), database.careerPlayerRuntimeDao().membershipsForCareer(CAREER))
        assertEquals(
            before,
            CareerCoreStateRoomAdapter.state(requireNotNull(database.careerCoreStateDao().findById(CAREER))),
        )
        database.close()
    }

    private fun database(): FootballDynastyDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private suspend fun seed(database: FootballDynastyDatabase, state: CareerState) {
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Phase 15 annual junior lifecycle",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            )
        )
        database.clubDao().upsertAll(listOf(club()))
        database.careerCoreStateDao().upsert(CareerCoreStateRoomAdapter.entity(state, 1L))
    }

    private fun careerState(seed: Long): CareerState {
        val snapshot = StatefulJavaRandomSource(seed).snapshot()
        return CareerState(
            id = CAREER,
            season = SeasonState(1, LegacyCalendarRules.seasonYear(1)),
            calendar = LegacyCalendarRules.calendarForSeason(1),
            managedClub = null,
            random = CareerRandomState(snapshot.initialSeed, snapshot.internalState, snapshot.draws),
        )
    }

    private fun target(currentGameEpochMillis: Long = 1_000L) = CareerJuniorAnnualClubTarget(
        clubId = CLUB,
        rosterKind = SENIOR,
        legacyR0 = false,
        legacyO = 3,
        legacyP0 = 4,
        legacyJ = 2,
        clubLevel = 20,
        currentYear = 2026,
        currentGameEpochMillis = currentGameEpochMillis,
        clubP0 = 0,
        clubB0 = 0,
        clubQ0 = true,
        seniorPositionCounts = listOf(0, 0, 0, 0, 0),
    )

    private fun draft(age: Int, name: String) = CareerJuniorDraftState(
        legacyN = 6,
        legacyB = false,
        legacyC = age,
        legacyE = 0,
        legacyJ = 4,
        legacyL = 11,
        legacyD = 29,
        name = name,
        legacyG = 1,
        legacyF = 10,
        legacyO = 50,
        legacyM = 6,
        legacyH = 80_000,
        legacyI = 650,
        developmentRemainder = 0.4,
    )

    private fun draftEntity(draft: CareerJuniorDraftState, ordinal: Int) = CareerJuniorDraftEntity(
        careerId = CAREER,
        clubId = CLUB,
        sourceOrdinal = ordinal,
        legacyN = draft.legacyN,
        legacyB = draft.legacyB,
        legacyC = draft.legacyC,
        legacyE = draft.legacyE,
        legacyJ = draft.legacyJ,
        legacyL = draft.legacyL,
        legacyD = draft.legacyD,
        name = draft.name,
        legacyG = draft.legacyG,
        legacyF = draft.legacyF,
        legacyO = draft.legacyO,
        legacyM = draft.legacyM,
        legacyH = draft.legacyH,
        legacyI = draft.legacyI,
        developmentRemainder = draft.developmentRemainder,
    )

    private fun club() = ClubEntity(
        id = CLUB,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = "fixture.ban",
        name = "Fixture",
        country = 29,
        state = 0,
        level = 20,
        stadium = "Fixture",
        capacity = 1,
        reputation = 1,
        primaryColor = "000000",
        secondaryColor = "ffffff",
        coach = "Fixture",
        coachCountry = 29,
        baseColor = 0,
        legacyAid = 0,
        legacySid = 0,
        legacyTid = 0,
        legacyVid = 0,
        legacyId = 1,
        legacyValid = true,
    )

    private companion object {
        const val CAREER = "career-phase15-annual-juniors"
        const val CLUB = "club-phase15-annual-juniors"
        const val SENIOR = "SENIOR"
    }
}
