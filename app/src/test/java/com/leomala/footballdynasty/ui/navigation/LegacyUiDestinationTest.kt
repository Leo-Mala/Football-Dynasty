package com.leomala.footballdynasty.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyUiDestinationTest {
    @Test
    fun `phase 17 destination set contains only proven presentation surfaces`() {
        assertEquals(
            listOf(
                "START",
                "NEW_OR_LOAD_CAREER",
                "CLUB_SELECTION",
                "CAREER_HOME",
                "SQUAD",
                "LINEUP_AND_TACTICS",
                "CALENDAR_AND_ROUNDS",
                "STANDINGS",
                "LIVE_MATCH",
                "RESULTS",
                "TRANSFER_MARKET",
                "CONTRACTS",
                "FINANCES",
                "STADIUM",
                "MANAGER",
                "JUNIORS",
                "SEASON_END",
            ),
            LegacyUiDestination.entries.map { it.name },
        )
    }
}
