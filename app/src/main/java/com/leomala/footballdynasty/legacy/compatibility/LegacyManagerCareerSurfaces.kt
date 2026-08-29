package com.leomala.footballdynasty.legacy.compatibility

/**
 * Static reachability evidence for manager/coach-facing legacy surfaces.
 *
 * These class names are proven by the certified legacy activity inventory. This
 * catalog deliberately records only that the surfaces exist in the supplied game;
 * it does not infer dismissal rules, job-offer conditions, reputation formulas,
 * ranking criteria, career progression, or navigation ordering.
 */
enum class LegacyManagerCareerSurface(
    val legacyClassName: String,
) {
    COACH("ActivityTecnico"),
    CLUB_INVITATION("ActivityConvite"),
    DISMISSALS("DialogDemissoes"),
    COACH_RANKING("ActivtyRankingTecnicos"),
    COACH_HALL("ActivityHallTecnicos"),
}

object LegacyManagerCareerSurfaces {
    val confirmed: Set<LegacyManagerCareerSurface> = linkedSetOf(
        LegacyManagerCareerSurface.COACH,
        LegacyManagerCareerSurface.CLUB_INVITATION,
        LegacyManagerCareerSurface.DISMISSALS,
        LegacyManagerCareerSurface.COACH_RANKING,
        LegacyManagerCareerSurface.COACH_HALL,
    )

    private val byLegacyClassName: Map<String, LegacyManagerCareerSurface> =
        confirmed.associateBy(LegacyManagerCareerSurface::legacyClassName)

    fun fromLegacyClassName(className: String): LegacyManagerCareerSurface? =
        byLegacyClassName[className]

    fun isConfirmedLegacyClassName(className: String): Boolean =
        className in byLegacyClassName
}
