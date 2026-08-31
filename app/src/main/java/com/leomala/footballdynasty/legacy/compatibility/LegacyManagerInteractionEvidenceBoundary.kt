package com.leomala.footballdynasty.legacy.compatibility

/** Semantically characterized subpaths hosted by the legacy player-info dialog. */
enum class LegacyCharacterizedPlayerDialogRuntimePath {
    CONTRACT_RENEWAL,
    LOAN_MANAGEMENT,
    PLAYER_SALE,
}

/**
 * Fail-closed bridge between reachable manager interactions and current official Phase 4R host
 * methods. Historical Phase 1 method aliases are deliberately rejected.
 */
object LegacyManagerInteractionEvidenceBoundary {
    private data class ExpectedRecoveredHostMethod(
        val legacyClassName: String,
        val methodSignature: String,
        val smaliFileName: String,
        val smaliMethodSignature: String,
        val instructionCount: Int,
        val branchCount: Int,
    )

    private val requiredRecoveredHostMethod: Map<LegacyManagerInteractionEvidence, ExpectedRecoveredHostMethod> =
        mapOf(
            LegacyManagerInteractionEvidence.PLAYER_SEARCH_PROPOSAL to
                ExpectedRecoveredHostMethod(
                    legacyClassName = "ActivityProcura",
                    methodSignature = "t(best.o,best.c0,int)",
                    smaliFileName = "ActivityProcura.smali",
                    smaliMethodSignature = "t(Lbest/o;Lbest/c0;I)I",
                    instructionCount = 136,
                    branchCount = 14,
                ),
            LegacyManagerInteractionEvidence.PLAYER_CONTRACT to
                ExpectedRecoveredHostMethod(
                    legacyClassName = "DialogIgrokInfo",
                    methodSignature = "onCreate(Bundle)",
                    smaliFileName = "DialogIgrokInfo.smali",
                    smaliMethodSignature = "onCreate(Landroid/os/Bundle;)V",
                    instructionCount = 530,
                    branchCount = 28,
                ),
            LegacyManagerInteractionEvidence.PLAYER_SALE to
                ExpectedRecoveredHostMethod(
                    legacyClassName = "DialogIgrokInfo",
                    methodSignature = "onCreate(Bundle)",
                    smaliFileName = "DialogIgrokInfo.smali",
                    smaliMethodSignature = "onCreate(Landroid/os/Bundle;)V",
                    instructionCount = 530,
                    branchCount = 28,
                ),
            LegacyManagerInteractionEvidence.PLAYER_RETIREMENT to
                ExpectedRecoveredHostMethod(
                    legacyClassName = "DialogIgrokInfo",
                    methodSignature = "onCreate(Bundle)",
                    smaliFileName = "DialogIgrokInfo.smali",
                    smaliMethodSignature = "onCreate(Landroid/os/Bundle;)V",
                    instructionCount = 530,
                    branchCount = 28,
                ),
            LegacyManagerInteractionEvidence.TEAM_PROPOSAL to
                ExpectedRecoveredHostMethod(
                    legacyClassName = "ActivityTimes",
                    methodSignature = "s(best.o,best.c0,int)",
                    smaliFileName = "ActivityTimes.smali",
                    smaliMethodSignature = "s(Lbest/o;Lbest/c0;I)I",
                    instructionCount = 133,
                    branchCount = 14,
                ),
            LegacyManagerInteractionEvidence.CAREER_CLUB_OFFER to
                ExpectedRecoveredHostMethod(
                    legacyClassName = "ActivityMainTeam",
                    methodSignature = "onStart()",
                    smaliFileName = "ActivityMainTeam.smali",
                    smaliMethodSignature = "onStart()V",
                    instructionCount = 93,
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
                "Missing official SMALI host method for ${expected.legacyClassName}.${expected.methodSignature}"
            }
            require(
                recovered.smaliFileName == expected.smaliFileName &&
                    recovered.smaliMethodSignature == expected.smaliMethodSignature &&
                    recovered.instructionCount == expected.instructionCount &&
                    recovered.branchCount == expected.branchCount,
            ) {
                "Official SMALI structure changed for $interaction"
            }
            recovered
        }

    val semanticRuntimeCharacterizedInteractions: Set<LegacyManagerInteractionEvidence> =
        setOf(
            LegacyManagerInteractionEvidence.PLAYER_SEARCH_PROPOSAL,
            LegacyManagerInteractionEvidence.PLAYER_CONTRACT,
            LegacyManagerInteractionEvidence.PLAYER_SALE,
        )

    val characterizedPlayerDialogRuntimePaths: Set<LegacyCharacterizedPlayerDialogRuntimePath> =
        setOf(
            LegacyCharacterizedPlayerDialogRuntimePath.CONTRACT_RENEWAL,
            LegacyCharacterizedPlayerDialogRuntimePath.LOAN_MANAGEMENT,
            LegacyCharacterizedPlayerDialogRuntimePath.PLAYER_SALE,
        )

    val characterizedPlayerSaleMethods: List<LegacyRecoveredPlayerSaleMethod> =
        LegacyPlayerSaleRecoveredMethodEvidence.confirmed.also { methods ->
            require(methods.size == 11)
            require(
                methods.map { it.instructionCount to it.branchCount } ==
                    listOf(
                        72 to 7,
                        29 to 0,
                        18 to 0,
                        45 to 7,
                        17 to 0,
                        9 to 1,
                        621 to 86,
                        84 to 20,
                        321 to 42,
                        83 to 9,
                        77 to 16,
                    ),
            )
        }

    val semanticRuntimeBlockedInteractions: Set<LegacyManagerInteractionEvidence> =
        LegacyManagerInteractionEvidenceCatalog.confirmed - semanticRuntimeCharacterizedInteractions

    fun recoveredHostMethodFor(
        interaction: LegacyManagerInteractionEvidence,
    ): LegacyRecoveredManagerMethod? = recoveredHostMethods[interaction]

    fun isSemanticRuntimeCharacterized(interaction: LegacyManagerInteractionEvidence): Boolean =
        interaction in semanticRuntimeCharacterizedInteractions

    fun isCharacterizedPlayerDialogRuntimePath(
        path: LegacyCharacterizedPlayerDialogRuntimePath,
    ): Boolean = path in characterizedPlayerDialogRuntimePaths

    fun isSemanticRuntimeBlocked(interaction: LegacyManagerInteractionEvidence): Boolean =
        interaction in semanticRuntimeBlockedInteractions
}
