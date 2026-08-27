package com.leomala.footballdynasty.domain.competition

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyLeagueStandingsRulesTest {
    @Test
    fun `win draw and loss mutate the exact best e0 counters`() {
        var row = LegacyLeagueStandingsRules.Row("A")

        row = LegacyLeagueStandingsRules.applyResult(row, goalsFor = 2, goalsAgainst = 1)
        assertEquals(
            LegacyLeagueStandingsRules.Row(
                clubId = "A",
                points = 3,
                played = 1,
                wins = 1,
                losses = 0,
                goalsFor = 2,
                goalsAgainst = 1,
            ),
            row,
        )
        assertEquals(0, row.draws)
        assertEquals(1, row.goalDifference)

        row = LegacyLeagueStandingsRules.applyResult(row, goalsFor = 0, goalsAgainst = 0)
        assertEquals(4, row.points)
        assertEquals(2, row.played)
        assertEquals(1, row.wins)
        assertEquals(0, row.losses)
        assertEquals(1, row.draws)

        row = LegacyLeagueStandingsRules.applyResult(row, goalsFor = 1, goalsAgainst = 3)
        assertEquals(4, row.points)
        assertEquals(3, row.played)
        assertEquals(1, row.wins)
        assertEquals(1, row.losses)
        assertEquals(1, row.draws)
        assertEquals(3, row.goalsFor)
        assertEquals(4, row.goalsAgainst)
        assertEquals(-1, row.goalDifference)
    }

    @Test
    fun `one resolved match updates only its two clubs`() {
        val initial = listOf(
            LegacyLeagueStandingsRules.Row("A"),
            LegacyLeagueStandingsRules.Row("B"),
            LegacyLeagueStandingsRules.Row("C", points = 7, played = 3, wins = 2, losses = 0, goalsFor = 5, goalsAgainst = 2),
        )

        val updated = LegacyLeagueStandingsRules.applyMatch(
            rows = initial,
            homeClubId = "A",
            awayClubId = "B",
            homeGoals = 2,
            awayGoals = 2,
        )

        assertEquals(1, updated[0].points)
        assertEquals(1, updated[0].played)
        assertEquals(2, updated[0].goalsFor)
        assertEquals(2, updated[0].goalsAgainst)
        assertEquals(1, updated[1].points)
        assertEquals(1, updated[1].played)
        assertEquals(initial[2], updated[2])
    }

    @Test
    fun `ranking follows points wins goal difference then goals for`() {
        val rows = listOf(
            row("lower-points", points = 9, wins = 9, gd = 20, gf = 30),
            row("lower-wins", points = 10, wins = 2, gd = 20, gf = 30),
            row("lower-gd", points = 10, wins = 3, gd = 4, gf = 30),
            row("lower-gf", points = 10, wins = 3, gd = 5, gf = 10),
            row("top", points = 10, wins = 3, gd = 5, gf = 11),
        )

        assertEquals(
            listOf("top", "lower-gf", "lower-gd", "lower-wins", "lower-points"),
            LegacyLeagueStandingsRules.rank(rows).map { it.clubId },
        )
    }

    @Test
    fun `complete comparator tie preserves existing table order`() {
        val rows = listOf(
            row("first", points = 10, wins = 3, gd = 5, gf = 11),
            row("second", points = 10, wins = 3, gd = 5, gf = 11),
            row("third", points = 10, wins = 3, gd = 5, gf = 11),
        )

        assertEquals(
            listOf("first", "second", "third"),
            LegacyLeagueStandingsRules.rank(rows).map { it.clubId },
        )
    }

    @Test
    fun `draws and goal difference are derived rather than separately mutated`() {
        val row = LegacyLeagueStandingsRules.Row(
            clubId = "A",
            points = 8,
            played = 5,
            wins = 2,
            losses = 1,
            goalsFor = 9,
            goalsAgainst = 7,
        )

        assertEquals(2, row.draws)
        assertEquals(2, row.goalDifference)
    }

    private fun row(
        id: String,
        points: Int,
        wins: Int,
        gd: Int,
        gf: Int,
    ): LegacyLeagueStandingsRules.Row {
        val ga = gf - gd
        return LegacyLeagueStandingsRules.Row(
            clubId = id,
            points = points,
            played = wins,
            wins = wins,
            losses = 0,
            goalsFor = gf,
            goalsAgainst = ga,
        )
    }
}
