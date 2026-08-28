package com.leomala.footballdynasty.domain.competition

import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LegacyGroupFixtureRulesTest {
    @Test
    fun `best j c two groups of six fingerprint is exact`() {
        val rounds = LegacyGroupFixtureRules.legacyCTwoGroupsOfSix(groups(2, 6))
        assertEquals(6, rounds.size)
        assertEquals(6, rounds.first().size)
        assertEquals(
            "a910821493215e1d64a5b012cc9ae6ffb7a96bc1327dc6656be88670fc810c54",
            fingerprint(rounds),
        )
    }

    @Test
    fun `best j d two groups of eight fingerprint is exact`() {
        val rounds = LegacyGroupFixtureRules.legacyDTwoGroupsOfEight(groups(2, 8))
        assertEquals(8, rounds.size)
        assertEquals(8, rounds.first().size)
        assertEquals(
            "a002ce9fb8a9b92a048542a8c38f50e45af57a18b2b20e51ebb2108155433d9b",
            fingerprint(rounds),
        )
    }

    @Test
    fun `best j k four groups of five fingerprint is exact`() {
        val rounds = LegacyGroupFixtureRules.legacyKFourGroupsOfFive(groups(4, 5))
        assertEquals(15, rounds.size)
        assertEquals(10, rounds.first().size)
        assertEquals(
            "d9cc1744970ca7e0a73756d6b80e6fe806ae6443cb822aed525d312d4da3eb3b",
            fingerprint(rounds),
        )
    }

    @Test
    fun `best j l four groups of four fingerprint is exact`() {
        val rounds = LegacyGroupFixtureRules.legacyLFourGroupsOfFour(groups(4, 4))
        assertEquals(12, rounds.size)
        assertEquals(8, rounds.first().size)
        assertEquals(
            "75c1ff25fabaa08129ec760d8709b1a2aa01f9f2b4f1be26cdbbafc396f9071b",
            fingerprint(rounds),
        )
    }

    @Test
    fun `group matrix rejects duplicate club identity across groups`() {
        val invalid = groups(2, 6).map { it.toMutableList() }.toMutableList()
        invalid[1][0] = invalid[0][0]
        assertThrows(IllegalArgumentException::class.java) {
            LegacyGroupFixtureRules.legacyCTwoGroupsOfSix(invalid)
        }
    }

    private fun groups(groupCount: Int, clubsPerGroup: Int): List<List<String>> =
        List(groupCount) { group ->
            List(clubsPerGroup) { club -> "g${group + 1}c${club + 1}" }
        }

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
