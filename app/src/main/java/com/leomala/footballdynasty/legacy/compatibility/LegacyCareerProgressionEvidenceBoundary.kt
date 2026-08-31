package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.manager.LegacyCoachProfileProjection

/**
 * Fail-closed boundary between evidence that is already serialized/proven and career behavior
 * that is only known to be reachable in the legacy UI.
 *
 * A proven coach identity field does not unlock dismissal, invitation, reputation, ranking,
 * club-switch or persistence semantics. Those remain blocked until method-level Java/SMALI
 * evidence is characterized.
 */
object LegacyCareerProgressionEvidenceBoundary {
    val provenSerializedCoachFields: Set<String> =
        LegacyCoachProfileProjection.provenSourceFields.toSet()

    /**
     * Phase 14 currently has two recovered method bodies attached to career-facing hosts:
     * `ActivityEscolhaTimes.E(String)` and `ActivityMainTeam.onStart()`. The recovery inventory
     * proves only that the bodies exist and retains their exact structural fingerprints; it does
     * not prove club-selection mutation, offer, dismissal, invitation, reputation or club-switch
     * semantics.
     *
     * Keeping this mapping here makes the remaining evidence gap explicit: the other career
     * surfaces are proven reachable by the activity map, but do not yet have method-level bodies
     * promoted into the versioned manager recovery catalog.
     */
    val recoveredCareerHostMethods: Map<LegacyManagerCareerSurface, LegacyRecoveredManagerMethod> =
        mapOf(
            LegacyManagerCareerSurface.CLUB_SELECTION to requireNotNull(
                LegacyManagerRecoveredMethodEvidence.findExact(
                    legacyClassName = "ActivityEscolhaTimes",
                    methodSignature = "E(String)",
                ),
            ) {
                "Missing recovered SMALI method for ActivityEscolhaTimes.E(String)"
            }.also { recovered ->
                require(
                    recovered.smaliFileName == "ActivityEscolhaTimes.smali" &&
                        recovered.instructionCount == 38 &&
                        recovered.branchCount == 9,
                ) {
                    "Recovered SMALI structure changed for ActivityEscolhaTimes.E(String): expected " +
                        "ActivityEscolhaTimes.smali 38/9, got ${recovered.smaliFileName} " +
                        "${recovered.instructionCount}/${recovered.branchCount}"
                }
            },
            LegacyManagerCareerSurface.CAREER_CLUB_HUB to requireNotNull(
                LegacyManagerRecoveredMethodEvidence.findExact(
                    legacyClassName = "ActivityMainTeam",
                    methodSignature = "onStart()",
                ),
            ) {
                "Missing recovered SMALI host method for ActivityMainTeam.onStart()"
            }.also { recovered ->
                require(
                    recovered.smaliFileName == "ActivityMainTeam.smali" &&
                        recovered.instructionCount == 97 &&
                        recovered.branchCount == 15,
                ) {
                    "Recovered SMALI structure changed for ActivityMainTeam.onStart(): expected " +
                        "ActivityMainTeam.smali 97/15, got ${recovered.smaliFileName} " +
                        "${recovered.instructionCount}/${recovered.branchCount}"
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
