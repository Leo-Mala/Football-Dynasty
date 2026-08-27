package com.leomala.footballdynasty.domain.competition

import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyFixedLeagueFixtureRulesTest {
    @Test
    fun `best j g three-club matrix fingerprint is exact`() {
        val rounds = LegacyFixedLeagueFixtureRules.legacyGThreeClubs(ids(3))
        assertEquals(6, rounds.size)
        assertEquals(1, rounds.single().size)
        assertEquals(
            "9b1ce217354a02b719edf6d5c9fecda180e0464a3d79d8a35dca6f781e9d0b20",
            fingerprint(rounds),
        )
    }

    @Test
    fun `best j h ten-round five-club matrix fingerprint is exact`() {
        val rounds = LegacyFixedLeagueFixtureRules.legacyHFiveClubs(ids(5))
        assertEquals(10, rounds.size)
        assertEquals(2, rounds.first().size)
        assertEquals(
            "9b4d79c3b7c2eb0a943b642c96a0b2ee7389dedb753ae90f9aa0d7e26bd58341",
            fingerprint(rounds),
        )
    }

    @Test
    fun `best j i five-round five-club matrix fingerprint is exact`() {
        val rounds = LegacyFixedLeagueFixtureRules.legacyIFiveClubs(ids(5))
        assertEquals(5, rounds.size)
        assertEquals(2, rounds.first().size)
        assertEquals(
            "c5f336756f0e62e560fffb59ec0a090b6ec37a57624c1fb93fab8f83602bb0e4",
            fingerprint(rounds),
        )
    }

    @Test
    fun `best j j nine-club single-cycle fingerprint is exact`() {
        val rounds = LegacyFixedLeagueFixtureRules.legacyJNineClubs(ids(9), reverseSecondCycle = false)
        assertEquals(9, rounds.size)
        assertEquals(4, rounds.first().size)
        assertEquals(
            "5703e8ecc1f4e22b50f3be2e54bd00a46ffbd02231bb0eae1d59d9babafe8967",
            fingerprint(rounds),
        )
    }

    @Test
    fun `best j j nine-club second cycle is the exact reversed matrix`() {
        val rounds = LegacyFixedLeagueFixtureRules.legacyJNineClubs(ids(9), reverseSecondCycle = true)
        assertEquals(18, rounds.size)
        assertEquals(
            "0c6456deacc927b86474f5663a650f7b9807c0899f7027cd478f674fca0954c4",
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
