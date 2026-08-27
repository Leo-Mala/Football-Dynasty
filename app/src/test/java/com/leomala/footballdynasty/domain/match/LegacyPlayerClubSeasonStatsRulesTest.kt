package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyPlayerClubSeasonStatsRulesTest {
    @Test
    fun `existing entry matches both club and current season`() {
        val entries = listOf(
            entry(season = 1, club = 10, h = 3),
            entry(season = 2, club = 10, h = 7),
        )
        val result = LegacyPlayerClubSeasonStatsRules.apply(entries, 2, 10, 5)

        assertEquals(1, result.matchedIndex)
        assertEquals(8, result.mutatedEntry.legacyH)
        assertFalse(result.createdEntry)
        assertEquals(3, result.updatedEntries!![0].legacyH)
    }

    @Test
    fun `first duplicate season club entry wins like q0 ordered scan`() {
        val entries = listOf(
            entry(4, 9, h = 1),
            entry(4, 9, h = 10),
        )
        val result = LegacyPlayerClubSeasonStatsRules.apply(entries, 4, 9, 5)

        assertEquals(0, result.matchedIndex)
        assertEquals(2, result.updatedEntries!![0].legacyH)
        assertEquals(10, result.updatedEntries[1].legacyH)
    }

    @Test
    fun `missing entry is created and retained when history list exists`() {
        val result = LegacyPlayerClubSeasonStatsRules.apply(emptyList(), 7, 22, 5)

        assertTrue(result.createdEntry)
        assertTrue(result.retainedEntry)
        assertEquals(1, result.updatedEntries!!.size)
        assertEquals(7, result.mutatedEntry.legacySeasonId)
        assertEquals(22, result.mutatedEntry.legacyClubId)
        assertEquals(-1, result.mutatedEntry.legacyTransientI)
        assertEquals(1, result.mutatedEntry.legacyH)
    }

    @Test
    fun `null history list still mutates transient new entry but cannot retain it`() {
        val result = LegacyPlayerClubSeasonStatsRules.apply(null, 3, 8, 5)

        assertTrue(result.createdEntry)
        assertFalse(result.retainedEntry)
        assertNull(result.updatedEntries)
        assertEquals(1, result.mutatedEntry.legacyH)
    }

    @Test
    fun `code two increments only legacy C`() {
        val e = applyCode(2)
        assertEquals(1, e.legacyC)
        assertEquals(0, e.legacyD)
    }

    @Test
    fun `code four increments only legacy D`() {
        val e = applyCode(4)
        assertEquals(0, e.legacyC)
        assertEquals(1, e.legacyD)
    }

    @Test
    fun `code three increments legacy C and D in same entry`() {
        val e = applyCode(3)
        assertEquals(1, e.legacyC)
        assertEquals(1, e.legacyD)
    }

    @Test
    fun `code one increments legacy G`() {
        assertEquals(1, applyCode(1).legacyG)
    }

    @Test
    fun `injury code five increments legacy H`() {
        assertEquals(1, applyCode(5).legacyH)
    }

    @Test
    fun `code zero increments legacy F`() {
        assertEquals(1, applyCode(0).legacyF)
    }

    @Test
    fun `code eight increments legacy E`() {
        assertEquals(1, applyCode(8).legacyE)
    }

    @Test
    fun `unknown code still creates missing entry without incrementing counters`() {
        val result = LegacyPlayerClubSeasonStatsRules.apply(emptyList(), 1, 2, 99)
        val e = result.mutatedEntry

        assertTrue(result.createdEntry)
        assertEquals(0, e.legacyC)
        assertEquals(0, e.legacyD)
        assertEquals(0, e.legacyE)
        assertEquals(0, e.legacyF)
        assertEquals(0, e.legacyG)
        assertEquals(0, e.legacyH)
    }

    private fun applyCode(code: Int) =
        LegacyPlayerClubSeasonStatsRules.apply(emptyList(), 1, 2, code).mutatedEntry

    private fun entry(
        season: Int,
        club: Int,
        h: Int = 0,
    ) = LegacyPlayerClubSeasonStatsRules.Entry(
        legacySeasonId = season,
        legacyClubId = club,
        legacyH = h,
    )
}
