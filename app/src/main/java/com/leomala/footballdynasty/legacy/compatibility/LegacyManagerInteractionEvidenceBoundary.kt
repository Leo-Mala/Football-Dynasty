package com.leomala.footballdynasty.legacy.compatibility

/**
 * Fail-closed bridge between manager interactions proven reachable in the legacy UI and
 * recovered SMALI host methods.
 *
 * A matching recovered method proves that executable legacy code exists behind the host
 * Activity, but does not by itself prove proposal validation, retirement, sale, contract,
 * invitation, or club-switch semantics. Runtime migration remains blocked until the relevant
 * control flow is semantically characterized.
 */
object LegacyManagerInteractionEvidenceBoundary {
    private val requiredRecoveredHostMethod: Map<LegacyManagerInteractionEvidence, Pair<String, String>> =
        mapOf(
            LegacyManagerInteractionEvidence.PLAYER_SEARCH_PROPOSAL to
                ("ActivityProcura" to "a(a.p,a.ac,int)"),
            LegacyManagerInteractionEvidence.PLAYER_CONTRACT to
                ("DialogIgrokInfo" to "onCreate(Bundle)"),
            LegacyManagerInteractionEvidence.PLAYER_SALE to
                ("DialogIgrokInfo" to "onCreate(Bundle)"),
            LegacyManagerInteractionEvidence.PLAYER_RETIREMENT to
                ("DialogIgrokInfo" to "onCreate(Bundle)"),
            LegacyManagerInteractionEvidence.TEAM_PROPOSAL to
                ("ActivityTimes" to "a(a.p,a.ac,int)"),
            LegacyManagerInteractionEvidence.CAREER_CLUB_OFFER to
                ("ActivityMainTeam" to "onStart()"),
        )

    val recoveredHostMethods: Map<LegacyManagerInteractionEvidence, LegacyRecoveredManagerMethod> =
        requiredRecoveredHostMethod.mapValues { (_, exactMethod) ->
            requireNotNull(
                LegacyManagerRecoveredMethodEvidence.findExact(
                    legacyClassName = exactMethod.first,
                    methodSignature = exactMethod.second,
                ),
            ) {
                "Missing recovered SMALI host method for ${exactMethod.first}.${exactMethod.second}"
            }
        }

    val semanticRuntimeBlockedInteractions: Set<LegacyManagerInteractionEvidence> =
        LegacyManagerInteractionEvidenceCatalog.confirmed.toSet()

    fun recoveredHostMethodFor(
        interaction: LegacyManagerInteractionEvidence,
    ): LegacyRecoveredManagerMethod? = recoveredHostMethods[interaction]

    fun isSemanticRuntimeBlocked(interaction: LegacyManagerInteractionEvidence): Boolean =
        interaction in semanticRuntimeBlockedInteractions
}
