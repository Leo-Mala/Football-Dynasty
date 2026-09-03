package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.manager.LegacyCoachProfileProjection

enum class LegacyCharacterizedCareerRuntimePath {
    MANAGER_NAME_VALIDATION,
    CLUB_INVITATION_ACCEPTANCE_DISPATCH,
    CLUB_INVITATION_CANCEL_DISPATCH,
    CLUB_MANAGER_TRANSFER_G_L_E,
    REPLACEMENT_CANDIDATE_POOL_T,
    REPLACEMENT_UNEMPLOYED_FALLBACK_U,
    MANAGER_SWAP_B4,
    DISMISSAL_GATE_L,
    END_OF_YEAR_DISPATCH_M,
    CAREER_CONTINUATION_I,
    PENDING_MATCH_LAUNCH_H,
    POST_SEASON_RESULTS_J,
    INVITATION_DISPATCH_K,
    END_OF_YEAR_RESUME_N,
    CONTROLLED_CLUB_SWITCH_S,
}

/** Fail-closed boundary between proven coach behavior and remaining career progression semantics. */
object LegacyCareerProgressionEvidenceBoundary {
    val provenSerializedCoachFields: Set<String> = LegacyCoachProfileProjection.provenSourceFields.toSet()

    val recoveredCareerHostMethods: Map<LegacyManagerCareerSurface, LegacyRecoveredManagerMethod> = mapOf(
        LegacyManagerCareerSurface.CLUB_SELECTION to requireNotNull(
            LegacyManagerRecoveredMethodEvidence.findExact("ActivityEscolhaTimes", "i(String)"),
        ).also { recovered ->
            require(recovered.smaliFileName == "ActivityEscolhaTimes.smali" && recovered.smaliMethodSignature == "i(Ljava/lang/String;)Z" && recovered.instructionCount == 38 && recovered.branchCount == 9)
        },
        LegacyManagerCareerSurface.CAREER_CLUB_HUB to requireNotNull(
            LegacyManagerRecoveredMethodEvidence.findExact("ActivityMainTeam", "onStart()"),
        ).also { recovered ->
            require(recovered.smaliFileName == "ActivityMainTeam.smali" && recovered.smaliMethodSignature == "onStart()V" && recovered.instructionCount == 93 && recovered.branchCount == 15)
        },
    )

    val characterizedEmploymentMethods: Set<LegacyRecoveredManagerMethod> = linkedSetOf(
        requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.b", "G(best.c0,best.f0,best.f0)")),
        requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.f0", "l(best.f0)")),
        requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.f0", "e(best.c0)")),
    ).also { methods ->
        require(methods.map { it.instructionCount to it.branchCount } == listOf(5 to 2, 100 to 5, 38 to 3))
    }

    val characterizedReplacementSubmethods: Set<LegacyRecoveredManagerMethod> = linkedSetOf(
        requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.b", "t(best.c0,int)")),
        requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.b", "u()")),
        requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.b", "b4(best.f0,best.f0)")),
    ).also { methods ->
        require(methods.map { it.instructionCount to it.branchCount } == listOf(120 to 20, 30 to 4, 9 to 0))
    }

    val characterizedYearEndMethods: Set<LegacyRecoveredManagerMethod> = linkedSetOf(
        requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", "l()")),
        requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", "m()")),
    ).also { methods ->
        require(methods.map { it.instructionCount to it.branchCount } == listOf(81 to 16, 65 to 11))
    }

    val characterizedContinuationMethods: Set<LegacyRecoveredManagerMethod> = linkedSetOf(
        requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", "i()")),
        requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", "h()")),
        requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", "j()")),
        requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", "k()")),
        requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", "n()")),
        requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", "s(String)")),
    ).also { methods ->
        require(methods.map { it.instructionCount to it.branchCount } == listOf(78 to 16, 37 to 3, 27 to 3, 117 to 20, 9 to 2, 80 to 10))
    }

    /**
     * Structural fingerprint for the replacement resolver host. Its persistence-independent
     * orchestration and the `best.b.B -> best.x.H0/G0 -> konrent.t.H0` discovery chain are now
     * executable domain rules; deeper helper semantics remain separately fail-closed as needed.
     */
    val recoveredReplacementManagerResolver: LegacyRecoveredManagerMethod = requireNotNull(
        LegacyManagerRecoveredMethodEvidence.findExact("best.c0", "y()"),
    ).also { recovered ->
        require(recovered.smaliFileName == "best/c0.smali" && recovered.smaliMethodSignature == "y()Lbest/f0;" && recovered.instructionCount == 103 && recovered.branchCount == 22)
    }

    val characterizedCareerRuntimePaths: Set<LegacyCharacterizedCareerRuntimePath> = setOf(
        LegacyCharacterizedCareerRuntimePath.MANAGER_NAME_VALIDATION,
        LegacyCharacterizedCareerRuntimePath.CLUB_INVITATION_ACCEPTANCE_DISPATCH,
        LegacyCharacterizedCareerRuntimePath.CLUB_INVITATION_CANCEL_DISPATCH,
        LegacyCharacterizedCareerRuntimePath.CLUB_MANAGER_TRANSFER_G_L_E,
        LegacyCharacterizedCareerRuntimePath.REPLACEMENT_CANDIDATE_POOL_T,
        LegacyCharacterizedCareerRuntimePath.REPLACEMENT_UNEMPLOYED_FALLBACK_U,
        LegacyCharacterizedCareerRuntimePath.MANAGER_SWAP_B4,
        LegacyCharacterizedCareerRuntimePath.DISMISSAL_GATE_L,
        LegacyCharacterizedCareerRuntimePath.END_OF_YEAR_DISPATCH_M,
        LegacyCharacterizedCareerRuntimePath.CAREER_CONTINUATION_I,
        LegacyCharacterizedCareerRuntimePath.PENDING_MATCH_LAUNCH_H,
        LegacyCharacterizedCareerRuntimePath.POST_SEASON_RESULTS_J,
        LegacyCharacterizedCareerRuntimePath.INVITATION_DISPATCH_K,
        LegacyCharacterizedCareerRuntimePath.END_OF_YEAR_RESUME_N,
        LegacyCharacterizedCareerRuntimePath.CONTROLLED_CLUB_SWITCH_S,
    )

    val reachableCareerSurfacesWithoutRecoveredHostBody: Set<LegacyManagerCareerSurface> =
        LegacyManagerCareerSurfaces.confirmed - recoveredCareerHostMethods.keys

    val semanticRuntimeBlockedSurfaces: Set<LegacyCareerProgressionSurfaceEvidence> =
        LegacyCareerProgressionSurfaceEvidenceCatalog.confirmed.toSet()

    fun isProvenSerializedCoachField(fieldName: String): Boolean = fieldName in provenSerializedCoachFields
    fun recoveredCareerHostMethodFor(surface: LegacyManagerCareerSurface): LegacyRecoveredManagerMethod? = recoveredCareerHostMethods[surface]
    fun hasRecoveredCareerHostBody(surface: LegacyManagerCareerSurface): Boolean = surface in recoveredCareerHostMethods
    fun isCharacterizedCareerRuntimePath(path: LegacyCharacterizedCareerRuntimePath): Boolean = path in characterizedCareerRuntimePaths
    fun isReachableCareerSurfaceAwaitingRecoveredHostBody(surface: LegacyManagerCareerSurface): Boolean = surface in reachableCareerSurfacesWithoutRecoveredHostBody
    fun isSemanticRuntimeBlocked(surface: LegacyCareerProgressionSurfaceEvidence): Boolean = surface in semanticRuntimeBlockedSurfaces
}
