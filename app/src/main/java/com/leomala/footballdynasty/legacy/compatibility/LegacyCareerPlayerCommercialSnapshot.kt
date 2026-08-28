package com.leomala.footballdynasty.legacy.compatibility

/**
 * Narrow compatibility slice for commercial fields proven on legacy `a.p`.
 *
 * Field names intentionally stay close to the decompiled source because the
 * exact gameplay semantics and sentinel values are not yet certified. This
 * type preserves evidence without pretending that the blocked `.s26` career
 * decoder is already end-to-end compatible.
 */
data class LegacyCareerPlayerCommercialSnapshot(
    val salario: Int,
    val rcClause: Int,
    val rcRenewYear: Int,
    val rcConvYear: Int,
    val pendSaleClub: Int,
    val pendSaleValue: Int,
    val pendIsLoan: Boolean,
)
