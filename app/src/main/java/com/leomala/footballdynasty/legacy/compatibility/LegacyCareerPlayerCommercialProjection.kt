package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.manager.LegacyPlayerCommercialState

/**
 * Lossless bridge from the proven legacy `a.p` commercial snapshot into the
 * persistence-independent manager domain.
 *
 * This projection is intentionally one-to-one. It does not interpret sentinel
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
}
