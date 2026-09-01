package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyManagerIdentityRuleTest {
    @Test
    fun `world counter allocates first manager as zero then increments`() {
        assertEquals(
            LegacyManagerIdAllocation(previousCounter = -1, managerId = 0, counterAfter = 0),
            LegacyManagerIdentityRule.allocate(-1),
        )
        assertEquals(
            LegacyManagerIdAllocation(previousCounter = 0, managerId = 1, counterAfter = 1),
            LegacyManagerIdentityRule.allocate(0),
        )
    }

    @Test
    fun `club stores minus one when manager reference is null`() {
        assertEquals(-1, LegacyManagerIdentityRule.clubStoredManagerId(null))
        assertEquals(17, LegacyManagerIdentityRule.clubStoredManagerId(17))
    }

    @Test
    fun `lazy resolver preserves world list order and returns first duplicate id`() {
        val managers = listOf(
            LegacyManagerIdentityRef(sourceOrdinal = 0, legacyManagerId = 7),
            LegacyManagerIdentityRef(sourceOrdinal = 1, legacyManagerId = 8),
            LegacyManagerIdentityRef(sourceOrdinal = 2, legacyManagerId = 7),
        )
        assertEquals(0, LegacyManagerIdentityRule.resolveFirstOrdinal(managers, 7))
        assertEquals(1, LegacyManagerIdentityRule.resolveFirstOrdinal(managers, 8))
        assertNull(LegacyManagerIdentityRule.resolveFirstOrdinal(managers, 99))
    }
}
