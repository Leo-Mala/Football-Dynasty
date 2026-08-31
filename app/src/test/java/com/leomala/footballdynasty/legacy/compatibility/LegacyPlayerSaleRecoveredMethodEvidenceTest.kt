package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyPlayerSaleRecoveredMethodEvidenceTest {
    @Test
    fun exactOfficialSaleChainFingerprintsRemainLocked() {
        assertEquals(
            listOf(
                "DialogIgrokInfo.B(int)" to (72 to 7),
                "DialogIgrokInfo.o(int)" to (29 to 0),
                "DialogIgrokInfo.n()" to (18 to 0),
                "components.n3.<init>(best.o,int,boolean,boolean)" to (45 to 7),
                "components.n3.g()" to (17 to 0),
                "components.n3.a()" to (9 to 1),
                "best.f.<init>(best.o,int,boolean,boolean,int)" to (621 to 86),
                "best.f.n(boolean)" to (84 to 20),
                "best.f.q(ArrayList,boolean)" to (321 to 42),
                "best.f.p()" to (83 to 9),
                "best.c0.Z0(best.o,boolean)" to (77 to 16),
            ),
            LegacyPlayerSaleRecoveredMethodEvidence.confirmed.map {
                "${it.legacyClassName}.${it.methodSignature}" to (it.instructionCount to it.branchCount)
            },
        )
    }
}
