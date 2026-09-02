package com.leomala.footballdynasty.legacy.compatibility

/**
 * Exact recovered-method evidence required before the coach post-match lifecycle can leave the
 * fail-closed boundary.
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
 * The official Brasfoot 2026/27 Java+SMALI corpus proves the normal non-`c0` caller order in
 * `best.s.f()`: `best.f0.j(best.s)` is invoked first and `best.f0.i(best.s)` is then invoked only
 * for the characterized competition predicate. Structural fingerprints for both manager methods
 * are now recovered. Production persistence remains blocked until their complete semantic state
 * mutation is represented and tested together.
 *
 * This object is deliberately evidence-only: it does not invent missing coach semantics and it
 * does not execute gameplay.
 */
object LegacyCoachPostMatchPromotionBoundary {
    const val callerMethod: String = "best.s.f()"
    const val postMatchAdjustmentMethod: String = "best.f0.i(best.s)"
    const val postMatchStatisticsMethod: String = "best.f0.j(best.s)"

    // Compatibility names retained while callers migrate to the semantically exact names above.
    const val homeManagerMethod: String = postMatchAdjustmentMethod
    const val pairedManagerMethod: String = postMatchStatisticsMethod

    /** Exact order inside the selected branch of `best.s.f()`: j(match) before i(match). */
    val characterizedCallerOrder: List<String> =
        listOf(postMatchStatisticsMethod, postMatchAdjustmentMethod)

    /** Exact competition `E()` values for which `best.s.f()` reaches `i(best.s)` after `j`. */
    val characterizedHCompetitionTypes: Set<Int> = linkedSetOf(1, 2, 3, 4, 5, 6, 8)

    /** The previously isolated H projection remains characterized by `LegacyCoachRawHRule`. */
    const val hProjectionCharacterized: Boolean = true

    val requiredRecoveredManagerMethods: List<LegacyRequiredCoachPostMatchMethod> =
        listOf(
            LegacyRequiredCoachPostMatchMethod(
                legacyClassName = "best.f0",
                methodSignature = postMatchAdjustmentMethod.substringAfter("best.f0."),
                smaliFileName = "best/f0.smali",
                smaliMethodSignature = "i(Lbest/s;)V",
            ),
            LegacyRequiredCoachPostMatchMethod(
                legacyClassName = "best.f0",
                methodSignature = postMatchStatisticsMethod.substringAfter("best.f0."),
                smaliFileName = "best/f0.smali",
                smaliMethodSignature = "j(Lbest/s;)V",
            ),
        )

    /** Structural Java↔SMALI recovery is a mandatory prerequisite and is catalog-driven. */
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
     * Semantic promotion is derived from exact method-level evidence rather than a hand-maintained
     * boolean. This remains false while either `i` or `j` lacks complete field/effect ordering.
     */
    val semanticLifecycleCharacterized: Boolean
        get() =
            LegacyCoachPostMatchSemanticEvidence.completeFor(
                characterizedCallerOrder,
            )

    val completeLifecycleCharacterized: Boolean
        get() = recoveredManagerMethodEvidenceComplete && semanticLifecycleCharacterized

    /** Never allow an H-only production write to masquerade as the complete legacy lifecycle. */
    fun productionPersistenceAllowed(): Boolean =
        hProjectionCharacterized && completeLifecycleCharacterized
}
