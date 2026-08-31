package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.*
import org.junit.Test

class LegacyLineupCommitRuleTest {
    @Test fun onlyNonNullStarterSlotsOneThroughTwentyFiveAreCommitted(){
        val r=LegacyLineupCommitRule.commit(listOf(LegacyLineupCommitSlot("S1",1),LegacyLineupCommitSlot("S25",25),LegacyLineupCommitSlot("ZERO",0),LegacyLineupCommitSlot("S26",26),LegacyLineupCommitSlot<String>(null,7)),emptyList(),emptyList(),0,false)
        assertEquals(listOf("S1","S25"),r.clubStarters);assertEquals(listOf(LegacyLineupPlayerWrite("S1",1,true),LegacyLineupPlayerWrite("S25",25,true)),r.playerWrites);assertTrue(r.matchLists.replaceSideLists);assertEquals(r.clubStarters,r.matchLists.startersPrimary);assertEquals(r.clubStarters,r.matchLists.startersMirror)
    }
    @Test fun benchRequiresIdentityEligibleMembershipGetsCodes26PlusAndDoesNotWriteStarterFalse(){
        class P(val id:String)
        val b1=P("B1");val equalButDifferent=P("B1");val b2=P("B2")
        val r=LegacyLineupCommitRule.commit<P>(emptyList(),listOf(b1,equalButDifferent,null,b2),listOf(b1,b2),1,true)
        assertEquals(listOf(b1,b2),r.clubBench);assertEquals(listOf(LegacyLineupPlayerWrite(b1,26,null),LegacyLineupPlayerWrite(b2,27,null)),r.playerWrites);assertTrue(r.finalizePlan.finishMainTeamActivity)
    }
    @Test fun unsupportedMatchSideLeavesExistingSideListsUntouched(){
        val r=LegacyLineupCommitRule.commit(listOf(LegacyLineupCommitSlot("S",3)),listOf("B"),listOf("B"),9,false)
        assertEquals(listOf("S"),r.clubStarters);assertEquals(listOf("B"),r.clubBench);assertFalse(r.matchLists.replaceSideLists);assertTrue(r.matchLists.startersPrimary.isEmpty());assertTrue(r.matchLists.bench.isEmpty())
    }
    @Test fun finalizationPlanPreservesLegacyGlobalSideEffects(){val p=LegacyLineupCommitRule.commit<String>(emptyList(),emptyList(),emptyList(),0,false).finalizePlan;assertTrue(p.setClubFlagE1True);assertFalse(p.finishMainTeamActivity);assertTrue(p.clearMainTeamStaticActivity);assertTrue(p.setMainTeamGTrue);assertTrue(p.setMainTeamHFalse);assertTrue(p.invokeBestNI);assertTrue(p.finishLineupActivity)}
}
