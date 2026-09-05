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
     * Structural parity for the explicit `new Random().nextInt(5)` draw reached inside
     * `best.o.s()` (annual senior growth). The surrounding branch semantics remain intentionally
     * outside this boundary until the complete SMALI control flow is frozen; exposing the draw
     * separately prevents hidden RNG consumption and keeps the persisted career RNG auditable.
     */
    fun bestOSGrowthDraw(random: RandomSource): Int = random.nextInt(5)

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
