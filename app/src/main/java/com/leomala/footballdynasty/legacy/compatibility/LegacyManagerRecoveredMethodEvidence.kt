package com.leomala.footballdynasty.legacy.compatibility

/**
 * Method-level SMALI recovery evidence for the manager loop.
 *
 * Every entry below is copied from the versioned `SMALI_RECOVERY.md` inventory,
 * where the corresponding decompiler stub was matched to its SMALI body and its
 * control-flow structure was verified. This is deliberately not a behavior map:
 * instruction and branch counts prove that a body was recovered, not what a
 * finance, transfer, tactic, lineup, stadium, or career rule means.
 *
 * A recovered method must still receive a semantic Java↔SMALI read plus
 * characterization tests before its behavior may be migrated into the modern
 * runtime.
 */
data class LegacyRecoveredManagerMethod(
    val legacyClassName: String,
    val methodSignature: String,
    val smaliFileName: String,
    val instructionCount: Int,
    val branchCount: Int,
)

object LegacyManagerRecoveredMethodEvidence {
    val confirmed: List<LegacyRecoveredManagerMethod> = listOf(
        LegacyRecoveredManagerMethod(
            legacyClassName = "ActivityEstadio",
            methodSignature = "onCreate(Bundle)",
            smaliFileName = "ActivityEstadio.smali",
            instructionCount = 281,
            branchCount = 12,
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "DialogTatics",
            methodSignature = "onCreate(Bundle)",
            smaliFileName = "DialogTatics.smali",
            instructionCount = 172,
            branchCount = 20,
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "ActivityEscala",
            methodSignature = "gL()",
            smaliFileName = "ActivityEscala.smali",
            instructionCount = 223,
            branchCount = 22,
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "ActivityProcura",
            methodSignature = "a(a.p,a.ac,int)",
            smaliFileName = "ActivityProcura.smali",
            instructionCount = 2,
            branchCount = 0,
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "ActivityEscolhaTimes",
            methodSignature = "E(String)",
            smaliFileName = "ActivityEscolhaTimes.smali",
            instructionCount = 38,
            branchCount = 9,
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "DialogIgrokInfo",
            methodSignature = "onCreate(Bundle)",
            smaliFileName = "DialogIgrokInfo.smali",
            instructionCount = 554,
            branchCount = 28,
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "ActivityTimes",
            methodSignature = "a(a.p,a.ac,int)",
            smaliFileName = "ActivityTimes.smali",
            instructionCount = 2,
            branchCount = 0,
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "ActivityMainTeam",
            methodSignature = "onStart()",
            smaliFileName = "ActivityMainTeam.smali",
            instructionCount = 97,
            branchCount = 15,
        ),
        LegacyRecoveredManagerMethod(
            legacyClassName = "ActivitySavedTatics",
            methodSignature = "sa()",
            smaliFileName = "ActivitySavedTatics.smali",
            instructionCount = 115,
            branchCount = 9,
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
