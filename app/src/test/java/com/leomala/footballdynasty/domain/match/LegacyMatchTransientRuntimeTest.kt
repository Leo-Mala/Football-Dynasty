package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchTransientRuntimeTest {
    @Test
    fun `goal event is appended and score is rebuilt from the event ledger`() {
        val fixture = fixture()

        LegacyMatchTransientRuntime.applyEvent(
            state = fixture.state,
            legacyType = 1,
            legacySubtype = 1,
            eventClub = fixture.home,
            originalPrimary = fixture.original,
            legacyPeriod = 1,
            legacyMinute = 12,
            random = QueueRandomSource(),
        )

        assertEquals(1, fixture.state.events.size)
        assertEquals(1, fixture.state.score().legacyE)
        assertEquals(0, fixture.state.score().legacyF)
        assertSame(fixture.home, fixture.state.events.single().legacyClub)
    }

    @Test
    fun `first legacy C increments player yellow count and stat M without removing player`() {
        val fixture = fixture()

        LegacyMatchTransientRuntime.applyLegacyC(
            state = fixture.state,
            club = fixture.home,
            player = fixture.original,
            legacyPeriod = 1,
            legacyMinute = 20,
            random = QueueRandomSource(),
        )

        assertEquals(1, fixture.original.legacyYellowCount)
        assertEquals(1, fixture.original.legacyStatM)
        assertEquals(0, fixture.original.legacyStatN)
        assertTrue(fixture.home.active.contains(fixture.original))
        assertEquals(listOf(2), fixture.state.events.map { it.legacyType })
    }

    @Test
    fun `second legacy C removes original then performs recovered automatic substitution`() {
        val fixture = fixture(originalYellowCount = 1, includeAutomaticOutgoing = true)

        val result = LegacyMatchTransientRuntime.applyLegacyC(
            state = fixture.state,
            club = fixture.home,
            player = fixture.original,
            legacyPeriod = 2,
            legacyMinute = 18,
            random = QueueRandomSource(),
        )

        assertTrue(result.substitutionApplied)
        assertEquals(2, fixture.original.legacyYellowCount)
        assertEquals(1, fixture.original.legacyStatM)
        assertEquals(1, fixture.original.legacyStatN)
        assertFalse(fixture.home.active.contains(fixture.original))
        assertFalse(fixture.home.active.contains(fixture.automaticOutgoing))
        assertTrue(fixture.home.active.contains(fixture.incoming))
        assertTrue(fixture.home.used.contains(fixture.incoming))
        assertFalse(fixture.home.bench.contains(fixture.incoming))
        assertTrue(fixture.incoming.selectedOrUsed)
        assertEquals(10, fixture.incoming.legacyG0)
        assertEquals(2, fixture.home.substitutionsRemaining)
        assertEquals(listOf(3, 6), fixture.state.events.map { it.legacyType })
        val substitution = fixture.state.events.last()
        assertSame(fixture.automaticOutgoing, substitution.primaryPlayer)
        assertSame(fixture.incoming, substitution.secondaryPlayer)
        assertEquals(2, substitution.legacyPeriod)
        assertEquals(18, substitution.legacyMinute)
    }

    @Test
    fun `red card with no substitutions left removes original but emits no substitution event`() {
        val fixture = fixture(substitutionsRemaining = 0)

        val result = LegacyMatchTransientRuntime.applyLegacyD(
            state = fixture.state,
            club = fixture.home,
            player = fixture.original,
            legacyPeriod = 1,
            legacyMinute = 35,
            random = QueueRandomSource(),
        )

        assertFalse(result.substitutionApplied)
        assertEquals(0, fixture.original.legacyStatM)
        assertEquals(1, fixture.original.legacyStatN)
        assertFalse(fixture.home.active.contains(fixture.original))
        assertEquals(listOf(4), fixture.state.events.map { it.legacyType })
    }

    @Test
    fun `injury applies recovered injury result then club season stat and substitution`() {
        val fixture = fixture(
            originalAge = 35,
            originalSkill = 5,
            originalLegacyL0 = 1,
        )

        val result = LegacyMatchTransientRuntime.applyEvent(
            state = fixture.state,
            legacyType = 5,
            legacySubtype = -1,
            eventClub = fixture.home,
            originalPrimary = fixture.original,
            legacyPeriod = 2,
            legacyMinute = 7,
            random = QueueRandomSource(0, 0, 10),
        )

        assertTrue(result.substitutionApplied)
        assertEquals(3, result.injuryResult?.durationDays)
        assertEquals(0, fixture.original.skill)
        assertEquals(3, fixture.original.lastInjuryResult?.durationDays)
        assertFalse(fixture.home.active.contains(fixture.original))
        assertTrue(fixture.home.active.contains(fixture.incoming))
        assertEquals(2, fixture.home.substitutionsRemaining)
        assertEquals(listOf(5, 6), fixture.state.events.map { it.legacyType })
        val stats = fixture.original.clubSeasonStats.orEmpty().single()
        assertEquals(2026, stats.legacySeasonId)
        assertEquals(101, stats.legacyClubId)
        assertEquals(1, stats.legacyH)
    }

    @Test
    fun `subtype two can replace event primary while effects remain on original primary`() {
        val fixture = fixture()
        val opposite = player("opposite", legacyG0 = 10)
        fixture.away.active += opposite

        val result = LegacyMatchTransientRuntime.applyEvent(
            state = fixture.state,
            legacyType = 2,
            legacySubtype = 2,
            eventClub = fixture.home,
            originalPrimary = fixture.original,
            legacyPeriod = 1,
            legacyMinute = 8,
            random = QueueRandomSource(100),
        )

        assertTrue(result.event.primaryPlayer === opposite)
        assertEquals(1, fixture.original.legacyStatM)
        assertEquals(0, opposite.legacyStatM)
        assertTrue(fixture.home.active.contains(fixture.original))
    }

    @Test
    fun `reference identity prevents removal of structurally identical twin player`() {
        val fixture = fixture(substitutionsRemaining = 0)
        val twin = player(
            value = fixture.original.value,
            legacyG0 = fixture.original.legacyG0,
            legacyL0 = fixture.original.legacyL0,
            legacyF0 = fixture.original.legacyF0,
            legacyR = fixture.original.legacyR,
        )
        fixture.home.active += twin

        LegacyMatchTransientRuntime.applyLegacyD(
            state = fixture.state,
            club = fixture.home,
            player = fixture.original,
            legacyPeriod = 1,
            legacyMinute = 40,
            random = QueueRandomSource(),
        )

        assertFalse(fixture.home.active.contains(fixture.original))
        assertTrue(fixture.home.active.contains(twin))
        assertSame(twin, fixture.home.active.single())
    }

    @Test
    fun `injury with null club does not silently redirect its persistent stat to home side`() {
        val fixture = fixture()
        var threw = false
        try {
            LegacyMatchTransientRuntime.applyEvent(
                state = fixture.state,
                legacyType = 5,
                legacySubtype = -1,
                eventClub = null,
                originalPrimary = fixture.original,
                legacyPeriod = 1,
                legacyMinute = 1,
                random = QueueRandomSource(0, 0, 10),
            )
        } catch (_: IllegalStateException) {
            threw = true
        }
        assertTrue(threw)
        assertTrue(fixture.original.clubSeasonStats.orEmpty().isEmpty())
    }

    private data class Fixture(
        val state: LegacyMatchTransientRuntime.State<String, String>,
        val home: LegacyMatchTransientRuntime.Club<String, String>,
        val away: LegacyMatchTransientRuntime.Club<String, String>,
        val original: LegacyMatchTransientRuntime.Player<String>,
        val automaticOutgoing: LegacyMatchTransientRuntime.Player<String>,
        val incoming: LegacyMatchTransientRuntime.Player<String>,
    )

    private fun fixture(
        substitutionsRemaining: Int = 3,
        originalYellowCount: Int = 0,
        includeAutomaticOutgoing: Boolean = false,
        originalAge: Int = 25,
        originalSkill: Int = 80,
        originalLegacyL0: Int = 1,
    ): Fixture {
        val original = player(
            "original",
            legacyG0 = 10,
            legacyL0 = originalLegacyL0,
            age = originalAge,
            skill = originalSkill,
        ).also { it.legacyYellowCount = originalYellowCount }
        val automaticOutgoing = player("automatic", legacyG0 = 18, legacyL0 = 0, legacyF0 = 1, legacyR = 0)
        val incoming = player("incoming", legacyG0 = 0, legacyL0 = 1, legacyF0 = 1, legacyR = 1)
        val homeActive = mutableListOf(original)
        if (includeAutomaticOutgoing) homeActive += automaticOutgoing
        val home = LegacyMatchTransientRuntime.Club(
            value = "home",
            legacyClubId = 101,
            active = homeActive,
            bench = mutableListOf(incoming),
            substitutionsRemaining = substitutionsRemaining,
        )
        val away = LegacyMatchTransientRuntime.Club<String, String>(
            value = "away",
            legacyClubId = 202,
            active = mutableListOf(),
            bench = mutableListOf(),
            substitutionsRemaining = 3,
        )
        return Fixture(
            state = LegacyMatchTransientRuntime.State(2026, home, away),
            home = home,
            away = away,
            original = original,
            automaticOutgoing = automaticOutgoing,
            incoming = incoming,
        )
    }

    private fun player(
        value: String,
        legacyG0: Int,
        legacyL0: Int = 1,
        legacyF0: Int = 1,
        legacyR: Int = 1,
        age: Int = 25,
        skill: Int = 80,
    ) = LegacyMatchTransientRuntime.Player(
        value = value,
        legacyG0 = legacyG0,
        legacyL0 = legacyL0,
        legacyF0 = legacyF0,
        legacyR = legacyR,
        age = age,
        energy = 50,
        skill = skill,
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
