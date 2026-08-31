package com.leomala.footballdynasty.legacy.compatibility

/** Characterized readable Phase 11 lineup subpaths from the current official corpus. */
enum class LegacyCharacterizedLineupRuntimePath {
    BENCH_REORDER_U,
    STARTER_BENCH_SWAP_V,
    STARTER_REORDER_W,
}

/** Characterized Phase 11 squad/tactics subpaths from the current official corpus. */
enum class LegacyCharacterizedTacticsRuntimePath {
    PLAYER_SUBROLE_DERIVATION_R1,
    ACTION_CANDIDATE_SELECTION_E,
    SAVED_TACTIC_CREATE_G,
    SAVED_TACTIC_LIST_REFRESH_E,
    SAVED_TACTIC_DELETE_B,
    SAVED_TACTIC_LOAD_F,
    SAVED_TACTIC_RESULT_CONSUME,
}

/**
 * Fail-closed Phase 11 boundary for lineup/tactics methods whose current official SMALI bodies are
 * recovered but whose gameplay semantics have not yet been fully characterized Java↔SMALI.
 */
object LegacySquadTacticsEvidenceBoundary {
    enum class CharacterizationState {
        RECOVERED_BODY_ONLY,
        SEMANTICS_CHARACTERIZED,
    }

    enum class ProvenSquadPrimitive {
        SENIOR_ROSTER_MEMBERSHIP,
        SOURCE_ORDER_PRESERVATION,
        OPAQUE_POSITION_CODE,
        OPAQUE_STATUS_CODE,
        OPAQUE_SIDE_CODE,
        OPAQUE_TRAIT_FIELDS,
    }

    val provenSquadPrimitives: Set<ProvenSquadPrimitive> = setOf(
        ProvenSquadPrimitive.SENIOR_ROSTER_MEMBERSHIP,
        ProvenSquadPrimitive.SOURCE_ORDER_PRESERVATION,
        ProvenSquadPrimitive.OPAQUE_POSITION_CODE,
        ProvenSquadPrimitive.OPAQUE_STATUS_CODE,
        ProvenSquadPrimitive.OPAQUE_SIDE_CODE,
        ProvenSquadPrimitive.OPAQUE_TRAIT_FIELDS,
    )

    val characterizedLineupRuntimePaths: Set<LegacyCharacterizedLineupRuntimePath> = setOf(
        LegacyCharacterizedLineupRuntimePath.BENCH_REORDER_U,
        LegacyCharacterizedLineupRuntimePath.STARTER_BENCH_SWAP_V,
        LegacyCharacterizedLineupRuntimePath.STARTER_REORDER_W,
    )

    val characterizedTacticsRuntimePaths: Set<LegacyCharacterizedTacticsRuntimePath> = setOf(
        LegacyCharacterizedTacticsRuntimePath.PLAYER_SUBROLE_DERIVATION_R1,
        LegacyCharacterizedTacticsRuntimePath.ACTION_CANDIDATE_SELECTION_E,
        LegacyCharacterizedTacticsRuntimePath.SAVED_TACTIC_CREATE_G,
        LegacyCharacterizedTacticsRuntimePath.SAVED_TACTIC_LIST_REFRESH_E,
        LegacyCharacterizedTacticsRuntimePath.SAVED_TACTIC_DELETE_B,
        LegacyCharacterizedTacticsRuntimePath.SAVED_TACTIC_LOAD_F,
        LegacyCharacterizedTacticsRuntimePath.SAVED_TACTIC_RESULT_CONSUME,
    )

    data class SemanticTarget(
        val legacyClassName: String,
        val methodSignature: String,
        val surfaceRole: String,
        val observedLayoutName: String?,
        val surfaceIsDynamicallyConstructed: Boolean,
        val smaliFileName: String,
        val smaliMethodSignature: String,
        val instructionCount: Int,
        val branchCount: Int,
        val characterizationState: CharacterizationState,
    )

    val requiredSemanticTargets: List<SemanticTarget> = listOf(
        SemanticTarget(
            legacyClassName = "ActivityEscalacao",
            methodSignature = "B()",
            surfaceRole = "lineup",
            observedLayoutName = "activity_escala",
            surfaceIsDynamicallyConstructed = false,
            smaliFileName = "ActivityEscalacao.smali",
            smaliMethodSignature = "y()V",
            instructionCount = 212,
            branchCount = 22,
            characterizationState = CharacterizationState.RECOVERED_BODY_ONLY,
        ),
        SemanticTarget(
            legacyClassName = "DialogTatics",
            methodSignature = "onCreate(Bundle)",
            surfaceRole = "tactics",
            observedLayoutName = null,
            surfaceIsDynamicallyConstructed = true,
            smaliFileName = "DialogTatics.smali",
            smaliMethodSignature = "onCreate(Landroid/os/Bundle;)V",
            instructionCount = 171,
            branchCount = 19,
            characterizationState = CharacterizationState.RECOVERED_BODY_ONLY,
        ),
        SemanticTarget(
            legacyClassName = "ActivitySavedTatics",
            methodSignature = "g()",
            surfaceRole = "saved-tactics",
            observedLayoutName = "activity_savedtatics",
            surfaceIsDynamicallyConstructed = false,
            smaliFileName = "ActivitySavedTatics.smali",
            smaliMethodSignature = "g()V",
            instructionCount = 103,
            branchCount = 8,
            characterizationState = CharacterizationState.SEMANTICS_CHARACTERIZED,
        ),
    )

    private val phase11LegacyClasses: Set<String> =
        requiredSemanticTargets.mapTo(linkedSetOf()) { target -> target.legacyClassName }

    val recoveredMethodsAwaitingSemanticCharacterization: Set<LegacyRecoveredManagerMethod> =
        LegacyManagerRecoveredMethodEvidence.confirmed
            .filter { evidence ->
                evidence.legacyClassName in phase11LegacyClasses &&
                    requiredSemanticTargets.any { target ->
                        target.legacyClassName == evidence.legacyClassName &&
                            target.methodSignature == evidence.methodSignature &&
                            target.characterizationState != CharacterizationState.SEMANTICS_CHARACTERIZED
                    }
            }
            .toSet()

    fun findTarget(
        legacyClassName: String,
        methodSignature: String,
    ): SemanticTarget? = requiredSemanticTargets.firstOrNull { target ->
        target.legacyClassName == legacyClassName && target.methodSignature == methodSignature
    }

    fun isCharacterizedLineupRuntimePath(path: LegacyCharacterizedLineupRuntimePath): Boolean =
        path in characterizedLineupRuntimePaths

    fun isCharacterizedTacticsRuntimePath(path: LegacyCharacterizedTacticsRuntimePath): Boolean =
        path in characterizedTacticsRuntimePaths

    fun isRecoveredPhase11Method(
        legacyClassName: String,
        methodSignature: String,
    ): Boolean = LegacyManagerRecoveredMethodEvidence
        .findExact(legacyClassName, methodSignature)
        ?.let { evidence ->
            requiredSemanticTargets.any { target ->
                target.legacyClassName == evidence.legacyClassName &&
                    target.methodSignature == evidence.methodSignature
            }
        }
        ?: false

    fun recoveryMetadataMatchesInventory(target: SemanticTarget): Boolean =
        LegacyManagerRecoveredMethodEvidence.findExact(
            target.legacyClassName,
            target.methodSignature,
        )?.let { evidence ->
            evidence.smaliFileName == target.smaliFileName &&
                evidence.smaliMethodSignature == target.smaliMethodSignature &&
                evidence.instructionCount == target.instructionCount &&
                evidence.branchCount == target.branchCount
        } ?: false

    fun surfaceEvidenceIsInternallyConsistent(target: SemanticTarget): Boolean =
        when {
            target.surfaceIsDynamicallyConstructed -> target.observedLayoutName == null
            else -> !target.observedLayoutName.isNullOrBlank()
        }

    fun isSemanticRuntimeBlocked(
        legacyClassName: String,
        methodSignature: String,
    ): Boolean = findTarget(legacyClassName, methodSignature)?.let { target ->
        isRecoveredPhase11Method(legacyClassName, methodSignature) &&
            target.characterizationState != CharacterizationState.SEMANTICS_CHARACTERIZED
    } ?: false

    fun allRequiredTargetsHaveRecoveredBodies(): Boolean = requiredSemanticTargets.all { target ->
        isRecoveredPhase11Method(target.legacyClassName, target.methodSignature) &&
            recoveryMetadataMatchesInventory(target)
    }

    fun allRequiredTargetsHaveConsistentSurfaceEvidence(): Boolean =
        requiredSemanticTargets.all(::surfaceEvidenceIsInternallyConsistent)

    fun allRequiredTargetsAreSemanticallyCharacterized(): Boolean = requiredSemanticTargets.all { target ->
        target.characterizationState == CharacterizationState.SEMANTICS_CHARACTERIZED
    }
}
