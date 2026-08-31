package com.leomala.footballdynasty.legacy.compatibility

/** Method-level structural evidence revalidated against the official Phase 4R corpus. */
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
        LegacyRecoveredManagerMethod("ActivityEstadio", "onCreate(Bundle)", "ActivityEstadio.smali", 259, 11, "onCreate(Landroid/os/Bundle;)V"),
        LegacyRecoveredManagerMethod("DialogTatics", "onCreate(Bundle)", "DialogTatics.smali", 171, 19, "onCreate(Landroid/os/Bundle;)V"),
        LegacyRecoveredManagerMethod("ActivityEscalacao", "B()", "ActivityEscalacao.smali", 212, 22, "y()V"),
        LegacyRecoveredManagerMethod("ActivityProcura", "t(best.o,best.c0,int)", "ActivityProcura.smali", 136, 14, "t(Lbest/o;Lbest/c0;I)I"),
        LegacyRecoveredManagerMethod("ActivityEscolhaTimes", "i(String)", "ActivityEscolhaTimes.smali", 38, 9, "i(Ljava/lang/String;)Z"),
        LegacyRecoveredManagerMethod("DialogIgrokInfo", "onCreate(Bundle)", "DialogIgrokInfo.smali", 530, 28, "onCreate(Landroid/os/Bundle;)V"),
        LegacyRecoveredManagerMethod("ActivityTimes", "s(best.o,best.c0,int)", "ActivityTimes.smali", 133, 14, "s(Lbest/o;Lbest/c0;I)I"),
        LegacyRecoveredManagerMethod("ActivityMainTeam", "onStart()", "ActivityMainTeam.smali", 93, 15, "onStart()V"),
        LegacyRecoveredManagerMethod("ActivitySavedTatics", "g()", "ActivitySavedTatics.smali", 103, 8, "g()V"),
        LegacyRecoveredManagerMethod("best.b", "G(best.c0,best.f0,best.f0)", "best/b.smali", 5, 2, "G(Lbest/c0;Lbest/f0;Lbest/f0;)V"),
        LegacyRecoveredManagerMethod("best.f0", "l(best.f0)", "best/f0.smali", 100, 5, "l(Lbest/f0;)V"),
        LegacyRecoveredManagerMethod("best.f0", "e(best.c0)", "best/f0.smali", 38, 3, "e(Lbest/c0;)V"),
        LegacyRecoveredManagerMethod("best.c0", "y()", "best/c0.smali", 103, 22, "y()Lbest/f0;"),
        LegacyRecoveredManagerMethod("best.b", "t(best.c0,int)", "best/b.smali", 120, 20, "t(Lbest/c0;I)Lbest/f0;"),
        LegacyRecoveredManagerMethod("best.b", "u()", "best/b.smali", 30, 4, "u()Lbest/f0;"),
        LegacyRecoveredManagerMethod("best.b", "b4(best.f0,best.f0)", "best/b.smali", 9, 0, "b4(Lbest/f0;Lbest/f0;)V"),
        LegacyRecoveredManagerMethod("best.n", "l()", "best/n.smali", 81, 16, "l()V"),
        LegacyRecoveredManagerMethod("best.n", "m()", "best/n.smali", 65, 11, "m()V"),
    )

    private val byExactMethod: Map<Pair<String, String>, LegacyRecoveredManagerMethod> =
        confirmed.associateBy { it.legacyClassName to it.methodSignature }

    fun findExact(legacyClassName: String, methodSignature: String): LegacyRecoveredManagerMethod? =
        byExactMethod[legacyClassName to methodSignature]

    fun forLegacyClass(legacyClassName: String): List<LegacyRecoveredManagerMethod> =
        confirmed.filter { it.legacyClassName == legacyClassName }
}
