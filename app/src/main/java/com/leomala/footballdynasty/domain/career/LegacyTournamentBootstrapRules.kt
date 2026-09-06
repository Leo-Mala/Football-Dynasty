package com.leomala.footballdynasty.domain.career

/**
 * Pure executable fragments recovered from legacy `konrent.b0.V()`.
 *
 * The legacy method first builds a candidate list from clubs whose raw `J()` is greater than 1,
 * shuffles that list with `Collections.shuffle`, then fills only still-null tier slots for raw
 * `J()==2/3/4/5` with the first matching candidate encountered. A later local
 * `java.util.Random().nextInt(3)` chooses one of three fixed four-club index orders.
 *
 * This boundary deliberately accepts the already-shuffled candidate order. It does not claim
 * ownership of the legacy unseeded `Collections.shuffle` or local `Random` lifecycle.
 */
object LegacyTournamentBootstrapRules {
    data class TierSlots<T>(
        val tier2: T?,
        val tier3: T?,
        val tier4: T?,
        val tier5: T?,
    )

    fun <T> eligibleCandidates(
        source: List<T>,
        rawJ: (T) -> Int,
    ): List<T> = source.filter { rawJ(it) > 1 }

    fun <T> fillMissingTierSlots(
        shuffledCandidates: List<T>,
        initial: TierSlots<T>,
        rawJ: (T) -> Int,
    ): TierSlots<T> {
        var tier2 = initial.tier2
        var tier3 = initial.tier3
        var tier4 = initial.tier4
        var tier5 = initial.tier5

        for (candidate in shuffledCandidates) {
            when (rawJ(candidate)) {
                2 -> if (tier2 == null) tier2 = candidate
                3 -> if (tier3 == null) tier3 = candidate
                4 -> if (tier4 == null) tier4 = candidate
                5 -> if (tier5 == null) tier5 = candidate
            }

            if (tier2 != null && tier3 != null && tier4 != null && tier5 != null) break
        }

        return TierSlots(tier2, tier3, tier4, tier5)
    }

    fun fourClubOrder(legacyNextInt3: Int): IntArray =
        when (legacyNextInt3) {
            0 -> intArrayOf(2, 0, 3, 1)
            1 -> intArrayOf(3, 2, 1, 0)
            2 -> intArrayOf(0, 2, 3, 1)
            else -> error("legacyNextInt3 must be a java.util.Random.nextInt(3) result")
        }
}
