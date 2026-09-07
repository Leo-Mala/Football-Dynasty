package com.leomala.footballdynasty.domain.career

/**
 * Exact pure projection of the rate accumulated into legacy senior field `best.o.N`
 * by the first half of executable `best.o.s()`.
 *
 * Field names intentionally remain raw legacy names. SMALI is authoritative; this
 * boundary does not assign sporting meaning to obfuscated scalars.
 */
object LegacyAnnualSeniorGrowthAccumulationRules {
    data class ClubState(
        val legacyF0: Int,
        val legacyR0: Boolean,
        val legacyP0: Int,
        val legacyJ: Int,
        val legacyJ0: Int,
    )

    data class PlayerState(
        val legacyE: Int,
        val legacyJ: Int,
        val legacyD0: Int,
        val legacyM: Int,
        val legacyMFlag: Boolean,
        val legacyW0: Boolean,
        val legacyO0: Boolean,
    )

    data class Result(
        val effectiveClubBand: Int,
        val clubBandBonus: Double,
        val rateBeforeNonNegativeGuard: Double,
        val increment: Double,
    )

    data class AccumulationResult(
        val rate: Result,
        val previousN: Double,
        val updatedN: Double,
    )

    fun calculate(club: ClubState, player: PlayerState): Result {
        var clubBand = club.legacyF0
        var clubBandBonus = 0.0
        if (!club.legacyR0) {
            clubBandBonus = 0.03
            clubBand = when {
                club.legacyP0 >= 4 -> 20
                club.legacyP0 == 3 -> 18
                else -> 12
            }
        }

        var rate = baseRate(clubBand, player.legacyE)

        if (player.legacyMFlag) rate += 0.04

        rate -= when (player.legacyJ) {
            in 30..40 -> 0.02
            in 41..50 -> 0.03
            in 51..70 -> 0.04
            in 71..100 -> 0.05
            else -> 0.0
        }

        if (player.legacyD0 > 0) {
            rate -= when {
                player.legacyD0 < 50 -> 0.05
                player.legacyD0 < 70 -> 0.02
                else -> 0.0
            }
            rate += when {
                player.legacyM >= 9 -> 0.07
                player.legacyM >= 7 -> 0.05
                else -> 0.0
            }
        }

        rate += when {
            player.legacyW0 -> 0.02
            player.legacyO0 -> 0.01
            else -> 0.0
        }

        rate = applyClubTypeAdjustment(
            rate = rate,
            legacyJ = club.legacyJ,
            legacyJ0 = club.legacyJ0,
        )

        rate += clubBandBonus

        // Executable SMALI substitutes 0.01 only for a strictly negative rate.
        // An exact zero remains zero.
        val increment = if (rate < 0.0) 0.01 else rate

        return Result(
            effectiveClubBand = clubBand,
            clubBandBonus = clubBandBonus,
            rateBeforeNonNegativeGuard = rate,
            increment = increment,
        )
    }

    /**
     * Executable `best.o.s()` immediately adds the calculated increment to retained field `N`.
     * Keep the arithmetic as an ordinary JVM Double addition: no clamp, rounding, or reset is
     * introduced at this boundary because the later cap/finalization section owns those effects.
     */
    fun accumulate(
        currentN: Double,
        club: ClubState,
        player: PlayerState,
    ): AccumulationResult {
        val rate = calculate(club = club, player = player)
        return AccumulationResult(
            rate = rate,
            previousN = currentN,
            updatedN = currentN + rate.increment,
        )
    }

    private fun baseRate(clubBand: Int, playerE: Int): Double = when {
        clubBand >= 19 -> when {
            playerE < 20 -> 0.16
            playerE < 23 -> 0.12
            playerE < 29 -> 0.10
            else -> 0.08
        }

        clubBand >= 15 -> when {
            playerE < 18 -> 0.12
            playerE < 21 -> 0.10
            playerE < 29 -> 0.08
            else -> 0.06
        }

        clubBand >= 11 -> when {
            playerE < 18 -> 0.10
            playerE < 21 -> 0.08
            playerE < 29 -> 0.06
            else -> 0.04
        }

        else -> when {
            playerE < 18 -> 0.08
            playerE < 21 -> 0.06
            playerE < 29 -> 0.04
            else -> 0.02
        }
    }

    private fun applyClubTypeAdjustment(
        rate: Double,
        legacyJ: Int,
        legacyJ0: Int,
    ): Double = when (legacyJ) {
        2, 3 -> if (rate > 0.06) rate - 0.02 else rate
        5 -> if (rate > 0.06) rate - 0.04 else rate
        4 -> if (legacyJ0 != 131 && rate > 0.06) rate - 0.03 else rate

        // SMALI contains sequential equality checks against the same j0 value
        // (29, 11, 42, 195), making the apparent exemption conjunction
        // unreachable. Effective executable behavior is therefore the normal
        // strict > 0.06 subtraction for every J==1 club.
        1 -> if (rate > 0.06) rate - 0.02 else rate

        0 -> when (legacyJ0) {
            3, 72, 104, 65, 97 -> rate + 0.01
            154, 85, 21 -> if (rate > 0.06) rate - 0.01 else rate
            else -> if (rate > 0.06) rate - 0.02 else rate
        }

        else -> rate
    }
}
