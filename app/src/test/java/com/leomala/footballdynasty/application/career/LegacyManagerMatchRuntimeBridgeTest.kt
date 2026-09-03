package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitRule
import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitSlot
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime
import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyManagerMatchRuntimeBridgeTest {
    @Test
    fun `characterized lineup feeds the same player objects into match substitution runtime`() {
        val starter = player("starter", legacyG0 = 10, legacyL0 = 1, legacyF0 = 1, legacyR = 1)
        val incoming = player("incoming", legacyG0 = 0, legacyL0 = 1, legacyF0 = 1, legacyR = 1)
        val lineup = LegacyLineupCommitRule.commit(
            starterSlots = listOf(LegacyLineupCommitSlot(starter, 10)),
            benchPlayers = listOf(incoming),
            eligibleRoster = listOf(incoming),
            matchSideIndex = 0,
            mainTeamActivityPresent = false,
        )
        val stale = player("stale", legacyG0 = 3)
        val home = club("home", 101, mutableListOf(stale), mutableListOf(), 3)
        val away = club("away", 202, mutableListOf(), mutableListOf(), 3)
        val state = LegacyMatchTransientRuntime.State(2026, home, away)

        val bridge = LegacyManagerMatchRuntimeBridge.applyLineup(state, lineup.matchLists)

        assertTrue(bridge.applied)
        assertSame(starter, home.active.single())
        assertSame(incoming, home.bench.single())
        assertSame(starter, bridge.startersMirror.single())

        val event = LegacyMatchTransientRuntime.applyEvent(
            state = state,
            legacyType = 5,
            legacySubtype = -1,
            eventClub = home,
            originalPrimary = starter,
            legacyPeriod = 2,
            legacyMinute = 7,
            random = QueueRandomSource(0, 0, 10),
        )

        assertTrue(event.substitutionApplied)
        assertFalse(home.active.contains(starter))
        assertSame(incoming, home.active.single())
        assertTrue(home.bench.isEmpty())
        assertSame(incoming, home.used.single())
        assertEquals(listOf(5, 6), state.events.map { it.legacyType })
    }

    @Test
    fun `away lineup only replaces the away runtime side and preserves source order`() {
        val homeOriginal = player("home-original", 4)
        val awayA = player("away-a", 2)
        val awayB = player("away-b", 3)
        val awayBench = player("away-bench", 0)
        val home = club("home", 101, mutableListOf(homeOriginal), mutableListOf(), 3)
        val away = club("away", 202, mutableListOf(player("stale-away", 5)), mutableListOf(), 3)
        val state = LegacyMatchTransientRuntime.State(2026, home, away)
        val lineup = LegacyLineupCommitRule.commit(
            starterSlots = listOf(
                LegacyLineupCommitSlot(awayA, 2),
                LegacyLineupCommitSlot(awayB, 3),
            ),
            benchPlayers = listOf(awayBench),
            eligibleRoster = listOf(awayBench),
            matchSideIndex = 1,
            mainTeamActivityPresent = false,
        )

        val result = LegacyManagerMatchRuntimeBridge.applyLineup(state, lineup.matchLists)

        assertTrue(result.applied)
        assertSame(homeOriginal, home.active.single())
        assertSame(awayA, away.active[0])
        assertSame(awayB, away.active[1])
        assertSame(awayBench, away.bench.single())
        assertSame(awayA, result.startersMirror[0])
        assertSame(awayB, result.startersMirror[1])
    }

    @Test
    fun `unsupported legacy side leaves existing match runtime lists untouched`() {
        val existing = player("existing", 8)
        val bench = player("existing-bench", 0)
        val home = club("home", 101, mutableListOf(existing), mutableListOf(bench), 3)
        val away = club("away", 202, mutableListOf(), mutableListOf(), 3)
        val state = LegacyMatchTransientRuntime.State(2026, home, away)
        val lineup = LegacyLineupCommitRule.commit(
            starterSlots = listOf(LegacyLineupCommitSlot(player("ignored", 4), 4)),
            benchPlayers = emptyList(),
            eligibleRoster = emptyList(),
            matchSideIndex = 9,
            mainTeamActivityPresent = false,
        )

        val result = LegacyManagerMatchRuntimeBridge.applyLineup(state, lineup.matchLists)

        assertFalse(result.applied)
        assertSame(existing, home.active.single())
        assertSame(bench, home.bench.single())
        assertTrue(away.active.isEmpty())
        assertTrue(away.bench.isEmpty())
    }

    private fun player(
        value: String,
        legacyG0: Int,
        legacyL0: Int = 1,
        legacyF0: Int = 1,
        legacyR: Int = 1,
    ) = LegacyMatchTransientRuntime.Player(
        value = value,
        legacyG0 = legacyG0,
        legacyL0 = legacyL0,
        legacyF0 = legacyF0,
        legacyR = legacyR,
        age = 25,
        energy = 50,
        skill = 80,
    )

    private fun club(
        value: String,
        id: Int,
        active: MutableList<LegacyMatchTransientRuntime.Player<String>>,
        bench: MutableList<LegacyMatchTransientRuntime.Player<String>>,
        substitutions: Int,
    ) = LegacyMatchTransientRuntime.Club(
        value = value,
        legacyClubId = id,
        active = active,
        bench = bench,
        substitutionsRemaining = substitutions,
    )

    private class QueueRandomSource(vararg values: Int) : RandomSource {
        private val queue = values.toMutableList()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            check(queue.isNotEmpty()) { "No queued RNG value for bound=$bound" }
            val value = queue.removeAt(0)
            require(value in 0 until bound) { "value=$value bound=$bound" }
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}
