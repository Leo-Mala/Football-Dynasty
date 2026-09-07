package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.match.LegacyMatchEventRecord
import com.leomala.footballdynasty.domain.match.LegacyMatchN2CounterRules
import com.leomala.footballdynasty.domain.match.LegacyMatchRatingMetricRuntimeRules
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerMatchPlayerRatingInputMapperTest {
    @Test
    fun `mapper uses persisted flags static facts transient n2 metrics and event identity`() {
        val persistedHome = persistedPlayer(
            id = "p-home",
            overall = 88,
            star = true,
            worldTop = true,
            position = 3,
            cr1 = 11,
            cr2 = 4,
        )
        val persistedAway = persistedPlayer(
            id = "p-away",
            overall = 75,
            star = false,
            worldTop = false,
            position = 2,
            cr1 = 7,
            cr2 = 10,
        )
        val homePlayer = runtimePlayer(persistedHome, legacyG0 = 14, legacyL0 = 3).also {
            it.legacyN2 = LegacyMatchN2CounterRules.State(rawE = 2)
        }
        val awayPlayer = runtimePlayer(persistedAway, legacyG0 = 7, legacyL0 = 2)
        val state = LegacyMatchTransientRuntime.State(
            currentSeasonId = 9,
            home = LegacyMatchTransientRuntime.Club(
                value = CareerMatchPersistedRuntimeResolver.PersistedClubRoster("home", 101, listOf(persistedHome)),
                legacyClubId = 101,
                active = mutableListOf(homePlayer),
                bench = mutableListOf(),
                substitutionsRemaining = 3,
            ),
            away = LegacyMatchTransientRuntime.Club(
                value = CareerMatchPersistedRuntimeResolver.PersistedClubRoster("away", 202, listOf(persistedAway)),
                legacyClubId = 202,
                active = mutableListOf(awayPlayer),
                bench = mutableListOf(),
                substitutionsRemaining = 3,
            ),
        )
        state.events += LegacyMatchEventRecord(
            legacyClub = state.home,
            legacyType = 99,
            legacyMinute = 12,
            legacyPeriod = 1,
            primaryPlayer = homePlayer,
            legacySide = 0,
        )

        val input = CareerMatchPlayerRatingInputMapper.map(
            state = state,
            metricState = LegacyMatchRatingMetricRuntimeRules.State(
                legacyEBySide = listOf(60, 40),
                legacyQ0BySide = listOf(4, 2),
                legacyWBySide = listOf(1, 3),
                legacyYBySide = listOf(5, 7),
            ),
            player = homePlayer,
            side = 0,
        )

        assertEquals(88, input.legacyPlayerJ)
        assertFalse(input.legacyP0Flag)
        assertEquals(14, input.legacyG0)
        assertEquals(14, input.legacyS)
        assertEquals(3, input.legacyGCategory)
        assertEquals(1, input.legacyF)
        assertEquals(11, input.legacyGCode)
        assertEquals(60, input.currentLegacyE)
        assertEquals(40, input.opponentLegacyE)
        assertEquals(4, input.currentLegacyQ0)
        assertEquals(2, input.opponentLegacyQ0)
        assertEquals(3, input.opponentLegacyW)
        assertEquals(7, input.opponentLegacyY)
        assertTrue(input.star)
        assertTrue(input.worldTop)
        assertEquals(2, input.n2.legacyH)
        assertEquals(12, input.participationMarker)
    }

    private fun persistedPlayer(
        id: String,
        overall: Int,
        star: Boolean,
        worldTop: Boolean,
        position: Int,
        cr1: Int,
        cr2: Int,
    ) = CareerMatchPersistedRuntimeResolver.PersistedPlayer(
        playerId = id,
        sourceType = CareerPlayerRuntimeStore.SOURCE_CANONICAL,
        age = 25,
        overall = overall,
        star = star,
        worldTop = worldTop,
        energy = 100,
        injuryUntilEpochDay = 0L,
        legacyHash = id.hashCode(),
        rosterKind = "SENIOR",
        sourceOrdinal = 0,
        facts = CareerMatchPersistedRuntimeResolver.StaticPlayerFacts(
            name = id,
            country = 1,
            position = position,
            status = 0,
            side = 0,
            cr1 = cr1,
            cr2 = cr2,
        ),
        clubSeasonStats = emptyList(),
    )

    private fun runtimePlayer(
        persisted: CareerMatchPersistedRuntimeResolver.PersistedPlayer,
        legacyG0: Int,
        legacyL0: Int,
    ) = LegacyMatchTransientRuntime.Player(
        value = persisted,
        legacyG0 = legacyG0,
        legacyL0 = legacyL0,
        legacyF0 = 0,
        legacyR = 1,
        age = persisted.age,
        energy = persisted.energy,
        skill = persisted.overall,
    )
}
