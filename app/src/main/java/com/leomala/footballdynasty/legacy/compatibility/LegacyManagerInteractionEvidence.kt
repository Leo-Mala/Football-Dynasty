package com.leomala.footballdynasty.legacy.compatibility

/**
 * Exact manager interactions proven reachable by the versioned legacy activity/layout map.
 *
 * This is evidence metadata only. A dialog/layout proving that an interaction exists does not
 * prove its validation rules, mutations, pricing, acceptance criteria, persistence semantics,
 * or navigation order. Those remain blocked on method-level Java/SMALI evidence.
 */
enum class LegacyManagerInteractionEvidence(
    val legacyClassName: String,
    val observedLayoutName: String,
) {
    PLAYER_SEARCH_PROPOSAL("ActivityProcura", "dialog_proposta"),
    PLAYER_CONTRACT("DialogIgrokInfo", "dialog_contrato"),
    PLAYER_SALE("DialogIgrokInfo", "dialog_venda"),
    PLAYER_RETIREMENT("DialogIgrokInfo", "dialog_aposentar"),
    TEAM_PROPOSAL("ActivityTimes", "dialog_proposta"),
    CAREER_CLUB_OFFER("ActivityMainTeam", "dialog_oferta"),
}

object LegacyManagerInteractionEvidenceCatalog {
    val confirmed: Set<LegacyManagerInteractionEvidence> = linkedSetOf(
        LegacyManagerInteractionEvidence.PLAYER_SEARCH_PROPOSAL,
        LegacyManagerInteractionEvidence.PLAYER_CONTRACT,
        LegacyManagerInteractionEvidence.PLAYER_SALE,
        LegacyManagerInteractionEvidence.PLAYER_RETIREMENT,
        LegacyManagerInteractionEvidence.TEAM_PROPOSAL,
        LegacyManagerInteractionEvidence.CAREER_CLUB_OFFER,
    )

    private val byExactSource: Map<Pair<String, String>, LegacyManagerInteractionEvidence> =
        confirmed.associateBy { it.legacyClassName to it.observedLayoutName }

    fun fromExactSource(
        legacyClassName: String,
        observedLayoutName: String,
    ): LegacyManagerInteractionEvidence? = byExactSource[legacyClassName to observedLayoutName]

    fun isConfirmedExactSource(
        legacyClassName: String,
        observedLayoutName: String,
    ): Boolean = (legacyClassName to observedLayoutName) in byExactSource
}
