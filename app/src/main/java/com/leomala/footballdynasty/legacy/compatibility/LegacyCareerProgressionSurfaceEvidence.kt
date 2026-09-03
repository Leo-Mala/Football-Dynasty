package com.leomala.footballdynasty.legacy.compatibility

/**
 * Reachable career/coach surfaces proven by the versioned legacy activity map.
 *
 * This catalog intentionally stops at reachability. A legacy Activity/layout proves that a
 * user-facing surface exists, but it does not prove invitation acceptance rules, dismissal
 * thresholds, reputation formulas, objective scoring, club-switch mutations, or persistence.
 * Those behaviors remain blocked until their Java/SMALI call paths are characterized.
 */
enum class LegacyCareerProgressionSurfaceEvidence(
    val legacyClassName: String,
    val observedLayoutName: String,
) {
    COACH_PROFILE("ActivityTecnico", "activity_tecnico"),
    CLUB_INVITATION("ActivityConvite", "activity_convite"),
    NATIONAL_TEAM_INVITATION("ActivityConviteSelecao", "activity_conviteselecao"),
    DISMISSALS("DialogDemissoes", "dialog_demissoes"),
    COACH_RANKING("ActivtyRankingTecnicos", "activity_ranking_tec"),
    COACH_HALL("ActivityHallTecnicos", "activity_bola_ouro"),
}

object LegacyCareerProgressionSurfaceEvidenceCatalog {
    val confirmed: Set<LegacyCareerProgressionSurfaceEvidence> = linkedSetOf(
        LegacyCareerProgressionSurfaceEvidence.COACH_PROFILE,
        LegacyCareerProgressionSurfaceEvidence.CLUB_INVITATION,
        LegacyCareerProgressionSurfaceEvidence.NATIONAL_TEAM_INVITATION,
        LegacyCareerProgressionSurfaceEvidence.DISMISSALS,
        LegacyCareerProgressionSurfaceEvidence.COACH_RANKING,
        LegacyCareerProgressionSurfaceEvidence.COACH_HALL,
    )

    private val byExactSource = confirmed.associateBy {
        it.legacyClassName to it.observedLayoutName
    }

    fun fromExactSource(
        legacyClassName: String,
        observedLayoutName: String,
    ): LegacyCareerProgressionSurfaceEvidence? = byExactSource[legacyClassName to observedLayoutName]

    fun isConfirmedExactSource(
        legacyClassName: String,
        observedLayoutName: String,
    ): Boolean = (legacyClassName to observedLayoutName) in byExactSource
}
