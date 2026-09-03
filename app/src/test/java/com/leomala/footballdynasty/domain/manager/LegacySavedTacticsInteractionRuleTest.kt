package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacySavedTacticsInteractionRuleTest {
    private fun tactic(name: String) = LegacySavedTacticSnapshot(name, 0, emptyList(), emptyList())

    @Test
    fun emptyListDisablesSpinnerAndClearsAdapter() {
        assertEquals(
            LegacySavedTacticsSpinnerState(false, null, null),
            LegacySavedTacticsInteractionRule.refresh(emptyList()),
        )
    }

    @Test
    fun refreshShowsNamesAndSelectsLastEntry() {
        val state = LegacySavedTacticsInteractionRule.refresh(listOf(tactic("A"), tactic("B")))
        assertTrue(state.enabled)
        assertEquals(listOf("A", "B"), state.names)
        assertEquals(1, state.selectedIndex)
    }

    @Test
    fun deleteRemovesSelectedEntryThenRefreshesToNewLastEntry() {
        val result = LegacySavedTacticsInteractionRule.delete(
            listOf(tactic("A"), tactic("B"), tactic("C")),
            1,
        )
        assertEquals(listOf("A", "C"), result.savedTactics.map { it.displayName })
        assertEquals(1, result.spinnerState.selectedIndex)
    }

    @Test
    fun upperOutOfRangeDeleteIsNoOp() {
        val initial = listOf(tactic("A"))
        val result = LegacySavedTacticsInteractionRule.delete(initial, 1)
        assertEquals(initial, result.savedTactics)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun negativeDeleteIndexIsNotSilentlyRejected() {
        LegacySavedTacticsInteractionRule.delete(listOf(tactic("A")), -1)
    }

    @Test
    fun validLoadReturnsExactLegacyResultPayloadAndFinishes() {
        val result = LegacySavedTacticsInteractionRule.requestLoad(listOf(tactic("A")), 0)
        assertEquals(-1, result.resultCode)
        assertEquals(0, result.savedTacticIndex)
        assertTrue(result.finishActivity)
    }

    @Test
    fun invalidUpperLoadLeavesActivityOpenWithoutResult() {
        val result = LegacySavedTacticsInteractionRule.requestLoad(listOf(tactic("A")), 1)
        assertNull(result.resultCode)
        assertNull(result.savedTacticIndex)
        assertFalse(result.finishActivity)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun negativeLoadIndexPreservesLegacyFailureInsteadOfClamping() {
        LegacySavedTacticsInteractionRule.requestLoad(listOf(tactic("A")), -1)
    }

    @Test
    fun activityResultConsumesOnlyRequest101AndOkResult() {
        val saved = listOf(tactic("A"), tactic("B"))
        assertEquals(saved[1], LegacySavedTacticsInteractionRule.consumeLoadResult(101, -1, 1, saved))
        assertNull(LegacySavedTacticsInteractionRule.consumeLoadResult(100, -1, 1, saved))
        assertNull(LegacySavedTacticsInteractionRule.consumeLoadResult(101, 0, 1, saved))
        assertNull(LegacySavedTacticsInteractionRule.consumeLoadResult(101, -1, 2, saved))
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun negativeActivityResultIndexPreservesLegacyGetFailure() {
        LegacySavedTacticsInteractionRule.consumeLoadResult(101, -1, -1, listOf(tactic("A")))
    }
}
