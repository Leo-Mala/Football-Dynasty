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

    /** Structural parity for the explicit `new Random().nextInt(5)` draw inside best.o.s(). */
    fun bestOSGrowthDraw(random: RandomSource): Int = random.nextInt(5)

    /**
     * Freezes the complete high-d0 cap-adjustment branch from legacy `best.o.s()` after the
     * preceding club-specific cap has already been applied.
     *
     * SMALI order is significant:
     * - `d0 < 60` returns without consuming RNG;
     * - `d0 >= 60` always consumes exactly one `nextInt(5)` draw, even when `m` is outside 7..10;
     * - m=7/8/9/10 adds 5/15/25/30 plus that draw;
     * - any resulting value above 100 is clamped to 100.
     *
     * The deliberately neutral parameter names retain the legacy field identities until their
     * sporting semantics are proven corpus-wide.
     */
    fun bestOSApplyHighD0CapAdjustment(
        random: RandomSource,
        cappedTarget: Int,
        d0: Int,
        m: Int,
    ): Int {
        if (d0 < 60) return cappedTarget

        val draw = bestOSGrowthDraw(random)
        val adjusted = when (m) {
            7 -> cappedTarget + 5 + draw
            8 -> cappedTarget + 15 + draw
            9 -> cappedTarget + 25 + draw
            10 -> cappedTarget + 30 + draw
            else -> cappedTarget
        }
        return adjusted.coerceAtMost(100)
    }

    /**
     * Deterministic replacement for legacy `Collections.shuffle(...)` sites reached by annual
     * subsystems. Reverse Fisher-Yates gives the same unbiased distribution while making the RNG
     * state explicit and persistible; it does not claim parity with the APK's implicit seed.
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
