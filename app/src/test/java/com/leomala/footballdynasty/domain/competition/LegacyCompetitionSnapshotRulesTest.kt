package com.leomala.footballdynasty.domain.competition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCompetitionSnapshotRulesTest {
    @Test
    fun `annual snapshot uses exact selector order count threshold and current club`() {
        val candidates = listOf(
            annual("gk", "old-club", average = 9.0, count = 1.0, selector = 0, ordinal = 0),
            annual("gk2", "club-gk2", average = 8.0, count = 3.0, selector = 0, ordinal = 1),
            annual("s1", "club-s1", average = 7.0, count = 3.0, selector = 1, ordinal = 2),
            annual("s2a", "club-s2a", average = 9.0, count = 3.0, selector = 2, ordinal = 3),
            annual("s2b", "club-s2b", average = 8.0, count = 3.0, selector = 2, ordinal = 4),
        )

        val result = LegacyCompetitionSnapshotRules.annual(
            candidates = candidates,
            threshold = 2,
            legacyYear = 2030,
            legacyCompetitionType = 1,
            competitionIndex = 0,
        )

        assertEquals(2030, result.legacyA)
        assertEquals(0, result.legacyB)
        assertEquals(listOf("gk2", "s1", "s2a", "s2b"), result.members.map { it.playerId })
        assertEquals("club-gk2", result.members.first().clubIdAtSnapshot)
        assertEquals("s2a", result.topPlayerId)
        assertTrue(result.markTopPlayerStar)
    }

    @Test
    fun `annual comparator is average descending then count descending then stable insertion order`() {
        val result = LegacyCompetitionSnapshotRules.annual(
            candidates = listOf(
                annual("late", "c1", average = 8.0, count = 4.0, selector = 0, ordinal = 5),
                annual("early", "c2", average = 8.0, count = 4.0, selector = 0, ordinal = 2),
                annual("lower-count", "c3", average = 8.0, count = 3.0, selector = 1, ordinal = 0),
            ),
            threshold = 0,
            legacyYear = 2031,
            legacyCompetitionType = 2,
            competitionIndex = 4,
        )

        assertEquals(listOf("early", "lower-count"), result.members.map { it.playerId })
        assertEquals("early", result.topPlayerId)
        assertFalse(result.markTopPlayerStar)
    }

    @Test
    fun `round snapshot uses exact g0 ranges and rating then random tie break ordering`() {
        val result = LegacyCompetitionSnapshotRules.round(
            candidates = listOf(
                round("gk-low-random", "club-a", rating = 8.0, g0 = 1, random = 10, ordinal = 0),
                round("gk-high-random", "club-b", rating = 8.0, g0 = 1, random = 90, ordinal = 1),
                round("slot9", "club-c", rating = 7.0, g0 = 9, random = 0, ordinal = 2),
                round("slot3", "club-d", rating = 7.5, g0 = 3, random = 0, ordinal = 3),
                round("slot8", "club-e", rating = 7.0, g0 = 8, random = 0, ordinal = 4),
                round("slot25", "club-f", rating = 6.0, g0 = 25, random = 0, ordinal = 5),
            ),
            legacyYear = 2032,
            existingSnapshotCount = 3,
        )

        assertEquals(2032, result.legacyA)
        assertEquals(2, result.legacyB)
        assertEquals(
            listOf("gk-high-random", "slot9", "slot3", "slot8", "slot25"),
            result.members.map { it.playerId },
        )
        assertNull(result.topPlayerId)
        assertFalse(result.markTopPlayerStar)
    }

    @Test
    fun `first round snapshot preserves legacy size minus one raw b value`() {
        val result = LegacyCompetitionSnapshotRules.round(
            candidates = emptyList(),
            legacyYear = 2033,
            existingSnapshotCount = 0,
        )

        assertEquals(-1, result.legacyB)
        assertTrue(result.members.isEmpty())
    }

    private fun annual(
        player: String,
        club: String,
        average: Double,
        count: Double,
        selector: Int,
        ordinal: Int,
    ) = LegacyCompetitionSnapshotRules.AnnualCandidate(
        playerId = player,
        clubIdAtSnapshot = club,
        legacyAverage = average,
        legacyCount = count,
        legacySelector = selector,
        stableOrdinal = ordinal,
    )

    private fun round(
        player: String,
        club: String,
        rating: Double,
        g0: Int,
        random: Int,
        ordinal: Int,
    ) = LegacyCompetitionSnapshotRules.RoundCandidate(
        playerId = player,
        clubIdAtSnapshot = club,
        ratingY0 = rating,
        legacyG0 = g0,
        randomOrder = random,
        stableOrdinal = ordinal,
    )
}
