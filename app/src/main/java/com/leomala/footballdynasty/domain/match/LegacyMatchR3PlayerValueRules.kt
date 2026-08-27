package com.leomala.footballdynasty.domain.match

/** Exact numeric transformation recovered from reachable legacy `components.r3.g(best.o)`. */
object LegacyMatchR3PlayerValueRules {
    data class LegacyClubState(
        val legacyP0: Int,
        val legacyQ0: Boolean,
    )

    fun value(
        legacyO: Int,
        legacyP0Flag: Boolean,
        legacyO0Flag: Boolean,
        legacyW0Flag: Boolean,
        legacyClub: LegacyClubState?,
    ): Double {
        var adjusted = legacyO

        if (legacyP0Flag) {
            adjusted = Math.round(adjusted * 0.7).toInt()
        }

        if (legacyO0Flag) {
            val multiplier = if (legacyClub != null && legacyClub.legacyP0 <= 3) 1.02 else 1.05
            adjusted = Math.round(adjusted * multiplier).toInt()
        } else if (legacyW0Flag) {
            val multiplier = if (legacyClub != null && legacyClub.legacyP0 <= 3) 1.05 else 1.10
            adjusted = Math.round(adjusted * multiplier).toInt()
        }

        if (legacyClub?.legacyQ0 == true) {
            adjusted = Math.round(adjusted * 1.05).toInt()
        }

        return adjusted.toDouble() / 10.0
    }
}
