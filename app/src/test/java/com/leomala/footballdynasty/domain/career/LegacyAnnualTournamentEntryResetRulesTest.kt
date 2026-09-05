package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
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
}
