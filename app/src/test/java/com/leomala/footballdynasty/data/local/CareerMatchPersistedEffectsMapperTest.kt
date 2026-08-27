package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.career.GameDate
import com.leomala.footballdynasty.domain.match.LegacyMatchEventRecord
import com.leomala.footballdynasty.domain.match.LegacyMatchEventType
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime
import com.leomala.footballdynasty.foundation.random.RandomSource
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerMatchPersistedEffectsMapperTest {
    @Test
    fun `runtime snapshot includes active bench used and event only removed players`() {
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
        val away = emptyClub(awayRoster)
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

        val updates = CareerMatchPersistedEffectsMapper.playerRuntimeUpdates(
            state,
            GameDate(2026, 1, 4),
        )

        assertEquals(
            listOf(
                CareerMatchPlayerRuntimeUpdate("active", 81, 80, 0L),
                CareerMatchPlayerRuntimeUpdate("bench", 92, 80, 0L),
                CareerMatchPlayerRuntimeUpdate("removed", 64, 80, 0L),
                CareerMatchPlayerRuntimeUpdate("used", 73, 80, 0L),
            ),
            updates,
        )
    }

    @Test
    fun `injury removed player persists skill and deadline from scheduled match day`() {
        val persisted = player("injured", age = 35, overall = 80, injuryUntilEpochDay = 500L)
        val injured = wrapper(persisted, energy = 60)
        val homeRoster = roster("home", 101, listOf(persisted))
        val awayRoster = roster("away", 202, emptyList())
        val home = LegacyMatchTransientRuntime.Club(
            value = homeRoster,
            legacyClubId = 101,
            active = mutableListOf(injured),
            bench = mutableListOf(),
            substitutionsRemaining = 0,
        )
        val away = emptyClub(awayRoster)
        val state = LegacyMatchTransientRuntime.State(1, home, away)

        LegacyMatchTransientRuntime.applyEvent(
            state = state,
            legacyType = LegacyMatchEventType.INJURY.legacyCode,
            legacySubtype = -1,
            eventClub = home,
            originalPrimary = injured,
            legacyPeriod = 1,
            legacyMinute = 12,
            random = QueueRandom(0, 0, 10),
        )
        assertTrue(home.active.isEmpty())

        val matchDate = GameDate(2026, 2, 10)
        val updates = CareerMatchPersistedEffectsMapper.playerRuntimeUpdates(state, matchDate)
        val expectedDeadline = LocalDate.of(2026, 2, 10).toEpochDay() + 3L

        assertEquals(
            listOf(CareerMatchPlayerRuntimeUpdate("injured", 60, 75, expectedDeadline)),
            updates,
        )
    }

    @Test
    fun `divergent duplicate wrappers fail instead of silently choosing runtime state`() {
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
        val state = LegacyMatchTransientRuntime.State(1, home, emptyClub(awayRoster))

        val error = runCatching {
            CareerMatchPersistedEffectsMapper.playerRuntimeUpdates(state, GameDate(2026, 1, 4))
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
        clubSeasonStats = player.clubSeasonStats,
    )

    private fun player(
        id: String,
        age: Int = 25,
        overall: Int = 80,
        injuryUntilEpochDay: Long = 0L,
    ) = CareerMatchPersistedRuntimeResolver.PersistedPlayer(
        playerId = id,
        sourceType = CareerPlayerRuntimeStore.SOURCE_CANONICAL,
        age = age,
        overall = overall,
        energy = 100,
        injuryUntilEpochDay = injuryUntilEpochDay,
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

    private fun emptyClub(
        roster: CareerMatchPersistedRuntimeResolver.PersistedClubRoster,
    ) = LegacyMatchTransientRuntime.Club<
        CareerMatchPersistedRuntimeResolver.PersistedClubRoster,
        CareerMatchPersistedRuntimeResolver.PersistedPlayer,
    >(
        value = roster,
        legacyClubId = roster.legacyClubId,
        active = mutableListOf(),
        bench = mutableListOf(),
        substitutionsRemaining = 3,
    )

    private class QueueRandom(vararg values: Int) : RandomSource {
        private val queue = values.toMutableList()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            check(queue.isNotEmpty()) { "No queued RNG value for bound $bound" }
            val value = queue.removeAt(0)
            require(value in 0 until bound)
            draws += 1
            return value
        }

        override fun nextBoolean(): Boolean = error("unused")
        override fun nextDouble(): Double = error("unused")
    }
}
