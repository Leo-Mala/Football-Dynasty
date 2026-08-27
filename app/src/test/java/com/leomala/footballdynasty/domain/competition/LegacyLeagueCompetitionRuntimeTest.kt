package com.leomala.footballdynasty.domain.competition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyLeagueCompetitionRuntimeTest {
    @Test
    fun `three legacy rounds advance U from one through competition end`() {
        var state = LegacyLeagueCompetitionRuntime.create(
            clubIds = listOf("A", "B", "C", "D"),
            legacyCycleCode = 1,
        )

        assertEquals(1, state.currentRoundNumber)
        assertEquals(3, state.totalRounds)
        assertFalse(state.finished)

        state = LegacyLeagueCompetitionRuntime.completeCurrentRound(
            state,
            listOf(
                r("A", "D", 2, 0),
                r("B", "C", 1, 1),
            ),
        )
        assertEquals(2, state.currentRoundNumber)
        assertEquals(listOf("A", "B", "C", "D"), state.standings.map { it.clubId })

        state = LegacyLeagueCompetitionRuntime.completeCurrentRound(
            state,
            listOf(
                r("D", "C", 3, 0),
                r("A", "B", 0, 1),
            ),
        )
        assertEquals(3, state.currentRoundNumber)
        assertEquals(listOf("B", "D", "A", "C"), state.standings.map { it.clubId })

        state = LegacyLeagueCompetitionRuntime.completeCurrentRound(
            state,
            listOf(
                r("B", "D", 0, 2),
                r("C", "A", 3, 3),
            ),
        )

        assertEquals(4, state.currentRoundNumber)
        assertTrue(state.finished)
        assertTrue(state.currentPlayableFixtures().isEmpty())
        assertEquals(listOf("D", "A", "B", "C"), state.standings.map { it.clubId })
        assertEquals(
            LegacyLeagueStandingsRules.Row(
                clubId = "D",
                points = 6,
                played = 3,
                wins = 2,
                losses = 1,
                goalsFor = 5,
                goalsAgainst = 2,
            ),
            state.standings[0],
        )
        assertEquals(4, state.standings[1].points)
        assertEquals(1, state.standings[1].draws)
        assertEquals(1, state.standings[1].goalDifference)
    }

    @Test
    fun `round results must follow the exact generated fixture order`() {
        val state = LegacyLeagueCompetitionRuntime.create(
            clubIds = listOf("A", "B", "C", "D"),
            legacyCycleCode = 1,
        )

        val error = runCatching {
            LegacyLeagueCompetitionRuntime.completeCurrentRound(
                state,
                listOf(
                    r("B", "C", 1, 0),
                    r("A", "D", 2, 0),
                ),
            )
        }.exceptionOrNull()

        assertTrue(error is IllegalArgumentException)
    }

    @Test
    fun `completed competition cannot process another round`() {
        var state = LegacyLeagueCompetitionRuntime.create(
            clubIds = listOf("A", "B"),
            legacyCycleCode = 1,
        )
        state = LegacyLeagueCompetitionRuntime.completeCurrentRound(
            state,
            listOf(r("A", "B", 1, 0)),
        )
        assertTrue(state.finished)

        val error = runCatching {
            LegacyLeagueCompetitionRuntime.completeCurrentRound(state, emptyList())
        }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }

    private fun r(
        home: String,
        away: String,
        homeGoals: Int,
        awayGoals: Int,
    ) = LegacyLeagueCompetitionRuntime.ResolvedFixture(home, away, homeGoals, awayGoals)
}
