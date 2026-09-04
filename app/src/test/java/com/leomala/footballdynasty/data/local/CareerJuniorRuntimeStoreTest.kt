package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerClubManagerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.career.CareerRandomState
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.LegacyCalendarRules
import com.leomala.footballdynasty.domain.career.SeasonState
import com.leomala.footballdynasty.domain.manager.LegacyJuniorRuntimeRules
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
class CareerJuniorRuntimeStoreTest {
    @Test
    fun `trial atomically persists interleaved RNG drafts and raw code 9 expense`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val before = careerState(seed = 1L)
        seed(database, before, cash = 1_000L)

        val expectedRandom = restore(before.random)
        val expectedDrafts = LegacyJuniorRuntimeRules.executeTrial(expectedRandom, 0, ::fixtureDraft)
        val expectedSnapshot = expectedRandom.snapshot()

        val store = CareerJuniorRuntimeStore(database, clockMillis = { 44L })
        val result = store.runTrial(before, CLUB, cost = 100, generateDraft = ::fixtureDraft)

        assertEquals(LegacyJuniorRuntimeRules.TrialAvailability.READY, result.availability)
        assertEquals(expectedDrafts, result.generated)
        assertEquals(expectedDrafts, store.listForClub(CAREER, CLUB))
        assertEquals(expectedSnapshot.toCareerRandomState(), result.stateAfter.random)
        assertEquals(
            result.stateAfter,
            CareerCoreStateRoomAdapter.state(requireNotNull(database.careerCoreStateDao().findById(CAREER))),
        )
        val finance = requireNotNull(CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        assertEquals(900L, finance.cash)
        assertEquals(100, finance.ledger.miscellaneousExpense)
        database.close()
        Unit
    }

    @Test
    fun `failed trial callback rolls back RNG finance and every staged draft`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val before = careerState(seed = 1L)
        seed(database, before, cash = 1_000L)
        var generated = 0

        val error = runCatching {
            CareerJuniorRuntimeStore(database).runTrial(before, CLUB, cost = 100) { requested, random ->
                generated++
                if (generated == 2) error("abort junior trial")
                fixtureDraft(requested, random)
            }
        }.exceptionOrNull()

        assertNotNull(error)
        assertEquals(
            before,
            CareerCoreStateRoomAdapter.state(requireNotNull(database.careerCoreStateDao().findById(CAREER))),
        )
        assertEquals(emptyList<CareerJuniorDraftState>(), CareerJuniorRuntimeStore(database).listForClub(CAREER, CLUB))
        val finance = requireNotNull(CareerManagerRuntimeStore(database).clubFinanceState(CAREER, CLUB))
        assertEquals(1_000L, finance.cash)
        assertEquals(0, finance.ledger.miscellaneousExpense)
        database.close()
        Unit
    }

    @Test
    fun `dismiss removes only selected draft without changing career RNG`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val before = careerState(seed = 11L)
        seed(database, before, cash = 1_000L)
        val store = CareerJuniorRuntimeStore(database)
        val trial = store.runTrial(before, CLUB, cost = 0, generateDraft = ::fixtureDraft)
        val persistedBeforeDismiss = database.careerJuniorDraftDao().listForClub(CAREER, CLUB)
        val selected = persistedBeforeDismiss.first()
        val stateBeforeDismiss = requireNotNull(database.careerCoreStateDao().findById(CAREER))

        store.dismiss(CAREER, CLUB, selected.sourceOrdinal, store.listForClub(CAREER, CLUB).first())

        val persistedAfter = database.careerJuniorDraftDao().listForClub(CAREER, CLUB)
        assertEquals(persistedBeforeDismiss.size - 1, persistedAfter.size)
        assertEquals(stateBeforeDismiss, requireNotNull(database.careerCoreStateDao().findById(CAREER)))
        assertEquals(trial.stateAfter.random, CareerCoreStateRoomAdapter.state(stateBeforeDismiss).random)
        database.close()
        Unit
    }

    private suspend fun seed(database: FootballDynastyDatabase, state: CareerState, cash: Long) {
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Phase 15 junior runtime",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            )
        )
        database.clubDao().upsertAll(listOf(club()))
        database.careerCoreStateDao().upsert(CareerCoreStateRoomAdapter.entity(state, 1L))
        database.careerManagerRuntimeDao().upsertClubRuntime(
            CareerClubManagerRuntimeEntity(
                careerId = CAREER,
                clubId = CLUB,
                active = true,
                cash = cash,
                primarySlotPlayerCode = null,
                secondarySlotPlayerCode = null,
                rawStateFlag = false,
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
                borrowed = 0,
                monthlyBorrowingCharge = 0,
            )
        )
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

    private fun fixtureDraft(requestedLegacyE: Int, random: com.leomala.footballdynasty.foundation.random.RandomSource): CareerJuniorDraftState {
        val n = random.nextInt(11)
        return CareerJuniorDraftState(
            legacyN = n,
            legacyB = false,
            legacyC = 17,
            legacyE = requestedLegacyE,
            legacyJ = 0,
            legacyL = 0,
            legacyD = 0,
            name = "fixture-$requestedLegacyE-$n",
            legacyG = 0,
            legacyF = 1,
            legacyO = 50,
            legacyM = 0,
            legacyH = 500,
            legacyI = 500,
            developmentRemainder = 0.0,
        )
    }

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
        country = 0,
        state = 0,
        level = 1,
        stadium = "Fixture",
        capacity = 1,
        reputation = 1,
        primaryColor = "000000",
        secondaryColor = "ffffff",
        coach = "Fixture",
        coachCountry = 0,
        baseColor = 0,
        legacyAid = 0,
        legacySid = 0,
        legacyTid = 0,
        legacyVid = 0,
        legacyId = 1,
        legacyValid = true,
    )

    private companion object {
        const val CAREER = "career-phase15-junior"
        const val CLUB = "club-phase15-junior"
    }
}
