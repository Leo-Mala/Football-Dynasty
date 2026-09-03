package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.foundation.random.RandomSource

class LegacySaleBuyerClub<T>(
    val identityKey: String,
    val value: T,
    val rawQ0: Boolean,
    val rawR0: Boolean,
    val rawOCode: Int,
    val rawP0Code: Int,
    val rosterSize: Int,
    val positionCounts: IntArray,
)

class LegacySaleBuyerContext<T>(
    val rawPCode: Int,
    val maxClubOCode: Int,
    val clubs: List<LegacySaleBuyerClub<T>>,
)

data class LegacySalePlayerSnapshot(
    val force: Int,
    val positionCode: Int,
    val star: Boolean,
    val worldTop: Boolean,
)

data class LegacySaleSourceClubSnapshot(
    val identityKey: String,
    val countryCode: Int,
    val rawR0: Boolean,
    val rawQ0: Boolean,
    val rawJCode: Int,
    val rawP0Code: Int,
    val rawOCode: Int,
)

class LegacySaleBuyerWorld<T>(
    val sourceCountryContext: LegacySaleBuyerContext<T>?,
    val contexts: List<LegacySaleBuyerContext<T>>,
    val fallbackClubs: List<LegacySaleBuyerClub<T>>,
)

data class LegacySaleBuyerDiscoveryResult<T>(
    val buyer: T?,
    val preparedClubIdentityKeysInOrder: List<String>,
)

/** Exact mode-0 buyer search used by `components.n3 -> best.f.n(false)` for an immediate sale. */
object LegacyPlayerSaleBuyerDiscoveryRule {
    private val nationalMinimumForceByO = intArrayOf(1, 40, 30, 20, 5)
    private val minimumForceByP0 = intArrayOf(1, 10, 20, 40, 50, 55)
    private val maximumForceByP0 = intArrayOf(20, 30, 45, 85, 100, 100)
    private val maximumPositionCounts = intArrayOf(3, 5, 5, 10, 5)

    fun <T> discover(
        player: LegacySalePlayerSnapshot,
        source: LegacySaleSourceClubSnapshot,
        world: LegacySaleBuyerWorld<T>,
        random: RandomSource,
    ): LegacySaleBuyerDiscoveryResult<T> {
        val primaryContexts = mutableListOf<LegacySaleBuyerContext<T>>()
        if (source.rawR0) {
            primaryContexts += requireNotNull(world.sourceCountryContext)
        }

        // Official constructor evaluates this expression before the star/world-top override.
        val countryForceGate = if (source.countryCode == 29 && player.force > 50) {
            random.nextInt(100) > 10
        } else {
            false
        }
        val broadPrimarySearch = if (!player.star && !player.worldTop) countryForceGate else true
        if (source.rawJCode == 0 || broadPrimarySearch) {
            for (context in world.contexts) {
                if (context.rawPCode == 0 && !primaryContexts.contains(context)) primaryContexts += context
            }
        }

        val secondaryContexts = world.contexts.filterTo(mutableListOf()) {
            it.rawPCode == 0 && !primaryContexts.contains(it)
        }
        val allZeroContexts = world.contexts.filterTo(mutableListOf()) { it.rawPCode == 0 }
        val prepared = mutableListOf<String>()

        val standardPath = if (player.star && source.rawQ0) {
            false
        } else {
            player.force <= 30 || !source.rawQ0 || random.nextInt(100) <= 60
        }

        var buyer: LegacySaleBuyerClub<T>? = null
        if (standardPath) {
            buyer = findInContexts(primaryContexts, player, source, prepared, random)
            if (buyer == null && source.rawP0Code > 2) {
                buyer = findInContexts(secondaryContexts, player, source, prepared, random)
            }
        } else {
            if (player.star) buyer = findInContexts(allZeroContexts, player, source, prepared, random)
            if (buyer == null && source.rawP0Code > 2) {
                buyer = findInContexts(secondaryContexts, player, source, prepared, random)
            }
            if (buyer == null) buyer = findInContexts(primaryContexts, player, source, prepared, random)
        }

        if (buyer == null) {
            buyer = findFallback(world.fallbackClubs, player, source, random)
        }

        return LegacySaleBuyerDiscoveryResult(
            buyer = buyer?.value,
            preparedClubIdentityKeysInOrder = prepared,
        )
    }

    private fun <T> findInContexts(
        contexts: List<LegacySaleBuyerContext<T>>,
        player: LegacySalePlayerSnapshot,
        source: LegacySaleSourceClubSnapshot,
        prepared: MutableList<String>,
        random: RandomSource,
    ): LegacySaleBuyerClub<T>? {
        val candidates = mutableListOf<LegacySaleBuyerClub<T>>()
        var minimumO = source.rawOCode - 1
        var maximumO = source.rawOCode + 1
        if (source.rawOCode == 1) minimumO = 1

        for (context in contexts) {
            if (player.force <= 5) {
                maximumO = context.maxClubOCode
                minimumO = 0
            } else if (player.force <= 20) {
                maximumO = context.maxClubOCode
            }
            if (player.force <= 20) minimumO = 0

            for (club in context.clubs) {
                if (
                    club.rawOCode <= maximumO &&
                    club.rawOCode >= minimumO &&
                    club.identityKey != source.identityKey &&
                    !club.rawQ0 &&
                    club.rosterSize < 30
                ) {
                    // Legacy calls D0(true) for every prefiltered club before shuffling.
                    prepared += club.identityKey
                    candidates += club
                }
            }
        }

        shuffleLikeCollections(candidates, random)
        val checkPositionCapacity = !source.rawQ0
        return candidates.firstOrNull { acceptsPlayer(it, player, checkPositionCapacity) }
    }

    private fun <T> findFallback(
        clubs: List<LegacySaleBuyerClub<T>>,
        player: LegacySalePlayerSnapshot,
        source: LegacySaleSourceClubSnapshot,
        random: RandomSource,
    ): LegacySaleBuyerClub<T>? {
        val candidates = clubs.filterTo(mutableListOf()) {
            !it.rawR0 && !it.rawQ0 && it.rosterSize < 30
        }
        shuffleLikeCollections(candidates, random)
        val checkPositionCapacity = !source.rawQ0
        return candidates.firstOrNull { acceptsPlayer(it, player, checkPositionCapacity) }
    }

    fun <T> acceptsPlayer(
        club: LegacySaleBuyerClub<T>,
        player: LegacySalePlayerSnapshot,
        checkPositionCapacity: Boolean,
    ): Boolean {
        if (club.rosterSize >= 30) return false
        val minimum = if (club.rawR0) {
            nationalMinimumForceByO[club.rawOCode]
        } else {
            minimumForceByP0[club.rawP0Code]
        }
        if (player.force < minimum) return false
        if (player.force > maximumForceByP0[club.rawP0Code]) return false
        if (checkPositionCapacity && player.positionCode in 0..4) {
            if (club.positionCounts[player.positionCode] > maximumPositionCounts[player.positionCode]) return false
        }
        return true
    }

    private fun <T> shuffleLikeCollections(values: MutableList<T>, random: RandomSource) {
        for (index in values.lastIndex downTo 1) {
            val other = random.nextInt(index + 1)
            if (other != index) {
                val tmp = values[index]
                values[index] = values[other]
                values[other] = tmp
            }
        }
    }
}

data class LegacyImmediateSaleResult<T>(
    val requestedSaleValueStored: Int,
    val effectiveTransferValue: Int,
    val buyer: T?,
    val preparedClubIdentityKeysInOrder: List<String>,
    val transferPlan: LegacyTransferExecutionPlan?,
    val transferredState: LegacyTransferRuntimeState?,
)

/** `DialogIgrokInfo.B(int) -> components.n3 -> best.o.T1(...)`, with one shared deterministic RNG. */
object LegacyImmediatePlayerSaleRuntimeRule {
    fun <T> execute(
        requestedValue: Int,
        playerBaseValue: Int,
        player: LegacySalePlayerSnapshot,
        source: LegacySaleSourceClubSnapshot,
        world: LegacySaleBuyerWorld<T>,
        random: RandomSource,
        runtimeStateForBuyer: (T) -> LegacyTransferRuntimeState,
        transferInputForBuyer: (T) -> LegacyTransferExecutionInput,
    ): LegacyImmediateSaleResult<T> {
        val effectiveValue = if (requestedValue < playerBaseValue) {
            val fraction = if (random.nextInt(100) > 50) 0.15 else 0.10
            val minimum = playerBaseValue - Math.round(playerBaseValue.toDouble() * fraction).toInt()
            if (minimum >= requestedValue) minimum else requestedValue
        } else {
            requestedValue
        }

        val discovery = LegacyPlayerSaleBuyerDiscoveryRule.discover(player, source, world, random)
        val buyer = discovery.buyer
        if (buyer == null) {
            return LegacyImmediateSaleResult(
                requestedSaleValueStored = requestedValue,
                effectiveTransferValue = effectiveValue,
                buyer = null,
                preparedClubIdentityKeysInOrder = discovery.preparedClubIdentityKeysInOrder,
                transferPlan = null,
                transferredState = null,
            )
        }

        val plan = LegacyTransferExecutionRule.plan(
            transferInputForBuyer(buyer).copy(
                transferValue = effectiveValue,
                legacySecondaryChargeFlag = true,
                loanMove = false,
                legacyNonFinancialMoveFlag = false,
            ),
        )
        val state = LegacyTransferRuntimeMutation.apply(runtimeStateForBuyer(buyer), plan)
        return LegacyImmediateSaleResult(
            requestedSaleValueStored = requestedValue,
            effectiveTransferValue = effectiveValue,
            buyer = buyer,
            preparedClubIdentityKeysInOrder = discovery.preparedClubIdentityKeysInOrder,
            transferPlan = plan,
            transferredState = state,
        )
    }
}

data class LegacyPlayerSaleListingState(
    val listedForSale: Boolean,
    val rawSaleValue: Int,
)

object LegacyPlayerSaleListingRule {
    fun listForSale(current: LegacyPlayerSaleListingState, value: Int): LegacyPlayerSaleListingState =
        current.copy(listedForSale = true, rawSaleValue = value)

    /** Official `DialogIgrokInfo.n()` clears only the list flag and leaves the raw sale value intact. */
    fun removeFromSaleList(current: LegacyPlayerSaleListingState): LegacyPlayerSaleListingState =
        current.copy(listedForSale = false)
}
