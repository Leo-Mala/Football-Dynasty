package com.leomala.footballdynasty.legacy.compatibility

/**
 * Exact recovered-method evidence required before the coach post-match lifecycle can leave the
 * fail-closed boundary.
 *
 * These signatures come from the reachable legacy caller chain. Instruction/branch counts are
 * intentionally not guessed: they must first be recovered from the official Java+SMALI corpus and
 * added to [LegacyManagerRecoveredMethodEvidence].
 */
data class LegacyRequiredCoachPostMatchMethod(
    val legacyClassName: String,
    val methodSignature: String,
    val smaliFileName: String,
    val smaliMethodSignature: String,
)

/**
 * Fail-closed promotion boundary for the reachable legacy coach post-match lifecycle.
 *
 * The official Brasfoot 2026/27 corpus proves the caller chain
 * `best.s.f() -> best.f0.i(best.s) / best.f0.j(best.s)`. The H-only projection of
 * `best.f0.i(best.s)` has already been characterized, but the complete field/effect ordering of
 * both manager methods has not yet been reconstructed. Production persistence must therefore stay
 * blocked until the complete lifecycle is characterized together.
 *
 * This object is deliberately evidence-only: it does not invent missing coach semantics and it
 * does not execute gameplay.
 */
object LegacyCoachPostMatchPromotionBoundary {
    const val callerMethod: String = "best.s.f()"
    const val homeManagerMethod: String = "best.f0.i(best.s)"
    const val pairedManagerMethod: String = "best.f0.j(best.s)"

    /** Exact competition `E()` values for which `best.s.f()` reaches the characterized H path. */
    val characterizedHCompetitionTypes: Set<Int> = linkedSetOf(1, 2, 3, 4, 5, 6, 8)

    /** The H-only projection is characterized and tested by `LegacyCoachRawHRule`. */
    const val hProjectionCharacterized: Boolean = true

    val requiredRecoveredManagerMethods: List<LegacyRequiredCoachPostMatchMethod> =
        listOf(
            LegacyRequiredCoachPostMatchMethod(
                legacyClassName = "best.f0",
                methodSignature = homeManagerMethod.substringAfter("best.f0."),
                smaliFileName = "best/f0.smali",
                smaliMethodSignature = "i(Lbest/s;)V",
            ),
            LegacyRequiredCoachPostMatchMethod(
                legacyClassName = "best.f0",
                methodSignature = pairedManagerMethod.substringAfter("best.f0."),
                smaliFileName = "best/f0.smali",
                smaliMethodSignature = "j(Lbest/s;)V",
            ),
        )

    /**
     * Structural Java↔SMALI recovery is a mandatory prerequisite. This remains false while i/j are
     * absent from the exact recovered-method catalog rather than being manually toggled to green.
     */
    val recoveredManagerMethodEvidenceComplete: Boolean
        get() =
            requiredRecoveredManagerMethods.all { required ->
                LegacyManagerRecoveredMethodEvidence.findExact(
                    legacyClassName = required.legacyClassName,
                    methodSignature = required.methodSignature,
                )?.let { recovered ->
                    recovered.smaliFileName == required.smaliFileName &&
                        recovered.smaliMethodSignature == required.smaliMethodSignature
                } == true
            }

    /**
     * Additional mutations/effects and their ordering still require semantic reconstruction after
     * the exact i/j method bodies have been recovered. Keep this separate from structural recovery
     * so neither prerequisite can silently stand in for the other.
     */
    const val semanticLifecycleCharacterized: Boolean = false

    val completeLifecycleCharacterized: Boolean
        get() = recoveredManagerMethodEvidenceComplete && semanticLifecycleCharacterized

    /**
     * Never allow an H-only production post-match write to masquerade as the complete legacy
     * manager lifecycle.
     */
    fun productionPersistenceAllowed(): Boolean =
        hProjectionCharacterized && completeLifecycleCharacterized
}
