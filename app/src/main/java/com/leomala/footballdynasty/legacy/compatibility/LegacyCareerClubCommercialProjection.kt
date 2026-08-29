package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.manager.LegacyClubCommercialState

/**
 * Decoder-facing representation of only the two commercial fields proven on legacy `a.ac`.
 *
 * This is deliberately a slice rather than a serialized-club model: unrelated `a.ac` fields
 * remain outside this boundary and must be preserved by whichever full-object decoder/encoder
 * owns them.
 */
data class LegacyDecodedClubCommercialSlice(
    val sourceClassName: String,
    val fields: Map<String, Any?>,
)

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

    /**
     * Exports only the proven `a.ac` commercial slice with its exact legacy field names.
     * The values remain opaque because their scalar shapes and financial semantics are not yet
     * certified. This does not claim to serialize a complete club object and invents no defaults
     * for unrelated fields.
     */
    fun toDecodedFieldSlice(state: LegacyClubCommercialState): LegacyDecodedClubCommercialSlice {
        val snapshot = toLegacySnapshot(state)
        return LegacyDecodedClubCommercialSlice(
            sourceClassName = LegacyCareerClubCommercialFields.SOURCE_CLASS,
            fields = linkedMapOf(
                LegacyCareerClubCommercialFields.INVESTMENT to snapshot.ctInvest,
                LegacyCareerClubCommercialFields.SPONSOR to snapshot.sponsor,
            ),
        )
    }

    /**
     * Replaces only the two proven commercial fields inside an already-decoded complete `a.ac`
     * field map, preserving every unrelated field and its existing value/reference.
     *
     * The method deliberately refuses a wrong source class or an input map missing either proven
     * field. It is therefore a narrow write-back boundary, not permission to synthesize a partial
     * legacy club object. No financial semantics or defaults are introduced.
     */
    fun writeBackToDecodedFields(
        sourceClassName: String,
        existingFields: Map<String, Any?>,
        state: LegacyClubCommercialState,
    ): Map<String, Any?>? {
        if (sourceClassName != LegacyCareerClubCommercialFields.SOURCE_CLASS) return null
        if (!existingFields.containsKey(LegacyCareerClubCommercialFields.INVESTMENT)) return null
        if (!existingFields.containsKey(LegacyCareerClubCommercialFields.SPONSOR)) return null

        val updated = LinkedHashMap(existingFields)
        val slice = toDecodedFieldSlice(state)
        slice.fields.forEach { (fieldName, value) -> updated[fieldName] = value }
        return updated
    }
}
