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
    private val phase11LegacyClasses: Set<String> = setOf(
        "DialogTatics",
        "ActivityEscala",
        "ActivitySavedTatics",
    )

    val recoveredMethodsAwaitingSemanticCharacterization: Set<LegacyRecoveredManagerMethod> =
        LegacyManagerRecoveredMethodEvidence.confirmed
            .filter { evidence -> evidence.legacyClassName in phase11LegacyClasses }
            .toSet()

    fun isRecoveredPhase11Method(
        legacyClassName: String,
        methodSignature: String,
    ): Boolean = LegacyManagerRecoveredMethodEvidence
        .findExact(legacyClassName, methodSignature)
        ?.let { evidence -> evidence in recoveredMethodsAwaitingSemanticCharacterization }
        ?: false

    fun isSemanticRuntimeBlocked(
        legacyClassName: String,
        methodSignature: String,
    ): Boolean = isRecoveredPhase11Method(legacyClassName, methodSignature)
}
