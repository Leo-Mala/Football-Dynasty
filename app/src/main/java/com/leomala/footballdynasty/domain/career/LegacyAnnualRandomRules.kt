package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Small deterministic boundary for annual random decisions proven in the Brasfoot 2026/27
 * legacy corpus. Method names deliberately retain the legacy labels where sporting semantics
 * are not yet proven.
 */
object LegacyAnnualRandomRules {
    /** Structural parity for legacy best.a0.a(): `nextInt(100) > 30`. */
    fun bestA0AGate(random: RandomSource): Boolean = random.nextInt(100) > 30

    /** Structural parity for legacy best.a0.i(): `nextInt(100) > 25`. */
    fun bestA0IGate(random: RandomSource): Boolean = random.nextInt(100) > 25

    /**
     * The Java decompiler lost best.a0.j(c0, boolean, boolean), so these sites are intentionally
     * numbered by SMALI occurrence instead of receiving invented sporting names.
     */
    enum class BestA0JRandomSite(val thresholdExclusive: Int) {
        SITE_1(10),
        SITE_2(90),
        SITE_3(30),
        SITE_4(30),
        SITE_5(35),
        SITE_6(45),
        SITE_7(75),
        SITE_8(95),
    }

    /** Every characterized best.a0.j random site uses `nextInt(100) > threshold`. */
    fun bestA0JGate(random: RandomSource, site: BestA0JRandomSite): Boolean =
        random.nextInt(100) > site.thresholdExclusive

    /** Structural parity for the random predicate used by reachable best.f constructor branches. */
    fun bestFConstructorGate(random: RandomSource): Boolean = random.nextInt(100) > 10

    /** Structural parity for the direct random predicate in reachable best.f.n(): `<= 60`. */
    fun bestFNGate(random: RandomSource): Boolean = random.nextInt(100) <= 60

    /**
     * Whether the legacy constructor expands its primary `J0()` group for modes 0/1/2.
     *
     * The ordering here is intentional. In mode 0 the legacy bytecode computes the conditional
     * random predicate before checking `O0/W0`, so a draw can be consumed even when either flag
     * later forces the result true. Mode 1 short-circuits those flags before the random predicate.
     * Preserving this distinction prevents RNG drift after save/reopen.
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
                legacyJ0 == 29 && subjectO > 50 && bestFConstructorGate(random)
            val branch = if (!subjectO0 && !subjectW0) randomQualified else true
            legacyJ == 0 || branch
        }

        1 -> {
            val branch = if (subjectO0 || subjectW0) {
                true
            } else {
                legacyJ0 == 29 && subjectO > 50 && bestFConstructorGate(random)
            }
            legacyJ == 0 || branch
        }

        2 -> true
        else -> false
    }

    enum class BestFNRoute {
        G_THEN_OPTIONAL_H,
        OPTIONAL_I_THEN_OPTIONAL_H_THEN_G,
    }

    /**
     * Reproduces the control decision at the top of legacy `best.f.n(boolean)`.
     *
     * SMALI proves that `nextInt(100) <= 60` is reached only when `O() > 30 && Q0()`. The
     * subsequent `O0() && Q0()` check can still force the alternate route, but happens after the
     * draw. This method deliberately preserves that order and therefore the exact draw count.
     */
    fun bestFNRoute(
        random: RandomSource,
        subjectO: Int,
        subjectO0: Boolean,
        currentQ0: Boolean,
    ): BestFNRoute {
        var primaryRoute = if (subjectO > 30 && currentQ0) {
            bestFNGate(random)
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

    data class BestFQRange(
        val minLegacyO: Int,
        val maxLegacyO: Int,
    )

    /**
     * Pure projection of the range calculation at the start of legacy `best.f.q(...)`.
     * Field-oriented names are retained because the sporting meaning of the obfuscated values is
     * not required to preserve the proven filter behavior.
     */
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
        if (currentO == 1) {
            min = 1
        }

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

    /** Structural candidate filter shared by the duplicated mode branches in `best.f.q(...)`. */
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

    /** Structural eligibility used by the fallback pool in legacy `best.f.p()`. */
    fun bestFPFallbackEligible(
        candidateR0: Boolean,
        candidateQ0: Boolean,
        rosterSize: Int,
    ): Boolean = !candidateR0 && !candidateQ0 && rosterSize < 30

    /**
     * Deterministic replacement for legacy `Collections.shuffle(...)` sites reached by the annual
     * best.a0 -> best.f path. This uses the standard reverse Fisher-Yates shape with injected RNG.
     * It preserves an unbiased shuffle while deliberately not claiming legacy default-seed parity.
     */
    fun <T> shuffleInPlace(values: MutableList<T>, random: RandomSource) {
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
