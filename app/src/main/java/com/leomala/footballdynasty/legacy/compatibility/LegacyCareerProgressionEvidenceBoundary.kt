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

    val semanticRuntimeBlockedSurfaces: Set<LegacyCareerProgressionSurfaceEvidence> =
        LegacyCareerProgressionSurfaceEvidenceCatalog.confirmed.toSet()

    fun isProvenSerializedCoachField(fieldName: String): Boolean =
        fieldName in provenSerializedCoachFields

    fun isSemanticRuntimeBlocked(surface: LegacyCareerProgressionSurfaceEvidence): Boolean =
        surface in semanticRuntimeBlockedSurfaces
}
