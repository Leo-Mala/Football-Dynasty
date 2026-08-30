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
    /**
     * Replaces only the four proven contract fields while preserving the pending-movement
     * slice byte-for-byte at the domain boundary.
     *
     * This is intentionally a structural operation, not a renewal/negotiation rule. The raw
     * values remain opaque and callers must not infer acceptance, expiry or money semantics.
     */
    fun withRawContractFields(
        salario: Int,
        rcClause: Int,
        rcRenewYear: Int,
        rcConvYear: Int,
    ): LegacyPlayerCommercialState = copy(
        contract = LegacyPlayerContractFields.fromRaw(
            salario = salario,
            rcClause = rcClause,
            rcRenewYear = rcRenewYear,
            rcConvYear = rcConvYear,
        ),
    )

    /**
     * Replaces only the three proven pending-movement fields while preserving the contract
     * slice byte-for-byte at the domain boundary.
     *
     * This does not execute a transfer, sale or loan. It only provides a lossless immutable
     * boundary for a future Java/SMALI-proven mutation to write the exact source-backed fields.
     */
    fun withRawPendingMovementFields(
        pendSaleClub: Int,
        pendSaleValue: Int,
        pendIsLoan: Boolean,
    ): LegacyPlayerCommercialState = copy(
        pendingMovement = LegacyPendingPlayerMovement.fromRaw(
            pendSaleClub = pendSaleClub,
            pendSaleValue = pendSaleValue,
            pendIsLoan = pendIsLoan,
        ),
    )

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
