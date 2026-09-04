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
import com.leomala.footballdynasty.domain.career.LegacyProceduralMaterializationRules
import com.leomala.footballdynasty.domain.career.LegacyProceduralPlayerRules
import com.leomala.footballdynasty.domain.career.SeasonState
import com.leomala.footballdynasty.foundation.random.StatefulJavaRandomSource
import com.leomala.footballdynasty.foundation.random.StatefulRandomSnapshot
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
class CareerJuniorManualPromotionStoreTest {
    @Test
    fun `manual promotion materializes only at promotion and commits draft player membership and RNG atomically`() = runBlocking {
        val database = database()
        val before = careerState(seed = 77L)
        seed(database, before)
        val draft = draftState()
        database.careerJuniorDraftDao().upsertAll(listOf(draftEntity(draft)))

        val expectedRandom = restore(before.random)
        val expectedMaterialized = LegacyProceduralMaterializationRules.materialize(
            random = expectedRandom,
            draft = draft.toProceduralDraft(),
            target = materializationTarget(),
        )
        val expectedSnapshot = expectedRandom.snapshot()
        val expectedPlayerId = LegacyProceduralMaterializationRules.deterministicPlayerId(
            CAREER,
            expectedSnapshot.draws,
        )

        val result = CareerJuniorManualPromotionStore(database, clockMillis = { 91L }).promote(
            expectedBefore = before,
            clubId = CLUB,
            draftSourceOrdinal = 0,
            expectedDraft = draft,
            target = promotionTarget(currentGameEpochMillis = 1_000L),
        )

        assertEquals(false, result.blockedBySeniorLimit)
        val promoted = assertNotNull(result.promoted)
        assertEquals(expectedPlayerId, promoted.runtime.playerId)
        assertEquals(expectedMaterialized.name, promoted.procedural?.name)
        assertEquals(SENIOR, promoted.membership?.rosterKind)
        assertEquals(0, promoted.membership?.sourceOrdinal)
        assertEquals(emptyList<CareerJuniorDraftEntity>(), database.careerJuniorDraftDao().listForClub(CAREER, CLUB))
        assertEquals(expectedSnapshot.toCareerRandomState(), result.stateAfter.random)
        assertEquals(
            result.stateAfter,
            CareerCoreStateRoomAdapter.state(requireNotNull(database.careerCoreStateDao().findById(CAREER))),
        )
        assertNotNull(database.careerPlayerRuntimeDao().findRuntime(CAREER, expectedPlayerId))
        assertNotNull(database.careerPlayerRuntimeDao().findProceduralPlayer(CAREER, expectedPlayerId))
        assertNotNull(database.careerPlayerRuntimeDao().findMembership(CAREER, expectedPlayerId))
        database.close()
        Unit
    }

    @Test
    fun `failure after promotion RNG rolls back player draft removal and career RNG`() = runBlocking {
        val database = database()
        val before = careerState(seed = 88L)
        seed(database, before)
        val draft = draftState()
        database.careerJuniorDraftDao().upsertAll(listOf(draftEntity(draft)))

        val error = runCatching {
            CareerJuniorManualPromotionStore(database).promote(
                expectedBefore = before,
                clubId = CLUB,
                draftSourceOrdinal = 0,
                expectedDraft = draft,
                target = promotionTarget(currentGameEpochMillis = Long.MAX_VALUE),
            )
        }.exceptionOrNull()

        assertNotNull(error)
        assertEquals(listOf(draftEntity(draft)), database.careerJuniorDraftDao().listForClub(CAREER, CLUB))
        assertEquals(emptyList<Any>(), database.careerPlayerRuntimeDao().runtimeForCareer(CAREER))
        assertEquals(emptyList<Any>(), database.careerPlayerRuntimeDao().proceduralPlayersForCareer(CAREER))
        assertEquals(emptyList<Any>(), database.careerPlayerRuntimeDao().membershipsForCareer(CAREER))
        assertEquals(
            before,
            CareerCoreStateRoomAdapter.state(requireNotNull(database.careerCoreStateDao().findById(CAREER))),
        )
        database.close()
        Unit
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
                displayName = "Phase 15 junior manual promotion",
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
        val seasonNumber = 1
        return CareerState(
            id = CAREER,
            season = SeasonState(seasonNumber, LegacyCalendarRules.seasonYear(seasonNumber)),
            calendar = LegacyCalendarRules.calendarForSeason(seasonNumber),
            managedClub = null,
            random = snapshot.toCareerRandomState(),
        )
    }

    private fun draftState() = CareerJuniorDraftState(
        legacyN = 6,
        legacyB = false,
        legacyC = 18,
        legacyE = 3,
        legacyJ = 4,
        legacyL = 11,
        legacyD = 29,
        name = "Junior Fixture",
        legacyG = 1,
        legacyF = 10,
        legacyO = 50,
        legacyM = 6,
        legacyH = 80_000,
        legacyI = 650,
        developmentRemainder = 0.4,
    )

    private fun draftEntity(draft: CareerJuniorDraftState) = CareerJuniorDraftEntity(
        careerId = CAREER,
        clubId = CLUB,
        sourceOrdinal = 0,
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

    private fun CareerJuniorDraftState.toProceduralDraft() = LegacyProceduralPlayerRules.Draft(
        legacyN = legacyN,
        legacyB = legacyB,
        legacyC = legacyC,
        legacyE = legacyE,
        legacyJ = legacyJ,
        legacyL = legacyL,
        legacyD = legacyD,
        name = name,
        legacyG = legacyG,
        legacyF = legacyF,
        legacyO = legacyO,
        legacyM = legacyM,
    )

    private fun materializationTarget() = LegacyProceduralMaterializationRules.TargetContext(
        legacyR0 = false,
        legacyO = 3,
        legacyP0 = 4,
        legacyJ = 2,
        clubLevel = 20,
        currentYear = 2026,
    )

    private fun promotionTarget(currentGameEpochMillis: Long) = CareerJuniorManualPromotionTarget(
        legacyR0 = false,
        legacyO = 3,
        legacyP0 = 4,
        legacyJ = 2,
        clubLevel = 20,
        currentYear = 2026,
        currentGameEpochMillis = currentGameEpochMillis,
        rosterKind = SENIOR,
    )

    private fun restore(state: CareerRandomState) = StatefulJavaRandomSource.restore(
        StatefulRandomSnapshot(state.initialSeed, state.internalState, state.draws)
    )

    private fun StatefulRandomSnapshot.toCareerRandomState() = CareerRandomState(
        initialSeed = initialSeed,
        internalState = internalState,
        draws = draws,
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
        const val CAREER = "career-phase15-manual-promotion"
        const val CLUB = "club-phase15-manual-promotion"
        const val SENIOR = "SENIOR"
    }
}