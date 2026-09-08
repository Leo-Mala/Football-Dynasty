package com.leomala.footballdynasty.domain.career

/**
 * Exact pure projection of legacy `best.k0.a(best.o)` / `components.n1.f(double)`.
 *
 * The legacy owner keeps one `components.n1` per player identity in `best.k0.g`. An eligible call
 * adds `best.o.y0()` to the retained sum, increments the retained count by one, recomputes the
 * average as sum/count, and overwrites the raw selector with the value resolved from the player's
 * raw fields. Persistence and match-score production are deliberately owned by higher layers.
 */
object LegacyCompetitionPlayerAggregateRules {
    private val RAW_SELECTOR_BY_G0 = intArrayOf(
        -1, 0, 5, 2, 2, 2, 2, 2, 2, 1, 3, 6, 6, 6, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4,
    )

    data class PlayerInput(
        val playerId: String,
        val legacyL0: Int,
        val legacyF0: Int,
        val legacyG0: Int,
        val legacyR: Int,
        val matchValueY0: Double,
    )

    data class Aggregate(
        val playerId: String,
        val rawSumC: Double,
        val rawCountD: Double,
        val rawAverageE: Double,
        val rawSelectorF: Int,
    )

    /**
     * Applies one already-reached legacy `k0.a(player)` call.
     *
     * An ineligible player leaves the aggregate untouched, exactly as the legacy early return does.
     * The caller is responsible for the outer `best.o.n(...)` gate that only calls `k0.a` when
     * `y0() > 0`, `B() != null` and `B().E() == 1`.
     */
    fun apply(
        existing: Aggregate?,
        input: PlayerInput,
    ): Aggregate? {
        require(input.playerId.isNotBlank()) { "Player id must not be blank" }
        require(existing == null || existing.playerId == input.playerId) {
            "Legacy k0 aggregate identity changed from ${existing?.playerId} to ${input.playerId}"
        }
        if (!isEligible(input)) return existing

        // The legacy indexes a fixed int[26] directly; preserving the direct access also preserves
        // fail-fast behavior for an invalid raw g0 rather than inventing a fallback selector.
        val mappedSelector = RAW_SELECTOR_BY_G0[input.legacyG0]
        val selector = when {
            mappedSelector == 3 && input.legacyR == 0 -> 6
            mappedSelector == 3 && input.legacyR == 1 -> 3
            mappedSelector == 5 && input.legacyL0 == 3 -> 3
            mappedSelector == 1 && input.legacyL0 == 3 -> 3
            else -> mappedSelector
        }

        val previousSum = existing?.rawSumC ?: 0.0
        val previousCount = existing?.rawCountD ?: 0.0
        val sum = previousSum + input.matchValueY0
        val count = previousCount + 1.0
        return Aggregate(
            playerId = input.playerId,
            rawSumC = sum,
            rawCountD = count,
            rawAverageE = sum / count,
            rawSelectorF = selector,
        )
    }

    fun isEligible(input: PlayerInput): Boolean {
        val initiallyEligible = when {
            input.legacyL0 == 0 && input.legacyG0 != 1 -> false
            input.legacyL0 == 1 && input.legacyF0 == 0 && input.legacyG0 != 9 -> false
            input.legacyL0 == 1 && input.legacyF0 == 1 && input.legacyG0 != 2 -> false
            input.legacyL0 == 2 -> input.legacyG0 in 3..8
            input.legacyL0 == 3 -> input.legacyG0 in 10..17
            input.legacyL0 == 4 -> input.legacyG0 >= 18
            else -> true
        }

        if (input.legacyL0 == 1 && input.legacyF0 == 0 && input.legacyG0 == 17) return true
        if (input.legacyL0 == 1 && input.legacyF0 == 1 && input.legacyG0 == 10) return true
        return initiallyEligible
    }
}
