package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.*
import org.junit.Test

class LegacyLineupEligibilityRuleTest {
    private fun p(id:String,skill:Int=50,energy:Int=50,position:Int=2,subrole:Int=0,star:Boolean=false,m0:Boolean=false,v0:Boolean=false,hasClub:Boolean=true,q0:Boolean?=true,contract:Long=100)=LegacyLineupEligibilityPlayer(LegacyLineupRuntimePlayer(id,position,0,subrole,skill,energy,star),m0,v0,hasClub,q0,contract)
    @Test fun m0AlwaysRejectsAndV0DependsOnCompetitionRestriction(){assertFalse(LegacyLineupEligibilityRule.isEligible(p("m",m0=true),false,true,200));assertTrue(LegacyLineupEligibilityRule.isEligible(p("v",v0=true),false,true,200));assertFalse(LegacyLineupEligibilityRule.isEligible(p("v",v0=true),true,true,200))}
    @Test fun modeOrClubStateBypassesContractExpiryBranch(){val e=p("p",contract=99);assertFalse(LegacyLineupEligibilityRule.isEligible(e,false,false,100));assertTrue(LegacyLineupEligibilityRule.isEligible(e,false,true,100));assertTrue(LegacyLineupEligibilityRule.isEligible(p("p",hasClub=false,contract=0),false,false,100));assertTrue(LegacyLineupEligibilityRule.isEligible(p("p",q0=null,contract=0),false,false,100));assertTrue(LegacyLineupEligibilityRule.isEligible(p("p",q0=false,contract=0),false,false,100));assertTrue(LegacyLineupEligibilityRule.isEligible(p("p",contract=100),false,false,100))}
    @Test fun classificationUsesLegacyComparators(){val r=LegacyLineupEligibilityRule.classify(listOf(p("loE",80,10,q0=false),p("hiE",80,90,q0=false),p("loS",60,99,q0=false),p("ub",80,position=3,subrole=1,m0=true),p("us",70,position=2,star=true,m0=true),p("un",70,position=2,m0=true)),emptyList(),false,false,100);assertEquals(listOf("hiE","loE","loS"),r.eligible.map{it.value});assertEquals(listOf("us","un","ub"),r.unavailable.map{it.value})}
    @Test fun auxiliaryPlayersAppendOnlyInModeThenJoinSort(){val aux=LegacyLineupRuntimePlayer("aux",4,0,0,99,99,false);val r=LegacyLineupEligibilityRule.classify(listOf(p("base",skill=20,q0=false)),listOf(aux),false,true,100);assertEquals(listOf("aux","base"),r.eligible.map{it.value});val no=LegacyLineupEligibilityRule.classify(listOf(p("base",skill=20,q0=false)),listOf(aux),false,false,100);assertEquals(listOf("base"),no.eligible.map{it.value})}
}
