package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.manager.LegacyCoachProfileProjection

/** Narrow Phase 14 subpaths whose behavior is already characterized from the official corpus. */
enum class LegacyCharacterizedCareerRuntimePath {
    MANAGER_NAME_VALIDATION,
    CLUB_INVITATION_ACCEPTANCE_DISPATCH,
    CLUB_INVITATION_CANCEL_DISPATCH,
    CLUB_MANAGER_TRANSFER_G_L_E,
}

/**
 * Fail-closed boundary between proven coach identity and career behavior.
 */
object LegacyCareerProgressionEvidenceBoundary {
    val provenSerializedCoachFields: Set<String> =
        LegacyCoachProfileProjection.provenSourceFields.toSet()

    /**
     * Current official Phase 4R Activity method identities. The historical
     * `ActivityEscolhaTimes.E(String)` name is superseded by `ActivityEscolhaTimes.i(String)` in
     * `com.brasfoot.v2020`.
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
     * Employment mutation called by the invitation flow is now characterized independently from
     * replacement-manager selection. These exact methods are Java↔SMALI verified.
     */
    val characterizedEmploymentMethods: Set<LegacyRecoveredManagerMethod> = linkedSetOf(
        requireNotNull(
            LegacyManagerRecoveredMethodEvidence.findExact(
                "best.b",
                "G(best.c0,best.f0,best.f0)",
            ),
        ),
        requireNotNull(
            LegacyManagerRecoveredMethodEvidence.findExact("best.f0", "l(best.f0)"),
        ),
        requireNotNull(
            LegacyManagerRecoveredMethodEvidence.findExact("best.f0", "e(best.c0)"),
        ),
    ).also { methods ->
        require(methods.map { it.instructionCount to it.branchCount } == listOf(5 to 2, 100 to 5, 38 to 3)) {
            "Official manager employment method structure changed"
        }
    }

    /**
     * `best.c0.y()` is recovered and readable as the replacement-manager resolver, but it invokes
     * legacy random candidate selection/shuffle paths. It remains blocked until those draws are
     * migrated through the project's `RandomSource` without changing draw order or bounds.
     */
    val recoveredReplacementManagerResolver: LegacyRecoveredManagerMethod = requireNotNull(
        LegacyManagerRecoveredMethodEvidence.findExact("best.c0", "y()"),
    ).also { recovered ->
        require(
            recovered.smaliFileName == "best/c0.smali" &&
                recovered.smaliMethodSignature == "y()Lbest/f0;" &&
                recovered.instructionCount == 103 &&
                recovered.branchCount == 22,
        ) {
            "Official SMALI structure changed for best.c0.y()"
        }
    }

    /**
     * `ActivityEscolhaTimes.i(String)`, invitation dispatch and the direct employment transition
     * `best.b.G -> best.f0.l/e` are characterized. The whole invitation/career progression surface
     * is still fail-closed because `best.c0.y()` replacement selection and downstream
     * `best.n.l()/m()` progression contain unresolved RNG/continuation semantics.
     */
    val characterizedCareerRuntimePaths: Set<LegacyCharacterizedCareerRuntimePath> =
        setOf(
            LegacyCharacterizedCareerRuntimePath.MANAGER_NAME_VALIDATION,
            LegacyCharacterizedCareerRuntimePath.CLUB_INVITATION_ACCEPTANCE_DISPATCH,
            LegacyCharacterizedCareerRuntimePath.CLUB_INVITATION_CANCEL_DISPATCH,
            LegacyCharacterizedCareerRuntimePath.CLUB_MANAGER_TRANSFER_G_L_E,
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
