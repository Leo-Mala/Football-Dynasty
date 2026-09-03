package com.leomala.footballdynasty.legacy.compatibility

data class LegacyRecoveredPlayerSaleMethod(
    val legacyClassName: String,
    val methodSignature: String,
    val smaliFileName: String,
    val instructionCount: Int,
    val branchCount: Int,
    val smaliMethodSignature: String,
)

/** Exact method-level chain for immediate/listed player sale in the official v2020 corpus. */
object LegacyPlayerSaleRecoveredMethodEvidence {
    val confirmed: List<LegacyRecoveredPlayerSaleMethod> = listOf(
        LegacyRecoveredPlayerSaleMethod("DialogIgrokInfo", "B(int)", "DialogIgrokInfo.smali", 72, 7, "B(I)V"),
        LegacyRecoveredPlayerSaleMethod("DialogIgrokInfo", "o(int)", "DialogIgrokInfo.smali", 29, 0, "o(I)V"),
        LegacyRecoveredPlayerSaleMethod("DialogIgrokInfo", "n()", "DialogIgrokInfo.smali", 18, 0, "n()V"),
        LegacyRecoveredPlayerSaleMethod("components.n3", "<init>(best.o,int,boolean,boolean)", "components/n3.smali", 45, 7, "<init>(Lbest/o;IZZ)V"),
        LegacyRecoveredPlayerSaleMethod("components.n3", "g()", "components/n3.smali", 17, 0, "g()V"),
        LegacyRecoveredPlayerSaleMethod("components.n3", "a()", "components/n3.smali", 9, 1, "a()V"),
        LegacyRecoveredPlayerSaleMethod("best.f", "<init>(best.o,int,boolean,boolean,int)", "best/f.smali", 621, 86, "<init>(Lbest/o;IZZI)V"),
        LegacyRecoveredPlayerSaleMethod("best.f", "n(boolean)", "best/f.smali", 84, 20, "n(Z)Lbest/c0;"),
        LegacyRecoveredPlayerSaleMethod("best.f", "q(ArrayList,boolean)", "best/f.smali", 321, 42, "q(Ljava/util/ArrayList;Z)Lbest/c0;"),
        LegacyRecoveredPlayerSaleMethod("best.f", "p()", "best/f.smali", 83, 9, "p()Lbest/c0;"),
        LegacyRecoveredPlayerSaleMethod("best.c0", "Z0(best.o,boolean)", "best/c0.smali", 77, 16, "Z0(Lbest/o;Z)Z"),
    )
}
