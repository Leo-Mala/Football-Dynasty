package com.leomala.footballdynasty.legacy.compatibility

/**
 * Single field-name boundary for the commercial state proven on legacy `a.p`.
 *
 * These names are confirmed by the decompiled/serialized player schema. This
 * catalog intentionally does not define sentinel values, units, offer validity,
 * affordability, transfer execution, loan rules, or any financial mutation.
 */
object LegacyCareerPlayerCommercialFields {
    const val SALARY = "salario"
    const val RELEASE_CLAUSE = "rcClause"
    const val RENEW_YEAR = "rcRenewYear"
    const val CONVERSION_YEAR = "rcConvYear"
    const val PENDING_SALE_CLUB = "pendSaleClub"
    const val PENDING_SALE_VALUE = "pendSaleValue"
    const val PENDING_IS_LOAN = "pendIsLoan"

    val confirmedNames: Set<String> = linkedSetOf(
        SALARY,
        RELEASE_CLAUSE,
        RENEW_YEAR,
        CONVERSION_YEAR,
        PENDING_SALE_CLUB,
        PENDING_SALE_VALUE,
        PENDING_IS_LOAN,
    )

    fun isConfirmed(name: String): Boolean = name in confirmedNames
}
