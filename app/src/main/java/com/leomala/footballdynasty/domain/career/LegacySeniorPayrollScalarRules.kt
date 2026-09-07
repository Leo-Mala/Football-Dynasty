package com.leomala.footballdynasty.domain.career

/**
 * Pure executable projection of legacy `best.o.o()` into the raw senior payroll scalar `best.o.n`.
 *
 * Raw obfuscated names are kept deliberately: no sporting meaning is assigned to these fields.
 * Authority is the pinned official `smali/best/o.smali` corpus. Arithmetic intentionally uses JVM
 * Int operations and `java.lang.Math.round(double).toInt()` so overflow/narrowing follows the
 * executable rather than a normalized financial model.
 */
object LegacySeniorPayrollScalarRules {
    data class ClubState(
        val legacyV0: Boolean,
        val legacyO: Int,
        val legacyF0: Int,
    )

    data class PlayerState(
        val legacyG: Int,
        val legacyJ: Int,
        val legacyE: Int,
        val legacyC: Boolean,
        val legacyD: Boolean,
    )

    fun calculate(
        club: ClubState,
        player: PlayerState,
        legacyGlobalV1: Boolean,
    ): Int {
        var scalar = if (club.legacyV0) {
            when (club.legacyO) {
                1 -> 750
                2 -> 550
                3 -> 500
                4, 5 -> 450
                else -> 350
            }
        } else {
            when (club.legacyO) {
                1 -> 600
                2 -> 500
                3 -> 450
                4, 5 -> 400
                else -> 350
            }
        }

        if (club.legacyF0 > 20) scalar += 50

        scalar += when (player.legacyG) {
            0 -> -70
            1 -> -30
            2 -> -40
            4 -> -50
            else -> 0
        }

        val halfRounded = java.lang.Math.round(scalar.toDouble() * 0.5).toInt()
        var result = player.legacyJ * 2 * halfRounded

        if (player.legacyE >= 32) {
            result -= (player.legacyE - 32) * 300
        }

        if (player.legacyC || player.legacyD) {
            result += player.legacyJ * 250
        }

        if (result < 500) result = 500

        if (player.legacyD) {
            result = java.lang.Math.round(result.toDouble() * 1.4).toInt()
        }

        if (legacyGlobalV1) result *= 4
        return result
    }
}
