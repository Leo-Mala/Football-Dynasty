package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.*
import org.junit.Test

class LegacyLineupFormationRuntimeRuleTest {
    private class P(val id: String)
    private fun p(id:String,position:Int,side:Int,subrole:Int,skill:Int=50,energy:Int=50,star:Boolean=false)=LegacyLineupRuntimePlayer(P(id),position,side,subrole,skill,energy,star)

    @Test fun exactFormationTablesMatchOfficialBestJ0(){
        assertEquals(26,LegacyLineupFormationTables.slotRequirements.size)
        assertEquals(listOf(0,-1,-1),LegacyLineupFormationTables.slotRequirements[1].toList())
        assertEquals(listOf(3,1,0),LegacyLineupFormationTables.slotRequirements[11].toList())
        assertEquals(listOf(4,0,-1),LegacyLineupFormationTables.slotRequirements[25].toList())
        assertEquals(listOf(1,2,4,4,12,15,15,20,20,23,23),LegacyLineupFormationTables.benchPreferenceSlots.toList())
        assertEquals(listOf(1,22,16,12,14,15,11,2,9,6,8),LegacyLineupFormationTables.formationSlots[0].toList())
        assertEquals(listOf(1,18,25,23,11,13,4,6,8,10,17),LegacyLineupFormationTables.formationSlots[10].toList())
    }
    @Test fun selectorUsesProgressiveLegacyRelaxationAndSourceOrder(){
        val wrong=p("wrong",2,0,0);val exact=p("exact",3,1,0)
        assertSame(exact,LegacyLineupFormationRuntimeRule.selectForSlot(listOf(wrong,exact),11))
        val f1=p("f1",4,1,0);val f2=p("f2",2,1,0)
        assertSame(f1,LegacyLineupFormationRuntimeRule.selectForSlot(listOf(f1,f2),11))
    }
    @Test fun automaticFormationSelectsElevenSortsSlotsAndSnapshotsResult(){
        val roster=listOf(p("gk",0,0,0),p("a22",4,1,0),p("m16",3,0,1),p("m12",3,1,0),p("m14",3,1,1),p("m15",3,0,1),p("m11",3,1,0),p("d2",1,1,0),p("d9",1,0,0),p("d6",2,1,0),p("d8",2,0,0),p("bench",4,0,0))
        val unavailable=listOf(p("unavailable",0,0,0));val result=LegacyLineupFormationRuntimeRule.buildAutomatic(0,roster,unavailable)
        assertEquals(11,result.starters.size);assertEquals(result.starters.map{it.player},result.snapshot.players);assertSame(unavailable.single(),result.bench.last())
        val pr=LegacyLineupFormationTables.starterDisplayOrder.withIndex().associate{it.value to it.index};val idx=result.starters.map{pr[it.slotCode]!!};assertEquals(idx.sorted(),idx)
    }
    @Test fun savedSnapshotUsesIdentityAndPreservesSlotOrder(){
        val e=p("eligible",1,1,0);val other=p("eligible",1,1,0)
        val s=LegacySavedLineupSnapshot(3,listOf<LegacyLineupRuntimePlayer<P>?>(e,other)+List(9){null},listOf(17,1)+List(9){2})
        val r=LegacyLineupFormationRuntimeRule.applySaved(s,listOf(e),emptyList());assertSame(e,r.starters[0].player);assertNull(r.starters[1].player);assertEquals(s.slotCodes,r.starters.map{it.slotCode})
    }
    @Test fun malformedParallelListsProduceNoStarters(){val r=LegacyLineupFormationRuntimeRule.applySaved(LegacySavedLineupSnapshot<P>(2,List(10){null},List(11){1}),emptyList(),emptyList());assertTrue(r.starters.isEmpty())}
    @Test(expected=IndexOutOfBoundsException::class) fun invalidSavedFormationIsNotNormalizedPastNameLookup(){LegacyLineupFormationRuntimeRule.applySaved(LegacySavedLineupSnapshot<P>(99,List(11){null},List(11){1}),emptyList(),emptyList())}
    @Test(expected=IndexOutOfBoundsException::class) fun invalidAutomaticFormationIsNotClamped(){LegacyLineupFormationRuntimeRule.buildAutomatic<P>(11,emptyList(),emptyList())}
}
