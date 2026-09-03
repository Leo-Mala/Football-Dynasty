package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.*
import org.junit.Test

class LegacyLineupStartRuleTest {
    private class P(val id:Int)
    @Test fun elevenIdentityEligibleValidSlotsStartBackgroundCommit(){val ps=List(11){P(it)};val p=LegacyLineupStartRule.plan(ps.mapIndexed{i,x->LegacyLineupCommitSlot(x,i+1)},ps,false);assertEquals(11,p.validStarterCount);assertTrue(p.showProgressAndLockWindow);assertTrue(p.runCommitOnBackgroundThread);assertFalse(p.showSelectPlayersToast)}
    @Test fun invalidOrEqualButDifferentPlayersDoNotCount(){val e=List(11){P(it)};val s=e.mapIndexed{i,x->when(i){0->LegacyLineupCommitSlot(P(x.id),1);1->LegacyLineupCommitSlot<P>(null,2);2->LegacyLineupCommitSlot(x,0);3->LegacyLineupCommitSlot(x,26);else->LegacyLineupCommitSlot(x,i+1)}};val p=LegacyLineupStartRule.plan(s,e,false);assertEquals(7,p.validStarterCount);assertTrue(p.showSelectPlayersToast);assertFalse(p.rebuildAutomaticAndRetry)}
    @Test fun d1FailurePreservesFourThenZeroResetBeforeRetry(){val p=LegacyLineupStartRule.plan<P>(emptyList(),emptyList(),true);assertEquals(listOf(4,0),p.legacyFormationResetSequence);assertTrue(p.rebuildAutomaticAndRetry)}
}
