package com.leomala.footballdynasty.data.local

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
    )

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
            )
        }
}
