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

    enum class BestA0IRoute {
        MODE_2_N_THEN_O_FALLBACK,
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

    /** Exact projection of `best.c0.Z0(o, boolean)` using already-counted target positions. */
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

    /** Exact club pre-filter at the start of SMALI-only `best.a0.i()`. */
    fun bestA0IClubEligible(
        clubQ0: Boolean,
        legacyJ: Int,
        legacyP0: Int,
    ): Boolean =
        !clubQ0 && if (legacyJ == 0) legacyP0 < 4 else legacyP0 < 5

    /**
     * Exact player pre-filter plus RNG gate in `best.a0.i()`. The random draw is intentionally
     * skipped unless all deterministic filters pass, matching the bytecode short-circuit order.
     */
    fun bestA0IPlayerEligible(
        random: RandomSource,
        subjectOverall: Int,
        legacyW: Int,
        subjectO0: Boolean,
    ): Boolean {
        if (subjectOverall <= 50 || legacyW >= 31 || !subjectO0) return false
        return LegacyAnnualRandomRules.bestA0IGate(random)
    }

    /** `best.a0.i()` always constructs `best.f` with mode 2, tries `n(false)`, then `o(false)`. */
    fun bestA0IRoute(): BestA0IRoute = BestA0IRoute.MODE_2_N_THEN_O_FALLBACK

    /**
     * Position chosen at the start of `best.a0.j(...)` after the legacy position list has already
     * been shuffled. Passing the order explicitly keeps the selection independently testable.
     */
    fun bestA0JOverloadedPosition(
        positionCounts: IntArray,
        shuffledPositions: List<Int>,
        highPass: Boolean,
    ): Int? {
        require(positionCounts.size >= 5) { "best.a0.j requires position counts 0..4" }
        require(shuffledPositions.all { it in 0..4 }) { "best.a0.j position order must contain only 0..4" }

        if (highPass) {
            shuffledPositions.firstOrNull { positionCounts[it] >= jHighPrimaryCounts[it] }
                ?.let { return it }
            return shuffledPositions.firstOrNull { positionCounts[it] >= jHighFallbackCounts[it] }
        }
        return shuffledPositions.firstOrNull { positionCounts[it] > jLowCounts[it] }
    }

    /**
     * Deterministic modern projection of the legacy `Collections.shuffle([0,1,2,3,4])` followed
     * by the exact overload scan. It intentionally does not claim implicit-seed bit parity.
     */
    fun bestA0JSelectOverloadedPosition(
        random: RandomSource,
        positionCounts: IntArray,
        highPass: Boolean,
    ): Int? {
        val positions = (0..4).toMutableList()
        LegacyAnnualRandomRules.shuffleInPlace(positions, random)
        return bestA0JOverloadedPosition(positionCounts, positions, highPass)
    }

    /** The SITE_1 draw is unconditional after the position scan, even when no position is found. */
    fun bestA0JInitialPlayerGate(random: RandomSource): Boolean =
        LegacyAnnualRandomRules.bestA0JGate(random, BestA0JRandomSite.SITE_1)

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
     * Exact routing after `best.a0.j(...)` has selected one player. This preserves sequential RNG
     * consumption: an O0 player on `p1=false` can consume SITE_3 and then one rating-band site.
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

    /**
     * Exact top-level branch of legacy `best.f.n(boolean)`.
     *
     * The ternary in Java/SMALI short-circuits `subjectO0 && currentQ0` to the alternate route
     * before evaluating the `nextInt(100) <= 60` predicate. Therefore that branch consumes zero
     * RNG draws. Only `!subjectO0 && subjectO > 30 && currentQ0` reaches the random predicate.
     */
    fun bestFNRoute(
        random: RandomSource,
        subjectO: Int,
        subjectO0: Boolean,
        currentQ0: Boolean,
    ): BestFNRoute {
        if (subjectO0 && currentQ0) {
            return BestFNRoute.OPTIONAL_I_THEN_OPTIONAL_H_THEN_G
        }
        if (subjectO <= 30 || !currentQ0) {
            return BestFNRoute.G_THEN_OPTIONAL_H
        }
        return if (LegacyAnnualRandomRules.bestFNGate(random)) {
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

        if (subjectO <= 20) {
            min = 0
            max = groupA0
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

    /** Mode-2 post-shuffle predicate inside `best.f.q(...)`: `!M1() && p0() >= 4`. */
    fun bestFMode2CandidateEligible(
        rosterSize: Int,
        candidateP0: Int,
    ): Boolean = !bestC0M1(rosterSize) && candidateP0 >= 4

    fun bestFPFallbackEligible(
        candidateR0: Boolean,
        candidateQ0: Boolean,
        rosterSize: Int,
    ): Boolean = !candidateR0 && !candidateQ0 && rosterSize < 30
}
