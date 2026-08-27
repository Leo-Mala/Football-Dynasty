package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchFinalIntegrationTest {
    @Test
    fun `resolved legacy C applies into transient runtime and preserves O timing`() {
        val f = fixture()
        val decision = LegacyMatchMinuteRules.Decision(
            side = LegacyMatchMinuteRules.Side.HOME,
            action = LegacyMatchMinuteRules.Action.LEGACY_C,
            refreshPlayerState = false,
            primaryBound = 1, secondaryBound = 1, tertiaryBound = 1,
        )
        val result = LegacyMatchMinuteRuntimeRules.applyDecision(
            state=f.state, decision=decision,
            counters=LegacyMatchMinuteActionRules.Counters(2,3,4),
            legacyPeriod=1, legacyMinute=9, random=QueueRandom(0),
        )
        assertEquals(3, result.counters.legacyO)
        assertEquals(1, f.player.legacyYellowCount)
        assertEquals(listOf(2), f.state.events.map { it.legacyType })
        assertSame(f.player, result.selectedPlayer)
    }

    @Test
    fun `second half J path reaches callback without fabricating event`() {
        val f = fixture()
        var calls=0
        val decision=LegacyMatchMinuteRules.Decision(
            LegacyMatchMinuteRules.Side.AWAY, LegacyMatchMinuteRules.Action.SECOND_HALF_J,
            false, 1,1,1,
        )
        val result=LegacyMatchMinuteRuntimeRules.applyDecision(
            f.state, decision, LegacyMatchMinuteActionRules.Counters(0,0,0),
            2,10,QueueRandom(), applySecondHalfJ={calls++},
        )
        assertEquals(1,calls); assertTrue(f.state.events.isEmpty()); assertEquals(null,result.eventResult)
    }

    @Test
    fun `accompanied q core keeps k then K then stamp append order`() {
        val order=mutableListOf<String>()
        val event=Any()
        val result=LegacyMatchAccompaniedMinuteRules.run(
            legacyMinute=14, legacyPeriod=2,
            runLegacyK={order+="k"},
            runR3K={order+="K"; event},
            stampAndAppend={e,m,p-> assertSame(event,e); order+="stamp:$m:$p"},
        )
        assertEquals(listOf("k","K","stamp:14:2"),order)
        assertSame(event,result.event)
    }

    @Test
    fun `accompanied finalization mirrors score rebuild and penalty short circuit`() {
        val a=LegacyMatchAccompaniedFinalizationRules.resolve(1,2,true,true,false,true)
        assertTrue(a.rebuildScoreFromEvents); assertTrue(a.routeToPenaltyFlow)
        val b=LegacyMatchAccompaniedFinalizationRules.resolve(2,2,true,false,true,true)
        assertFalse(b.rebuildScoreFromEvents); assertFalse(b.routeToPenaltyFlow)
    }

    @Test
    fun `r3 goal plan materializes into same runtime ledger score and player counters`() {
        val f=fixture()
        val secondary=LegacyMatchTransientRuntime.Player("assist",10,1,1,1,25,50,80)
        val gp=LegacyMatchGoalMaterializationRules.Player(f.player, f.player.legacyL0)
        val gs=LegacyMatchGoalMaterializationRules.Player(secondary, secondary.legacyL0)
        val goal=LegacyMatchGoalMaterializationRules.Result(
            finalSubtype=LegacyMatchGoalEventRules.GoalSubtype.NORMAL,
            eventPrimary=gp,
            eventSecondary=gs,
            penaltyFlag=false,
            statMutations=listOf(
                LegacyMatchGoalMaterializationRules.StatMutation(gp, LegacyMatchGoalMaterializationRules.StatOperation.PRIMARY_S),
                LegacyMatchGoalMaterializationRules.StatMutation(gs, LegacyMatchGoalMaterializationRules.StatOperation.SECONDARY_L),
                LegacyMatchGoalMaterializationRules.StatMutation(gs, LegacyMatchGoalMaterializationRules.StatOperation.SECONDARY_COMPETITION_SIDE_EFFECT),
            ),
        )
        val plan=plan(
            LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_S_CURRENT,
            LegacyMatchR3EventRoutingRules.Mutation.MATERIALIZE_GOAL_CURRENT,
            LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_Y_CURRENT,
        )
        val result=LegacyMatchR3RuntimeRules.apply(
            state=f.state,currentSide=0,plan=plan,
            r3State=LegacyMatchR3MutationApplicationRules.State(listOf(0,0),listOf(0,0),listOf(0,0),5),
            goal=goal,legacyPeriod=1,legacyMinute=21,
        )
        assertEquals(1,result.score.legacyE); assertEquals(0,result.score.legacyF)
        assertEquals(listOf(1,0),result.r3State.legacyIBySide)
        assertEquals(listOf(1,0),result.r3State.legacyYBySide)
        assertEquals(1,result.goalStats.primaryS[f.player])
        assertEquals(1,result.goalStats.secondaryL[secondary])
        assertEquals(1,result.goalStats.competitionSideEffect[secondary])
        assertEquals(21,result.goalEvent?.legacyMinute)
    }

    @Test
    fun `r3 non goal plan does not require fabricated goal result`() {
        val f=fixture()
        val result=LegacyMatchR3RuntimeRules.apply(
            state=f.state,currentSide=1,
            plan=plan(LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_S_CURRENT, LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_Z_CURRENT),
            r3State=LegacyMatchR3MutationApplicationRules.State(listOf(0,0),listOf(0,0),listOf(0,0),null),
            goal=null,legacyPeriod=2,legacyMinute=30,
        )
        assertEquals(listOf(0,1),result.r3State.legacyIBySide)
        assertEquals(listOf(0,1),result.r3State.legacyZBySide)
        assertEquals(null,result.goalEvent); assertTrue(f.state.events.isEmpty())
    }

    @Test
    fun `modern mapper exports score without mutating transient state`() {
        val f=fixture()
        f.state.events += LegacyMatchEventRecord(
            legacyClub=f.home, legacyType=1, legacySubtype=1, legacyMinute=4, legacyPeriod=1,
            primaryPlayer=f.player, legacySide=0,
        )
        val mapped=LegacyMatchModernResultMapper.map(f.state,"m1","H","A")
        assertEquals("m1",mapped.id); assertEquals("H",mapped.homeClubId); assertEquals(1,mapped.homeGoals); assertEquals(0,mapped.awayGoals)
        assertEquals(1,f.state.events.size)
    }

    @Test
    fun `automatic runtime executes exact 45 plus added loops using shared RNG`() {
        val f=fixture()
        var halftime=0; var jCalls=0; var r3Calls=0
        val random=ZeroRandom()
        val result=LegacyMatchAutomaticRuntimeRules.run<String,String,String>(
            state=f.state, random=random, homeTacticIndex=0, awayTacticIndex=0,
            initialCounters=LegacyMatchMinuteActionRules.Counters(0,0,0),
            advanceR3={_,_->r3Calls++; null},
            halftimeTransition={h,m->assertEquals(2,h);assertEquals(0,m);halftime++},
            applySecondHalfJ={jCalls++},
        )
        assertEquals(0,result.simulation.firstHalfAddedMinutes)
        assertEquals(1,result.simulation.secondHalfAddedMinutes)
        assertEquals(91,r3Calls); assertEquals(1,halftime); assertEquals(41,jCalls)
        assertEquals(LegacyMatchMinuteActionRules.Counters(0,0,0),result.counters)
    }

    private data class Fixture(
        val state:LegacyMatchTransientRuntime.State<String,String>,
        val home:LegacyMatchTransientRuntime.Club<String,String>,
        val away:LegacyMatchTransientRuntime.Club<String,String>,
        val player:LegacyMatchTransientRuntime.Player<String>,
    )
    private fun fixture():Fixture{
        val p=LegacyMatchTransientRuntime.Player("p",10,1,1,1,25,50,80)
        val home=LegacyMatchTransientRuntime.Club("home",101,mutableListOf(p),mutableListOf(),substitutionsRemaining=0)
        val away=LegacyMatchTransientRuntime.Club<String,String>("away",202,mutableListOf(),mutableListOf(),substitutionsRemaining=0)
        return Fixture(LegacyMatchTransientRuntime.State(2026,home,away),home,away,p)
    }
    private fun plan(vararg m:LegacyMatchR3EventRoutingRules.Mutation)=LegacyMatchR3EventRoutingRules.Result(
        selectedIndex=-1,weightTable=LegacyMatchR3EventRoutingRules.WeightTable.B0,multipliers=listOf(1.0,1.0,1.0),storedLegacyGAfter=0.0,
        sIncrementTiming=LegacyMatchR3EventRoutingRules.SIncrementTiming.AFTER_WEIGHTED_DRAW,mutations=m.toList(),
    )
    private class QueueRandom(vararg values:Int):RandomSource{
        private val q=values.toMutableList(); override var draws:Long=0; private set
        override fun nextInt(bound:Int):Int{check(q.isNotEmpty());val v=q.removeAt(0);require(v in 0 until bound);draws++;return v}
        override fun nextBoolean()=error("unused")
        override fun nextDouble()=error("unused")
    }
    private class ZeroRandom:RandomSource{
        override var draws:Long=0; private set
        override fun nextInt(bound:Int):Int{draws++;return 0}
        override fun nextBoolean():Boolean{draws++;return false}
        override fun nextDouble():Double{draws++;return 0.0}
    }
}
