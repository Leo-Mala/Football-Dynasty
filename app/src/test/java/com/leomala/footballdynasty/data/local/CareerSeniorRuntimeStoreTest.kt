package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerProceduralPlayerEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.repository.RoomCareerRepository
import com.leomala.footballdynasty.data.repository.RoomCareerStateRepository
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.LegacyAnnualSeniorProgressionRules
import com.leomala.footballdynasty.domain.career.LegacySeniorPayrollScalarRules
import com.leomala.footballdynasty.domain.model.Career
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerSeniorRuntimeStoreTest {
    @Test
    fun `annual growth commits overall M N and career rng together across reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "phase15-senior-runtime-reopen.db"
        context.deleteDatabase(name)
        var db = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        db.clubDao().upsertAll(listOf(club("club-a")))
        RoomCareerRepository(db) { 100L }.save(
            Career(CAREER, "Senior", null, null)
        )
        val before = CareerStateFactory.create(CAREER, seed = 17L)
        RoomCareerStateRepository(db) { 100L }.save(before)
        CareerPlayerRuntimeStore(db).saveProceduralPlayer(
            runtime(legacyAnnualM = true, legacyAnnualN = 0.95),
            procedural(),
            membership(),
        )

        val result = CareerSeniorRuntimeStore(db).applyAnnualProgression(
            expectedBefore = before,
            playerId = PLAYER,
            club = progressionClub(),
            legacyD0 = 60,
            legacyM = 0,
        )
        assertEquals(51, result.runtime.overall)
        assertFalse(requireNotNull(result.runtime.legacyAnnualM))
        assertTrue(requireNotNull(result.runtime.legacyAnnualN) < 1.0)
        assertEquals(before.random.draws + 1L, result.stateAfter.random.draws)
        db.close()

        db = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        val reopenedRuntime = requireNotNull(db.careerPlayerRuntimeDao().findRuntime(CAREER, PLAYER))
        val reopenedState = requireNotNull(RoomCareerStateRepository(db).findById(CAREER))
        assertEquals(result.runtime.overall, reopenedRuntime.overall)
        assertEquals(result.runtime.legacyAnnualM, reopenedRuntime.legacyAnnualM)
        assertEquals(result.runtime.legacyAnnualN, reopenedRuntime.legacyAnnualN)
        assertEquals(result.stateAfter.random, reopenedState.random)
        db.close()
        context.deleteDatabase(name)
    }

    @Test
    fun `migrated unknown M N fails closed without advancing rng`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        db.clubDao().upsertAll(listOf(club("club-a")))
        RoomCareerRepository(db) { 100L }.save(Career(CAREER, "Senior", null, null))
        val before = CareerStateFactory.create(CAREER, seed = 17L)
        RoomCareerStateRepository(db) { 100L }.save(before)
        CareerPlayerRuntimeStore(db).saveProceduralPlayer(
            runtime(legacyAnnualM = null, legacyAnnualN = null),
            procedural(),
            membership(),
        )

        try {
            CareerSeniorRuntimeStore(db).applyAnnualProgression(
                expectedBefore = before,
                playerId = PLAYER,
                club = progressionClub(),
                legacyD0 = 60,
                legacyM = 0,
            )
            throw AssertionError("Expected fail-closed unknown M/N")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message.orEmpty().contains("best.o.M"))
        }
        val afterRuntime = requireNotNull(db.careerPlayerRuntimeDao().findRuntime(CAREER, PLAYER))
        val afterState = requireNotNull(RoomCareerStateRepository(db).findById(CAREER))
        assertNull(afterRuntime.legacyAnnualM)
        assertNull(afterRuntime.legacyAnnualN)
        assertEquals(before.random, afterState.random)
        db.close()
    }

    @Test
    fun `raw payroll recompute uses persisted player facts and stores exact scalar`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        db.clubDao().upsertAll(listOf(club("club-a")))
        RoomCareerRepository(db) { 100L }.save(Career(CAREER, "Senior", null, null))
        CareerPlayerRuntimeStore(db).saveProceduralPlayer(
            runtime(legacyAnnualM = false, legacyAnnualN = 0.0),
            procedural(position = 3),
            membership(),
        )
        val clubState = LegacySeniorPayrollScalarRules.ClubState(
            legacyV0 = true,
            legacyO = 1,
            legacyF0 = 21,
        )
        val expected = LegacySeniorPayrollScalarRules.calculate(
            club = clubState,
            player = LegacySeniorPayrollScalarRules.PlayerState(
                legacyG = 3,
                legacyJ = 50,
                legacyE = 20,
                legacyC = false,
                legacyD = false,
            ),
            legacyGlobalV1 = false,
        )
        val store = CareerSeniorRuntimeStore(db)
        assertEquals(expected, store.recomputeRawPayroll(CAREER, PLAYER, clubState, false))
        assertEquals(expected, store.requireRawPayroll(CAREER, PLAYER))
        assertEquals(expected, db.careerPlayerRuntimeDao().findRuntime(CAREER, PLAYER)?.legacyRawPayrollN)
        db.close()
    }

    private fun progressionClub() = LegacyAnnualSeniorProgressionRules.ClubState(
        hasCurrentClub = true,
        legacyF0 = 20,
        legacyR0 = true,
        legacyP0 = 4,
        legacyJ = 6,
        legacyJ0 = 0,
        legacyO = 1,
    )

    private fun runtime(
        legacyAnnualM: Boolean?,
        legacyAnnualN: Double?,
    ) = CareerPlayerRuntimeEntity(
        careerId = CAREER,
        playerId = PLAYER,
        sourceType = CareerPlayerRuntimeStore.SOURCE_PROCEDURAL,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = 20,
        overall = 50,
        marketValue = 1000,
        star = false,
        worldTop = false,
        legacyHash = 0,
        legacyGeneratedO = 0,
        legacyCreatedYear = 2026,
        contractEndEpochMillis = 1000L,
        legacyPreviousMarketValue = 0,
        legacyQ = false,
        legacyX = false,
        legacyY = false,
        legacyZ = false,
        legacyAnnualM = legacyAnnualM,
        legacyAnnualN = legacyAnnualN,
        legacyRawPayrollN = null,
    )

    private fun procedural(position: Int = 3) = CareerProceduralPlayerEntity(
        careerId = CAREER,
        playerId = PLAYER,
        name = "Senior",
        country = 29,
        position = position,
        status = 0,
        side = 1,
        cr1 = 4,
        cr2 = 11,
    )

    private fun membership() = CareerSquadMembershipEntity(
        careerId = CAREER,
        playerId = PLAYER,
        clubId = "club-a",
        rosterKind = "SENIOR",
        sourceOrdinal = 0,
    )

    private fun club(id: String) = ClubEntity(
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
        legacyId = 0,
        legacyValid = true,
    )

    private companion object {
        const val CAREER = "career-senior"
        const val PLAYER = "player-senior"
    }
}
