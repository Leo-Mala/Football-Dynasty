package com.leomala.footballdynasty.legacy.compatibility

/**
 * Opaque decoder-facing snapshot of the two commercial fields proven on legacy `a.ac`.
 *
 * Their field names are proven by the corpus, but their scalar types, nullability,
 * sentinels, units, formulas and gameplay meaning are not yet certified. Values are
 * therefore retained verbatim as [Any?]. This is an evidence-preservation boundary,
 * not a financial domain model.
 */
data class LegacyCareerClubCommercialSnapshot(
    val ctInvest: Any?,
    val sponsor: Any?,
)

/**
 * Extracts the proven `a.ac` commercial slice without coercion or defaulting.
 *
 * The source class must match the exact serialized career-club class proven by the
 * legacy corpus. Presence is checked with [Map.containsKey] so a present legacy null
 * is preserved and remains distinguishable from a missing field. Unrelated serialized
 * club fields are deliberately ignored.
 */
object LegacyCareerClubCommercialSnapshotExtractor {
    fun extract(
        sourceClassName: String,
        fields: Map<String, Any?>,
    ): LegacyCareerClubCommercialSnapshot? {
        if (sourceClassName != LegacyCareerClubCommercialFields.SOURCE_CLASS) return null
        if (!fields.containsKey(LegacyCareerClubCommercialFields.INVESTMENT)) return null
        if (!fields.containsKey(LegacyCareerClubCommercialFields.SPONSOR)) return null

        return LegacyCareerClubCommercialSnapshot(
            ctInvest = fields[LegacyCareerClubCommercialFields.INVESTMENT],
            sponsor = fields[LegacyCareerClubCommercialFields.SPONSOR],
        )
    }
}
