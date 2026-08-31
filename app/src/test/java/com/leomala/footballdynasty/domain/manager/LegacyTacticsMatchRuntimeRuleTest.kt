package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyTacticsMatchRuntimeRuleTest {
    @Test
    fun `match engine reads raw option slot two and ignores neighboring tactic slots`() {
        val state = LegacyTacticsRawState(
            optionSlots = listOf(9, 2, 1, 0),
            checkboxT = false,
        )
        assertEquals(1, LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(state))

        val changedNeighbors = state.copy(optionSlots = listOf(7, 0, 1, 1))
        assertEquals(1, LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(changedNeighbors))
    }

    @Test
    fun `halftime dialog commit changes the exact slot later consumed by the match engine`() {
        val before = LegacyTacticsRawState(
            optionSlots = listOf(0, 1, 0, 1),
            checkboxT = false,
        )
        val ui = LegacyTacticsDialogUiState(
            optionSlot1Selection = 1,
            optionSlot2Selection = 2,
            optionSlot3Selection = 1,
            checkboxT = false,
            controlsEnabled = true,
        )
        val after = LegacyTacticsDialogRuntimeRule.commit(before, ui, editableQ0 = true)

        assertEquals(0, LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(before))
        assertEquals(2, LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(after))
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun `short raw tactics array is not silently repaired`() {
        LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(
            LegacyTacticsRawState(listOf(0, 1), checkboxT = false)
        )
    }
}
