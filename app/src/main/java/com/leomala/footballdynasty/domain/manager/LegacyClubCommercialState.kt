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
    /**
     * Replaces only the proven `ctInvest` value while preserving the sponsor value
     * exactly as received from the legacy boundary.
     *
     * This is deliberately structural. It does not interpret investment amount,
     * currency, affordability, duration, sentinel values or any financial rule.
     */
    fun withInvestmentRaw(investmentRaw: Any?): LegacyClubCommercialState = copy(
        investmentRaw = investmentRaw,
    )

    /**
     * Replaces only the proven `sponsor` value while preserving the investment value
     * exactly as received from the legacy boundary.
     *
     * This is deliberately structural. It does not interpret sponsor identity,
     * revenue, duration, nullability or any commercial rule.
     */
    fun withSponsorRaw(sponsorRaw: Any?): LegacyClubCommercialState = copy(
        sponsorRaw = sponsorRaw,
    )

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
