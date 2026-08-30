package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyTransferRuntimeStateTest {
    @Test
    fun `purchase applies proven cash roster slot contract flag salary and raw copy mutations`() {
        val playerCode = 77
        val state = LegacyTransferRuntimeState(
            mainTeamDirty = false,
            player = LegacyTransferPlayerRuntimeState(
                playerCode = playerCode,
                clubCode = 10,
                salaryCode = 25,
                contractEndMillis = 500L,
                rawX = true,
                rawY = true,
                rawZ = true,
                rawCrossActiveFlag = false,
                rawOCode = -123,
                rawDCode = 999,
            ),
            sourceClub = LegacyTransferClubRuntimeState(
                clubCode = 10,
                active = true,
                funds = 1_000L,
                rosterPlayerCodes = listOf(1, playerCode, 2, playerCode),
                primarySlotPlayerCode = playerCode,
                secondarySlotPlayerCode = playerCode,
                rawStateFlag = true,
            ),
            destinationClub = LegacyTransferClubRuntimeState(
                clubCode = 20,
                active = true,
                funds = 2_000L,
                rosterPlayerCodes = listOf(3, 4),
                primarySlotPlayerCode = null,
                secondarySlotPlayerCode = null,
                rawStateFlag = true,
            ),
        )
        val plan = LegacyTransferExecutionRule.plan(
            LegacyTransferExecutionInput(
                sourceClubPresent = true,
                sourceClubActive = true,
                destinationClubActive = true,
                destinationClubId = 20,
                transferValue = 300,
                legacySecondaryChargeFlag = false,
                loanMove = false,
                legacyNonFinancialMoveFlag = false,
                playerContractEndMillisBefore = 500L,
                currentGameMillis = 0L,
                currentCalendarMillis = 1_000L,
                sourcePrimarySlotMatchesPlayer = true,
                sourceSecondarySlotMatchesPlayer = true,
            ),
        )

        val after = LegacyTransferRuntimeMutation.apply(state, plan, salaryAfterPurchase = 50)

        assertTrue(after.mainTeamDirty)
        assertEquals(20, after.player.clubCode)
        assertEquals(50, after.player.salaryCode)
        assertEquals(1_000L + 180L * 86_400_000L, after.player.contractEndMillis)
        assertFalse(after.player.rawX)
        assertTrue(after.player.rawY)
        assertFalse(after.player.rawZ)
        assertTrue(after.player.rawCrossActiveFlag)
        // best.o.Q1() is unconditional inside T1: raw field o is copied to raw field D.
        assertEquals(-123, after.player.rawOCode)
        assertEquals(-123, after.player.rawDCode)

        val source = requireNotNull(after.sourceClub)
        assertEquals(1_300L, source.funds)
        // ArrayList.remove(Object) removes the first matching code only.
        assertEquals(listOf(1, 2, playerCode), source.rosterPlayerCodes)
        assertNull(source.primarySlotPlayerCode)
        assertNull(source.secondarySlotPlayerCode)
        assertFalse(source.rawStateFlag)

        assertEquals(1_700L, after.destinationClub.funds)
        // best.c0.f(...) uses ArrayList.add, so the player code is appended.
        assertEquals(listOf(3, 4, playerCode), after.destinationClub.rosterPlayerCodes)
    }

    @Test
    fun `loan preserves cash sets raw loan flag and uses 365 day contract`() {
        val state = LegacyTransferRuntimeState(
            mainTeamDirty = false,
            player = LegacyTransferPlayerRuntimeState(
                playerCode = 9,
                clubCode = 1,
                salaryCode = 70,
                contractEndMillis = 10L,
                rawX = true,
                rawY = false,
                rawZ = true,
                rawCrossActiveFlag = false,
            ),
            sourceClub = LegacyTransferClubRuntimeState(1, false, 100L, listOf(9), null, null, true),
            destinationClub = LegacyTransferClubRuntimeState(2, false, 200L, emptyList(), null, null, true),
        )
        val plan = LegacyTransferExecutionRule.plan(
            LegacyTransferExecutionInput(
                sourceClubPresent = true,
                sourceClubActive = false,
                destinationClubActive = false,
                destinationClubId = 2,
                transferValue = 999,
                legacySecondaryChargeFlag = false,
                loanMove = true,
                legacyNonFinancialMoveFlag = false,
                playerContractEndMillisBefore = 10L,
                currentGameMillis = 0L,
                currentCalendarMillis = 5_000L,
                sourcePrimarySlotMatchesPlayer = false,
                sourceSecondarySlotMatchesPlayer = false,
            ),
        )

        val after = LegacyTransferRuntimeMutation.apply(state, plan)

        assertEquals(100L, requireNotNull(after.sourceClub).funds)
        assertEquals(200L, after.destinationClub.funds)
        assertEquals(5_000L + 365L * 86_400_000L, after.player.contractEndMillis)
        assertTrue(after.player.rawY)
        assertEquals(70, after.player.salaryCode)
        assertFalse(after.mainTeamDirty)
    }

    @Test
    fun `missing source still appends to destination and does not synthesize a source club`() {
        val state = LegacyTransferRuntimeState(
            mainTeamDirty = false,
            player = LegacyTransferPlayerRuntimeState(5, -1, 1, 2L, false, false, false, false),
            sourceClub = null,
            destinationClub = LegacyTransferClubRuntimeState(8, true, 500L, listOf(6), null, null, false),
        )
        val plan = LegacyTransferExecutionRule.plan(
            LegacyTransferExecutionInput(
                sourceClubPresent = false,
                sourceClubActive = false,
                destinationClubActive = true,
                destinationClubId = 8,
                transferValue = 100,
                legacySecondaryChargeFlag = false,
                loanMove = false,
                legacyNonFinancialMoveFlag = false,
                playerContractEndMillisBefore = 2L,
                currentGameMillis = 0L,
                currentCalendarMillis = null,
                sourcePrimarySlotMatchesPlayer = false,
                sourceSecondarySlotMatchesPlayer = false,
            ),
        )

        val after = LegacyTransferRuntimeMutation.apply(state, plan)

        assertNull(after.sourceClub)
        assertEquals(listOf(6, 5), after.destinationClub.rosterPlayerCodes)
        assertEquals(400L, after.destinationClub.funds)
        assertEquals(2L, after.player.contractEndMillis)
    }
}
