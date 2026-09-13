package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime
import com.leomala.footballdynasty.domain.model.Match
import com.leomala.footballdynasty.foundation.random.RandomSource

typealias CareerMatchTransientState = LegacyMatchTransientRuntime.State<
    CareerMatchPersistedRuntimeResolver.PersistedClubRoster,
    CareerMatchPersistedRuntimeResolver.PersistedPlayer,
>

/**
 * Small Phase 17 production composition seam around the already-certified Phase 8 match runtime.
 *
 * This class deliberately owns no persistence and no RNG. [random] is the exact source restored by
 * [com.leomala.footballdynasty.domain.career.CareerMatchRuntimeBridge] and must be forwarded to the
 * certified legacy runtime without replacement or parallel draws.
 */
class CareerMatchRuntimeComposition private constructor(
    private val simulator: ProductiveSimulator?,
) {
    fun interface ProductiveSimulator {
        fun simulate(
            scheduled: ScheduledCareerMatch,
            state: CareerMatchTransientState,
            homeTacticIndex: Int,
            awayTacticIndex: Int,
            random: RandomSource,
        ): Match
    }

    val available: Boolean
        get() = simulator != null

    fun simulate(
        scheduled: ScheduledCareerMatch,
        state: CareerMatchTransientState,
        homeTacticIndex: Int,
        awayTacticIndex: Int,
        random: RandomSource,
    ): Match {
        val wired = requireNotNull(simulator) {
            "Productive legacy match runtime composition is not wired"
        }
        return wired.simulate(
            scheduled = scheduled,
            state = state,
            homeTacticIndex = homeTacticIndex,
            awayTacticIndex = awayTacticIndex,
            random = random,
        )
    }

    companion object {
        fun unwired(): CareerMatchRuntimeComposition = CareerMatchRuntimeComposition(null)

        fun wired(simulator: ProductiveSimulator): CareerMatchRuntimeComposition =
            CareerMatchRuntimeComposition(simulator)
    }
}
