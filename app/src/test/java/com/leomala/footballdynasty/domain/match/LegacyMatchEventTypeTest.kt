package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyMatchEventTypeTest {
    @Test
    fun `legacy codes one through seven map exactly`() {
        assertEquals(LegacyMatchEventType.GOAL, LegacyMatchEventType.fromLegacyCode(1))
        assertEquals(LegacyMatchEventType.YELLOW_CARD, LegacyMatchEventType.fromLegacyCode(2))
        assertEquals(LegacyMatchEventType.SECOND_YELLOW_RED, LegacyMatchEventType.fromLegacyCode(3))
        assertEquals(LegacyMatchEventType.RED_CARD, LegacyMatchEventType.fromLegacyCode(4))
        assertEquals(LegacyMatchEventType.INJURY, LegacyMatchEventType.fromLegacyCode(5))
        assertEquals(LegacyMatchEventType.SUBSTITUTION, LegacyMatchEventType.fromLegacyCode(6))
        assertEquals(LegacyMatchEventType.MISSED_PENALTY, LegacyMatchEventType.fromLegacyCode(7))
    }

    @Test
    fun `UI marker codes are not silently treated as match event types`() {
        assertNull(LegacyMatchEventType.fromLegacyCode(-1))
        assertNull(LegacyMatchEventType.fromLegacyCode(91))
        assertNull(LegacyMatchEventType.fromLegacyCode(96))
    }
}
