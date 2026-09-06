package com.leomala.footballdynasty.domain.career

/**
 * Exact pure projection of the target/cap selection in executable legacy `best.o.s()` immediately
 * after the retained `N` accumulation and before the already-frozen high-`d0` RNG adjustment.
 *
 * Raw legacy names are preserved deliberately. No sporting meaning is assigned to obfuscated
 * fields or predicates. Authority: official `smali/best/o.smali` from the pinned corpus.
 */
object LegacyAnnualSeniorGrowthTargetRules {
    data class ClubState(
        val legacyR0: Boolean,
        val legacyO: Int,
        val legacyP0: Int,
        val legacyJ: Int,
        val legacyJ0: Int,
    )

    data class Result(
        val baselineTarget: Int,
        val clubSpecificCap: Int,
        val cappedTarget: Int,
    )

    fun calculate(club: ClubState): Result {
        val baselineTarget = if (club.legacyR0) {
            when (club.legacyO) {
                0, 4 -> 30
                1 -> 100
                2 -> 60
                3 -> 40
                else -> 20
            }
        } else {
            when (club.legacyP0) {
                5, 4 -> 100
                3 -> 75
                2 -> 40
                1 -> 30
                else -> 20
            }
        }

        val clubSpecificCap = when (club.legacyJ) {
            0 -> when (club.legacyJ0) {
                3, 72, 104, 65, 97 -> 95
                154, 85 -> 90
                21, 162 -> 80
                else -> 70
            }

            1 -> when (club.legacyJ0) {
                29, 11 -> 90
                195, 46, 42 -> 80
                151, 150 -> 70
                else -> 60
            }

            2 -> when (club.legacyJ0) {
                10, 129, 57 -> 75
                141, 169, 190 -> 70
                else -> 60
            }

            3 -> when (club.legacyJ0) {
                107, 49 -> 75
                98, 9, 59, 43 -> 70
                else -> 60
            }

            4 -> when (club.legacyJ0) {
                131 -> 80
                68 -> 70
                51 -> 65
                else -> 55
            }

            5 -> if (club.legacyJ0 == 143) 60 else 45
            else -> 100
        }

        return Result(
            baselineTarget = baselineTarget,
            clubSpecificCap = clubSpecificCap,
            cappedTarget = minOf(baselineTarget, clubSpecificCap),
        )
    }
}
