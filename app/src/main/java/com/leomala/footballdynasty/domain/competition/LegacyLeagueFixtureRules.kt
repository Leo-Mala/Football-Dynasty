package com.leomala.footballdynasty.domain.competition

/**
 * Exact, persistence-independent port of legacy `best.j.b(konrent.t, ArrayList<c0>, int)`.
 *
 * The legacy method stores each pairing in positions 0 and 3 of a four-slot array. This modern
 * boundary exposes only those two meaningful positions as home/away ids, while preserving the
 * original rotation, round reordering, home/away reversal, cycle repetition and odd-size quirks.
 *
 * Important parity detail: when the input size is odd, legacy appends a null slot but continues to
 * use the original size when computing modulo indices and the fixed opponent. This is intentionally
 * preserved rather than replaced by a conventional bye algorithm.
 */
object LegacyLeagueFixtureRules {
    data class Fixture(
        val homeClubId: String?,
        val awayClubId: String?,
    )

    fun generate(
        clubIds: List<String>,
        legacyCycleCode: Int,
    ): List<List<Fixture>> {
        require(clubIds.distinct().size == clubIds.size) { "Club ids must be unique" }
        require(clubIds.none { it.isBlank() }) { "Club ids must not be blank" }

        val originalSize = clubIds.size
        if (originalSize == 0) return emptyList()

        val padded = clubIds.map<String, String?> { it }.toMutableList()
        if (originalSize % 2 == 1) padded += null

        val roundCount = padded.size - 1
        val matchesPerRound = padded.size / 2
        val firstPass = MutableList(roundCount) { MutableList(matchesPerRound) { Fixture(null, null) } }

        for (round in 0 until roundCount) {
            for (match in 0 until matchesPerRound) {
                // `best.j.b` keeps using the original pre-padding size here.
                val legacyLastIndex = originalSize - 1
                var home = padded[(round + match) % legacyLastIndex]
                var away = padded[((legacyLastIndex - match) + round) % legacyLastIndex]
                if (match == 0) away = padded[legacyLastIndex]
                firstPass[round][match] = Fixture(home, away)
            }
        }

        val reordered = MutableList(roundCount) { emptyList<Fixture>() }
        var frontIndex = 0
        var midpointIndex = padded.size / 2
        for (round in firstPass.indices) {
            if (round % 2 == 0) {
                reordered[round] = firstPass[frontIndex].toList()
                frontIndex += 1
            } else {
                reordered[round] = firstPass[midpointIndex].toList()
                midpointIndex += 1
            }
        }

        for (round in reordered.indices) {
            if (round % 2 == 1 && reordered[round].isNotEmpty()) {
                val copy = reordered[round].toMutableList()
                val first = copy[0]
                copy[0] = Fixture(first.awayClubId, first.homeClubId)
                reordered[round] = copy
            }
        }

        val reversed = reordered.map { round ->
            round.map { fixture -> Fixture(fixture.awayClubId, fixture.homeClubId) }
        }

        val cycleCount = when (legacyCycleCode) {
            2 -> 2
            3 -> 3
            4 -> 4
            else -> 1
        }
        return buildList {
            repeat(cycleCount) { cycle ->
                addAll(if (cycle % 2 == 0) reordered else reversed)
            }
        }
    }
}
