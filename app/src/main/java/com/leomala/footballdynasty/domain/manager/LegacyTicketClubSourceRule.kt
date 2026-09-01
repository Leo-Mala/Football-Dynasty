package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.career.LegacyCountryAssetCodes

/**
 * Immutable source fields consumed by legacy ticket pricing/attendance before career-only state.
 *
 * `best.c0.p0()` reads the serialized team reputation with an in-place [0,5] clamp. `best.c0.J()`
 * resolves the serialized country through `best.y.P<country>.g()`. Both values therefore come from
 * the official `.ban` club source and must never be reconstructed from modern ratings or standings.
 */
data class LegacyTicketClubSourceFields(
    val country: Int,
    val reputation: Int,
)

data class LegacyTicketClubSourceProjection(
    val homeRawP0: Int,
    val awayRawP0: Int,
    val homeRawJ: Int,
)

object LegacyTicketClubSourceRule {
    fun project(
        home: LegacyTicketClubSourceFields,
        away: LegacyTicketClubSourceFields,
    ): LegacyTicketClubSourceProjection {
        val homeJ = LegacyCountryAssetCodes.groupForLegacyCountry(home.country)
            ?: throw IllegalArgumentException("Legacy home country P${home.country} is outside P0..P220")
        return LegacyTicketClubSourceProjection(
            homeRawP0 = clampLegacyP0(home.reputation),
            awayRawP0 = clampLegacyP0(away.reputation),
            homeRawJ = homeJ,
        )
    }

    private fun clampLegacyP0(value: Int): Int = when {
        value > 5 -> 5
        value < 0 -> 0
        else -> value
    }
}
