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
            ),
            LegacyManagerRecoveredMethodEvidence.confirmed,
        )
    }

    @Test
    fun resolvesOnlyCurrentOfficialCorpusSignatures() {
        assertEquals(
            "y()V",
            requireNotNull(
                LegacyManagerRecoveredMethodEvidence.findExact("ActivityEscalacao", "B()"),
            ).smaliMethodSignature,
        )
        assertEquals(
            "i(Ljava/lang/String;)Z",
            requireNotNull(
                LegacyManagerRecoveredMethodEvidence.findExact("ActivityEscolhaTimes", "i(String)"),
            ).smaliMethodSignature,
        )
        assertNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivityEscala", "gL()"))
        assertNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivityEscolhaTimes", "E(String)"))
        assertNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivitySavedTatics", "sa()"))
        assertNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivityTactics", "onCreate(Bundle)"))
    }

    @Test
    fun retainsCurrentRecoveredStructureWithoutAssigningBehavior() {
        val playerInfo = LegacyManagerRecoveredMethodEvidence.findExact(
            "DialogIgrokInfo",
            "onCreate(Bundle)",
        )
        requireNotNull(playerInfo)

        assertEquals(530, playerInfo.instructionCount)
        assertEquals(28, playerInfo.branchCount)
        assertTrue(playerInfo.instructionCount > playerInfo.branchCount)
    }

    @Test
    fun canLocateAllRecoveredMethodsForAnExactLegacyClass() {
        assertEquals(
            listOf(
                LegacyRecoveredManagerMethod(
                    "ActivityProcura",
                    "t(best.o,best.c0,int)",
                    "ActivityProcura.smali",
                    136,
                    14,
                    "t(Lbest/o;Lbest/c0;I)I",
                ),
            ),
            LegacyManagerRecoveredMethodEvidence.forLegacyClass("ActivityProcura"),
        )
        assertTrue(LegacyManagerRecoveredMethodEvidence.forLegacyClass("ActivitySearch").isEmpty())
    }
}
