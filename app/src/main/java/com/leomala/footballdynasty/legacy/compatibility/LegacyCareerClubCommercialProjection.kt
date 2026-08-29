package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.manager.LegacyClubCommercialState

/**
 * Lossless bridge between the proven opaque `a.ac` commercial snapshot and the
 * persistence-independent manager domain.
 *
 * This projection performs no conversion, defaulting, normalization or gameplay
 * interpretation. Exact runtime values (including null) are preserved in both
 * directions until Java/SMALI evidence proves scalar types and financial rules.
 */
object LegacyCareerClubCommercialProjection {
    fun toDomain(snapshot: LegacyCareerClubCommercialSnapshot): LegacyClubCommercialState =
        LegacyClubCommercialState.fromRaw(
            investmentRaw = snapshot.ctInvest,
            sponsorRaw = snapshot.sponsor,
        )

    /**
     * Projects the proven commercial slice directly from decoder output while preserving the
     * extractor's strict `a.ac` source-class and field-presence boundary.
     *
     * Returning null means the supplied decoded object is not a complete proven `a.ac`
     * commercial slice. No fallback, coercion or financial interpretation is attempted.
     */
    fun fromDecodedFields(
        sourceClassName: String,
        fields: Map<String, Any?>,
    ): LegacyClubCommercialState? =
        LegacyCareerClubCommercialSnapshotExtractor.extract(
            sourceClassName = sourceClassName,
            fields = fields,
        )?.let(::toDomain)

    fun toLegacySnapshot(state: LegacyClubCommercialState): LegacyCareerClubCommercialSnapshot =
        LegacyCareerClubCommercialSnapshot(
            ctInvest = state.investmentRaw,
            sponsor = state.sponsorRaw,
        )
}
