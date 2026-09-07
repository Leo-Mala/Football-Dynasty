package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Pure executable characterization of reachable youth-team rules from the official Phase 15
 * Brasfoot corpus. SMALI is authoritative where Java decompilation diverges.
 *
 * This object deliberately does not persist or materialize players. Its purpose is to keep the
 * proven trial/development/annual-decision order isolated so the later Room boundary can store the
 * exact pre-promotion `best.p` state without moving RNG draws earlier in the career.
 */
object LegacyJuniorRuntimeRules {
    const val MAX_JUNIORS: Int = 18
    const val MAX_SENIORS_FOR_MANUAL_PROMOTION: Int = 30
    const val TRIAL_EXPENSE_RAW_CODE: Int = 9

    private val trialPositions = intArrayOf(0, 1, 2, 3, 3, 4)
    private val annualPotentialThresholdByClubP0 = intArrayOf(1, 4, 5, 6, 6, 6)
    private val annualMinimumSeniorCountByPosition = intArrayOf(3, 5, 5, 8, 6)

    enum class TrialAvailability {
        READY,
        INSUFFICIENT_CASH,
        JUNIOR_LIMIT_REACHED,
    }

    data class DevelopmentState(
        val age: Int,
        val legacyN: Int,
        val legacyO: Int,
        val remainder: Double,
    )

    data class AnnualContext(
        val clubP0: Int,
        val seniorPositionCounts: List<Int>,
        val seniorCount: Int,
        val clubB0: Int,
        val clubQ0: Boolean,
    )

    enum class AnnualAction {
        NONE,
        PROMOTE,
        PROMOTE_AND_STAGE_REPLACEMENT,
        REFRESH_DRAFT,
    }

    data class AnnualDecision(
        val ageAfterIncrement: Int,
        val action: AnnualAction,
    )

    /** `ActivityJuniores.j()`: the cash check happens before the 18-player youth limit check. */
    fun trialAvailability(cash: Int, cost: Int, juniorCount: Int): TrialAvailability {
        require(cost >= 0)
        require(juniorCount >= 0)
        return when {
            cash < cost -> TrialAvailability.INSUFFICIENT_CASH
            juniorCount < MAX_JUNIORS -> TrialAvailability.READY
            else -> TrialAvailability.JUNIOR_LIMIT_REACHED
        }
    }

    /**
     * Exact outer RNG/control order of SMALI `best.b.h2(c0)`.
     *
     * Every one of the six gate draws occurs. A successful gate invokes [generate] immediately,
     * so all RNG consumed inside legacy `best.p.d(...)` is interleaved before the next gate draw.
     * This is important: collecting the six gate results first would change the career RNG stream.
     */
    fun <T> executeTrial(
        random: RandomSource,
        currentJuniorCount: Int,
        generate: (requestedLegacyE: Int, random: RandomSource) -> T,
    ): List<T> {
        require(currentJuniorCount >= 0)
        var juniorCount = currentJuniorCount
        val generated = mutableListOf<T>()
        for (requestedLegacyE in trialPositions) {
            val gate = random.nextInt(3)
            if (gate == 0 && juniorCount < MAX_JUNIORS) {
                generated += generate(requestedLegacyE, random)
                juniorCount++
            }
        }
        return generated
    }

    /** Manual `ActivityJuniores.k()/i()` promotion is blocked at 30 senior players. */
    fun canPromoteManually(seniorCount: Int): Boolean {
        require(seniorCount >= 0)
        return seniorCount < MAX_SENIORS_FOR_MANUAL_PROMOTION
    }

    /**
     * Exact executable body of `best.p.b()`.
     *
     * The legacy bytecode uses a strict `> 1.0` promotion of the fractional remainder, not `>=`.
     * Players older than 20 are unchanged by this method.
     */
    fun progressDevelopment(state: DevelopmentState): DevelopmentState {
        if (state.age > 20) return state

        val ageIncrement = when {
            state.age <= 17 -> 0.5
            state.age == 18 -> 0.375
            state.age == 19 -> 0.35
            state.age == 20 -> 0.125
            else -> 0.01
        }
        val potentialIncrement = when {
            state.legacyN <= 3 -> 0.03
            state.legacyN <= 6 -> 0.04
            state.legacyN <= 8 -> 0.07
            state.legacyN == 9 -> 0.10
            state.legacyN == 10 -> 0.11
            else -> 0.0
        }

        var remainder = state.remainder + ageIncrement + potentialIncrement
        var legacyO = state.legacyO
        if (remainder > 1.0 && legacyO < 100) {
            legacyO++
            remainder -= 1.0
        }
        return state.copy(legacyO = legacyO, remainder = remainder)
    }

    /**
     * Exact branch decision from SMALI `best.p.c(c0)` after its unconditional age increment.
     *
     * `PROMOTE` here refers to legacy `best.t.e(TRUE, p, club)`, which is intentionally distinct
     * from the manual activity path `best.t.e(FALSE, p, club)`. Materialization is not performed by
     * this pure rule because the TRUE path still needs its own field/RNG audit.
     */
    fun annualDecision(
        age: Int,
        legacyN: Int,
        legacyE: Int,
        context: AnnualContext?,
    ): AnnualDecision {
        val ageAfterIncrement = age + 1
        if (context == null || ageAfterIncrement < 20) {
            return AnnualDecision(ageAfterIncrement, AnnualAction.NONE)
        }
        require(context.clubP0 in annualPotentialThresholdByClubP0.indices)
        require(legacyE in annualMinimumSeniorCountByPosition.indices)
        require(context.seniorPositionCounts.size > legacyE)
        require(context.seniorCount >= 0)

        val qualifiesForPromotion =
            legacyN >= annualPotentialThresholdByClubP0[context.clubP0] &&
                context.seniorPositionCounts[legacyE] < annualMinimumSeniorCountByPosition[legacyE]

        val action = when {
            qualifiesForPromotion && context.seniorCount < MAX_SENIORS_FOR_MANUAL_PROMOTION -> {
                if (context.clubB0 < 10) {
                    AnnualAction.PROMOTE_AND_STAGE_REPLACEMENT
                } else {
                    AnnualAction.PROMOTE
                }
            }
            qualifiesForPromotion -> AnnualAction.NONE
            !context.clubQ0 -> AnnualAction.REFRESH_DRAFT
            else -> AnnualAction.NONE
        }
        return AnnualDecision(ageAfterIncrement, action)
    }
}
