package com.leomala.footballdynasty.domain.manager

/**
 * Neutral projection of the salary/contract fields proven on legacy `a.p`.
 *
 * The raw values intentionally remain uninterpreted. In particular, this type does
 * not infer currency, clause activation, contract duration, renewal semantics,
 * conversion semantics, or sentinel meanings before their Java/SMALI control flow
 * has been characterized.
 *
 * Only scalar values cross this boundary so the modern domain remains independent
 * from the legacy compatibility implementation layer.
 */
data class LegacyPlayerContractFields(
    val salaryCode: Int,
    val clauseCode: Int,
    val renewalYearCode: Int,
    val conversionYearCode: Int,
) {
    companion object {
        fun fromRaw(
            salario: Int,
            rcClause: Int,
            rcRenewYear: Int,
            rcConvYear: Int,
        ): LegacyPlayerContractFields = LegacyPlayerContractFields(
            salaryCode = salario,
            clauseCode = rcClause,
            renewalYearCode = rcRenewYear,
            conversionYearCode = rcConvYear,
        )
    }
}
