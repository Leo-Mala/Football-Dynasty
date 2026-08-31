package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyTacticsDialogRuntimeRuleTest {
    private class Player(val key: Int) {
        override fun equals(other: Any?): Boolean = other is Player && other.key == key
        override fun hashCode(): Int = key
    }

    @Test
    fun loadReadsLegacySlotsAndCheckboxWithoutInventingDefaults() {
        val ui = LegacyTacticsDialogRuntimeRule.load(
            LegacyTacticsRawState(listOf(99, 2, 1, 0), checkboxT = true),
            editableQ0 = false,
        )
        assertEquals(2, ui.optionSlot1Selection)
        assertEquals(1, ui.optionSlot2Selection)
        assertEquals(0, ui.optionSlot3Selection)
        assertTrue(ui.checkboxT)
        assertFalse(ui.controlsEnabled)

        val unknown = LegacyTacticsDialogRuntimeRule.load(
            LegacyTacticsRawState(listOf(0, 9, -1, 2), checkboxT = false),
            editableQ0 = true,
        )
        assertNull(unknown.optionSlot1Selection)
        assertNull(unknown.optionSlot2Selection)
        assertNull(unknown.optionSlot3Selection)
    }

    @Test
    fun commitMatchesJAndQ0FalseIsCompleteNoOp() {
        val original = LegacyTacticsRawState(listOf(7, 2, 2, 1, 8), checkboxT = false)
        val ui = LegacyTacticsDialogUiState(0, null, 0, checkboxT = true, controlsEnabled = true)
        val committed = LegacyTacticsDialogRuntimeRule.commit(original, ui, editableQ0 = true)
        assertEquals(listOf(7, 0, 2, 0, 8), committed.optionSlots)
        assertTrue(committed.checkboxT)
        assertEquals(original, LegacyTacticsDialogRuntimeRule.commit(original, ui, editableQ0 = false))
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun shortLegacyOptionArrayIsNotSilentlyRepaired() {
        LegacyTacticsDialogRuntimeRule.load(
            LegacyTacticsRawState(listOf(0, 1, 2), checkboxT = false),
            editableQ0 = true,
        )
    }

    @Test
    fun exactSpecialPlayerKeysWriteOnlyTheirLegacySlot() {
        val player = Player(1)
        val empty = LegacySpecialTacticsAssignments<Player>(null, null, null, null)
        assertSame(player, LegacyTacticsDialogRuntimeRule.assignSpecialPlayer("cap", empty, player).captain)
        assertSame(player, LegacyTacticsDialogRuntimeRule.assignSpecialPlayer("bFaltas", empty, player).freeKick)
        assertSame(player, LegacyTacticsDialogRuntimeRule.assignSpecialPlayer("bEscanteios", empty, player).corner)
        assertSame(player, LegacyTacticsDialogRuntimeRule.assignSpecialPlayer("fNove", empty, player).falseNine)
        assertEquals(empty, LegacyTacticsDialogRuntimeRule.assignSpecialPlayer("unknown", empty, player))
    }

    @Test
    fun staleReferenceCleanupMatchesBestC0Fallback() {
        val player = Player(1)
        assertSame(player, LegacyTacticsDialogRuntimeRule.sanitizeReference(player, true, false, false))
        assertNull(LegacyTacticsDialogRuntimeRule.sanitizeReference(player, false, false, true))
        assertSame(player, LegacyTacticsDialogRuntimeRule.sanitizeReference(player, false, true, true))
        assertNull(LegacyTacticsDialogRuntimeRule.sanitizeReference(player, false, true, false))
    }

    @Test
    fun playerPickerUsesReferenceIdentityAndRejectsInvalidSelections() {
        val first = Player(7)
        val equalButDifferent = Player(7)
        val second = Player(8)
        val candidates = listOf(first, second)
        assertEquals(0, LegacyTacticsDialogRuntimeRule.initialPickerSelection(candidates, equalButDifferent))
        assertEquals(1, LegacyTacticsDialogRuntimeRule.initialPickerSelection(candidates, second))
        assertEquals(-1, LegacyTacticsDialogRuntimeRule.initialPickerSelection(emptyList<Player>(), second))
        assertSame(second, LegacyTacticsDialogRuntimeRule.confirmPickerSelection(candidates, 1))
        assertNull(LegacyTacticsDialogRuntimeRule.confirmPickerSelection(candidates, -1))
        assertNull(LegacyTacticsDialogRuntimeRule.confirmPickerSelection(candidates, 2))
    }
}
