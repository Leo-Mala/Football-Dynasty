package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.manager.LegacyCoachProfileProjection

/** Narrow Phase 14 subpaths whose behavior is already characterized from the official corpus. */
enum class LegacyCharacterizedCareerRuntimePath {
    MANAGER_NAME_VALIDATION,
    CLUB_INVITATION_ACCEPTANCE_DISPATCH,
    CLUB_INVITATION_CANCEL_DISPATCH,
}

/**
 * Fail-closed boundary between proven coach identity and career behavior.
 */
object LegacyCareerProgressionEvidenceBoundary {
    val provenSerializedCoachFields: Set<String> =
        LegacyCoachProfileProjection.provenSourceFields.toSet()

    /**
     * Current official Phase 4R method identities. The historical `ActivityEscolhaTimes.E(String)`
     * name is superseded by `ActivityEscolhaTimes.i(String)` in `com.brasfoot.v2020`.
     */
    val recoveredCareerHostMethods: Map<LegacyManagerCareerSurface, LegacyRecoveredManagerMethod> =
        mapOf(
            LegacyManagerCareerSurface.CLUB_SELECTION to requireNotNull(
                LegacyManagerRecoveredMethodEvidence.findExact(
                    legacyClassName = "ActivityEscolhaTimes",
                    methodSignature = "i(String)",
                ),
            ) {
                "Missing official SMALI method for ActivityEscolhaTimes.i(String)"
            }.also { recovered ->
                require(
                    recovered.smaliFileName == "ActivityEscolhaTimes.smali" &&
                        recovered.smaliMethodSignature == "i(Ljava/lang/String;)Z" &&
                        recovered.instructionCount == 38 &&
                        recovered.branchCount == 9,
                ) {
                    "Official SMALI structure changed for ActivityEscolhaTimes.i(String)"
                }
            },
            LegacyManagerCareerSurface.CAREER_CLUB_HUB to requireNotNull(
                LegacyManagerRecoveredMethodEvidence.findExact(
                    legacyClassName = "ActivityMainTeam",
                    methodSignature = "onStart()",
                ),
            ) {
                "Missing official SMALI host method for ActivityMainTeam.onStart()"
            }.also { recovered ->
                require(
                    recovered.smaliFileName == "ActivityMainTeam.smali" &&
                        recovered.smaliMethodSignature == "onStart()V" &&
                        recovered.instructionCount == 93 &&
                        recovered.branchCount == 15,
                ) {
                    "Official SMALI structure changed for ActivityMainTeam.onStart()"
                }
            },
        )

    /**
     * `ActivityEscolhaTimes.i(String)` is characterized as manager-name validation.
     * `ActivityConvite.onClickAccept/onClickCancel` are characterized only through their exact
     * dispatch/ordering. The internals of `c0.y()`, `best.b.G(...)` and continuation `best.n.l()`
     * remain separate evidence targets, so the full invitation surface remains fail-closed.
     */
    val characterizedCareerRuntimePaths: Set<LegacyCharacterizedCareerRuntimePath> =
        setOf(
            LegacyCharacterizedCareerRuntimePath.MANAGER_NAME_VALIDATION,
            LegacyCharacterizedCareerRuntimePath.CLUB_INVITATION_ACCEPTANCE_DISPATCH,
            LegacyCharacterizedCareerRuntimePath.CLUB_INVITATION_CANCEL_DISPATCH,
        )

    val reachableCareerSurfacesWithoutRecoveredHostBody: Set<LegacyManagerCareerSurface> =
        LegacyManagerCareerSurfaces.confirmed - recoveredCareerHostMethods.keys

    val semanticRuntimeBlockedSurfaces: Set<LegacyCareerProgressionSurfaceEvidence> =
        LegacyCareerProgressionSurfaceEvidenceCatalog.confirmed.toSet()

    fun isProvenSerializedCoachField(fieldName: String): Boolean =
        fieldName in provenSerializedCoachFields

    fun recoveredCareerHostMethodFor(
        surface: LegacyManagerCareerSurface,
    ): LegacyRecoveredManagerMethod? = recoveredCareerHostMethods[surface]

    fun hasRecoveredCareerHostBody(surface: LegacyManagerCareerSurface): Boolean =
        surface in recoveredCareerHostMethods

    fun isCharacterizedCareerRuntimePath(path: LegacyCharacterizedCareerRuntimePath): Boolean =
        path in characterizedCareerRuntimePaths

    fun isReachableCareerSurfaceAwaitingRecoveredHostBody(
        surface: LegacyManagerCareerSurface,
    ): Boolean = surface in reachableCareerSurfacesWithoutRecoveredHostBody

    fun isSemanticRuntimeBlocked(surface: LegacyCareerProgressionSurfaceEvidence): Boolean =
        surface in semanticRuntimeBlockedSurfaces
}
