package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyMatchEventRecordTest {
    @Test
    fun `default constructor mirrors best l defaults`() {
        val event = LegacyMatchEventRecord.default<String, String>()

        assertNull(event.legacyClub)
        assertEquals(-1, event.legacyType)
        assertEquals(-1, event.legacySubtype)
        assertEquals(-1, event.legacyMinute)
        assertEquals(-1, event.legacyPeriod)
        assertNull(event.primaryPlayer)
        assertNull(event.secondaryPlayer)
        assertFalse(event.legacyFlagH)
        assertEquals(0, event.legacySide)
        assertFalse(event.legacyFlagJ)
    }

    @Test
    fun `side constructor changes only legacy side`() {
        val event = LegacyMatchEventRecord.forSide<String, String>(1)

        assertEquals(1, event.legacySide)
        assertEquals(-1, event.legacyType)
        assertEquals(-1, event.legacySubtype)
        assertEquals(-1, event.legacyMinute)
        assertEquals(-1, event.legacyPeriod)
        assertFalse(event.legacyFlagH)
    }

    @Test
    fun `marker constructor sets type and H flag while side remains zero`() {
        val event = LegacyMatchEventRecord.marker<String, String>(91, true)

        assertEquals(91, event.legacyType)
        assertEquals(true, event.legacyFlagH)
        assertEquals(0, event.legacySide)
        assertEquals(-1, event.legacySubtype)
        assertEquals(-1, event.legacyMinute)
        assertEquals(-1, event.legacyPeriod)
    }

    @Test
    fun `copy maps setters without coupling independent fields`() {
        val event = LegacyMatchEventRecord.forSide<String, String>(1).copy(
            legacyClub = "club",
            legacyType = 1,
            legacySubtype = 3,
            legacyMinute = 47,
            legacyPeriod = 2,
            primaryPlayer = "p1",
            secondaryPlayer = "p2",
            legacyFlagJ = true,
        )

        assertEquals("club", event.legacyClub)
        assertEquals(1, event.legacyType)
        assertEquals(3, event.legacySubtype)
        assertEquals(47, event.legacyMinute)
        assertEquals(2, event.legacyPeriod)
        assertEquals("p1", event.primaryPlayer)
        assertEquals("p2", event.secondaryPlayer)
        assertEquals(1, event.legacySide)
        assertEquals(true, event.legacyFlagJ)
        assertFalse(event.legacyFlagH)
    }
}
