package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerPlayerClubSeasonStatEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerProceduralPlayerEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.data.local.entity.PlayerEntity
import com.leomala.footballdynasty.data.repository.RoomCareerRepository
import com.leomala.footballdynasty.data.repository.RoomCareerStateRepository
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.career.SeasonState
import com.leomala.footballdynasty.domain.model.Career
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerMatchPersistedRuntimeResolverTest {
    @Test
    fun `persisted roster resolves players and hydrates proven career match state`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        RoomCareerRepository(database) { 10L }.save(Career("career-a", "Phase 9", null, null))
        RoomCareerStateRepository(database) { 11L }.save(
            CareerStateFactory.create("career-a", 123L).copy(season = SeasonState(9, 2026))
        )
        database.clubDao().upsertAll(listOf(club("home", 101), club("away", 202)))
        database.playerDao().upsertAll(listOf(canonicalPlayer("canonical-home")))

        val dao = database.careerPlayerRuntimeDao()
        dao.upsertRuntime(
            runtime(
                "career-a",
                "canonical-home",
                CareerPlayerRuntimeStore.SOURCE_CANONICAL,
                age = 24,
                overall = 88,
                energy = 47,
                injuryUntilEpochDay = 222L,
            )
        )
        dao.upsertMembership(membership("career-a", "canonical-home", "home", 0))
        dao.upsertClubSeasonStat(
            CareerPlayerClubSeasonStatEntity(
                careerId = "career-a",
                playerId = "canonical-home",
                legacySeasonId = 9,
                legacyClubId = 101,
                legacyC = 1,
                legacyD = 2,
                legacyE = 3,
                legacyF = 4,
                legacyG = 5,
                legacyH = 6,
            )
        )
        dao.upsertRuntime(
            runtime(
                "career-a",
                "procedural-away",
                CareerPlayerRuntimeStore.SOURCE_PROCEDURAL,
                age = 19,
                overall = 73,
                energy = 39,
            )
        )
        dao.upsertProceduralPlayer(
            CareerProceduralPlayerEntity(
                careerId = "career-a",
                playerId = "procedural-away",
                name = "Procedural Away",
                country = 7,
                position = 4,
                status = 2,
                side = 2,
                cr1 = 11,
                cr2 = 12,
            )
        )
        dao.upsertMembership(membership("career-a", "procedural-away", "away", 0))

        val resolver = CareerMatchPersistedRuntimeResolver(database)
        val scheduled = ScheduledCareerMatch("m1", 3, 1, "home", "away")
        val roster = resolver.resolve("career-a", scheduled)

        assertEquals(9, roster.currentSeasonId)
        assertEquals(101, roster.home.legacyClubId)
        assertEquals(202, roster.away.legacyClubId)
        assertEquals(listOf("canonical-home"), roster.home.players.map { it.playerId })
        assertEquals(listOf("procedural-away"), roster.away.players.map { it.playerId })
        assertEquals("Canonical Home", roster.home.players.single().facts.name)
        assertEquals("Procedural Away", roster.away.players.single().facts.name)
        assertEquals(88, roster.home.players.single().overall)
        assertEquals(47, roster.home.players.single().energy)
        assertEquals(222L, roster.home.players.single().injuryUntilEpochDay)
        assertEquals(6, roster.home.players.single().clubSeasonStats.single().legacyH)

        val state = resolver.hydratePhase8State(
            roster = roster,
            evidence = CareerMatchPersistedRuntimeResolver.TransientMatchEvidence(
                home = CareerMatchPersistedRuntimeResolver.TransientClubEvidence(
                    active = listOf(transient("canonical-home", legacyG0 = 18)),
                    bench = emptyList(),
                    substitutionsRemaining = 3,
                    legacyModeFlag = false,
                ),
                away = CareerMatchPersistedRuntimeResolver.TransientClubEvidence(
                    active = listOf(transient("procedural-away", legacyG0 = 14)),
                    bench = emptyList(),
                    substitutionsRemaining = 3,
                    legacyModeFlag = true,
                ),
            ),
        )

        val home = state.home.active.single()
        assertEquals(9, state.currentSeasonId)
        assertEquals("canonical-home", home.value.playerId)
        assertEquals(24, home.age)
        assertEquals(88, home.skill)
        assertEquals(47, home.energy)
        assertEquals(18, home.legacyG0)
        assertEquals(3, home.legacyL0)
        assertEquals(0, home.legacyF0)
        assertEquals(1, home.legacyR)
        assertEquals(6, home.clubSeasonStats!!.single().legacyH)
        assertEquals(101, state.home.legacyClubId)

        val away = state.away.active.single()
        assertEquals(73, away.skill)
        assertEquals(39, away.energy)
        assertEquals(4, away.legacyL0)
        assertEquals(1, away.legacyF0)
        assertEquals(1, away.legacyR)
        assertEquals(202, state.away.legacyClubId)
        assertTrue(state.away.legacyModeFlag)

        database.close()
        Unit
    }

    @Test
    fun `hydration rejects a player from the opposite scheduled club`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        RoomCareerRepository(database) { 10L }.save(Career("career-b", "Phase 9", null, null))
        RoomCareerStateRepository(database) { 11L }.save(CareerStateFactory.create("career-b", 321L))
        database.clubDao().upsertAll(listOf(club("home", 101), club("away", 202)))
        database.playerDao().upsertAll(
            listOf(canonicalPlayer("home-player"), canonicalPlayer("away-player"))
        )
        val dao = database.careerPlayerRuntimeDao()
        dao.upsertRuntime(runtime("career-b", "home-player", CareerPlayerRuntimeStore.SOURCE_CANONICAL, 25, 80, 50))
        dao.upsertMembership(membership("career-b", "home-player", "home", 0))
        dao.upsertRuntime(runtime("career-b", "away-player", CareerPlayerRuntimeStore.SOURCE_CANONICAL, 26, 79, 50))
        dao.upsertMembership(membership("career-b", "away-player", "away", 0))

        val resolver = CareerMatchPersistedRuntimeResolver(database)
        val roster = resolver.resolve(
            "career-b",
            ScheduledCareerMatch("m2", 4, 1, "home", "away"),
        )

        try {
            resolver.hydratePhase8State(
                roster,
                CareerMatchPersistedRuntimeResolver.TransientMatchEvidence(
                    home = CareerMatchPersistedRuntimeResolver.TransientClubEvidence(
                        active = listOf(transient("away-player", legacyG0 = 18)),
                        bench = emptyList(),
                        substitutionsRemaining = 3,
                        legacyModeFlag = false,
                    ),
                    away = CareerMatchPersistedRuntimeResolver.TransientClubEvidence(
                        active = listOf(transient("away-player", legacyG0 = 18)),
                        bench = emptyList(),
                        substitutionsRemaining = 3,
                        legacyModeFlag = false,
                    ),
                ),
            )
            fail("Expected wrong-club player identity rejection")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message.orEmpty().contains("does not belong to scheduled club home"))
        }

        database.close()
        Unit
    }

    private fun transient(
        playerId: String,
        legacyG0: Int,
    ) = CareerMatchPersistedRuntimeResolver.TransientPlayerEvidence(
        playerId = playerId,
        legacyG0 = legacyG0,
    )

    private fun runtime(
        careerId: String,
        playerId: String,
        sourceType: String,
        age: Int,
        overall: Int,
        energy: Int,
        injuryUntilEpochDay: Long = 0L,
    ) = CareerPlayerRuntimeEntity(
        careerId = careerId,
        playerId = playerId,
        sourceType = sourceType,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = age,
        overall = overall,
        marketValue = 1_000,
        star = false,
        worldTop = false,
        legacyHash = playerId.hashCode(),
        legacyGeneratedO = 0,
        legacyCreatedYear = 0,
        contractEndEpochMillis = 0L,
        legacyPreviousMarketValue = 0,
        legacyQ = false,
        legacyX = false,
        legacyY = false,
        legacyZ = false,
        energy = energy,
        injuryUntilEpochDay = injuryUntilEpochDay,
    )

    private fun membership(
        careerId: String,
        playerId: String,
        clubId: String,
        sourceOrdinal: Int,
    ) = CareerSquadMembershipEntity(
        careerId = careerId,
        playerId = playerId,
        clubId = clubId,
        rosterKind = "SENIOR",
        sourceOrdinal = sourceOrdinal,
    )

    private fun canonicalPlayer(id: String) = PlayerEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        name = if (id == "canonical-home") "Canonical Home" else id,
        age = 22,
        country = 1,
        position = 3,
        status = 2,
        side = 0,
        cr1 = 5,
        cr2 = 6,
        star = false,
        worldTop = false,
        legacyAid = 0,
        legacySid = 0,
        legacyTid = 0,
        legacyHash = id.hashCode(),
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
}
