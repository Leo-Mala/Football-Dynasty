package com.leomala.footballdynasty.legacy.compatibility

/**
 * Method-level evidence anchored to the official Phase 4R Brasfoot corpus.
 *
 * The class name is retained for compatibility with existing Marco B boundaries, but these entries
 * are no longer copied from the historical Phase 1 `SMALI_RECOVERY.md`. They are revalidated
 * against the official `com.brasfoot.v2020` corpus documented in
 * `docs/phase4r/MANAGER_METHOD_EVIDENCE.md`.
 *
 * Structural recovery is not a behavior map. A method still needs Java↔SMALI semantic
 * characterization before unproven gameplay may be migrated.
 */
data class LegacyRecoveredManagerMethod(
    val legacyClassName: String,
    val methodSignature: String,
    val smaliFileName: String,
    val instructionCount: Int,
    val branchCount: Int,
    val smaliMethodSignature: String = methodSignature,
)

object LegacyManagerRecoveredMethodEvidence {
    const val officialLegacyPackage: String = "com.brasfoot.v2020"

    val confirmed: List<LegacyRecoveredManagerMethod> = listOf(
        LegacyRecoveredManagerMethod(
            legacyClassName = "ActivityEstadio",
            methodSignature = "onCreate(Bundle)",
            smaliFileName = "ActivityEstadio.smali",
            instructionCount = 259,
            branchCount = 11,
            smaliMethodSignature = "onCreate(Landroid/os/Bundle;)V",
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "DialogTatics",
            methodSignature = "onCreate(Bundle)",
            smaliFileName = "DialogTatics.smali",
            instructionCount = 171,
            branchCount = 19,
            smaliMethodSignature = "onCreate(Landroid/os/Bundle;)V",
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "ActivityEscalacao",
            methodSignature = "B()",
            smaliFileName = "ActivityEscalacao.smali",
            instructionCount = 212,
            branchCount = 22,
            smaliMethodSignature = "y()V",
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "ActivityProcura",
            methodSignature = "t(best.o,best.c0,int)",
            smaliFileName = "ActivityProcura.smali",
            instructionCount = 136,
            branchCount = 14,
            smaliMethodSignature = "t(Lbest/o;Lbest/c0;I)I",
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "ActivityEscolhaTimes",
            methodSignature = "i(String)",
            smaliFileName = "ActivityEscolhaTimes.smali",
            instructionCount = 38,
            branchCount = 9,
            smaliMethodSignature = "i(Ljava/lang/String;)Z",
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "DialogIgrokInfo",
            methodSignature = "onCreate(Bundle)",
            smaliFileName = "DialogIgrokInfo.smali",
            instructionCount = 530,
            branchCount = 28,
            smaliMethodSignature = "onCreate(Landroid/os/Bundle;)V",
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "ActivityTimes",
            methodSignature = "s(best.o,best.c0,int)",
            smaliFileName = "ActivityTimes.smali",
            instructionCount = 133,
            branchCount = 14,
            smaliMethodSignature = "s(Lbest/o;Lbest/c0;I)I",
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "ActivityMainTeam",
            methodSignature = "onStart()",
            smaliFileName = "ActivityMainTeam.smali",
            instructionCount = 93,
            branchCount = 15,
            smaliMethodSignature = "onStart()V",
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "ActivitySavedTatics",
            methodSignature = "g()",
            smaliFileName = "ActivitySavedTatics.smali",
            instructionCount = 103,
            branchCount = 8,
            smaliMethodSignature = "g()V",
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "best.b",
            methodSignature = "G(best.c0,best.f0,best.f0)",
            smaliFileName = "best/b.smali",
            instructionCount = 5,
            branchCount = 2,
            smaliMethodSignature = "G(Lbest/c0;Lbest/f0;Lbest/f0;)V",
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "best.f0",
            methodSignature = "l(best.f0)",
            smaliFileName = "best/f0.smali",
            instructionCount = 100,
            branchCount = 5,
            smaliMethodSignature = "l(Lbest/f0;)V",
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "best.f0",
            methodSignature = "e(best.c0)",
            smaliFileName = "best/f0.smali",
            instructionCount = 38,
            branchCount = 3,
            smaliMethodSignature = "e(Lbest/c0;)V",
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "best.c0",
            methodSignature = "y()",
            smaliFileName = "best/c0.smali",
            instructionCount = 103,
            branchCount = 22,
            smaliMethodSignature = "y()Lbest/f0;",
        ),
    )

    private val byExactMethod: Map<Pair<String, String>, LegacyRecoveredManagerMethod> =
        confirmed.associateBy { it.legacyClassName to it.methodSignature }

    fun findExact(
        legacyClassName: String,
        methodSignature: String,
    ): LegacyRecoveredManagerMethod? = byExactMethod[legacyClassName to methodSignature]

    fun forLegacyClass(legacyClassName: String): List<LegacyRecoveredManagerMethod> =
        confirmed.filter { it.legacyClassName == legacyClassName }
}
