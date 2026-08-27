package com.leomala.footballdynasty.domain.match

/**
 * Pure Java/SMALI parity for the persisted/static `best.o` values consumed by the Phase 8 match
 * runtime. These values are reconstructed from facts already stored by the modern career model;
 * no match-only state is invented here.
 */
object LegacyMatchPersistedPlayerRules {
    /** `best.o.l0()` returns the position field copied from the source player. */
    fun legacyL0(position: Int): Int = position

    /** `best.o.A1(int)` normalizes side as `0` for zero and `1` for every non-zero value. */
    fun legacyF0(side: Int): Int = if (side == 0) 0 else 1

    /**
     * Exact control-flow projection of SMALI `best.o.r1()`, which derives `best.o.F` returned by
     * `R()` from position + CR1 + CR2. The duplicated CR1==6 branch for position 1 is deliberate:
     * the official bytecode tests CR1 again there, not CR2.
     */
    fun legacyR(position: Int, cr1: Int, cr2: Int): Int = when (position) {
        0, 2 -> 0
        1 -> when {
            cr1 == 13 || cr1 == 6 -> 1
            cr1 == 7 || cr1 == 10 -> 0
            cr2 == 13 -> 1
            cr1 == 6 -> 1 // Preserve the duplicate bytecode branch exactly.
            cr2 == 7 || cr2 == 10 -> 0
            cr1 == 8 || cr1 == 9 || cr1 == 11 || cr1 == 4 -> 1
            else -> 0
        }
        3 -> when {
            cr1 == 11 || cr1 == 9 || cr1 == 8 || cr1 == 4 -> 1
            cr1 == 7 || cr1 == 10 -> 0
            cr2 == 11 || cr2 == 9 || cr2 == 8 || cr2 == 4 -> 1
            cr2 == 7 || cr2 == 10 -> 0
            else -> 1
        }
        4 -> when {
            cr1 == 7 || cr1 == 10 -> 0
            cr1 == 8 || cr1 == 13 || cr1 == 6 -> 2
            else -> 1
        }
        else -> 0
    }
}
