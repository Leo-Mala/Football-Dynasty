package com.leomala.footballdynasty.domain.competition

/** Exact large fixed fixture matrices used by legacy `best.j.e/f`. */
object LegacyLargeFixedLeagueFixtureRules {
    fun legacyENineteenClubs(clubIds: List<String>): List<List<LegacyLeagueFixtureRules.Fixture>> {
        val first = mapFlat(
            clubIds = clubIds,
            expectedClubs = 19,
            rounds = 19,
            matchesPerRound = 9,
            flat = NINETEEN_ONE_BASED,
            oneBased = true,
        )
        return first + first.map { round ->
            round.map { fixture ->
                LegacyLeagueFixtureRules.Fixture(fixture.awayClubId, fixture.homeClubId)
            }
        }
    }

    fun legacyFTwentyFiveClubs(clubIds: List<String>): List<List<LegacyLeagueFixtureRules.Fixture>> =
        mapFlat(
            clubIds = clubIds,
            expectedClubs = 25,
            rounds = 25,
            matchesPerRound = 12,
            flat = TWENTY_FIVE_ZERO_BASED,
        )

    private fun mapFlat(
        clubIds: List<String>,
        expectedClubs: Int,
        rounds: Int,
        matchesPerRound: Int,
        flat: IntArray,
        oneBased: Boolean = false,
    ): List<List<LegacyLeagueFixtureRules.Fixture>> {
        require(clubIds.size == expectedClubs) { "Legacy matrix requires $expectedClubs clubs" }
        require(clubIds.distinct().size == clubIds.size) { "Club ids must be unique" }
        require(clubIds.none { it.isBlank() }) { "Club ids must not be blank" }
        require(flat.size == rounds * matchesPerRound * 2) { "Legacy fixture matrix shape changed" }

        var cursor = 0
        return List(rounds) {
            List(matchesPerRound) {
                val home = flat[cursor++] - if (oneBased) 1 else 0
                val away = flat[cursor++] - if (oneBased) 1 else 0
                LegacyLeagueFixtureRules.Fixture(clubIds[home], clubIds[away])
            }
        }
    }

    // `best.j.f4250a`, one-based; `best.j.e` appends its exact home/away reverse.
    private val NINETEEN_ONE_BASED = intArrayOf(
        1, 2, 19, 4, 18, 5, 17, 6, 16, 7, 15, 8, 14, 9, 13, 10, 12, 11, 1, 3, 2, 4, 19, 6,
        18, 7, 17, 8, 16, 9, 15, 10, 14, 11, 13, 12, 1, 4, 3, 5, 2, 6, 19, 8, 18, 9, 17, 10,
        16, 11, 15, 12, 14, 13, 1, 5, 4, 6, 3, 7, 2, 8, 19, 10, 18, 11, 17, 12, 16, 13, 15, 14,
        1, 6, 5, 7, 4, 8, 3, 9, 2, 10, 19, 12, 18, 13, 17, 14, 16, 15, 1, 7, 6, 8, 5, 9,
        4, 10, 3, 11, 2, 12, 19, 14, 18, 15, 17, 16, 1, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13,
        2, 14, 19, 16, 18, 17, 1, 9, 8, 10, 7, 11, 6, 12, 5, 13, 4, 14, 3, 15, 2, 16, 19, 18,
        1, 10, 9, 11, 8, 12, 7, 13, 6, 14, 5, 15, 4, 16, 3, 17, 2, 18, 1, 11, 10, 12, 9, 13,
        8, 14, 7, 15, 6, 16, 5, 17, 4, 18, 3, 19, 1, 12, 11, 13, 10, 14, 9, 15, 8, 16, 7, 17,
        6, 18, 5, 19, 3, 2, 1, 13, 12, 14, 11, 15, 10, 16, 9, 17, 8, 18, 7, 19, 5, 2, 4, 3,
        1, 14, 13, 15, 12, 16, 11, 17, 10, 18, 9, 19, 7, 2, 6, 3, 5, 4, 1, 15, 14, 16, 13, 17,
        12, 18, 11, 19, 9, 2, 8, 3, 7, 4, 6, 5, 1, 16, 15, 17, 14, 18, 13, 19, 11, 2, 10, 3,
        9, 4, 8, 5, 7, 6, 1, 17, 16, 18, 15, 19, 13, 2, 12, 3, 11, 4, 10, 5, 9, 6, 8, 7,
        1, 18, 17, 19, 15, 2, 14, 3, 13, 4, 12, 5, 11, 6, 10, 7, 9, 8, 1, 19, 17, 2, 16, 3,
        15, 4, 14, 5, 13, 6, 12, 7, 11, 8, 10, 9, 19, 2, 18, 3, 17, 4, 16, 5, 15, 6, 14, 7,
        13, 8, 12, 9, 11, 10,
    )

    // `best.j.f4251b`, zero-based; `best.j.f` uses the 25 rounds exactly once.
    private val TWENTY_FIVE_ZERO_BASED = intArrayOf(
        1, 24, 2, 23, 3, 22, 4, 21, 5, 20, 6, 19, 7, 18, 8, 17, 9, 16, 10, 15, 11, 14, 12, 13,
        14, 12, 15, 11, 16, 10, 17, 9, 18, 8, 19, 7, 20, 6, 21, 5, 22, 4, 23, 3, 24, 2, 0, 1,
        2, 0, 3, 24, 4, 23, 5, 22, 6, 21, 7, 20, 8, 19, 9, 18, 10, 17, 11, 16, 12, 15, 13, 14,
        15, 13, 16, 12, 17, 11, 18, 10, 19, 9, 20, 8, 21, 7, 22, 6, 23, 5, 24, 4, 0, 3, 1, 2,
        3, 1, 4, 0, 5, 24, 6, 23, 7, 22, 8, 21, 9, 20, 10, 19, 11, 18, 12, 17, 13, 16, 14, 15,
        16, 14, 17, 13, 18, 12, 19, 11, 20, 10, 21, 9, 22, 8, 23, 7, 24, 6, 0, 5, 1, 4, 2, 3,
        4, 2, 5, 1, 6, 0, 7, 24, 8, 23, 9, 22, 10, 21, 11, 20, 12, 19, 13, 18, 14, 17, 15, 16,
        17, 15, 18, 14, 19, 13, 20, 12, 21, 11, 22, 10, 23, 9, 24, 8, 0, 7, 1, 6, 2, 5, 3, 4,
        5, 3, 6, 2, 7, 1, 8, 0, 9, 24, 10, 23, 11, 22, 12, 21, 13, 20, 14, 19, 15, 18, 16, 17,
        18, 16, 19, 15, 20, 14, 21, 13, 22, 12, 23, 11, 24, 10, 0, 9, 1, 8, 2, 7, 3, 6, 4, 5,
        6, 4, 7, 3, 8, 2, 9, 1, 10, 0, 11, 24, 12, 23, 13, 22, 14, 21, 15, 20, 16, 19, 17, 18,
        19, 17, 20, 16, 21, 15, 22, 14, 23, 13, 24, 12, 0, 11, 1, 10, 2, 9, 3, 8, 4, 7, 5, 6,
        7, 5, 8, 4, 9, 3, 10, 2, 11, 1, 12, 0, 13, 24, 14, 23, 15, 22, 16, 21, 17, 20, 18, 19,
        20, 18, 21, 17, 22, 16, 23, 15, 24, 14, 0, 13, 1, 12, 2, 11, 3, 10, 4, 9, 5, 8, 6, 7,
        8, 6, 9, 5, 10, 4, 11, 3, 12, 2, 13, 1, 14, 0, 15, 24, 16, 23, 17, 22, 18, 21, 19, 20,
        21, 19, 22, 18, 23, 17, 24, 16, 0, 15, 1, 14, 2, 13, 3, 12, 4, 11, 5, 10, 6, 9, 7, 8,
        9, 7, 10, 6, 11, 5, 12, 4, 13, 3, 14, 2, 15, 1, 16, 0, 17, 24, 18, 23, 19, 22, 20, 21,
        22, 20, 23, 19, 24, 18, 0, 17, 1, 16, 2, 15, 3, 14, 4, 13, 5, 12, 6, 11, 7, 10, 8, 9,
        10, 8, 11, 7, 12, 6, 13, 5, 14, 4, 15, 3, 16, 2, 17, 1, 18, 0, 19, 24, 20, 23, 21, 22,
        23, 21, 24, 20, 0, 19, 1, 18, 2, 17, 3, 16, 4, 15, 5, 14, 6, 13, 7, 12, 8, 11, 9, 10,
        11, 9, 12, 8, 13, 7, 14, 6, 15, 5, 16, 4, 17, 3, 18, 2, 19, 1, 20, 0, 21, 24, 22, 23,
        24, 22, 0, 21, 1, 20, 2, 19, 3, 18, 4, 17, 5, 16, 6, 15, 7, 14, 8, 13, 9, 12, 10, 11,
        12, 10, 13, 9, 14, 8, 15, 7, 16, 6, 17, 5, 18, 4, 19, 3, 20, 2, 21, 1, 22, 0, 23, 24,
        0, 23, 1, 22, 2, 21, 3, 20, 4, 19, 5, 18, 6, 17, 7, 16, 8, 15, 9, 14, 10, 13, 11, 12,
        13, 11, 14, 10, 15, 9, 16, 8, 17, 7, 18, 6, 19, 5, 20, 4, 21, 3, 22, 2, 23, 1, 24, 0,
    )
}
