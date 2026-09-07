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
        /** Exact transient `konrent.t.a0(best.o)` capture, when the linked owner is type-1 `konrent.t`. */
        val transientLeagueCapture: LegacyLeagueTransientRatingCaptureRules.Capture? = null,
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
        /** True only after the caller proved this match is owned by type-1 serialized `konrent.t`. */
        captureTransientLeague: Boolean = false,
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
            // When its outer gate passes, `konrent.t.a0(player)` immediately constructs `z2`, whose
            // constructor owns another fresh `new Random().nextInt(100)`. Keep that factory call
            // interleaved per player; do not batch it after all ratings and do not use career RNG.
            val transientLeagueCapture = if (captureTransientLeague && result.ratingY0 > 0.0) {
                LegacyLeagueTransientRatingCaptureRules.capture(
                    legacyCompetitionType = 1,
                    isLegacyKonrentT = true,
                    ratingY0 = result.ratingY0,
                    legacyG0 = result.resolvedLegacyS,
                    implicitRandom = implicitRandomFactory(),
                )
            } else {
                null
            }
            RatedPlayer(
                playerId = entry.player.value.playerId,
                side = entry.side,
                ratingY0 = result.ratingY0,
                resolvedLegacyS = result.resolvedLegacyS,
                legacyL0 = entry.player.legacyL0,
                legacyF0 = entry.player.legacyF0,
                legacyR = entry.player.legacyR,
                transientLeagueCapture = transientLeagueCapture,
            )
        }
}
