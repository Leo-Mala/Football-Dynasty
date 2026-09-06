package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyTournamentBootstrapRulesTest {
    private data class Club(val id: String, val rawJ: Int)

    @Test
    fun `bootstrap begins with all eight owned references cleared`() {
        val result = LegacyTournamentBootstrapRules.clearOwnedReferences<Club>()

        assertNull(result.o)
        assertNull(result.dUpper)
        assertNull(result.e)
        assertNull(result.f)
        assertNull(result.h)
        assertNull(result.g)
        assertNull(result.j)
        assertNull(result.i)
    }

    @Test
    fun `eligible candidates preserve source order and require raw J greater than one`() {
        val source = listOf(
            Club("j1", 1),
            Club("j2a", 2),
            Club("j0", 0),
            Club("j5", 5),
            Club("j2b", 2),
        )

        val result = LegacyTournamentBootstrapRules.eligibleCandidates(source) { it.rawJ }

        assertEquals(listOf("j2a", "j5", "j2b"), result.map { it.id })
    }

    @Test
    fun `fallback fills only missing tier slots with first match in shuffled order`() {
        val existingTier3 = Club("existing3", 3)
        val shuffled = listOf(
            Club("j4-first", 4),
            Club("j2-first", 2),
            Club("j4-second", 4),
            Club("j3-ignored", 3),
            Club("j5-first", 5),
            Club("j2-second", 2),
        )

        val result = LegacyTournamentBootstrapRules.fillMissingTierSlots(
            shuffledCandidates = shuffled,
            initial = LegacyTournamentBootstrapRules.TierSlots(
                tier2 = null,
                tier3 = existingTier3,
                tier4 = null,
                tier5 = null,
            ),
        ) { it.rawJ }

        assertEquals("j2-first", result.tier2?.id)
        assertEquals("existing3", result.tier3?.id)
        assertEquals("j4-first", result.tier4?.id)
        assertEquals("j5-first", result.tier5?.id)
    }

    @Test
    fun `legacy local random draw maps to the three executable four club orders`() {
        assertArrayEquals(intArrayOf(2, 0, 3, 1), LegacyTournamentBootstrapRules.fourClubOrder(0))
        assertArrayEquals(intArrayOf(3, 2, 1, 0), LegacyTournamentBootstrapRules.fourClubOrder(1))
        assertArrayEquals(intArrayOf(0, 2, 3, 1), LegacyTournamentBootstrapRules.fourClubOrder(2))
    }
}