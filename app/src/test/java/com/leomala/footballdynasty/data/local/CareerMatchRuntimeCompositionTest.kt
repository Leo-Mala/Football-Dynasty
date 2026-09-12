package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime
import com.leomala.footballdynasty.domain.model.Match
import com.leomala.footballdynasty.foundation.random.SeededRandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerMatchRuntimeCompositionTest {
    @Test
    fun `unwired composition fails closed without consuming career rng`() {
        val composition = CareerMatchRuntimeComposition.unwired()
        val random = SeededRandomSource(17L)

        assertFalse(composition.available)
        val failure = runCatching {
            composition.simulate(
                scheduled = scheduled(),
                state = state(),
                homeTacticIndex = 0,
                awayTacticIndex = 0,
                random = random,
            )
        }.exceptionOrNull()

        assertTrue(failure is IllegalArgumentException)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `wired composition forwards exact career rng and runtime references`() {
        val scheduled = scheduled()
        val state = state()
        val random = SeededRandomSource(41L)
        var seenScheduled: ScheduledCareerMatch? = null
        var seenState: CareerMatchTransientState? = null
        var seenRandom: Any? = null
        var seenHomeTactic = -1
        var seenAwayTactic = -1

        val composition = CareerMatchRuntimeComposition.wired { match, transient, homeTactic, awayTactic, source ->
            seenScheduled = match
            seenState = transient
            seenRandom = source
            seenHomeTactic = homeTactic
            seenAwayTactic = awayTactic
            source.nextInt(100)
            Match(match.matchId, match.homeClubId, match.awayClubId, 1, 0)
        }

        val result = composition.simulate(
            scheduled = scheduled,
            state = state,
            homeTacticIndex = 3,
            awayTacticIndex = 7,
            random = random,
        )

        assertTrue(composition.available)
        assertSame(scheduled, seenScheduled)
        assertSame(state, seenState)
        assertSame(random, seenRandom)
        assertEquals(3, seenHomeTactic)
        assertEquals(7, seenAwayTactic)
        assertEquals(1L, random.draws)
        assertEquals(Match("match-1", "home", "away", 1, 0), result)
    }

    private fun scheduled() = ScheduledCareerMatch(
        matchId = "match-1",
        dayIndex = 0,
        eventTypeCode = 1,
        homeClubId = "home",
        awayClubId = "away",
    )

    private fun state(): CareerMatchTransientState = LegacyMatchTransientRuntime.State(
        currentSeasonId = 1,
        home = LegacyMatchTransientRuntime.Club(
            value = CareerMatchPersistedRuntimeResolver.PersistedClubRoster("home", 101, emptyList()),
            legacyClubId = 101,
            active = mutableListOf(),
            bench = mutableListOf(),
            substitutionsRemaining = 5,
            legacyModeFlag = false,
        ),
        away = LegacyMatchTransientRuntime.Club(
            value = CareerMatchPersistedRuntimeResolver.PersistedClubRoster("away", 202, emptyList()),
            legacyClubId = 202,
            active = mutableListOf(),
            bench = mutableListOf(),
            substitutionsRemaining = 5,
            legacyModeFlag = false,
        ),
    )
}
