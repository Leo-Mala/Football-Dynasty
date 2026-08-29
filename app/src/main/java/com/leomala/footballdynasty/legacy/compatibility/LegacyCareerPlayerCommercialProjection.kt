package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.manager.LegacyPlayerCommercialState

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
}
