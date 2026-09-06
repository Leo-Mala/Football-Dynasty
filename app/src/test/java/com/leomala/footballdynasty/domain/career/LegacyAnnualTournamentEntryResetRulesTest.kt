package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAnnualTournamentEntryResetRulesTest {
    @Test
    fun `selector traversal preserves exact legacy order and multiplicity`() {
        assertEquals(
            listOf(0, 1, 2, 2, 5, 6, 6, 3, 3, 4, 4),
            LegacyAnnualTournamentEntryResetRules.planSelectorTraversal().map { it.selector },
        )
    }

    @Test
    fun `selector traversal preserves eleven fixed calls`() {
        val actions = LegacyAnnualTournamentEntryResetRules.planSelectorTraversal()

        assertEquals(11, actions.size)
        assertEquals((0 until 11).toList(), actions.map { it.ordinal })
    }

    @Test
    fun `duplicate selectors remain at their proven positions`() {
        val actions = LegacyAnnualTournamentEntryResetRules.planSelectorTraversal()

        assertEquals(2, actions[2].selector)
        assertEquals(2, actions[3].selector)
        assertEquals(6, actions[5].selector)
        assertEquals(6, actions[6].selector)
        assertEquals(3, actions[7].selector)
        assertEquals(3, actions[8].selector)
        assertEquals(4, actions[9].selector)
        assertEquals(4, actions[10].selector)
    }

    @Test
    fun `D0 threshold preserves raw integer formula and strict U comparison`() {
        assertEquals(
            0,
            LegacyAnnualTournamentEntryResetRules.calculateThreshold(
                rawU = 4,
                rawHSize = 5,
                rawA0 = 0,
            ),
        )
        assertEquals(
            2,
            LegacyAnnualTournamentEntryResetRules.calculateThreshold(
                rawU = 5,
                rawHSize = 5,
                rawA0 = 0,
            ),
        )
        assertEquals(
            2,
            LegacyAnnualTournamentEntryResetRules.calculateThreshold(
                rawU = 4,
                rawHSize = 8,
                rawA0 = 2,
            ),
        )
    }

    @Test
    fun `U sorting is average descending then count descending`() {
        val result =
            LegacyAnnualTournamentEntryResetRules.apply(
                index = 1,
                rawB = 0,
                rawU = 0,
                rawHSize = 0,
                rawA0 = 0,
                entries =
                    listOf(
                        entry("p-low", "c-low", count = 9.0, average = 7.0, selector = 0),
                        entry("p-tie-low", "c-tie-low", count = 2.0, average = 10.0, selector = 1),
                        entry("p-tie-high", "c-tie-high", count = 4.0, average = 10.0, selector = 2),
                    ),
            )

        assertEquals(
            listOf("p-tie-high", "p-tie-low", "p-low"),
            result.sortedEntries.map { it.player },
        )
    }

    @Test
    fun `selection uses inclusive threshold preserves club pairing and advances duplicate selector`() {
        val result =
            LegacyAnnualTournamentEntryResetRules.apply(
                index = 1,
                rawB = 0,
                rawU = 6,
                rawHSize = 5,
                rawA0 = 0,
                entries =
                    listOf(
                        entry("p0-below", "c0", count = 2.0, average = 20.0, selector = 0),
                        entry("p1", "c1", count = 3.0, average = 19.0, selector = 1),
                        entry("p2-first", "c2-first", count = 3.0, average = 18.0, selector = 2),
                        entry("p2-second", "c2-second", count = 5.0, average = 17.0, selector = 2),
                        entry("p1", "c6-duplicate-player", count = 8.0, average = 16.0, selector = 6),
                        entry("p6", "c6", count = 3.0, average = 15.0, selector = 6),
                    ),
            )

        assertEquals(3, result.threshold)
        assertEquals(
            listOf(
                LegacyAnnualTournamentEntryResetRules.SelectedEntry("p1", "c1", 1),
                LegacyAnnualTournamentEntryResetRules.SelectedEntry("p2-first", "c2-first", 2),
                LegacyAnnualTournamentEntryResetRules.SelectedEntry("p2-second", "c2-second", 2),
                LegacyAnnualTournamentEntryResetRules.SelectedEntry("p6", "c6", 6),
            ),
            result.selectedEntries,
        )
    }

    @Test
    fun `k0 i picks first sorted threshold player independently from selector`() {
        val result =
            LegacyAnnualTournamentEntryResetRules.apply(
                index = 1,
                rawB = 0,
                rawU = 6,
                rawHSize = 5,
                rawA0 = 0,
                entries =
                    listOf(
                        entry("top-below", "c0", count = 2.0, average = 20.0, selector = 9),
                        entry("first-threshold", "c1", count = 3.0, average = 19.0, selector = 9),
                        entry("later", "c2", count = 9.0, average = 18.0, selector = 0),
                    ),
            )

        assertEquals("first-threshold", result.firstThresholdPlayer)
        assertFalse(result.markFirstThresholdPlayer)
    }

    @Test
    fun `o1 true signal exists only for raw b one and index zero`() {
        val entries = listOf(entry("player", "club", count = 1.0, average = 1.0, selector = 0))

        assertTrue(
            LegacyAnnualTournamentEntryResetRules.apply(
                index = 0,
                rawB = 1,
                rawU = 0,
                rawHSize = 0,
                rawA0 = 0,
                entries = entries,
            ).markFirstThresholdPlayer,
        )
        assertFalse(
            LegacyAnnualTournamentEntryResetRules.apply(
                index = 1,
                rawB = 1,
                rawU = 0,
                rawHSize = 0,
                rawA0 = 0,
                entries = entries,
            ).markFirstThresholdPlayer,
        )
        assertFalse(
            LegacyAnnualTournamentEntryResetRules.apply(
                index = 0,
                rawB = 0,
                rawU = 0,
                rawHSize = 0,
                rawA0 = 0,
                entries = entries,
            ).markFirstThresholdPlayer,
        )
    }

    @Test
    fun `o1 true signal is absent when no threshold player exists`() {
        val result =
            LegacyAnnualTournamentEntryResetRules.apply(
                index = 0,
                rawB = 1,
                rawU = 6,
                rawHSize = 5,
                rawA0 = 0,
                entries = listOf(entry("player", "club", count = 2.0, average = 1.0, selector = 0)),
            )

        assertNull(result.firstThresholdPlayer)
        assertFalse(result.markFirstThresholdPlayer)
    }

    private fun entry(
        player: String,
        club: String,
        count: Double,
        average: Double,
        selector: Int,
    ) =
        LegacyAnnualTournamentEntryResetRules.RawEntry(
            player = player,
            club = club,
            rawSumC = count * average,
            rawCountD = count,
            rawAverageE = average,
            rawSelectorF = selector,
        )
}
