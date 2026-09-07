package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.career.LegacyAnnualFResetRules.Action
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LegacyAnnualFResetRulesTest {
    @Test
    fun `three passes preserve exact tournament global player and first entry order`() {
        val actions = LegacyAnnualFResetRules.plan(
            LegacyAnnualFResetRules.Input(
                tournamentEntryCounts = listOf(2, 1),
                globalPlayerCount = 2,
                firstEntryPlayerCounts = listOf(2, 1),
            ),
        )

        assertEquals(
            listOf(
                Action.CallTournamentReset(competitionIndex = 0, entryIndex = 0, argument = 0),
                Action.CallTournamentReset(competitionIndex = 0, entryIndex = 1, argument = 1),
                Action.CallTournamentReset(competitionIndex = 1, entryIndex = 0, argument = 0),
                Action.ResetGlobalPlayerCounter(playerIndex = 0),
                Action.ResetGlobalPlayerCounter(playerIndex = 1),
                Action.ProgressFirstEntryPlayer(competitionIndex = 0, playerIndex = 0),
                Action.ProgressFirstEntryPlayer(competitionIndex = 0, playerIndex = 1),
                Action.ProgressFirstEntryPlayer(competitionIndex = 1, playerIndex = 0),
            ),
            actions,
        )
    }

    @Test
    fun `tournament reset argument is inner entry index exactly`() {
        val actions = LegacyAnnualFResetRules.plan(
            LegacyAnnualFResetRules.Input(
                tournamentEntryCounts = listOf(3),
                globalPlayerCount = 0,
                firstEntryPlayerCounts = listOf(0),
            ),
        )

        assertEquals(
            listOf(
                Action.CallTournamentReset(0, 0, 0),
                Action.CallTournamentReset(0, 1, 1),
                Action.CallTournamentReset(0, 2, 2),
            ),
            actions,
        )
    }

    @Test
    fun `third pass models only first z0 entry players`() {
        val actions = LegacyAnnualFResetRules.plan(
            LegacyAnnualFResetRules.Input(
                tournamentEntryCounts = listOf(4),
                globalPlayerCount = 0,
                firstEntryPlayerCounts = listOf(2),
            ),
        )

        assertEquals(
            listOf(
                Action.CallTournamentReset(0, 0, 0),
                Action.CallTournamentReset(0, 1, 1),
                Action.CallTournamentReset(0, 2, 2),
                Action.CallTournamentReset(0, 3, 3),
                Action.ProgressFirstEntryPlayer(0, 0),
                Action.ProgressFirstEntryPlayer(0, 1),
            ),
            actions,
        )
    }

    @Test
    fun `empty global collections produce no actions`() {
        assertEquals(
            emptyList<Action>(),
            LegacyAnnualFResetRules.plan(
                LegacyAnnualFResetRules.Input(
                    tournamentEntryCounts = emptyList(),
                    globalPlayerCount = 0,
                    firstEntryPlayerCounts = emptyList(),
                ),
            ),
        )
    }

    @Test
    fun `input rejects mismatched competition projections`() {
        assertThrows(IllegalArgumentException::class.java) {
            LegacyAnnualFResetRules.Input(
                tournamentEntryCounts = listOf(1),
                globalPlayerCount = 0,
                firstEntryPlayerCounts = emptyList(),
            )
        }
    }
}
