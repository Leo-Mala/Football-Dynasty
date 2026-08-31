package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyPlayerSaleRuleTest {
    private class QueueRandomSource(private val values: ArrayDeque<Int>) : RandomSource {
        override var draws: Long = 0
            private set
        override fun nextInt(bound: Int): Int {
            draws++
            val value = values.removeFirst()
            require(value in 0 until bound) { "$value not in 0 until $bound" }
            return value
        }
        override fun nextBoolean(): Boolean = error("unused")
        override fun nextDouble(): Double = error("unused")
    }

    @Test
    fun immediateSaleUsesFifteenPercentFloorThenBuyerSearchAndT1MutationInOneDrawOrder() {
        val buyerValue = "buyer"
        val buyerClub = LegacySaleBuyerClub(
            identityKey = "buyer-club",
            value = buyerValue,
            rawQ0 = false,
            rawR0 = false,
            rawOCode = 2,
            rawP0Code = 3,
            rosterSize = 20,
            positionCounts = intArrayOf(1, 1, 1, 1, 1),
        )
        val context = LegacySaleBuyerContext(rawPCode = 0, maxClubOCode = 4, clubs = listOf(buyerClub))
        val player = LegacySalePlayerSnapshot(force = 45, positionCode = 3, star = false, worldTop = false)
        val source = LegacySaleSourceClubSnapshot(
            identityKey = "source",
            countryCode = 1,
            rawR0 = false,
            rawQ0 = true,
            rawJCode = 0,
            rawP0Code = 3,
            rawOCode = 2,
        )
        // 51 => sale floor uses 15%; 30 => best.f.n(false) takes standard branch.
        val random = QueueRandomSource(ArrayDeque(listOf(51, 30)))

        val result = LegacyImmediatePlayerSaleRuntimeRule.execute(
            requestedValue = 700,
            playerBaseValue = 1000,
            player = player,
            source = source,
            world = LegacySaleBuyerWorld(null, listOf(context), listOf(buyerClub)),
            random = random,
            runtimeStateForBuyer = {
                LegacyTransferRuntimeState(
                    mainTeamDirty = false,
                    player = LegacyTransferPlayerRuntimeState(
                        playerCode = 10,
                        clubCode = 1,
                        salaryCode = 50,
                        contractEndMillis = 0L,
                        rawX = true,
                        rawY = false,
                        rawZ = true,
                        rawCrossActiveFlag = false,
                    ),
                    sourceClub = LegacyTransferClubRuntimeState(1, true, 100L, listOf(10, 11), 10, null, true),
                    destinationClub = LegacyTransferClubRuntimeState(2, true, 2000L, listOf(20), null, null, true),
                )
            },
            transferInputForBuyer = {
                LegacyTransferExecutionInput(
                    sourceClubPresent = true,
                    sourceClubActive = true,
                    destinationClubActive = true,
                    destinationClubId = 2,
                    transferValue = -999,
                    legacySecondaryChargeFlag = false,
                    loanMove = true,
                    legacyNonFinancialMoveFlag = true,
                    playerContractEndMillisBefore = 0L,
                    currentGameMillis = 0L,
                    currentCalendarMillis = 1_000L,
                    sourcePrimarySlotMatchesPlayer = true,
                    sourceSecondarySlotMatchesPlayer = false,
                )
            },
        )

        assertEquals(700, result.requestedSaleValueStored)
        assertEquals(850, result.effectiveTransferValue)
        assertEquals(buyerValue, result.buyer)
        assertEquals(listOf("buyer-club"), result.preparedClubIdentityKeysInOrder)
        assertEquals(2L, random.draws)
        val state = requireNotNull(result.transferredState)
        assertEquals(950L, requireNotNull(state.sourceClub).funds)
        assertEquals(1150L, state.destinationClub.funds)
        assertFalse(requireNotNull(state.sourceClub).rosterPlayerCodes.contains(10))
        assertTrue(state.destinationClub.rosterPlayerCodes.contains(10))
        assertEquals(2, state.player.clubCode)
        assertEquals(180L, requireNotNull(result.transferPlan).contractDurationDays)
    }

    @Test
    fun requestedValueAboveBaseConsumesNoPricingDrawAndNoBuyerLeavesOnlyStoredPriceEffect() {
        val random = QueueRandomSource(ArrayDeque())
        val result = LegacyImmediatePlayerSaleRuntimeRule.execute<String>(
            requestedValue = 1200,
            playerBaseValue = 1000,
            player = LegacySalePlayerSnapshot(20, 1, false, false),
            source = LegacySaleSourceClubSnapshot("source", 1, false, false, 1, 2, 2),
            world = LegacySaleBuyerWorld(null, emptyList(), emptyList()),
            random = random,
            runtimeStateForBuyer = { error("no buyer") },
            transferInputForBuyer = { error("no buyer") },
        )
        assertEquals(1200, result.requestedSaleValueStored)
        assertEquals(1200, result.effectiveTransferValue)
        assertNull(result.buyer)
        assertNull(result.transferPlan)
        assertNull(result.transferredState)
        assertEquals(0L, random.draws)
    }

    @Test
    fun buyerAcceptancePreservesExactForceAndPositionCapacityBounds() {
        val base = LegacySaleBuyerClub("c", "c", false, false, 2, 3, 20, intArrayOf(1, 1, 1, 10, 1))
        assertTrue(LegacyPlayerSaleBuyerDiscoveryRule.acceptsPlayer(base, LegacySalePlayerSnapshot(40, 3, false, false), true))
        assertFalse(LegacyPlayerSaleBuyerDiscoveryRule.acceptsPlayer(base, LegacySalePlayerSnapshot(39, 3, false, false), true))
        val overloaded = LegacySaleBuyerClub("o", "o", false, false, 2, 3, 20, intArrayOf(1, 1, 1, 11, 1))
        assertFalse(LegacyPlayerSaleBuyerDiscoveryRule.acceptsPlayer(overloaded, LegacySalePlayerSnapshot(45, 3, false, false), true))
        assertTrue(LegacyPlayerSaleBuyerDiscoveryRule.acceptsPlayer(overloaded, LegacySalePlayerSnapshot(45, 3, false, false), false))
    }

    @Test
    fun listingAndRemovalPreserveRawValueExactlyLikeDialogMethodsOAndN() {
        val initial = LegacyPlayerSaleListingState(listedForSale = false, rawSaleValue = 321)
        val listed = LegacyPlayerSaleListingRule.listForSale(initial, 777)
        assertTrue(listed.listedForSale)
        assertEquals(777, listed.rawSaleValue)
        val removed = LegacyPlayerSaleListingRule.removeFromSaleList(listed)
        assertFalse(removed.listedForSale)
        assertEquals(777, removed.rawSaleValue)
    }
}
