package com.leomala.footballdynasty.legacy.compatibility

enum class LegacyCharacterizedLineupRuntimePath {
    BENCH_REORDER_U,
    STARTER_BENCH_SWAP_V,
    STARTER_REORDER_W,
    ELIGIBILITY_CLASSIFICATION_V_K0,
    AUTOMATIC_FORMATION_I,
    SAVED_FORMATION_APPLY_J,
    BENCH_BUILD_K_L,
    LINEUP_SNAPSHOT_Q,
    START_VALIDATION_X,
    FINAL_COMMIT_B_Y,
}

enum class LegacyCharacterizedTacticsRuntimePath {
    PLAYER_SUBROLE_DERIVATION_R1,
    ACTION_CANDIDATE_SELECTION_E,
    SAVED_TACTIC_CREATE_G,
    SAVED_TACTIC_LIST_REFRESH_E,
    SAVED_TACTIC_DELETE_B,
    SAVED_TACTIC_LOAD_F,
    SAVED_TACTIC_RESULT_CONSUME,
    DIALOG_STATE_LOAD_ON_CREATE,
    DIALOG_STATE_COMMIT_J,
    SPECIAL_PLAYER_ASSIGNMENT_K,
    SPECIAL_PLAYER_REFERENCE_CLEANUP,
    PLAYER_PICKER_V2,
    MATCH_ENGINE_OPTION_SLOT_2,
    HALFTIME_DIALOG_SHARED_CLUB_STATE,
}

object LegacySquadTacticsEvidenceBoundary {
    enum class CharacterizationState { RECOVERED_BODY_ONLY, SEMANTICS_CHARACTERIZED }
    enum class ProvenSquadPrimitive { SENIOR_ROSTER_MEMBERSHIP, SOURCE_ORDER_PRESERVATION, OPAQUE_POSITION_CODE, OPAQUE_STATUS_CODE, OPAQUE_SIDE_CODE, OPAQUE_TRAIT_FIELDS }
    val provenSquadPrimitives = ProvenSquadPrimitive.entries.toSet()
    val characterizedLineupRuntimePaths = LegacyCharacterizedLineupRuntimePath.entries.toSet()
    val characterizedTacticsRuntimePaths = LegacyCharacterizedTacticsRuntimePath.entries.toSet()

    const val matchEngineTacticOptionSlot: Int = 2
    const val matchEngineMethodSignature: String = "best.s.k(best.s,int,int)"
    const val halftimeTacticsHostMethod: String = "ActivityIntervalo.k()"

    data class SemanticTarget(
        val legacyClassName:String,val methodSignature:String,val surfaceRole:String,val observedLayoutName:String?,val surfaceIsDynamicallyConstructed:Boolean,
        val smaliFileName:String,val smaliMethodSignature:String,val instructionCount:Int,val branchCount:Int,val characterizationState:CharacterizationState,
    )

    val requiredSemanticTargets = listOf(
        SemanticTarget("ActivityEscalacao","B()","lineup","activity_escala",false,"ActivityEscalacao.smali","y()V",212,22,CharacterizationState.SEMANTICS_CHARACTERIZED),
        SemanticTarget("DialogTatics","onCreate(Bundle)","tactics",null,true,"DialogTatics.smali","onCreate(Landroid/os/Bundle;)V",171,19,CharacterizationState.SEMANTICS_CHARACTERIZED),
        SemanticTarget("ActivitySavedTatics","g()","saved-tactics","activity_savedtatics",false,"ActivitySavedTatics.smali","g()V",103,8,CharacterizationState.SEMANTICS_CHARACTERIZED),
    )
    private val phase11LegacyClasses = requiredSemanticTargets.mapTo(linkedSetOf()){it.legacyClassName}
    val recoveredMethodsAwaitingSemanticCharacterization: Set<LegacyRecoveredManagerMethod> = LegacyManagerRecoveredMethodEvidence.confirmed.filter { e -> e.legacyClassName in phase11LegacyClasses && requiredSemanticTargets.any { t -> t.legacyClassName==e.legacyClassName && t.methodSignature==e.methodSignature && t.characterizationState!=CharacterizationState.SEMANTICS_CHARACTERIZED } }.toSet()
    fun findTarget(legacyClassName:String,methodSignature:String)=requiredSemanticTargets.firstOrNull{it.legacyClassName==legacyClassName&&it.methodSignature==methodSignature}
    fun isCharacterizedLineupRuntimePath(path:LegacyCharacterizedLineupRuntimePath)=path in characterizedLineupRuntimePaths
    fun isCharacterizedTacticsRuntimePath(path:LegacyCharacterizedTacticsRuntimePath)=path in characterizedTacticsRuntimePaths
    fun isRecoveredPhase11Method(legacyClassName:String,methodSignature:String)=LegacyManagerRecoveredMethodEvidence.findExact(legacyClassName,methodSignature)?.let{e->requiredSemanticTargets.any{it.legacyClassName==e.legacyClassName&&it.methodSignature==e.methodSignature}}?:false
    fun recoveryMetadataMatchesInventory(target:SemanticTarget)=LegacyManagerRecoveredMethodEvidence.findExact(target.legacyClassName,target.methodSignature)?.let{e->e.smaliFileName==target.smaliFileName&&e.smaliMethodSignature==target.smaliMethodSignature&&e.instructionCount==target.instructionCount&&e.branchCount==target.branchCount}?:false
    fun surfaceEvidenceIsInternallyConsistent(target:SemanticTarget)=if(target.surfaceIsDynamicallyConstructed) target.observedLayoutName==null else !target.observedLayoutName.isNullOrBlank()
    fun isSemanticRuntimeBlocked(legacyClassName:String,methodSignature:String)=findTarget(legacyClassName,methodSignature)?.let{isRecoveredPhase11Method(legacyClassName,methodSignature)&&it.characterizationState!=CharacterizationState.SEMANTICS_CHARACTERIZED}?:false
    fun allRequiredTargetsHaveRecoveredBodies()=requiredSemanticTargets.all{isRecoveredPhase11Method(it.legacyClassName,it.methodSignature)&&recoveryMetadataMatchesInventory(it)}
    fun allRequiredTargetsHaveConsistentSurfaceEvidence()=requiredSemanticTargets.all(::surfaceEvidenceIsInternallyConsistent)
    fun allRequiredTargetsAreSemanticallyCharacterized()=requiredSemanticTargets.all{it.characterizationState==CharacterizationState.SEMANTICS_CHARACTERIZED}
}
