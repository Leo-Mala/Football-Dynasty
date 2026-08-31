package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacySavedTacticsRuleTest {
    private val existing = listOf(
        LegacySavedTacticSnapshot("Old", 0, listOf(1), listOf(9)),
    )

    @Test
    fun exactEmptyNameIsRejectedBeforeFormationOrSlotAccess() {
        val result = LegacySavedTacticsRule.save(
            inputName = "",
            formationIndex = 99,
            formationNames = emptyList(),
            currentLegacyRuntimePlayerIds = listOf(1),
            currentSlotCodes = emptyList(),
            existingSavedTactics = existing,
        )

        assertEquals(LegacySavedTacticsSaveStatus.EMPTY_NAME, result.status)
        assertEquals(existing, result.savedTactics)
        assertTrue(result.feedbackVisible)
        assertEquals("", result.inputTextAfter)
        assertFalse(result.refreshSavedTacticsList)
    }

    @Test
    fun thirtyCharactersAreAcceptedAndThirtyOneAreRejected() {
        val accepted = LegacySavedTacticsRule.save(
            "A".repeat(30), 0, listOf("4-4-2"), emptyList(), emptyList(), emptyList(),
        )
        assertEquals(LegacySavedTacticsSaveStatus.SAVED, accepted.status)

        val rejected = LegacySavedTacticsRule.save(
            "A".repeat(31), 99, emptyList(), listOf(1), emptyList(), existing,
        )
        assertEquals(LegacySavedTacticsSaveStatus.NAME_TOO_LONG, rejected.status)
        assertEquals(existing, rejected.savedTactics)
        assertFalse(rejected.refreshSavedTacticsList)
    }

    @Test
    fun whitespaceIsNotTrimmed() {
        val result = LegacySavedTacticsRule.save(
            "  ", 0, listOf("4-3-3"), emptyList(), emptyList(), emptyList(),
        )
        assertEquals(LegacySavedTacticsSaveStatus.SAVED, result.status)
        assertEquals("4-3-3   ", result.savedTactics.single().displayName)
    }

    @Test
    fun successfulSaveCopiesParallelSlotsUsesMinusOneForNullAndClearsInput() {
        val result = LegacySavedTacticsRule.save(
            inputName = "Pressao",
            formationIndex = 1,
            formationNames = listOf("4-4-2", "4-3-3"),
            currentLegacyRuntimePlayerIds = listOf(10, null, 30),
            currentSlotCodes = listOf(4, 5, 6, 999),
            existingSavedTactics = existing,
        )

        assertEquals(LegacySavedTacticsSaveStatus.SAVED, result.status)
        assertEquals(2, result.savedTactics.size)
        val saved = result.savedTactics.last()
        assertEquals("4-3-3 Pressao", saved.displayName)
        assertEquals(1, saved.formationIndex)
        assertEquals(listOf(10, -1, 30), saved.legacyRuntimePlayerIds)
        assertEquals(listOf(4, 5, 6), saved.slotCodes)
        assertEquals("", result.inputTextAfter)
        assertTrue(result.feedbackVisible)
        assertTrue(result.refreshSavedTacticsList)
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun shortParallelSlotListIsNotSilentlyRepaired() {
        LegacySavedTacticsRule.save(
            "Ok", 0, listOf("4-4-2"), listOf(1, 2), listOf(7), emptyList(),
        )
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun invalidFormationIndexIsNotClamped() {
        LegacySavedTacticsRule.save(
            "Ok", 2, listOf("4-4-2"), emptyList(), emptyList(), emptyList(),
        )
    }
}
