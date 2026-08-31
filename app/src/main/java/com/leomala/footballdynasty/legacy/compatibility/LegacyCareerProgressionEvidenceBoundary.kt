package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.manager.LegacyCoachProfileProjection

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

    fun isReachableCareerSurfaceAwaitingRecoveredHostBody(
        surface: LegacyManagerCareerSurface,
    ): Boolean = surface in reachableCareerSurfacesWithoutRecoveredHostBody

    fun isSemanticRuntimeBlocked(surface: LegacyCareerProgressionSurfaceEvidence): Boolean =
        surface in semanticRuntimeBlockedSurfaces
}
