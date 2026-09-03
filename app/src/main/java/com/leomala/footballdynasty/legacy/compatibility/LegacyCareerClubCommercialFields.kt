package com.leomala.footballdynasty.legacy.compatibility

/**
 * Field-name boundary for the club commercial state proven on legacy `a.ac`.
 *
 * The decompiled corpus proves these names exist on the serialized career club,
 * but the current evidence does not yet certify their scalar types, sentinel
 * values, units, update formulas, or user-visible meaning. Keeping this boundary
 * name-only prevents the modern runtime from accidentally inventing a balance,
 * sponsorship value, investment rule, or other financial behavior before the
 * Java/SMALI control flow is characterized.
 */
object LegacyCareerClubCommercialFields {
    const val SOURCE_CLASS = "a.ac"
    const val INVESTMENT = "ctInvest"
    const val SPONSOR = "sponsor"

    val confirmedNames: Set<String> = linkedSetOf(INVESTMENT, SPONSOR)

    fun isConfirmed(name: String): Boolean = name in confirmedNames
}
