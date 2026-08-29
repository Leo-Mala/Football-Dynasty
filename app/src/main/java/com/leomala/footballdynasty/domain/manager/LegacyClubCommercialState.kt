package com.leomala.footballdynasty.domain.manager

/**
 * Persistence-independent, lossless holder for the two commercial values proven
 * on legacy career club `a.ac`.
 *
 * Both values deliberately remain opaque. The corpus currently proves field
 * presence only, not scalar type, nullability contract, monetary unit, sentinel,
 * formula, or user-visible financial meaning. No gameplay decision may be based
 * on these values until Java/SMALI evidence certifies that behavior.
 */
data class LegacyClubCommercialState private constructor(
    val investmentRaw: Any?,
    val sponsorRaw: Any?,
) {
    companion object {
        fun fromRaw(
            investmentRaw: Any?,
            sponsorRaw: Any?,
        ): LegacyClubCommercialState = LegacyClubCommercialState(
            investmentRaw = investmentRaw,
            sponsorRaw = sponsorRaw,
        )
    }
}
