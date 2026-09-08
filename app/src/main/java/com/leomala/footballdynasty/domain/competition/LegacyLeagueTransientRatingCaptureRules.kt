package com.leomala.footballdynasty.domain.competition

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Exact transient projection of legacy `konrent.t.a0(best.o)` reached from `best.o.n(...)`.
 *
 * `components.z2` is transient and constructs its own `new Random().nextInt(100)` tie-break value.
 * Callers therefore supply an implicit compatibility RNG here; the persisted career RNG must not be
 * consumed by this rule.
 */
object LegacyLeagueTransientRatingCaptureRules {
    data class Capture(
        val ratingY0: Double,
        val legacyG0: Int,
        val randomOrder: Int,
    )

    fun capture(
        legacyCompetitionType: Int,
        isLegacyKonrentT: Boolean,
        ratingY0: Double,
        legacyG0: Int,
        implicitRandom: RandomSource,
    ): Capture? {
        if (ratingY0 <= 0.0 || legacyCompetitionType != 1 || !isLegacyKonrentT) return null
        return Capture(
            ratingY0 = ratingY0,
            legacyG0 = legacyG0,
            randomOrder = implicitRandom.nextInt(100),
        )
    }
}
