package com.leomala.footballdynasty.domain.career

/**
 * Pure projection of reachable legacy `best.k0.c(index)`.
 *
 * The official SMALI proves all behavior modeled here:
 * - `U()` sorts `components.n1` by raw average (`e`) descending and then raw count (`d`)
 *   descending;
 * - `konrent.t.D0()` derives the inclusive count threshold from raw `U`, `H.size` and `a0`;
 * - selectors are visited in the exact fixed order `[0, 1, 2, 2, 5, 6, 6, 3, 3, 4, 4]`;
 * - each selector picks the first sorted entry with that selector, `count >= threshold`, and a
 *   player not already picked for the current `best.h0`; duplicates therefore advance to the
 *   next distinct eligible player;
 * - `best.h0` keeps the selected player together with that player's club;
 * - `k0.i` receives the first sorted player whose count meets the same inclusive threshold;
 * - only when raw `k0.b == 1`, `index == 0`, and that player exists does the legacy call
 *   `best.o.o1(TRUE)`. The caller owns that mutation; this rule returns an explicit signal.
 *
 * Raw names are intentionally retained because the corpus does not prove sporting semantics for
 * these fields/selectors. This boundary does not claim persistence semantics for `best.k0`,
 * `components.n1` or `best.h0`.
 */
object LegacyAnnualTournamentEntryResetRules {
    val SELECTOR_SEQUENCE: List<Int> = listOf(0, 1, 2, 2, 5, 6, 6, 3, 3, 4, 4)

    data class Action(
        val ordinal: Int,
        val selector: Int,
    )

    data class RawEntry<Player, Club>(
        val player: Player,
        val club: Club,
        val rawSumC: Double,
        val rawCountD: Double,
        val rawAverageE: Double,
        val rawSelectorF: Int,
    )

    data class SelectedEntry<Player, Club>(
        val player: Player,
        val club: Club,
        val selector: Int,
    )

    data class Result<Player, Club>(
        val threshold: Int,
        val sortedEntries: List<RawEntry<Player, Club>>,
        val selectedEntries: List<SelectedEntry<Player, Club>>,
        val firstThresholdPlayer: Player?,
        val markFirstThresholdPlayer: Boolean,
    )

    fun planSelectorTraversal(): List<Action> =
        SELECTOR_SEQUENCE.mapIndexed { ordinal, selector ->
            Action(
                ordinal = ordinal,
                selector = selector,
            )
        }

    /** Exact integer-effective projection of reachable `konrent.t.D0()`. */
    fun calculateThreshold(
        rawU: Int,
        rawHSize: Int,
        rawA0: Int,
    ): Int {
        var rawV0 =
            if (rawA0 == 0) {
                (rawHSize - 1) * 2
            } else {
                ((rawHSize / rawA0) * 2) - 1
            }

        rawV0 /= 2
        return if (rawU > rawV0) rawU / 2 else 0
    }

    fun <Player, Club> apply(
        index: Int,
        rawB: Int,
        rawU: Int,
        rawHSize: Int,
        rawA0: Int,
        entries: List<RawEntry<Player, Club>>,
    ): Result<Player, Club> {
        val threshold = calculateThreshold(rawU = rawU, rawHSize = rawHSize, rawA0 = rawA0)
        val sorted =
            entries.sortedWith(
                Comparator { left, right ->
                    val byAverage = java.lang.Double.compare(right.rawAverageE, left.rawAverageE)
                    if (byAverage != 0) {
                        byAverage
                    } else {
                        java.lang.Double.compare(right.rawCountD, left.rawCountD)
                    }
                },
            )
        val selectedPlayers = mutableListOf<Player>()
        val selectedEntries = buildList {
            SELECTOR_SEQUENCE.forEach { selector ->
                val candidate =
                    sorted.firstOrNull { entry ->
                        entry.rawSelectorF == selector &&
                            entry.rawCountD >= threshold.toDouble() &&
                            !selectedPlayers.contains(entry.player)
                    }

                if (candidate != null) {
                    selectedPlayers += candidate.player
                    add(
                        SelectedEntry(
                            player = candidate.player,
                            club = candidate.club,
                            selector = selector,
                        ),
                    )
                }
            }
        }

        val firstThresholdPlayer =
            sorted.firstOrNull { entry -> entry.rawCountD >= threshold.toDouble() }?.player

        return Result(
            threshold = threshold,
            sortedEntries = sorted,
            selectedEntries = selectedEntries,
            firstThresholdPlayer = firstThresholdPlayer,
            markFirstThresholdPlayer = rawB == 1 && index == 0 && firstThresholdPlayer != null,
        )
    }
}
