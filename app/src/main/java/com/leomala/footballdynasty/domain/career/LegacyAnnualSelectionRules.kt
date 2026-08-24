package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.career.LegacyAnnualRandomRules.BestA0JRandomSite
import com.leomala.footballdynasty.foundation.random.RandomSource

/** Pure projections of annual selection/control rules proven by Java + SMALI. */
object LegacyAnnualSelectionRules {
    private val nationalMinimumOverall = intArrayOf(1, 40, 30, 20, 5)
    private val divisionalMinimumOverall = intArrayOf(1, 10, 20, 40, 50, 55)
    private val divisionalMaximumOverall = intArrayOf(20, 30, 45, 85, 100, 100)

    private val jHighPrimaryCounts = intArrayOf(4, 5, 5, 10, 8)
    private val jHighFallbackCounts = intArrayOf(3, 4, 4, 8, 6)
    private val jLowCounts = intArrayOf(3, 4, 4, 6, 4)

    data class BestFQRange(
        val minLegacyO: Int,
        val maxLegacyO: Int,
    )

    enum class BestFNRoute {
        G_THEN_OPTIONAL_H,
        OPTIONAL_I_THEN_OPTIONAL_H_THEN_G,
    }

    data class BestA0JExecution(
        val mode: Int,
        val useN: Boolean,
    )

    fun legacyMinimumOverall(
        targetR0: Boolean,
        targetO: Int,
        targetP0: Int,
    ): Int = if (targetR0) {
        nationalMinimumOverall[targetO]
    } else {
        divisionalMinimumOverall[targetP0]
    }

    /** Exact projection of legacy `best.c0.M1()`. */
    fun bestC0M1(rosterSize: Int): Boolean = rosterSize >= 30

    /** Exact projection of `best.c0.a1(o, boolean)`. The boolean is unused by the APK method. */
    fun bestC0A1(
        rosterSize: Int,
        targetR0: Boolean,
        targetO: Int,
        targetP0: Int,
        subjectOverall: Int,
    ): Boolean =
        rosterSize < 30 &&
            subjectOverall >= legacyMinimumOverall(targetR0, targetO, targetP0)

    /** Exact projection of `best.c0.Z0(o, boolean)` using the already-counted target positions. */
    fun bestC0Z0(
        rosterSize: Int,
        targetR0: Boolean,
        targetO: Int,
        targetP0: Int,
        subjectOverall: Int,
        subjectPosition: Int,
        enforcePositionCaps: Boolean,
        positionCounts: IntArray,
    ): Boolean {
        if (rosterSize >= 30) return false
        if (subjectOverall < legacyMinimumOverall(targetR0, targetO, targetP0)) return false
        if (subjectOverall > divisionalMaximumOverall[targetP0]) return false
        if (!enforcePositionCaps) return true

        val maxExisting = when (subjectPosition) {
            0 -> 3
            1 -> 5
            2 -> 5
            3 -> 10
            4 -> 5
            else -> return true
        }
        return positionCounts.getOrElse(subjectPosition) { 0 } <= maxExisting
    }

    /**
     * Position chosen at the start of `best.a0.j(...)` after the legacy position list has already
     * been shuffled. Passing the order explicitly keeps the selection independently testable.
     */
    fun bestA0JOverloadedPosition(
        positionCounts: IntArray,
        shuffledPositions: List<Int>,
        highPass: Boolean,
    ): Int? {
        if (highPass) {
            shuffledPositions.firstOrNull { positionCounts[it] >= jHighPrimaryCounts[it] }
                ?.let { return it }
            return shuffledPositions.firstOrNull { positionCounts[it] >= jHighFallbackCounts[it] }
        }
        return shuffledPositions.firstOrNull { positionCounts[it] > jLowCounts[it] }
    }

    /** Filter applied after the unconditional SITE_1 draw in `best.a0.j(...)`. */
    fun bestA0JPlayerEligible(
        playerPosition: Int,
        selectedPosition: Int,
        playerN0: Boolean,
        playerO0: Boolean,
        site1Passed: Boolean,
    ): Boolean =
        playerPosition == selectedPosition &&
            !playerN0 &&
            (!playerO0 || site1Passed)

    /**
     * Exact routing after `best.a0.j(...)` has selected one player. This preserves the sequential
     * RNG consumption: an O0 player on the `p1=false` path can consume SITE_3 and then one rating
     * band site if SITE_3 fails.
     */
    fun bestA0JExecution(
        random: RandomSource,
        p1: Boolean,
        playerOverall: Int,
        playerO0: Boolean,
    ): BestA0JExecution {
        if (p1) {
            val useN = if (playerOverall < 40) {
                !LegacyAnnualRandomRules.bestA0JGate(random, BestA0JRandomSite.SITE_2)
            } else {
                true
            }
            return BestA0JExecution(mode = 0, useN = useN)
        }

        if (playerO0 && LegacyAnnualRandomRules.bestA0JGate(random, BestA0JRandomSite.SITE_3)) {
            return BestA0JExecution(mode = 1, useN = true)
        }

        val site = when {
            playerOverall >= 90 -> BestA0JRandomSite.SITE_4
            playerOverall >= 80 -> BestA0JRandomSite.SITE_5
            playerOverall >= 70 -> BestA0JRandomSite.SITE_6
            playerOverall >= 60 -> BestA0JRandomSite.SITE_7
            else -> BestA0JRandomSite.SITE_8
        }
        return if (LegacyAnnualRandomRules.bestA0JGate(random, site)) {
            BestA0JExecution(mode = 1, useN = true)
        } else {
            BestA0JExecution(mode = 0, useN = false)
        }
    }

    /**
     * Whether the legacy `best.f` constructor expands its primary group. Mode 0 deliberately
     * evaluates the qualifying random predicate before O0/W0; mode 1 short-circuits the flags.
     */
    fun bestFConstructorExpandsPrimaryGroup(
        random: RandomSource,
        mode: Int,
        legacyJ: Int,
        legacyJ0: Int,
        subjectO: Int,
        subjectO0: Boolean,
        subjectW0: Boolean,
    ): Boolean = when (mode) {
        0 -> {
            val randomQualified =
                legacyJ0 == 29 &&
                    subjectO > 50 &&
                    LegacyAnnualRandomRules.bestFConstructorGate(random)
            val branch = if (!subjectO0 && !subjectW0) randomQualified else true
            legacyJ == 0 || branch
        }

        1 -> {
            val branch = if (subjectO0 || subjectW0) {
                true
            } else {
                legacyJ0 == 29 &&
                    subjectO > 50 &&
                    LegacyAnnualRandomRules.bestFConstructorGate(random)
            }
            legacyJ == 0 || branch
        }

        2 -> true
        else -> false
    }

    /** Exact control decision at the top of legacy `best.f.n(boolean)`. */
    fun bestFNRoute(
        random: RandomSource,
        subjectO: Int,
        subjectO0: Boolean,
        currentQ0: Boolean,
    ): BestFNRoute {
        var primaryRoute = if (subjectO > 30 && currentQ0) {
            LegacyAnnualRandomRules.bestFNGate(random)
        } else {
            true
        }

        if (subjectO0 && currentQ0) {
            primaryRoute = false
        }

        return if (primaryRoute) {
            BestFNRoute.G_THEN_OPTIONAL_H
        } else {
            BestFNRoute.OPTIONAL_I_THEN_OPTIONAL_H_THEN_G
        }
    }

    /** Per-group range calculation at the start of legacy `best.f.q(...)`. */
    fun bestFQRange(
        mode: Int,
        currentO: Int,
        currentJ: Int,
        currentP0: Int,
        subjectO: Int,
        groupA0: Int,
    ): BestFQRange {
        var min = currentO - 1
        var max = currentO + 1
        if (currentO == 1) min = 1

        if (mode == 1) {
            if (currentJ != 0 || currentP0 < 4 || subjectO < 40) {
                min = 1
                max = 2
            } else {
                min = 1
                max = 1
            }
        }

        if (subjectO <= 5) {
            min = 0
            max = groupA0
        } else if (subjectO <= 20) {
            min = 0
        }

        return BestFQRange(minLegacyO = min, maxLegacyO = max)
    }

    fun bestFQCandidateEligible(
        candidateO: Int,
        range: BestFQRange,
        isCurrent: Boolean,
        candidateQ0: Boolean,
        rosterSize: Int,
    ): Boolean =
        candidateO in range.minLegacyO..range.maxLegacyO &&
            !isCurrent &&
            !candidateQ0 &&
            rosterSize < 30

    fun bestFPFallbackEligible(
        candidateR0: Boolean,
        candidateQ0: Boolean,
        rosterSize: Int,
    ): Boolean = !candidateR0 && !candidateQ0 && rosterSize < 30
}
