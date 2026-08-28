package com.leomala.footballdynasty.domain.competition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyLeagueFixtureRulesTest {
    @Test
    fun `four clubs preserve exact best j b first-cycle ordering`() {
        val rounds = LegacyLeagueFixtureRules.generate(
            clubIds = listOf("A", "B", "C", "D"),
            legacyCycleCode = 1,
        )

        assertEquals(
            listOf(
                listOf(f("A", "D"), f("B", "C")),
                listOf(f("D", "C"), f("A", "B")),
                listOf(f("B", "D"), f("C", "A")),
            ),
            rounds,
        )
    }

    @Test
    fun `second cycle is exact home-away reversal of first cycle`() {
        val rounds = LegacyLeagueFixtureRules.generate(
            clubIds = listOf("A", "B", "C", "D"),
            legacyCycleCode = 2,
        )

        assertEquals(6, rounds.size)
        assertEquals(
            listOf(
                listOf(f("D", "A"), f("C", "B")),
                listOf(f("C", "D"), f("B", "A")),
                listOf(f("D", "B"), f("A", "C")),
            ),
            rounds.drop(3),
        )
    }

    @Test
    fun `odd-size legacy modulo quirk is preserved rather than normalized to byes`() {
        val rounds = LegacyLeagueFixtureRules.generate(
            clubIds = listOf("A", "B", "C", "D", "E"),
            legacyCycleCode = 1,
        )

        assertEquals(
            listOf(
                listOf(f("A", "E"), f("B", "D"), f("C", "C")),
                listOf(f("E", "D"), f("A", "C"), f("B", "B")),
                listOf(f("B", "E"), f("C", "A"), f("D", "D")),
                listOf(f("E", "A"), f("B", "D"), f("C", "C")),
                listOf(f("C", "E"), f("D", "B"), f("A", "A")),
            ),
            rounds,
        )
        assertTrue(rounds.flatten().any { it.homeClubId == it.awayClubId })
    }

    @Test
    fun `legacy cycle code outside two three four falls back to one cycle`() {
        val clubs = listOf("A", "B", "C", "D")
        assertEquals(
            LegacyLeagueFixtureRules.generate(clubs, 1),
            LegacyLeagueFixtureRules.generate(clubs, 99),
        )
    }

    @Test
    fun `three and four cycle codes repeat the exact alternating legacy blocks`() {
        val clubs = listOf("A", "B", "C", "D")
        val one = LegacyLeagueFixtureRules.generate(clubs, 1)
        val two = LegacyLeagueFixtureRules.generate(clubs, 2)
        val three = LegacyLeagueFixtureRules.generate(clubs, 3)
        val four = LegacyLeagueFixtureRules.generate(clubs, 4)

        assertEquals(one.size * 3, three.size)
        assertEquals(one.size * 4, four.size)
        assertEquals(two + one, three)
        assertEquals(two + two, four)
    }

    private fun f(home: String?, away: String?) = LegacyLeagueFixtureRules.Fixture(home, away)
}
