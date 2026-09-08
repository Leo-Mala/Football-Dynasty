package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.match.LegacyMatchRatingMetricRuntimeRules
import com.leomala.footballdynasty.domain.match.LegacyMatchRatingParticipantRuntime
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime
import com.leomala.footballdynasty.foundation.random.SeededRandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class CareerMatchPlayerRatingRuntimeExecutorTest {
    @Test
    fun `executor preserves u0 v0 F G order and creates fresh implicit RNG per player`() {
        val h1 = persisted("h1", 1)
        val a1 = persisted("a1", 2)
        val hs = persisted("hs", 3)
        val asub = persisted("as", 4)
        val h1Runtime = runtime(h1)
        val a1Runtime = runtime(a1)
        val hsRuntime = runtime(hs)
        val asRuntime = runtime(asub)
        val state = LegacyMatchTransientRuntime.State(
            currentSeasonId = 1,
            home = LegacyMatchTransientRuntime.Club(
                value = club("home", 10, listOf(h1, hs)),
                legacyClubId = 10,
                active = mutableListOf(h1Runtime),
                bench = mutableListOf(hsRuntime),
                substitutionsRemaining = 3,
            ),
            away = LegacyMatchTransientRuntime.Club(
                value = club("away", 20, listOf(a1, asub)),
                legacyClubId = 20,
                active = mutableListOf(a1Runtime),
                bench = mutableListOf(asRuntime),
                substitutionsRemaining = 3,
            ),
        )
        val snapshot = LegacyMatchRatingParticipantRuntime.capture(state)
        state.home.used += hsRuntime
        state.away.used += asRuntime

        var factoryCalls = 0
        val rated = CareerMatchPlayerRatingRuntimeExecutor.execute(
            state = state,
            participantSnapshot = snapshot,
            metricState = LegacyMatchRatingMetricRuntimeRules.State(),
            implicitRandomFactory = {
                factoryCalls += 1
                SeededRandomSource(factoryCalls.toLong())
            },
        )

        assertEquals(listOf("h1", "a1", "hs", "as"), rated.map { it.playerId })
        assertEquals(listOf(0, 1, 0, 1), rated.map { it.side })
        assertEquals(4, factoryCalls)
        assertEquals(listOf(1, 1, 1, 1), rated.map { it.resolvedLegacyS })
    }

    private fun runtime(
        player: CareerMatchPersistedRuntimeResolver.PersistedPlayer,
    ) = LegacyMatchTransientRuntime.Player(
        value = player,
        legacyG0 = 1,
        legacyL0 = 0,
        legacyF0 = 0,
        legacyR = 0,
        age = player.age,
        energy = player.energy,
        skill = player.overall,
    )

    private fun club(
        id: String,
        legacyId: Int,
        players: List<CareerMatchPersistedRuntimeResolver.PersistedPlayer>,
    ) = CareerMatchPersistedRuntimeResolver.PersistedClubRoster(id, legacyId, players)

    private fun persisted(
        id: String,
        ordinal: Int,
    ) = CareerMatchPersistedRuntimeResolver.PersistedPlayer(
        playerId = id,
        sourceType = CareerPlayerRuntimeStore.SOURCE_CANONICAL,
        age = 25,
        overall = 80,
        star = false,
        worldTop = false,
        energy = 100,
        injuryUntilEpochDay = 0L,
        legacyHash = ordinal,
        rosterKind = "SENIOR",
        sourceOrdinal = ordinal,
        facts = CareerMatchPersistedRuntimeResolver.StaticPlayerFacts(
            name = id,
            country = 1,
            position = 0,
            status = 0,
            side = 0,
            cr1 = 7,
            cr2 = 10,
        ),
        clubSeasonStats = emptyList(),
    )
}
