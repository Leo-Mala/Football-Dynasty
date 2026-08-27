package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.match.LegacyMatchEventRecord
import com.leomala.footballdynasty.domain.match.LegacyMatchEventType
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerMatchPersistedEffectsMapperTest {
    @Test
    fun `energy snapshot includes active bench used and event only removed players`() {
        val active = wrapper(player("active"), energy = 81)
        val bench = wrapper(player("bench"), energy = 92)
        val used = wrapper(player("used"), energy = 73)
        val removed = wrapper(player("removed"), energy = 64)
        val homeRoster = roster("home", 101, listOf(active.value, bench.value, used.value, removed.value))
        val awayRoster = roster("away", 202, emptyList())
        val home = LegacyMatchTransientRuntime.Club(
            value = homeRoster,
            legacyClubId = 101,
            active = mutableListOf(active),
            bench = mutableListOf(bench),
            used = mutableListOf(used),
            substitutionsRemaining = 1,
        )
        val away = LegacyMatchTransientRuntime.Club<
            CareerMatchPersistedRuntimeResolver.PersistedClubRoster,
            CareerMatchPersistedRuntimeResolver.PersistedPlayer,
        >(
            value = awayRoster,
            legacyClubId = 202,
            active = mutableListOf(),
            bench = mutableListOf(),
            substitutionsRemaining = 3,
        )
        val state = LegacyMatchTransientRuntime.State(1, home, away)
        state.events += LegacyMatchEventRecord(
            legacyClub = home,
            legacyType = LegacyMatchEventType.INJURY.legacyCode,
            legacySubtype = -1,
            legacyMinute = 12,
            legacyPeriod = 1,
            primaryPlayer = removed,
            legacySide = 0,
        )

        val updates = CareerMatchPersistedEffectsMapper.energyUpdates(state)

        assertEquals(
            listOf(
                CareerMatchPlayerEnergyUpdate("active", 81),
                CareerMatchPlayerEnergyUpdate("bench", 92),
                CareerMatchPlayerEnergyUpdate("removed", 64),
                CareerMatchPlayerEnergyUpdate("used", 73),
            ),
            updates,
        )
    }

    @Test
    fun `divergent duplicate wrappers fail instead of silently choosing energy`() {
        val persisted = player("same")
        val one = wrapper(persisted, energy = 80)
        val two = wrapper(persisted, energy = 79)
        val homeRoster = roster("home", 101, listOf(persisted))
        val awayRoster = roster("away", 202, emptyList())
        val home = LegacyMatchTransientRuntime.Club(
            value = homeRoster,
            legacyClubId = 101,
            active = mutableListOf(one),
            bench = mutableListOf(two),
            substitutionsRemaining = 3,
        )
        val away = LegacyMatchTransientRuntime.Club<
            CareerMatchPersistedRuntimeResolver.PersistedClubRoster,
            CareerMatchPersistedRuntimeResolver.PersistedPlayer,
        >(
            value = awayRoster,
            legacyClubId = 202,
            active = mutableListOf(),
            bench = mutableListOf(),
            substitutionsRemaining = 3,
        )
        val state = LegacyMatchTransientRuntime.State(1, home, away)

        val error = runCatching {
            CareerMatchPersistedEffectsMapper.energyUpdates(state)
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }

    private fun wrapper(
        player: CareerMatchPersistedRuntimeResolver.PersistedPlayer,
        energy: Int,
    ) = LegacyMatchTransientRuntime.Player(
        value = player,
        legacyG0 = 2,
        legacyL0 = player.facts.position,
        legacyF0 = 0,
        legacyR = 0,
        age = player.age,
        energy = energy,
        skill = player.overall,
    )

    private fun player(id: String) = CareerMatchPersistedRuntimeResolver.PersistedPlayer(
        playerId = id,
        sourceType = CareerPlayerRuntimeStore.SOURCE_CANONICAL,
        age = 25,
        overall = 80,
        energy = 100,
        injuryUntilEpochDay = 0L,
        legacyHash = id.hashCode(),
        rosterKind = "SENIOR",
        sourceOrdinal = 0,
        facts = CareerMatchPersistedRuntimeResolver.StaticPlayerFacts(
            name = id,
            country = 1,
            position = 3,
            status = 2,
            side = 0,
            cr1 = 5,
            cr2 = 6,
        ),
        clubSeasonStats = emptyList(),
    )

    private fun roster(
        clubId: String,
        legacyClubId: Int,
        players: List<CareerMatchPersistedRuntimeResolver.PersistedPlayer>,
    ) = CareerMatchPersistedRuntimeResolver.PersistedClubRoster(
        clubId = clubId,
        legacyClubId = legacyClubId,
        players = players,
    )
}
