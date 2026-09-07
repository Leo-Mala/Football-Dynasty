package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.competition.LegacyLeagueTransientRatingCaptureRules
import com.leomala.footballdynasty.domain.match.LegacyMatchPlayerRatingRules
import com.leomala.footballdynasty.domain.match.LegacyMatchRatingMetricRuntimeRules
import com.leomala.footballdynasty.domain.match.LegacyMatchRatingParticipantRuntime
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime
import com.leomala.footballdynasty.foundation.random.RandomSource

/** Executes transient legacy `best.s.e() -> best.o.n(...)` rating calls in exact list order. */
object CareerMatchPlayerRatingRuntimeExecutor {
    data class RatedPlayer(
        val playerId: String,
        val side: Int,
        val ratingY0: Double,
        val resolvedLegacyS: Int,
        val legacyL0: Int,
        val legacyF0: Int,
        val legacyR: Int,
    ) {
        /** Exact inputs consumed immediately afterward by legacy `best.k0.a(best.o)`. */
        fun toCompetitionMutation() = CareerCompetitionPlayerRatingMutation(
            playerId = playerId,
            ratingY0 = ratingY0,
            legacyG0 = resolvedLegacyS,
            legacyL0 = legacyL0,
            legacyF0 = legacyF0,
            legacyR = legacyR,
        )

        /** Exact transient `konrent.t.a0(best.o)` projection when the match owner is a type-1 league. */
        fun toTransientLeagueCapture(
            implicitRandom: RandomSource,
        ): LegacyLeagueTransientRatingCaptureRules.Capture? =
            LegacyLeagueTransientRatingCaptureRules.capture(
                legacyCompetitionType = 1,
                isLegacyKonrentT = true,
                ratingY0 = ratingY0,
                legacyG0 = resolvedLegacyS,
                implicitRandom = implicitRandom,
            )
    }

    fun execute(
        state: LegacyMatchTransientRuntime.State<
            CareerMatchPersistedRuntimeResolver.PersistedClubRoster,
            CareerMatchPersistedRuntimeResolver.PersistedPlayer,
        >,
        participantSnapshot: LegacyMatchRatingParticipantRuntime.Snapshot<
            CareerMatchPersistedRuntimeResolver.PersistedPlayer,
        >,
        metricState: LegacyMatchRatingMetricRuntimeRules.State,
        implicitRandomFactory: () -> RandomSource,
    ): List<RatedPlayer> =
        LegacyMatchRatingParticipantRuntime.entriesInLegacyOrder(state, participantSnapshot).map { entry ->
            val input = CareerMatchPlayerRatingInputMapper.map(
                state = state,
                metricState = metricState,
                player = entry.player,
                side = entry.side,
            )
            // Legacy `best.o.n(...)` constructs a fresh `new java.util.Random()` for every call.
            val result: LegacyMatchPlayerRatingRules.Result = LegacyMatchPlayerRatingRules.resolve(
                input = input,
                implicitRandom = implicitRandomFactory(),
            )
            RatedPlayer(
                playerId = entry.player.value.playerId,
                side = entry.side,
                ratingY0 = result.ratingY0,
                resolvedLegacyS = result.resolvedLegacyS,
                legacyL0 = entry.player.legacyL0,
                legacyF0 = entry.player.legacyF0,
                legacyR = entry.player.legacyR,
            )
        }
}
