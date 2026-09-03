package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.manager.LegacyPlayerCommercialState

/**
 * Decoder-facing representation of only the seven commercial fields proven on legacy `a.p`.
 *
 * This is deliberately a slice rather than a serialized-player model: unrelated `a.p` fields
 * remain outside this boundary and must be preserved by whichever full-object decoder/encoder
 * owns them.
 */
data class LegacyDecodedPlayerCommercialSlice(
    val sourceClassName: String,
    val fields: Map<String, Any?>,
)

/**
 * Lossless bridge between the proven legacy `a.p` commercial snapshot and the
 * persistence-independent manager domain.
 *
 * Both directions are intentionally one-to-one. They do not interpret sentinel
 * values, monetary units, contract validity, transfer acceptance, loan rules,
 * affordability, or any state mutation. Those behaviors remain blocked until
 * their Java/SMALI control flow is characterized.
 */
object LegacyCareerPlayerCommercialProjection {
    fun toDomain(snapshot: LegacyCareerPlayerCommercialSnapshot): LegacyPlayerCommercialState =
        LegacyPlayerCommercialState.fromRaw(
            salario = snapshot.salario,
            rcClause = snapshot.rcClause,
            rcRenewYear = snapshot.rcRenewYear,
            rcConvYear = snapshot.rcConvYear,
            pendSaleClub = snapshot.pendSaleClub,
            pendSaleValue = snapshot.pendSaleValue,
            pendIsLoan = snapshot.pendIsLoan,
        )

    /**
     * Projects the proven player commercial slice directly from decoder output while
     * preserving the extractor's exact `a.p` source-class and primitive-type boundary.
     *
     * Returning null means the supplied decoded object is not a complete proven `a.p`
     * commercial slice. No fallback, coercion, defaulting, sentinel interpretation, or
     * transfer/contract behavior is introduced here.
     */
    fun fromDecodedFields(
        sourceClassName: String,
        fields: Map<String, Any?>,
    ): LegacyPlayerCommercialState? =
        LegacyCareerPlayerCommercialSnapshotExtractor.extract(
            sourceClassName = sourceClassName,
            fields = fields,
        )?.let(::toDomain)

    fun toLegacySnapshot(state: LegacyPlayerCommercialState): LegacyCareerPlayerCommercialSnapshot =
        LegacyCareerPlayerCommercialSnapshot(
            salario = state.contract.salaryCode,
            rcClause = state.contract.clauseCode,
            rcRenewYear = state.contract.renewalYearCode,
            rcConvYear = state.contract.conversionYearCode,
            pendSaleClub = state.pendingMovement.clubCode,
            pendSaleValue = state.pendingMovement.valueCode,
            pendIsLoan = state.pendingMovement.loanFlag,
        )

    /**
     * Exports only the proven `a.p` commercial slice with its exact legacy field names and
     * primitive shapes. This does not claim to serialize a complete player object and does not
     * invent defaults for any unrelated field.
     */
    fun toDecodedFieldSlice(state: LegacyPlayerCommercialState): LegacyDecodedPlayerCommercialSlice {
        val snapshot = toLegacySnapshot(state)
        return LegacyDecodedPlayerCommercialSlice(
            sourceClassName = LegacyCareerPlayerCommercialFields.SOURCE_CLASS,
            fields = linkedMapOf(
                LegacyCareerPlayerCommercialFields.SALARY to snapshot.salario,
                LegacyCareerPlayerCommercialFields.RELEASE_CLAUSE to snapshot.rcClause,
                LegacyCareerPlayerCommercialFields.RENEW_YEAR to snapshot.rcRenewYear,
                LegacyCareerPlayerCommercialFields.CONVERSION_YEAR to snapshot.rcConvYear,
                LegacyCareerPlayerCommercialFields.PENDING_SALE_CLUB to snapshot.pendSaleClub,
                LegacyCareerPlayerCommercialFields.PENDING_SALE_VALUE to snapshot.pendSaleValue,
                LegacyCareerPlayerCommercialFields.PENDING_IS_LOAN to snapshot.pendIsLoan,
            ),
        )
    }

    /**
     * Replaces only the seven proven commercial fields inside an already-decoded complete `a.p`
     * field map, preserving every unrelated field and its existing value/reference.
     *
     * The method deliberately refuses a wrong source class or an input map missing any proven
     * field. It is therefore a narrow write-back boundary, not permission to synthesize a partial
     * legacy player object. No transfer, contract, monetary, sentinel, or loan semantics are
     * introduced.
     */
    fun writeBackToDecodedFields(
        sourceClassName: String,
        existingFields: Map<String, Any?>,
        state: LegacyPlayerCommercialState,
    ): Map<String, Any?>? {
        if (sourceClassName != LegacyCareerPlayerCommercialFields.SOURCE_CLASS) return null
        if (!LegacyCareerPlayerCommercialFields.confirmedNames.all(existingFields::containsKey)) {
            return null
        }

        val updated = LinkedHashMap(existingFields)
        val slice = toDecodedFieldSlice(state)
        slice.fields.forEach { (fieldName, value) -> updated[fieldName] = value }
        return updated
    }
}
