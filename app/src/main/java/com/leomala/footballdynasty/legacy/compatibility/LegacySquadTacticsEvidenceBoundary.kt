package com.leomala.footballdynasty.legacy.compatibility

/**
 * Fail-closed Phase 11 boundary for lineup/tactics methods whose SMALI bodies are recovered
 * but whose gameplay semantics have not yet been characterized Java↔SMALI.
 *
 * Recovery proves that a legacy method body exists and is reachable evidence. It does not prove
 * formation, eligibility, injury/suspension, automatic-lineup or tactical mutation rules. Those
 * semantics must remain blocked until a method-level read is backed by characterization tests.
 */
object LegacySquadTacticsEvidenceBoundary {
    enum class CharacterizationState {
        RECOVERED_BODY_ONLY,
        SEMANTICS_CHARACTERIZED,
    }

    /**
     * Phase 11 squad facts already supported by legacy model evidence and existing runtime
     * projections. These are deliberately narrower than lineup/tactics semantics.
     */
    enum class ProvenSquadPrimitive {
        SENIOR_ROSTER_MEMBERSHIP,
        SOURCE_ORDER_PRESERVATION,
        OPAQUE_POSITION_CODE,
        OPAQUE_STATUS_CODE,
    }

    val provenSquadPrimitives: Set<ProvenSquadPrimitive> = setOf(
        ProvenSquadPrimitive.SENIOR_ROSTER_MEMBERSHIP,
        ProvenSquadPrimitive.SOURCE_ORDER_PRESERVATION,
        ProvenSquadPrimitive.OPAQUE_POSITION_CODE,
        ProvenSquadPrimitive.OPAQUE_STATUS_CODE,
    )

    data class SemanticTarget(
        val legacyClassName: String,
        val methodSignature: String,
        val surfaceRole: String,
        val observedLayoutName: String?,
        val surfaceIsDynamicallyConstructed: Boolean,
        val smaliFileName: String,
        val instructionCount: Int,
        val branchCount: Int,
        val characterizationState: CharacterizationState,
    )

    /**
     * Exact Phase 11 targets jointly proven by `ACTIVITY_MAP.md` (surface exists) and
     * `SMALI_RECOVERY.md` (method body recovered). Layout metadata is copied from the versioned
     * activity inventory; a null layout is allowed only where that inventory explicitly records
     * dynamic construction. None of this surface evidence assigns gameplay meaning.
     *
     * No target may leave RECOVERED_BODY_ONLY until the actual Java↔SMALI call path has been read
     * semantically and its claimed behavior has characterization tests.
     */
    val requiredSemanticTargets: List<SemanticTarget> = listOf(
        SemanticTarget(
            legacyClassName = "ActivityEscala",
            methodSignature = "gL()",
            surfaceRole = "lineup",
            observedLayoutName = "activity_escala",
            surfaceIsDynamicallyConstructed = false,
            smaliFileName = "ActivityEscala.smali",
            instructionCount = 223,
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
            instructionCount = 172,
            branchCount = 20,
            characterizationState = CharacterizationState.RECOVERED_BODY_ONLY,
        ),
        SemanticTarget(
            legacyClassName = "ActivitySavedTatics",
            methodSignature = "sa()",
            surfaceRole = "saved-tactics",
            observedLayoutName = "activity_savedtatics",
            surfaceIsDynamicallyConstructed = false,
            smaliFileName = "ActivitySavedTatics.smali",
            instructionCount = 115,
            branchCount = 9,
            characterizationState = CharacterizationState.RECOVERED_BODY_ONLY,
        ),
    )

    private val phase11LegacyClasses: Set<String> =
        requiredSemanticTargets.mapTo(linkedSetOf()) { target -> target.legacyClassName }

    val recoveredMethodsAwaitingSemanticCharacterization: Set<LegacyRecoveredManagerMethod> =
        LegacyManagerRecoveredMethodEvidence.confirmed
            .filter { evidence -> evidence.legacyClassName in phase11LegacyClasses }
            .toSet()

    fun findTarget(
        legacyClassName: String,
        methodSignature: String,
    ): SemanticTarget? = requiredSemanticTargets.firstOrNull { target ->
        target.legacyClassName == legacyClassName && target.methodSignature == methodSignature
    }

    fun isRecoveredPhase11Method(
        legacyClassName: String,
        methodSignature: String,
    ): Boolean = LegacyManagerRecoveredMethodEvidence
        .findExact(legacyClassName, methodSignature)
        ?.let { evidence -> evidence in recoveredMethodsAwaitingSemanticCharacterization }
        ?: false

    fun recoveryMetadataMatchesInventory(target: SemanticTarget): Boolean =
        LegacyManagerRecoveredMethodEvidence.findExact(
            target.legacyClassName,
            target.methodSignature,
        )?.let { evidence ->
            evidence.smaliFileName == target.smaliFileName &&
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
