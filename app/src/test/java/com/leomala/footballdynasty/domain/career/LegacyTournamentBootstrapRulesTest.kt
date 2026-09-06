package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyTournamentBootstrapRulesTest {
    private data class Club(val id: String, val rawJ: Int, val rawJ0: Int = 0)

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
    fun `prior participants route from six legacy sources and preserve callback order`() {
        val e = Club("e", 0)
        val f = Club("f", 0)
        val h = Club("h", 0)
        val g = Club("g", 0)
        val j = Club("j", 0)
        val i = Club("i", 0)

        val result = LegacyTournamentBootstrapRules.recoverPriorParticipants(
            y0 = present(e),
            v0 = present(f),
            x0 = present(h),
            w0 = present(g),
            z0 = present(j),
            b0 = present(i),
        ) { it.rawJ0 }

        assertEquals(e, result.e)
        assertEquals(f, result.f)
        assertEquals(h, result.h)
        assertEquals(g, result.g)
        assertEquals(j, result.j)
        assertEquals(i, result.i)
        assertEquals(
            listOf(
                LegacyTournamentBootstrapRules.PriorSource.Y0 to e,
                LegacyTournamentBootstrapRules.PriorSource.V0 to f,
                LegacyTournamentBootstrapRules.PriorSource.X0 to h,
                LegacyTournamentBootstrapRules.PriorSource.W0 to g,
                LegacyTournamentBootstrapRules.PriorSource.Z0 to j,
                LegacyTournamentBootstrapRules.PriorSource.B0 to i,
            ),
            result.postSelectionCalls.map { it.source to it.participant },
        )
    }

    @Test
    fun `present prior source emits callback even when first participant is null`() {
        val absent = LegacyTournamentBootstrapRules.PriorCompetition<Club>(present = false, first = null)
        val presentNull = LegacyTournamentBootstrapRules.PriorCompetition<Club>(present = true, first = null)

        val result = LegacyTournamentBootstrapRules.recoverPriorParticipants(
            y0 = presentNull,
            v0 = absent,
            x0 = absent,
            w0 = absent,
            z0 = absent,
            b0 = absent,
        ) { it.rawJ0 }

        assertNull(result.e)
        assertEquals(1, result.postSelectionCalls.size)
        assertEquals(LegacyTournamentBootstrapRules.PriorSource.Y0, result.postSelectionCalls.single().source)
        assertNull(result.postSelectionCalls.single().participant)
    }

    @Test
    fun `v0 raw j0 131 replaces F only after callback remains anchored to first participant`() {
        val first = Club("v0-first", 0, rawJ0 = 131)
        val replacement = Club("v0-k0", 0)
        val absent = LegacyTournamentBootstrapRules.PriorCompetition<Club>(present = false, first = null)

        val result = LegacyTournamentBootstrapRules.recoverPriorParticipants(
            y0 = absent,
            v0 = LegacyTournamentBootstrapRules.PriorCompetition(
                present = true,
                first = first,
                v0ReplacementWhenRawJ0Is131 = replacement,
            ),
            x0 = absent,
            w0 = absent,
            z0 = absent,
            b0 = absent,
        ) { it.rawJ0 }

        assertEquals(replacement, result.f)
        assertEquals(
            listOf(LegacyTournamentBootstrapRules.PriorSource.V0 to first),
            result.postSelectionCalls.map { it.source to it.participant },
        )
    }

    @Test
    fun `v0 keeps first participant when raw j0 is not 131`() {
        val first = Club("v0-first", 0, rawJ0 = 130)
        val replacement = Club("unused", 0)
        val absent = LegacyTournamentBootstrapRules.PriorCompetition<Club>(present = false, first = null)

        val result = LegacyTournamentBootstrapRules.recoverPriorParticipants(
            y0 = absent,
            v0 = LegacyTournamentBootstrapRules.PriorCompetition(
                present = true,
                first = first,
                v0ReplacementWhenRawJ0Is131 = replacement,
            ),
            x0 = absent,
            w0 = absent,
            z0 = absent,
            b0 = absent,
        ) { it.rawJ0 }

        assertEquals(first, result.f)
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

    private fun present(club: Club): LegacyTournamentBootstrapRules.PriorCompetition<Club> =
        LegacyTournamentBootstrapRules.PriorCompetition(present = true, first = club)
}
