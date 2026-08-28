package com.leomala.footballdynasty.domain.competition

/** Exact fixed fixture matrices used by legacy `best.j.g/h/i/j`. */
object LegacyFixedLeagueFixtureRules {
    fun legacyGThreeClubs(clubIds: List<String>): List<List<LegacyLeagueFixtureRules.Fixture>> =
        mapFlat(clubIds, expectedClubs = 3, rounds = 6, matchesPerRound = 1, flat = THREE)

    fun legacyHFiveClubs(clubIds: List<String>): List<List<LegacyLeagueFixtureRules.Fixture>> =
        mapFlat(clubIds, expectedClubs = 5, rounds = 10, matchesPerRound = 2, flat = FIVE)

    fun legacyIFiveClubs(clubIds: List<String>): List<List<LegacyLeagueFixtureRules.Fixture>> =
        mapFlat(clubIds, expectedClubs = 5, rounds = 5, matchesPerRound = 2, flat = FIVE.copyOf(20))

    fun legacyJNineClubs(
        clubIds: List<String>,
        reverseSecondCycle: Boolean,
    ): List<List<LegacyLeagueFixtureRules.Fixture>> {
        val first = mapFlat(
            clubIds = clubIds,
            expectedClubs = 9,
            rounds = 9,
            matchesPerRound = 4,
            flat = NINE_ONE_BASED,
            oneBased = true,
        )
        if (!reverseSecondCycle) return first
        return first + first.map { round ->
            round.map { fixture ->
                LegacyLeagueFixtureRules.Fixture(fixture.awayClubId, fixture.homeClubId)
            }
        }
    }

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
                val rawHome = flat[cursor++] - if (oneBased) 1 else 0
                val rawAway = flat[cursor++] - if (oneBased) 1 else 0
                LegacyLeagueFixtureRules.Fixture(clubIds[rawHome], clubIds[rawAway])
            }
        }
    }

    // `best.j.f4257h`, zero-based.
    private val THREE = intArrayOf(
        2, 0, 1, 2, 1, 0, 0, 2, 2, 1, 0, 1,
    )

    // `best.j.f4258i`, zero-based. `best.j.i` uses only the first five rounds.
    private val FIVE = intArrayOf(
        3, 2, 1, 0, 1, 4, 0, 2, 2, 1, 4, 3, 0, 3, 2, 4,
        4, 0, 3, 1, 2, 3, 0, 1, 4, 1, 2, 0, 1, 2, 3, 4,
        3, 0, 4, 2, 0, 4, 1, 3,
    )

    // `best.j.f4256g`, one-based in the legacy table.
    private val NINE_ONE_BASED = intArrayOf(
        1, 6, 2, 7, 3, 8, 4, 9, 1, 2, 3, 6, 4, 7, 5, 8,
        1, 3, 4, 2, 5, 6, 9, 8, 1, 4, 5, 3, 9, 6, 8, 7,
        1, 5, 9, 3, 7, 6, 8, 2, 9, 5, 8, 4, 7, 3, 6, 2,
        1, 9, 7, 5, 2, 3, 6, 4, 1, 8, 7, 9, 2, 5, 3, 4,
        1, 7, 6, 8, 2, 9, 4, 5,
    )
}
