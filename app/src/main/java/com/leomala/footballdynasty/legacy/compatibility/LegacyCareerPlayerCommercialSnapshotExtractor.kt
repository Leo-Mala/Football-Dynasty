package com.leomala.footballdynasty.legacy.compatibility

/**
 * Strict decoder-facing extractor for the seven commercial fields proven on legacy `a.p`.
 *
 * The serialized player object contains many fields besides this commercial slice, so
 * unrelated entries are deliberately ignored. The source class must match the exact
 * serialized player class proven by the legacy corpus, and every field in the slice must
 * be present with its exact proven JVM scalar type. No numeric coercion, default value,
 * sentinel interpretation, or gameplay meaning is introduced here.
 */
object LegacyCareerPlayerCommercialSnapshotExtractor {
    fun extract(
        sourceClassName: String,
        fields: Map<String, Any?>,
    ): LegacyCareerPlayerCommercialSnapshot? {
        if (sourceClassName != LegacyCareerPlayerCommercialFields.SOURCE_CLASS) return null

        val salario = fields[LegacyCareerPlayerCommercialFields.SALARY] as? Int ?: return null
        val rcClause = fields[LegacyCareerPlayerCommercialFields.RELEASE_CLAUSE] as? Int ?: return null
        val rcRenewYear = fields[LegacyCareerPlayerCommercialFields.RENEW_YEAR] as? Int ?: return null
        val rcConvYear = fields[LegacyCareerPlayerCommercialFields.CONVERSION_YEAR] as? Int ?: return null
        val pendSaleClub = fields[LegacyCareerPlayerCommercialFields.PENDING_SALE_CLUB] as? Int ?: return null
        val pendSaleValue = fields[LegacyCareerPlayerCommercialFields.PENDING_SALE_VALUE] as? Int ?: return null
        val pendIsLoan = fields[LegacyCareerPlayerCommercialFields.PENDING_IS_LOAN] as? Boolean ?: return null

        return LegacyCareerPlayerCommercialSnapshot(
            salario = salario,
            rcClause = rcClause,
            rcRenewYear = rcRenewYear,
            rcConvYear = rcConvYear,
            pendSaleClub = pendSaleClub,
            pendSaleValue = pendSaleValue,
            pendIsLoan = pendIsLoan,
        )
    }
}
