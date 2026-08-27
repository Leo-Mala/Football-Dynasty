package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import com.leomala.footballdynasty.domain.model.Match



/** Applies the already-resolved legacy `best.s.k(...)` action directly to the transient match runtime. */
object LegacyMatchMinuteRuntimeRules {
    data class Result<TClub, TPlayer>(
        val counters: LegacyMatchMinuteActionRules.Counters,
        val selectedPlayer: LegacyMatchTransientRuntime.Player<TPlayer>?,
        val eventResult: LegacyMatchTransientRuntime.ApplyResult<TClub, TPlayer>?,
    )

    fun <TClub, TPlayer> applyDecision(
        state: LegacyMatchTransientRuntime.State<TClub, TPlayer>,
        decision: LegacyMatchMinuteRules.Decision,
        counters: LegacyMatchMinuteActionRules.Counters,
        legacyPeriod: Int,
        legacyMinute: Int,
        random: RandomSource,
        refreshPlayerState: () -> Unit = {},
        applySecondHalfJ: () -> Unit = {},
    ): Result<TClub, TPlayer> {
        val club = when (decision.side) {
            LegacyMatchMinuteRules.Side.HOME -> state.home
            LegacyMatchMinuteRules.Side.AWAY -> state.away
        }
        var eventResult: LegacyMatchTransientRuntime.ApplyResult<TClub, TPlayer>? = null
        val action = LegacyMatchMinuteActionRules.apply(
            decision = decision,
            counters = counters,
            random = random,
            activeCandidates = {
                club.active.map {
                    LegacyMatchPlayerSelectionRules.Candidate(
                        value = it,
                        legacyPositionIndex = it.legacyG0,
                    )
                }
            },
            refreshPlayerState = refreshPlayerState,
            applyLegacyC = { player ->
                eventResult = LegacyMatchTransientRuntime.applyLegacyC(
                    state, club, player, legacyPeriod, legacyMinute, random,
                )
            },
            applyLegacyD = { player ->
                eventResult = LegacyMatchTransientRuntime.applyLegacyD(
                    state, club, player, legacyPeriod, legacyMinute, random,
                )
            },
            applyLegacyType5 = { player ->
                eventResult = LegacyMatchTransientRuntime.applyEvent(
                    state = state,
                    legacyType = LegacyMatchEventType.INJURY.legacyCode,
                    legacySubtype = -1,
                    eventClub = club,
                    originalPrimary = player,
                    legacyPeriod = legacyPeriod,
                    legacyMinute = legacyMinute,
                    random = random,
                )
            },
            applySecondHalfJ = applySecondHalfJ,
        )
        return Result(action.counters, action.selectedPlayer, eventResult)
    }
}


/** Exact reusable core of legacy `best.s.q(minute, period)` used by accompanied `ActivityJogo.D()`. */
object LegacyMatchAccompaniedMinuteRules {
    enum class Operation { RUN_LEGACY_K, RUN_R3_K, STAMP_AND_APPEND_EVENT }
    data class Result<T>(val event: T?, val operations: List<Operation>)

    fun <T> run(
        legacyMinute: Int,
        legacyPeriod: Int,
        runLegacyK: () -> Unit,
        runR3K: () -> T?,
        stampAndAppend: (event: T, minute: Int, period: Int) -> Unit,
    ): Result<T> {
        val ops = mutableListOf<Operation>()
        runLegacyK(); ops += Operation.RUN_LEGACY_K
        val event = runR3K(); ops += Operation.RUN_R3_K
        if (event != null) {
            stampAndAppend(event, legacyMinute, legacyPeriod)
            ops += Operation.STAMP_AND_APPEND_EVENT
        }
        return Result(event, ops)
    }
}


/** Non-UI core routing recovered from `ActivityJogo.E()/d()/h()` at accompanied-match end. */
object LegacyMatchAccompaniedFinalizationRules {
    data class Result(val rebuildScoreFromEvents: Boolean, val routeToPenaltyFlow: Boolean)
    fun resolve(
        cachedLegacyScoreValue: Int,
        presentationLegacyScoreValue: Int,
        legacyJ0Flag: Boolean,
        p0Unresolved: Boolean,
        homeLegacyQ0Flag: Boolean,
        awayLegacyQ0Flag: Boolean,
    ): Result = Result(
        rebuildScoreFromEvents = cachedLegacyScoreValue != presentationLegacyScoreValue,
        routeToPenaltyFlow = legacyJ0Flag && p0Unresolved && (homeLegacyQ0Flag || awayLegacyQ0Flag),
    )
}


/** Applies a proven `r3.b/c` mutation plan, including goal materialization, to the transient runtime. */
object LegacyMatchR3RuntimeRules {
    data class GoalStatState<TPlayer>(
        val primaryS: Map<LegacyMatchTransientRuntime.Player<TPlayer>, Int> = emptyMap(),
        val secondaryL: Map<LegacyMatchTransientRuntime.Player<TPlayer>, Int> = emptyMap(),
        val competitionSideEffect: Map<LegacyMatchTransientRuntime.Player<TPlayer>, Int> = emptyMap(),
        val ownGoalT: Map<LegacyMatchTransientRuntime.Player<TPlayer>, Int> = emptyMap(),
    )
    data class Result<TClub, TPlayer>(
        val r3State: LegacyMatchR3MutationApplicationRules.State,
        val goalStats: GoalStatState<TPlayer>,
        val goalEvent: LegacyMatchEventRecord<LegacyMatchTransientRuntime.Club<TClub, TPlayer>, LegacyMatchTransientRuntime.Player<TPlayer>>?,
        val score: LegacyMatchScoreRules.Score,
    )
    fun <TClub, TPlayer> apply(
        state: LegacyMatchTransientRuntime.State<TClub, TPlayer>, currentSide: Int,
        plan: LegacyMatchR3EventRoutingRules.Result, r3State: LegacyMatchR3MutationApplicationRules.State,
        goalStats: GoalStatState<TPlayer> = GoalStatState(),
        goal: LegacyMatchGoalMaterializationRules.Result<LegacyMatchTransientRuntime.Player<TPlayer>>?,
        legacyPeriod: Int, legacyMinute: Int,
    ): Result<TClub, TPlayer> {
        var stats=goalStats
        var event: LegacyMatchEventRecord<LegacyMatchTransientRuntime.Club<TClub, TPlayer>, LegacyMatchTransientRuntime.Player<TPlayer>>?=null
        fun inc(map:Map<LegacyMatchTransientRuntime.Player<TPlayer>,Int>, p:LegacyMatchTransientRuntime.Player<TPlayer>)=map.toMutableMap().also{it[p]=(it[p]?:0)+1}.toMap()
        val applied=LegacyMatchR3MutationApplicationRules.apply(currentSide,plan,r3State){
            val g=checkNotNull(goal){"Recovered r3 goal mutation requires materialized goal"}
            for(m in g.statMutations){ val p=m.player.value; stats=when(m.operation){
                LegacyMatchGoalMaterializationRules.StatOperation.PRIMARY_S->stats.copy(primaryS=inc(stats.primaryS,p))
                LegacyMatchGoalMaterializationRules.StatOperation.SECONDARY_L->stats.copy(secondaryL=inc(stats.secondaryL,p))
                LegacyMatchGoalMaterializationRules.StatOperation.SECONDARY_COMPETITION_SIDE_EFFECT->stats.copy(competitionSideEffect=inc(stats.competitionSideEffect,p))
                LegacyMatchGoalMaterializationRules.StatOperation.OWN_GOAL_T->stats.copy(ownGoalT=inc(stats.ownGoalT,p))
            }}
            val club=when(currentSide){0->state.home;1->state.away;else->throw IllegalArgumentException("Legacy r3 side must be 0 or 1: $currentSide")}
            event=LegacyMatchEventRecord(legacyClub=club,legacyType=LegacyMatchEventType.GOAL.legacyCode,legacySubtype=g.finalSubtype.legacyCode,legacyMinute=legacyMinute,legacyPeriod=legacyPeriod,primaryPlayer=g.eventPrimary?.value,secondaryPlayer=g.eventSecondary?.value,legacyFlagH=g.penaltyFlag,legacySide=currentSide)
            state.events += checkNotNull(event)
        }
        return Result(applied.state,stats,event,state.score())
    }
}



/** Stable Phase 8 output boundary from transient legacy runtime into the existing modern match model. */
object LegacyMatchModernResultMapper {
    fun <TClub, TPlayer> map(
        state: LegacyMatchTransientRuntime.State<TClub, TPlayer>,
        matchId: String,
        homeClubId: String?,
        awayClubId: String?,
    ): Match {
        val score = state.score()
        return Match(
            id = matchId,
            homeClubId = homeClubId,
            awayClubId = awayClubId,
            homeGoals = score.legacyE,
            awayGoals = score.legacyF,
        )
    }
}



/** Executes the recovered automatic `Q0()` half loops while applying direct `k()` mutations to the transient runtime. */
object LegacyMatchAutomaticRuntimeRules {
    data class Result<TEvent>(
        val counters: LegacyMatchMinuteActionRules.Counters,
        val simulation: LegacyMatchAutomaticSimulationRules.Result<TEvent>,
    )

    fun <TClub, TPlayer, TEvent> run(
        state: LegacyMatchTransientRuntime.State<TClub, TPlayer>,
        random: RandomSource,
        homeTacticIndex: Int,
        awayTacticIndex: Int,
        initialCounters: LegacyMatchMinuteActionRules.Counters,
        advanceR3: (half: Int, minute: Int) -> TEvent?,
        halftimeTransition: (half: Int, minute: Int) -> Unit,
        refreshPlayerState: () -> Unit = {},
        applySecondHalfJ: () -> Unit = {},
    ): Result<TEvent> {
        var counters = initialCounters
        val simulation = LegacyMatchAutomaticSimulationRules.run(
            random = random,
            runMinuteRule = { half, minute ->
                val decision = LegacyMatchMinuteRules.decide(
                    random = random,
                    half = half,
                    minute = minute,
                    homeTacticIndex = homeTacticIndex,
                    awayTacticIndex = awayTacticIndex,
                    primaryCounter = counters.legacyO,
                    secondaryCounter = counters.legacyP,
                    tertiaryCounter = counters.legacyQ,
                )
                counters = LegacyMatchMinuteRuntimeRules.applyDecision(
                    state = state,
                    decision = decision,
                    counters = counters,
                    legacyPeriod = half,
                    legacyMinute = minute,
                    random = random,
                    refreshPlayerState = refreshPlayerState,
                    applySecondHalfJ = applySecondHalfJ,
                ).counters
            },
            advanceR3 = advanceR3,
            halftimeTransition = halftimeTransition,
        )
        return Result(counters, simulation)
    }
}
