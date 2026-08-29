package com.leomala.footballdynasty.domain.manager

/**
 * Aggregate view of the commercial player state that is already proven on legacy `a.p`.
 *
 * This deliberately composes the certified raw contract and pending-movement slices
 * without assigning business meaning to their integer codes. Sentinel interpretation,
 * affordability, acceptance, transfer execution, loan behavior and financial mutation
 * remain blocked until their Java/SMALI control flow is characterized.
 */
data class LegacyPlayerCommercialState(
    val contract: LegacyPlayerContractFields,
    val pendingMovement: LegacyPendingPlayerMovement,
) {
    companion object {
        fun fromRaw(
            salario: Int,
            rcClause: Int,
            rcRenewYear: Int,
            rcConvYear: Int,
            pendSaleClub: Int,
            pendSaleValue: Int,
            pendIsLoan: Boolean,
        ): LegacyPlayerCommercialState = LegacyPlayerCommercialState(
            contract = LegacyPlayerContractFields.fromRaw(
                salario = salario,
                rcClause = rcClause,
                rcRenewYear = rcRenewYear,
                rcConvYear = rcConvYear,
            ),
            pendingMovement = LegacyPendingPlayerMovement.fromRaw(
                pendSaleClub = pendSaleClub,
                pendSaleValue = pendSaleValue,
                pendIsLoan = pendIsLoan,
            ),
        )
    }
}
