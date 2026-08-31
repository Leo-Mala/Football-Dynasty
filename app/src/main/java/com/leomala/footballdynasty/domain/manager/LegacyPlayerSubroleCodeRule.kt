package com.leomala.footballdynasty.domain.manager

/**
 * Exact `best.o.r1()` subrole-code derivation from the official legacy corpus.
 *
 * Inputs are the already-preserved legacy position/CR codes. The output intentionally remains a
 * numeric legacy code; only usages explicitly proven by the legacy UI may assign sporting meaning.
 * Unknown position codes retain the previous value because the bytecode returns without writing F.
 */
object LegacyPlayerSubroleCodeRule {
    fun resolve(
        positionCode: Int,
        cr1: Int,
        cr2: Int,
        currentSubroleCode: Int = 0,
    ): Int = when (positionCode) {
        0, 2 -> 0
        1 -> when {
            cr1 == 13 || cr1 == 6 -> 1
            cr1 == 7 || cr1 == 10 -> 0
            cr2 == 13 -> 1
            // Preserves the redundant primary-code check present in the SMALI branch order.
            cr1 == 6 -> 1
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
        else -> currentSubroleCode
    }
}
