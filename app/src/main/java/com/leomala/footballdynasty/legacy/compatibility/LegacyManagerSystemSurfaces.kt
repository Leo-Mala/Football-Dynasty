package com.leomala.footballdynasty.legacy.compatibility

/**
 * Exact manager-loop surfaces proven by the official Phase 4R corpus.
 *
 * This catalog records reachability/provenance only. It deliberately does not infer lineup
 * eligibility, formation semantics, transfer rules, contract rules, finance formulas, stadium
 * prices, navigation order, or any other gameplay behavior that still requires Java/SMALI
 * evidence.
 */
enum class LegacyManagerSystemSurface(
    val legacyClassName: String,
) {
    LINEUP("ActivityEscalacao"),
    TACTICS("DialogTatics"),
    SAVED_TACTICS("ActivitySavedTatics"),
    PLAYER_SEARCH("ActivityProcura"),
    PLAYER_INFO("DialogIgrokInfo"),
    FINANCES("ActivityFinancas"),
    STADIUM("ActivityEstadio"),
}

object LegacyManagerSystemSurfaces {
    val confirmed: Set<LegacyManagerSystemSurface> = linkedSetOf(
        LegacyManagerSystemSurface.LINEUP,
        LegacyManagerSystemSurface.TACTICS,
        LegacyManagerSystemSurface.SAVED_TACTICS,
        LegacyManagerSystemSurface.PLAYER_SEARCH,
        LegacyManagerSystemSurface.PLAYER_INFO,
        LegacyManagerSystemSurface.FINANCES,
        LegacyManagerSystemSurface.STADIUM,
    )

    private val byLegacyClassName: Map<String, LegacyManagerSystemSurface> =
        confirmed.associateBy(LegacyManagerSystemSurface::legacyClassName)

    fun fromLegacyClassName(className: String): LegacyManagerSystemSurface? =
        byLegacyClassName[className]

    fun isConfirmedLegacyClassName(className: String): Boolean =
        className in byLegacyClassName
}
