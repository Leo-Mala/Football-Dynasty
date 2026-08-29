package com.leomala.footballdynasty.legacy.compatibility

/**
 * Single field-name boundary for the commercial state proven on legacy `a.p`.
 *
 * These names and scalar JVM shapes are confirmed by the decompiled/serialized
 * player schema. This catalog intentionally does not define sentinel values,
 * units, offer validity, affordability, transfer execution, loan rules, or any
 * financial mutation.
 */
object LegacyCareerPlayerCommercialFields {
    const val SOURCE_CLASS = "a.p"
    const val SALARY = "salario"
    const val RELEASE_CLAUSE = "rcClause"
    const val RENEW_YEAR = "rcRenewYear"
    const val CONVERSION_YEAR = "rcConvYear"
    const val PENDING_SALE_CLUB = "pendSaleClub"
    const val PENDING_SALE_VALUE = "pendSaleValue"
    const val PENDING_IS_LOAN = "pendIsLoan"

    enum class ScalarType {
        INT,
        BOOLEAN,
    }

    /**
     * Decoder-facing schema for the seven commercial fields whose names and
     * primitive JVM types are already proven on legacy `a.p`.
     *
     * Keeping this map exact prevents a future `.s26` decoder from widening,
     * coercing, or guessing types while the gameplay meaning of the raw values
     * is still under Java/SMALI characterization.
     */
    val confirmedTypes: Map<String, ScalarType> = linkedMapOf(
        SALARY to ScalarType.INT,
        RELEASE_CLAUSE to ScalarType.INT,
        RENEW_YEAR to ScalarType.INT,
        CONVERSION_YEAR to ScalarType.INT,
        PENDING_SALE_CLUB to ScalarType.INT,
        PENDING_SALE_VALUE to ScalarType.INT,
        PENDING_IS_LOAN to ScalarType.BOOLEAN,
    )

    val confirmedNames: Set<String> = confirmedTypes.keys

    fun isConfirmed(name: String): Boolean = name in confirmedTypes

    fun typeOf(name: String): ScalarType? = confirmedTypes[name]
}
