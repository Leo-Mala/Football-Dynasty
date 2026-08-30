package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of the stadium expansion quote path in
 * `ActivityEstadio.c()/e()/i()/onCreate()`.
 *
 * Evidence is cross-checked against the official Brasfoot 2026 Java + SMALI.
 * This intentionally preserves the legacy branch ordering in `onCreate()`:
 * `J() >= 2` is checked before `J() >= 6` and `J() >= 10`, making the latter
 * branches unreachable. Do not reorder those checks as a cleanup.
 */
data class LegacyStadiumExpansionQuote(
    val additions: List<Int>,
    val effectiveLimits: List<Int>,
    val availableAdditions: List<Int>,
    val accepted: Boolean,
    val totalCost: Int?,
    val constructionDays: Int?,
)

object LegacyStadiumExpansionRule {
    private const val FIXED_CONSTRUCTION_COST: Int = 100_000

    private val defaultLimits = listOf(18_000, 80_000, 9_000, 700)

    // `best.j0.Q`, retained here as legacy evidence rather than modern policy.
    private val legacyLimitTable = listOf(
        listOf(18_000, 80_000, 9_000, 700),
        listOf(20_000, 100_000, 10_000, 800),
        listOf(22_000, 110_000, 10_000, 900),
        listOf(25_000, 120_000, 10_000, 1_200),
    )

    private val projectedCapacityThresholds = listOf(
        listOf(1_000, 2_500, 3_500, 10_000, 18_000),
        listOf(5_000, 15_000, 30_000, 60_000, 80_000),
        listOf(1_000, 2_000, 3_000, 5_000, 9_000),
        listOf(100, 200, 500, 600, 700),
    )

    private val unitCosts = listOf(
        listOf(80, 160, 240, 500, 700),
        listOf(120, 380, 640, 700, 1_400),
        listOf(300, 600, 750, 800, 1_200),
        listOf(1_500, 3_500, 4_000, 6_000, 6_400),
    )

    fun effectiveLimits(
        currentCapacities: List<Int>,
        legacyJValue: Int,
    ): List<Int> {
        require(currentCapacities.size == 4)

        val limits = defaultLimits.mapIndexed { index, default ->
            maxOf(default, currentCapacities[index])
        }.toMutableList()

        // Preserve the exact legacy if / else-if ordering.
        if (legacyJValue >= 2) {
            return legacyLimitTable[1]
        } else if (legacyJValue >= 6) {
            return legacyLimitTable[2]
        } else if (legacyJValue >= 10) {
            return legacyLimitTable[3]
        }

        return limits
    }

    fun quote(
        currentCapacities: List<Int>,
        additions: List<Int>,
        legacyJValue: Int,
    ): LegacyStadiumExpansionQuote {
        require(currentCapacities.size == 4)
        require(additions.size == 4)
        require(additions.all { it >= 0 })

        val limits = effectiveLimits(currentCapacities, legacyJValue)
        val available = limits.indices.map { limits[it] - currentCapacities[it] }
        val anyAddition = additions.sum() != 0
        val withinLimits = additions.indices.all { additions[it] <= available[it] }
        val accepted = anyAddition && withinLimits

        if (!accepted) {
            return LegacyStadiumExpansionQuote(
                additions = additions,
                effectiveLimits = limits,
                availableAdditions = available,
                accepted = false,
                totalCost = null,
                constructionDays = null,
            )
        }

        val variableCost = additions.indices.sumOf { index ->
            categoryCost(
                category = index,
                currentCapacity = currentCapacities[index],
                addition = additions[index],
            )
        }
        val totalAdded = additions.sum()

        return LegacyStadiumExpansionQuote(
            additions = additions,
            effectiveLimits = limits,
            availableAdditions = available,
            accepted = true,
            totalCost = variableCost + FIXED_CONSTRUCTION_COST,
            constructionDays = constructionDays(totalAdded),
        )
    }

    fun categoryCost(
        category: Int,
        currentCapacity: Int,
        addition: Int,
    ): Int {
        require(category in 0..3)
        require(addition >= 0)

        val projectedCapacity = currentCapacity + addition
        val thresholds = projectedCapacityThresholds[category]
        var costIndex = 4
        for (index in thresholds.indices) {
            if (projectedCapacity <= thresholds[index]) {
                costIndex = index
                break
            }
        }
        return unitCosts[category][costIndex] * addition
    }

    fun constructionDays(totalAdded: Int): Int = when {
        totalAdded < 1_000 -> 15
        totalAdded < 10_000 -> 20
        totalAdded < 30_000 -> 30
        else -> 40
    }
}
