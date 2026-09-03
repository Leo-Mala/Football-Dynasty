package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyCoachMatchManagerResolutionRuleTest {
    private val managers = listOf(
        LegacyManagerIdentityRef(sourceOrdinal = 0, legacyManagerId = 7),
        LegacyManagerIdentityRef(sourceOrdinal = 1, legacyManagerId = 7),
        LegacyManagerIdentityRef(sourceOrdinal = 2, legacyManagerId = 9),
    )

    @Test
    fun `first duplicate manager in ArrayList order remains observable`() {
        val resolved = LegacyCoachMatchManagerResolutionRule.resolveFirst(
            LegacyCoachMatchClubManagerRef("home", 7),
            managers,
        )
        assertEquals(0, resolved?.manager?.sourceOrdinal)
    }

    @Test
    fun `missing or absent manager fails closed by skipping that club`() {
        assertNull(
            LegacyCoachMatchManagerResolutionRule.resolveFirst(
                LegacyCoachMatchClubManagerRef("home", -1),
                managers,
            )
        )
        assertNull(
            LegacyCoachMatchManagerResolutionRule.resolveFirst(
                LegacyCoachMatchClubManagerRef("home", 999),
                managers,
            )
        )
    }

    @Test
    fun `post match manager order is home then away`() {
        val resolved = LegacyCoachMatchManagerResolutionRule.orderedForMatch(
            home = LegacyCoachMatchClubManagerRef("home", 9),
            away = LegacyCoachMatchClubManagerRef("away", 7),
            managersInWorldOrder = managers,
        )
        assertEquals(listOf("home", "away"), resolved.map { it.clubId })
        assertEquals(listOf(2, 0), resolved.map { it.manager.sourceOrdinal })
    }

    @Test
    fun `same resolved manager on both sides is not silently deduplicated`() {
        val resolved = LegacyCoachMatchManagerResolutionRule.orderedForMatch(
            home = LegacyCoachMatchClubManagerRef("home", 7),
            away = LegacyCoachMatchClubManagerRef("away", 7),
            managersInWorldOrder = managers,
        )
        assertEquals(listOf(0, 0), resolved.map { it.manager.sourceOrdinal })
    }
}
