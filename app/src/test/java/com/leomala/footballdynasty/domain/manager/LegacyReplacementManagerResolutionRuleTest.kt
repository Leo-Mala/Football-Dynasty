package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyReplacementManagerResolutionRuleTest {
    private class Manager(val id: String, val human: Boolean)
    private class Club(val id: String, val manager: Manager?)

    @Test
    fun refreshRuleProjectsExactF0KFieldsConsumedByClubDiscovery() {
        val original = LegacyReplacementSearchManager(
            rawU = 91,
            rawE = 8,
            userControlled = true,
            rawD = 6,
            rawW = 4,
            currentClubDivisionValue = 7,
            excludedClubIdentityKey = "old",
        )
        val refreshed = LegacyReplacementManagerRefreshRule.refreshForCurrentClub(
            original,
            LegacyReplacementCurrentClubSnapshot("current", countryCode = 29, divisionValue = 3),
        )

        assertEquals(91, refreshed.rawU)
        assertEquals(29, refreshed.rawE)
        assertEquals(2, refreshed.rawD)
        assertEquals(4, refreshed.rawW)
        assertEquals(7, refreshed.currentClubDivisionValue)
        assertEquals("current", refreshed.excludedClubIdentityKey)
    }

    @Test
    fun directModeZeroCandidateShortCircuitsAndUsesGThenFinalRepairForInitialHuman() {
        val outgoing = Manager("out", human = true)
        val incoming = Manager("in", human = false)
        val calls = mutableListOf<String>()

        val result = LegacyReplacementManagerResolutionRule.resolve<Manager, Club>(
            currentManager = { calls += "y0"; outgoing },
            refreshCurrentManager = { calls += "k:${it.id}" },
            isHumanManager = { calls += "K:${it.id}"; it.human },
            tryCandidateMode = { mode -> calls += "t:$mode"; if (mode == 0) incoming else null },
            targetRawLevelField = { calls += "raw-level"; 5 },
            firstUnemployedNonHuman = { calls += "u"; null },
            searchReplacementClubsFalse = { calls += "B:${it.id}:false"; emptyList() },
            managerOfClub = { calls += "club-y0:${it.id}"; it.manager },
            sameManagerReference = { first, second -> first === second },
            swapManagers = { first, second -> calls += "b4:${first.id}:${second.id}" },
            transferManagerToTarget = { first, second -> calls += "G:${first?.id}:${second.id}" },
            isTargetQ0 = { calls += "Q0"; false },
            repairTargetSquad = { calls += "club-o" },
        )

        assertSame(incoming, result.selectedManager)
        assertEquals(
            listOf(
                LegacyReplacementManagerResolutionEffect.REFRESH_CURRENT_MANAGER,
                LegacyReplacementManagerResolutionEffect.TRY_MODE_0,
                LegacyReplacementManagerResolutionEffect.TRANSFER_MANAGER_TO_TARGET,
                LegacyReplacementManagerResolutionEffect.REPAIR_TARGET_SQUAD,
            ),
            result.effectsInOrder,
        )
        assertEquals(
            listOf("y0", "k:out", "y0", "K:out", "t:0", "y0", "G:out:in", "Q0", "club-o"),
            calls,
        )
    }

    @Test
    fun modeTwoGateReadsRawLevelOnlyAfterModesZeroAndOne() {
        val current = Manager("current", human = false)
        val incoming = Manager("mode2", human = false)
        var rawLevel = 9
        val modes = mutableListOf<Int>()

        val result = LegacyReplacementManagerResolutionRule.resolve<Manager, Club>(
            currentManager = { current },
            refreshCurrentManager = {},
            isHumanManager = { it.human },
            tryCandidateMode = { mode ->
                modes += mode
                if (mode == 1) rawLevel = 3
                if (mode == 2) incoming else null
            },
            targetRawLevelField = { rawLevel },
            firstUnemployedNonHuman = { error("must short-circuit") },
            searchReplacementClubsFalse = { error("must short-circuit") },
            managerOfClub = { it.manager },
            sameManagerReference = { first, second -> first === second },
            swapManagers = { _, _ -> error("must not swap") },
            transferManagerToTarget = { _, _ -> },
            isTargetQ0 = { false },
            repairTargetSquad = {},
        )

        assertSame(incoming, result.selectedManager)
        assertEquals(listOf(0, 1, 2), modes)
    }

    @Test
    fun rawLevelAboveThreeSkipsModeTwoAndHumanFallsThroughMinusOneThenU() {
        val current = Manager("human", human = true)
        val fromU = Manager("u", human = false)
        val modes = mutableListOf<Int>()
        var uCalls = 0

        val result = LegacyReplacementManagerResolutionRule.resolve<Manager, Club>(
            currentManager = { current },
            refreshCurrentManager = {},
            isHumanManager = { it.human },
            tryCandidateMode = { mode -> modes += mode; null },
            targetRawLevelField = { 4 },
            firstUnemployedNonHuman = { uCalls++; fromU },
            searchReplacementClubsFalse = { error("human path must not call B") },
            managerOfClub = { it.manager },
            sameManagerReference = { first, second -> first === second },
            swapManagers = { _, _ -> error("must not swap") },
            transferManagerToTarget = { _, _ -> },
            isTargetQ0 = { true },
            repairTargetSquad = { error("Q0 true must suppress final o()") },
        )

        assertSame(fromU, result.selectedManager)
        assertEquals(listOf(0, 1, -1), modes)
        assertEquals(1, uCalls)
        assertFalse(result.effectsInOrder.contains(LegacyReplacementManagerResolutionEffect.TRY_MODE_2))
        assertTrue(result.effectsInOrder.contains(LegacyReplacementManagerResolutionEffect.TRY_FIRST_UNEMPLOYED))
        assertFalse(result.effectsInOrder.contains(LegacyReplacementManagerResolutionEffect.REPAIR_TARGET_SQUAD))
    }

    @Test
    fun nonHumanExhaustionUsesBFalseAndB4RatherThanG() {
        val current = Manager("current", human = false)
        val replacement = Manager("replacement", human = false)
        val replacementClub = Club("other", replacement)
        val calls = mutableListOf<String>()

        val result = LegacyReplacementManagerResolutionRule.resolve<Manager, Club>(
            currentManager = { calls += "y0"; current },
            refreshCurrentManager = { calls += "k" },
            isHumanManager = { it.human },
            tryCandidateMode = { mode -> calls += "t:$mode"; null },
            targetRawLevelField = { 3 },
            firstUnemployedNonHuman = { calls += "u"; null },
            searchReplacementClubsFalse = { calls += "B:${it.id}:false"; listOf(replacementClub) },
            managerOfClub = { calls += "club-y0:${it.id}"; it.manager },
            sameManagerReference = { first, second -> first === second },
            swapManagers = { first, second -> calls += "b4:${first.id}:${second.id}" },
            transferManagerToTarget = { _, _ -> error("B fallback must not call G") },
            isTargetQ0 = { false },
            repairTargetSquad = { error("initial manager is not human") },
        )

        assertSame(replacement, result.selectedManager)
        assertTrue(result.effectsInOrder.contains(LegacyReplacementManagerResolutionEffect.SEARCH_REPLACEMENT_CLUBS_FALSE))
        assertTrue(result.effectsInOrder.contains(LegacyReplacementManagerResolutionEffect.SWAP_MANAGERS))
        assertFalse(result.effectsInOrder.contains(LegacyReplacementManagerResolutionEffect.TRANSFER_MANAGER_TO_TARGET))
        assertTrue(calls.contains("B:current:false"))
        assertTrue(calls.contains("b4:replacement:current"))
    }

    @Test
    fun sameReferenceCandidateDoesNotTransferOrSwap() {
        val current = Manager("same", human = false)
        var transferCalls = 0
        val result = LegacyReplacementManagerResolutionRule.resolve<Manager, Club>(
            currentManager = { current },
            refreshCurrentManager = {},
            isHumanManager = { it.human },
            tryCandidateMode = { current },
            targetRawLevelField = { 0 },
            firstUnemployedNonHuman = { null },
            searchReplacementClubsFalse = { emptyList() },
            managerOfClub = { it.manager },
            sameManagerReference = { first, second -> first === second },
            swapManagers = { _, _ -> error("must not swap") },
            transferManagerToTarget = { _, _ -> transferCalls++ },
            isTargetQ0 = { false },
            repairTargetSquad = {},
        )

        assertSame(current, result.selectedManager)
        assertEquals(0, transferCalls)
        assertFalse(result.effectsInOrder.contains(LegacyReplacementManagerResolutionEffect.TRANSFER_MANAGER_TO_TARGET))
    }

    @Test
    fun nullInitialManagerCanStillAcceptImmediateCandidateThroughGWithNullOutgoing() {
        val incoming = Manager("incoming", human = false)
        var outgoingSeen: Manager? = Manager("sentinel", human = false)

        val result = LegacyReplacementManagerResolutionRule.resolve<Manager, Club>(
            currentManager = { null },
            refreshCurrentManager = { error("no initial manager") },
            isHumanManager = { it.human },
            tryCandidateMode = { mode -> if (mode == 0) incoming else null },
            targetRawLevelField = { 0 },
            firstUnemployedNonHuman = { null },
            searchReplacementClubsFalse = { emptyList() },
            managerOfClub = { it.manager },
            sameManagerReference = { first, second -> first === second },
            swapManagers = { _, _ -> error("must not swap") },
            transferManagerToTarget = { outgoing, _ -> outgoingSeen = outgoing },
            isTargetQ0 = { false },
            repairTargetSquad = {},
        )

        assertSame(incoming, result.selectedManager)
        assertNull(outgoingSeen)
    }

    @Test(expected = NullPointerException::class)
    fun nullInitialManagerPreservesLegacyDereferenceAfterCandidateModesExhaust() {
        LegacyReplacementManagerResolutionRule.resolve<Manager, Club>(
            currentManager = { null },
            refreshCurrentManager = { error("no initial manager") },
            isHumanManager = { it.human },
            tryCandidateMode = { null },
            targetRawLevelField = { 4 },
            firstUnemployedNonHuman = { null },
            searchReplacementClubsFalse = { emptyList() },
            managerOfClub = { it.manager },
            sameManagerReference = { first, second -> first === second },
            swapManagers = { _, _ -> },
            transferManagerToTarget = { _, _ -> },
            isTargetQ0 = { false },
            repairTargetSquad = {},
        )
    }
}
