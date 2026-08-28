package com.leomala.footballdynasty.domain.competition

import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyLargeFixedLeagueFixtureRulesTest {
    @Test
    fun `best j e nineteen-club double-cycle matrix fingerprint is exact`() {
        val rounds = LegacyLargeFixedLeagueFixtureRules.legacyENineteenClubs(ids(19))
        assertEquals(38, rounds.size)
        assertEquals(9, rounds.first().size)
        assertEquals(
            "0ffcaab267ced2ec542f7d169abe559d4c2304c66a0f508f03907c480bc8f7a0",
            fingerprint(rounds),
        )
    }

    @Test
    fun `best j f twenty-five-club matrix fingerprint is exact`() {
        val rounds = LegacyLargeFixedLeagueFixtureRules.legacyFTwentyFiveClubs(ids(25))
        assertEquals(25, rounds.size)
        assertEquals(12, rounds.first().size)
        assertEquals(
            "5aee106fca7be667875cce65ef55fa4012fafc62ab6f623507e5f578e940b6ef",
            fingerprint(rounds),
        )
    }

    private fun ids(count: Int): List<String> = List(count) { it.toString() }

    private fun fingerprint(rounds: List<List<LegacyLeagueFixtureRules.Fixture>>): String {
        val canonical = rounds.joinToString("|") { round ->
            round.joinToString(",") { fixture ->
                "${fixture.homeClubId}-${fixture.awayClubId}"
            }
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
