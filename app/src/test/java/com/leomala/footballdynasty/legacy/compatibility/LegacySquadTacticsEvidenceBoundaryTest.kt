package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.*
import org.junit.Test

class LegacySquadTacticsEvidenceBoundaryTest {
 @Test fun allCurrentOfficialPhase11HostsAreNowCharacterized(){assertTrue(LegacySquadTacticsEvidenceBoundary.allRequiredTargetsAreSemanticallyCharacterized());assertTrue(LegacySquadTacticsEvidenceBoundary.recoveredMethodsAwaitingSemanticCharacterization.isEmpty());assertFalse(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("ActivityEscalacao","B()"));assertFalse(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("DialogTatics","onCreate(Bundle)"));assertFalse(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("ActivitySavedTatics","g()"))}
 @Test fun lineupPathsCoverEligibilityFormationSavedApplyValidationAndCommit(){assertEquals(LegacyCharacterizedLineupRuntimePath.entries.toSet(),LegacySquadTacticsEvidenceBoundary.characterizedLineupRuntimePaths);LegacyCharacterizedLineupRuntimePath.entries.forEach{assertTrue(LegacySquadTacticsEvidenceBoundary.isCharacterizedLineupRuntimePath(it))}}
 @Test fun tacticsPathsRemainCharacterized(){assertEquals(LegacyCharacterizedTacticsRuntimePath.entries.toSet(),LegacySquadTacticsEvidenceBoundary.characterizedTacticsRuntimePaths)}
 @Test fun historicalNamesRemainRejected(){assertNull(LegacySquadTacticsEvidenceBoundary.findTarget("ActivityEscala","gL()"));assertFalse(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("ActivityEscala","gL()"))}
 @Test fun structuralEvidenceStillMatchesOfficialCorpus(){val t=LegacySquadTacticsEvidenceBoundary.requiredSemanticTargets;assertEquals(3,t.size);assertTrue(t.all(LegacySquadTacticsEvidenceBoundary::recoveryMetadataMatchesInventory));assertTrue(LegacySquadTacticsEvidenceBoundary.allRequiredTargetsHaveRecoveredBodies());assertTrue(LegacySquadTacticsEvidenceBoundary.allRequiredTargetsHaveConsistentSurfaceEvidence());val l=requireNotNull(LegacySquadTacticsEvidenceBoundary.findTarget("ActivityEscalacao","B()"));assertEquals(212,l.instructionCount);assertEquals(22,l.branchCount);assertEquals("y()V",l.smaliMethodSignature)}
}
