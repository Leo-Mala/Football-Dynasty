package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyManagerRecoveredMethodEvidenceTest {
    @Test
    fun matchesOfficialPhase4rManagerMethodInventoryExactly() {
        assertEquals("com.brasfoot.v2020", LegacyManagerRecoveredMethodEvidence.officialLegacyPackage)
        assertEquals(
            listOf(
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
                LegacyRecoveredManagerMethod("best.n", "i()", "best/n.smali", 78, 16, "i()V"),
                LegacyRecoveredManagerMethod("best.n", "h()", "best/n.smali", 37, 3, "h()V"),
                LegacyRecoveredManagerMethod("best.n", "j()", "best/n.smali", 27, 3, "j()V"),
                LegacyRecoveredManagerMethod("best.n", "k()", "best/n.smali", 117, 20, "k()V"),
            ),
            LegacyManagerRecoveredMethodEvidence.confirmed,
        )
    }

    @Test
    fun resolvesOnlyCurrentOfficialCorpusSignatures() {
        assertEquals("y()V", requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivityEscalacao", "B()")).smaliMethodSignature)
        assertEquals("i(Ljava/lang/String;)Z", requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivityEscolhaTimes", "i(String)")).smaliMethodSignature)
        assertEquals("G(Lbest/c0;Lbest/f0;Lbest/f0;)V", requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.b", "G(best.c0,best.f0,best.f0)")).smaliMethodSignature)
        assertEquals("y()Lbest/f0;", requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.c0", "y()")).smaliMethodSignature)
        assertEquals("l()V", requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", "l()")).smaliMethodSignature)
        assertEquals("m()V", requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", "m()")).smaliMethodSignature)
        assertEquals("i()V", requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", "i()")).smaliMethodSignature)
        assertEquals("h()V", requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", "h()")).smaliMethodSignature)
        assertEquals("j()V", requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", "j()")).smaliMethodSignature)
        assertEquals("k()V", requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", "k()")).smaliMethodSignature)
        assertNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivityEscala", "gL()"))
        assertNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivityEscolhaTimes", "E(String)"))
        assertNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivitySavedTatics", "sa()"))
        assertNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivityTactics", "onCreate(Bundle)"))
    }

    @Test
    fun employmentTransitionMethodsAndReplacementResolverRemainDistinctEvidenceTargets() {
        val transfer = requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.b", "G(best.c0,best.f0,best.f0)"))
        val leave = requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.f0", "l(best.f0)"))
        val join = requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.f0", "e(best.c0)"))
        val replacement = requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.c0", "y()"))
        assertEquals(5, transfer.instructionCount)
        assertEquals(100, leave.instructionCount)
        assertEquals(38, join.instructionCount)
        assertEquals(103, replacement.instructionCount)
        assertEquals(22, replacement.branchCount)
    }

    @Test
    fun recoveredYearEndAndContinuationMethodsKeepOfficialStructuralFingerprints() {
        val expected = mapOf(
            "l()" to (81 to 16),
            "m()" to (65 to 11),
            "i()" to (78 to 16),
            "h()" to (37 to 3),
            "j()" to (27 to 3),
            "k()" to (117 to 20),
        )
        expected.forEach { (signature, fingerprint) ->
            val recovered = requireNotNull(LegacyManagerRecoveredMethodEvidence.findExact("best.n", signature))
            assertEquals(fingerprint, recovered.instructionCount to recovered.branchCount)
        }
    }

    @Test
    fun canLocateAllRecoveredMethodsForAnExactLegacyClass() {
        assertEquals(
            listOf(LegacyRecoveredManagerMethod("ActivityProcura", "t(best.o,best.c0,int)", "ActivityProcura.smali", 136, 14, "t(Lbest/o;Lbest/c0;I)I")),
            LegacyManagerRecoveredMethodEvidence.forLegacyClass("ActivityProcura"),
        )
        assertEquals(2, LegacyManagerRecoveredMethodEvidence.forLegacyClass("best.f0").size)
        assertEquals(6, LegacyManagerRecoveredMethodEvidence.forLegacyClass("best.n").size)
        assertTrue(LegacyManagerRecoveredMethodEvidence.forLegacyClass("ActivitySearch").isEmpty())
    }
}
