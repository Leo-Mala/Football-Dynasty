package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * End-to-end pure execution plan for reachable legacy `konrent.b0.V()`.
 *
 * This composes the already-proven prior-participant callbacks, fallback `J>1` pool, tier fills,
 * fixed permutation and exact `konrent.a0`/`konrent.f0.b(...)` construction arguments. Legacy
 * implicit `Collections.shuffle` and local `new Random().nextInt(3)` are both routed through the
 * project RandomSource, as required by AGENTS.md. No implicit-seed bit-parity claim is made.
 *
 * `a0`/`f0` durability belongs to the Phase 16 persistence audit; Phase 15 only freezes the
 * reachable functional state/construction contract.
 */
object LegacyTournamentBootstrapExecutionRules {
    data class A0ConstructionPlan(
        val participantCount: Int,
        val legacyK0E: Int,
        val rawFourthArgument: Int = 1,
        val flags: List<Boolean> = List(7) { false },
        val rawLastArgument: Int = 150,
    )

    data class F0BuildCall(
        val rawIndex: Int = 0,
        val rawBoolean1: Boolean = false,
        val rawInt1: Int = 0,
        val rawInt2: Int = 0,
        val legacyK0E: Int,
        val rawBoolean2: Boolean = false,
    )

    data class Construction<T>(
        val legacyNextInt3: Int,
        val participants: List<T>,
        val a0: A0ConstructionPlan,
        val f0: F0BuildCall,
    )

    data class Result<T>(
        val prior: LegacyTournamentBootstrapRules.PriorParticipantRecovery<T>,
        val shuffledFallbackCandidates: List<T>,
        val tiers: LegacyTournamentBootstrapRules.TierSlots<T>,
        val construction: Construction<T>?,
    )

    fun <T : Any> execute(
        random: RandomSource,
        y0: LegacyTournamentBootstrapRules.PriorCompetition<T>,
        v0: LegacyTournamentBootstrapRules.PriorCompetition<T>,
        x0: LegacyTournamentBootstrapRules.PriorCompetition<T>,
        w0: LegacyTournamentBootstrapRules.PriorCompetition<T>,
        z0: LegacyTournamentBootstrapRules.PriorCompetition<T>,
        b0: LegacyTournamentBootstrapRules.PriorCompetition<T>,
        fallbackSource: List<T>,
        legacyK0E: Int,
        rawJ: (T) -> Int,
        rawJ0: (T) -> Int,
    ): Result<T> {
        val prior =
            LegacyTournamentBootstrapRules.recoverPriorParticipants(
                y0 = y0,
                v0 = v0,
                x0 = x0,
                w0 = w0,
                z0 = z0,
                b0 = b0,
                rawJ0 = rawJ0,
            )

        val fallback =
            LegacyTournamentBootstrapRules.eligibleCandidates(fallbackSource, rawJ).toMutableList()
        if (fallback.isNotEmpty()) {
            LegacyAnnualRandomRules.shuffleInPlace(fallback, random)
        }

        val tiers =
            LegacyTournamentBootstrapRules.fillMissingTierSlots(
                shuffledCandidates = fallback,
                initial =
                    LegacyTournamentBootstrapRules.TierSlots(
                        tier2 = prior.g,
                        tier3 = prior.h,
                        tier4 = prior.j,
                        tier5 = prior.i,
                    ),
                rawJ = rawJ,
            )

        val e = prior.e
        val f = prior.f
        val g = tiers.tier2
        val h = tiers.tier3
        val j = tiers.tier4
        val i = tiers.tier5
        val construction =
            if (e != null && f != null && g != null && h != null && i != null && j != null) {
                // Exact base array in SMALI is [E, H, I, J]; G/F gate construction but are not in it.
                val base = listOf(e, h, i, j)
                val draw = random.nextInt(3)
                val order = LegacyTournamentBootstrapRules.fourClubOrder(draw)
                val participants = order.map { index -> base[index] }
                Construction(
                    legacyNextInt3 = draw,
                    participants = participants,
                    a0 =
                        A0ConstructionPlan(
                            participantCount = participants.size,
                            legacyK0E = legacyK0E,
                        ),
                    f0 = F0BuildCall(legacyK0E = legacyK0E),
                )
            } else {
                null
            }

        return Result(
            prior = prior,
            shuffledFallbackCandidates = fallback,
            tiers = tiers,
            construction = construction,
        )
    }
}
