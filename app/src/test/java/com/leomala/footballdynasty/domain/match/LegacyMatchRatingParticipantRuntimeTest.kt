package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class LegacyMatchRatingParticipantRuntimeTest {
    @Test
    fun `original starters remain before substitution entrants in side-local order`() {
        val h1 = player("h1")
        val h2 = player("h2")
        val hs = player("hs")
        val a1 = player("a1")
        val state = state(h1, h2, hs, a1)
        val snapshot = LegacyMatchRatingParticipantRuntime.capture(state)

        state.home.active.remove(h1)
        state.home.active.add(hs)
        state.home.bench.remove(hs)
        state.home.used.add(hs)

        val home = LegacyMatchRatingParticipantRuntime.playersInLegacyOrder(state, snapshot, 0)
        assertEquals(3, home.size)
        assertSame(h1, home[0])
        assertSame(h2, home[1])
        assertSame(hs, home[2])

        val away = LegacyMatchRatingParticipantRuntime.playersInLegacyOrder(state, snapshot, 1)
        assertEquals(1, away.size)
        assertSame(a1, away.single())
    }

    @Test
    fun `global rating traversal is u0 then v0 then F then G`() {
        val h1 = player("h1")
        val h2 = player("h2")
        val hs = player("hs")
        val a1 = player("a1")
        val asub = player("as")
        val state = state(h1, h2, hs, a1).also {
            it.away.bench += asub
        }
        val snapshot = LegacyMatchRatingParticipantRuntime.capture(state)
        state.home.used += hs
        state.away.used += asub

        val entries = LegacyMatchRatingParticipantRuntime.entriesInLegacyOrder(state, snapshot)

        assertEquals(listOf(0, 0, 1, 0, 1), entries.map { it.side })
        assertEquals(listOf("h1", "h2", "a1", "hs", "as"), entries.map { it.player.value })
    }

    private fun state(
        h1: LegacyMatchTransientRuntime.Player<String>,
        h2: LegacyMatchTransientRuntime.Player<String>,
        hs: LegacyMatchTransientRuntime.Player<String>,
        a1: LegacyMatchTransientRuntime.Player<String>,
    ) = LegacyMatchTransientRuntime.State(
        currentSeasonId = 1,
        home = LegacyMatchTransientRuntime.Club(
            value = "home",
            legacyClubId = 1,
            active = mutableListOf(h1, h2),
            bench = mutableListOf(hs),
            substitutionsRemaining = 3,
        ),
        away = LegacyMatchTransientRuntime.Club(
            value = "away",
            legacyClubId = 2,
            active = mutableListOf(a1),
            bench = mutableListOf(),
            substitutionsRemaining = 3,
        ),
    )

    private fun player(id: String) = LegacyMatchTransientRuntime.Player(
        value = id,
        legacyG0 = 14,
        legacyL0 = 3,
        legacyF0 = 0,
        legacyR = 1,
        age = 25,
        energy = 100,
        skill = 80,
    )
}
