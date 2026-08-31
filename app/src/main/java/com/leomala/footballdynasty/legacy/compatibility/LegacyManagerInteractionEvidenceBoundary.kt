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
    private data class ExpectedRecoveredHostMethod(
        val legacyClassName: String,
        val methodSignature: String,
        val smaliFileName: String,
        val instructionCount: Int,
        val branchCount: Int,
    )

    private val requiredRecoveredHostMethod: Map<LegacyManagerInteractionEvidence, ExpectedRecoveredHostMethod> =
        mapOf(
            LegacyManagerInteractionEvidence.PLAYER_SEARCH_PROPOSAL to
                ExpectedRecoveredHostMethod(
                    legacyClassName = "ActivityProcura",
                    methodSignature = "a(a.p,a.ac,int)",
                    smaliFileName = "ActivityProcura.smali",
                    instructionCount = 2,
                    branchCount = 0,
                ),
            LegacyManagerInteractionEvidence.PLAYER_CONTRACT to
                ExpectedRecoveredHostMethod(
                    legacyClassName = "DialogIgrokInfo",
                    methodSignature = "onCreate(Bundle)",
                    smaliFileName = "DialogIgrokInfo.smali",
                    instructionCount = 554,
                    branchCount = 28,
                ),
            LegacyManagerInteractionEvidence.PLAYER_SALE to
                ExpectedRecoveredHostMethod(
                    legacyClassName = "DialogIgrokInfo",
                    methodSignature = "onCreate(Bundle)",
                    smaliFileName = "DialogIgrokInfo.smali",
                    instructionCount = 554,
                    branchCount = 28,
                ),
            LegacyManagerInteractionEvidence.PLAYER_RETIREMENT to
                ExpectedRecoveredHostMethod(
                    legacyClassName = "DialogIgrokInfo",
                    methodSignature = "onCreate(Bundle)",
                    smaliFileName = "DialogIgrokInfo.smali",
                    instructionCount = 554,
                    branchCount = 28,
                ),
            LegacyManagerInteractionEvidence.TEAM_PROPOSAL to
                ExpectedRecoveredHostMethod(
                    legacyClassName = "ActivityTimes",
                    methodSignature = "a(a.p,a.ac,int)",
                    smaliFileName = "ActivityTimes.smali",
                    instructionCount = 2,
                    branchCount = 0,
                ),
            LegacyManagerInteractionEvidence.CAREER_CLUB_OFFER to
                ExpectedRecoveredHostMethod(
                    legacyClassName = "ActivityMainTeam",
                    methodSignature = "onStart()",
                    smaliFileName = "ActivityMainTeam.smali",
                    instructionCount = 97,
                    branchCount = 15,
                ),
        )

    val recoveredHostMethods: Map<LegacyManagerInteractionEvidence, LegacyRecoveredManagerMethod> =
        requiredRecoveredHostMethod.mapValues { (interaction, expected) ->
            val recovered = requireNotNull(
                LegacyManagerRecoveredMethodEvidence.findExact(
                    legacyClassName = expected.legacyClassName,
                    methodSignature = expected.methodSignature,
                ),
            ) {
                "Missing recovered SMALI host method for ${expected.legacyClassName}.${expected.methodSignature}"
            }
            require(
                recovered.smaliFileName == expected.smaliFileName &&
                    recovered.instructionCount == expected.instructionCount &&
                    recovered.branchCount == expected.branchCount,
            ) {
                "Recovered SMALI structure changed for $interaction: expected " +
                    "${expected.smaliFileName} ${expected.instructionCount}/${expected.branchCount}, got " +
                    "${recovered.smaliFileName} ${recovered.instructionCount}/${recovered.branchCount}"
            }
            recovered
        }

    /**
     * Interactions whose behavioral dispatch has progressed beyond reachability-only evidence.
     *
     * `PLAYER_SEARCH_PROPOSAL` is backed by the characterized `ActivityProcura.u(int)` action
     * dispatch and the executable purchase/loan composition in `LegacySearchTransferRuntimeRule`.
     * `PLAYER_CONTRACT` is backed by the characterized `DialogIgrokInfo.s/f/e/l` renewal path and
     * `LegacyContractRenewalRuntimeRule`. The unresolved internal storage behavior of
     * `p.c(days, false)` remains represented as an invocation, so this unlocks only the proven
     * renewal interaction and does not guess contract-end persistence semantics.
     *
     * No other dialog hosted by the same or another recovered Activity is unlocked here.
     */
    val semanticRuntimeCharacterizedInteractions: Set<LegacyManagerInteractionEvidence> =
        setOf(
            LegacyManagerInteractionEvidence.PLAYER_SEARCH_PROPOSAL,
            LegacyManagerInteractionEvidence.PLAYER_CONTRACT,
        )

    val semanticRuntimeBlockedInteractions: Set<LegacyManagerInteractionEvidence> =
        LegacyManagerInteractionEvidenceCatalog.confirmed - semanticRuntimeCharacterizedInteractions

    fun recoveredHostMethodFor(
        interaction: LegacyManagerInteractionEvidence,
    ): LegacyRecoveredManagerMethod? = recoveredHostMethods[interaction]

    fun isSemanticRuntimeCharacterized(interaction: LegacyManagerInteractionEvidence): Boolean =
        interaction in semanticRuntimeCharacterizedInteractions

    fun isSemanticRuntimeBlocked(interaction: LegacyManagerInteractionEvidence): Boolean =
        interaction in semanticRuntimeBlockedInteractions
}
