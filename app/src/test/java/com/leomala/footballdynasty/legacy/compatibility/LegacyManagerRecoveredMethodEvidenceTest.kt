package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyManagerRecoveredMethodEvidenceTest {
    @Test
    fun matchesVersionedManagerSmaliRecoveryInventoryExactly() {
        assertEquals(
            listOf(
                LegacyRecoveredManagerMethod("ActivityEstadio", "onCreate(Bundle)", "ActivityEstadio.smali", 281, 12),
                LegacyRecoveredManagerMethod("DialogTatics", "onCreate(Bundle)", "DialogTatics.smali", 172, 20),
                LegacyRecoveredManagerMethod("ActivityEscala", "gL()", "ActivityEscala.smali", 223, 22),
                LegacyRecoveredManagerMethod("ActivityProcura", "a(a.p,a.ac,int)", "ActivityProcura.smali", 2, 0),
                LegacyRecoveredManagerMethod("DialogIgrokInfo", "onCreate(Bundle)", "DialogIgrokInfo.smali", 554, 28),
                LegacyRecoveredManagerMethod("ActivityTimes", "a(a.p,a.ac,int)", "ActivityTimes.smali", 2, 0),
                LegacyRecoveredManagerMethod("ActivityMainTeam", "onStart()", "ActivityMainTeam.smali", 97, 15),
                LegacyRecoveredManagerMethod("ActivitySavedTatics", "sa()", "ActivitySavedTatics.smali", 115, 9),
            ),
            LegacyManagerRecoveredMethodEvidence.confirmed,
        )
    }

    @Test
    fun resolvesOnlyExactRecoveredMethodSignatures() {
        assertEquals(
            LegacyRecoveredManagerMethod("ActivityEscala", "gL()", "ActivityEscala.smali", 223, 22),
            LegacyManagerRecoveredMethodEvidence.findExact("ActivityEscala", "gL()"),
        )
        assertNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivityEscala", "gl()"))
        assertNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivityEscala", "onCreate(Bundle)"))
        assertNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivityTactics", "onCreate(Bundle)"))
    }

    @Test
    fun retainsRecoveredStructureWithoutAssigningBehavior() {
        val playerInfo = LegacyManagerRecoveredMethodEvidence.findExact(
            "DialogIgrokInfo",
            "onCreate(Bundle)",
        )
        requireNotNull(playerInfo)

        assertEquals(554, playerInfo.instructionCount)
        assertEquals(28, playerInfo.branchCount)
        assertTrue(playerInfo.instructionCount > playerInfo.branchCount)
    }

    @Test
    fun canLocateAllRecoveredMethodsForAnExactLegacyClass() {
        assertEquals(
            listOf(
                LegacyRecoveredManagerMethod(
                    "ActivityProcura",
                    "a(a.p,a.ac,int)",
                    "ActivityProcura.smali",
                    2,
                    0,
                ),
            ),
            LegacyManagerRecoveredMethodEvidence.forLegacyClass("ActivityProcura"),
        )
        assertTrue(LegacyManagerRecoveredMethodEvidence.forLegacyClass("ActivitySearch").isEmpty())
    }
}
