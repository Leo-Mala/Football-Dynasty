package com.leomala.footballdynasty.domain.competition

/**
 * Exact group-stage fixture matrices used by legacy `best.j.c/d/k/l`.
 *
 * Each four-int tuple is `(homeGroup, homeIndex, awayGroup, awayIndex)`, one-based exactly as
 * stored in the legacy APK. The modern port resolves those coordinates against the already-formed
 * group lists; it does not reshuffle or infer group membership.
 */
object LegacyGroupFixtureRules {
    fun legacyCTwoGroupsOfSix(groups: List<List<String>>): List<List<LegacyLeagueFixtureRules.Fixture>> =
        mapMatrix(groups, expectedGroups = 2, clubsPerGroup = 6, rounds = 6, matchesPerRound = 6, flat = GROUP_2X6_C)

    fun legacyDTwoGroupsOfEight(groups: List<List<String>>): List<List<LegacyLeagueFixtureRules.Fixture>> =
        mapMatrix(groups, expectedGroups = 2, clubsPerGroup = 8, rounds = 8, matchesPerRound = 8, flat = GROUP_2X8_D)

    fun legacyKFourGroupsOfFive(groups: List<List<String>>): List<List<LegacyLeagueFixtureRules.Fixture>> =
        mapMatrix(groups, expectedGroups = 4, clubsPerGroup = 5, rounds = 15, matchesPerRound = 10, flat = GROUP_4X5_K)

    fun legacyLFourGroupsOfFour(groups: List<List<String>>): List<List<LegacyLeagueFixtureRules.Fixture>> =
        mapMatrix(groups, expectedGroups = 4, clubsPerGroup = 4, rounds = 12, matchesPerRound = 8, flat = GROUP_4X4_L)

    private fun mapMatrix(
        groups: List<List<String>>,
        expectedGroups: Int,
        clubsPerGroup: Int,
        rounds: Int,
        matchesPerRound: Int,
        flat: IntArray,
    ): List<List<LegacyLeagueFixtureRules.Fixture>> {
        require(groups.size == expectedGroups) { "Legacy group matrix requires $expectedGroups groups" }
        require(groups.all { it.size == clubsPerGroup }) {
            "Legacy group matrix requires $clubsPerGroup clubs per group"
        }
        val allClubIds = groups.flatten()
        require(allClubIds.none { it.isBlank() }) { "Club ids must not be blank" }
        require(allClubIds.distinct().size == allClubIds.size) { "Club ids must be globally unique" }
        require(flat.size == rounds * matchesPerRound * 4) { "Legacy group fixture matrix shape changed" }

        var cursor = 0
        return List(rounds) {
            List(matchesPerRound) {
                val homeGroup = flat[cursor++] - 1
                val homeIndex = flat[cursor++] - 1
                val awayGroup = flat[cursor++] - 1
                val awayIndex = flat[cursor++] - 1
                LegacyLeagueFixtureRules.Fixture(
                    homeClubId = groups[homeGroup][homeIndex],
                    awayClubId = groups[awayGroup][awayIndex],
                )
            }
        }
    }

    // `best.j.f4254e` one-based group/club coordinates.
    private val GROUP_2X6_C = intArrayOf(
        2, 4, 1, 3, 2, 3, 1, 2, 2, 1, 1, 1, 2, 5, 1, 4, 2, 6, 1, 6, 2, 2, 1, 5,
        1, 1, 2, 5, 1, 6, 2, 2, 1, 2, 2, 6, 1, 4, 2, 1, 1, 5, 2, 4, 1, 3, 2, 3,
        2, 4, 1, 6, 2, 2, 1, 3, 2, 1, 1, 2, 2, 3, 1, 1, 2, 6, 1, 4, 2, 5, 1, 5,
        1, 1, 2, 4, 1, 6, 2, 3, 1, 3, 2, 6, 1, 2, 2, 5, 1, 5, 2, 1, 1, 4, 2, 2,
        2, 3, 1, 4, 2, 4, 1, 2, 2, 2, 1, 1, 2, 1, 1, 6, 2, 6, 1, 5, 2, 5, 1, 3,
        1, 1, 2, 6, 1, 2, 2, 2, 1, 4, 2, 4, 1, 6, 2, 5, 1, 3, 2, 1, 1, 5, 2, 3,
    )

    // `best.j.f4255f` one-based group/club coordinates.
    private val GROUP_2X8_D = intArrayOf(
        1, 8, 2, 6, 1, 6, 2, 7, 1, 3, 2, 1, 1, 1, 2, 3, 2, 4, 1, 2, 2, 5, 1, 5,
        2, 8, 1, 4, 2, 2, 1, 7, 1, 5, 2, 4, 1, 4, 2, 2, 1, 7, 2, 8, 1, 2, 2, 5,
        2, 3, 1, 6, 2, 7, 1, 1, 2, 1, 1, 8, 2, 6, 1, 3, 1, 8, 2, 8, 1, 6, 2, 4,
        1, 3, 2, 2, 1, 1, 2, 5, 2, 3, 1, 5, 2, 7, 1, 2, 2, 1, 1, 7, 2, 6, 1, 4,
        1, 4, 2, 7, 1, 5, 2, 1, 1, 2, 2, 6, 2, 5, 1, 3, 1, 7, 2, 3, 1, 8, 2, 4,
        2, 8, 1, 6, 2, 2, 1, 1, 1, 4, 2, 4, 1, 5, 2, 2, 1, 7, 2, 5, 1, 2, 2, 8,
        2, 3, 1, 3, 2, 7, 1, 8, 2, 1, 1, 1, 2, 6, 1, 6, 1, 6, 2, 2, 1, 3, 2, 4,
        1, 1, 2, 8, 2, 3, 1, 4, 2, 1, 1, 2, 2, 6, 1, 5, 1, 8, 2, 5, 2, 7, 1, 7,
        1, 8, 2, 3, 1, 6, 2, 1, 1, 3, 2, 7, 1, 1, 2, 6, 2, 4, 1, 7, 2, 5, 1, 4,
        2, 8, 1, 5, 2, 2, 1, 2, 1, 4, 2, 1, 1, 5, 2, 7, 1, 7, 2, 6, 1, 2, 2, 3,
        2, 4, 1, 1, 2, 5, 1, 6, 2, 8, 1, 3, 2, 2, 1, 8,
    )

    // `best.j.f4253d` one-based group/club coordinates.
    private val GROUP_4X5_K = intArrayOf(
        4, 3, 1, 1, 4, 1, 1, 5, 4, 2, 1, 4, 4, 5, 1, 3, 4, 4, 1, 2, 2, 3, 3, 1,
        2, 4, 3, 3, 2, 2, 3, 4, 2, 1, 3, 2, 2, 5, 3, 5, 1, 1, 4, 5, 1, 5, 4, 4,
        1, 3, 4, 3, 1, 4, 4, 1, 1, 2, 4, 2, 3, 1, 2, 5, 3, 4, 2, 3, 3, 5, 2, 4,
        3, 2, 2, 2, 3, 3, 2, 1, 1, 1, 4, 2, 1, 2, 4, 5, 4, 1, 1, 3, 4, 3, 1, 5,
        1, 4, 4, 4, 3, 1, 2, 1, 3, 4, 2, 5, 2, 3, 3, 5, 2, 4, 3, 2, 2, 2, 3, 3,
        4, 1, 1, 1, 4, 2, 1, 5, 4, 5, 1, 4, 4, 3, 1, 2, 4, 4, 1, 3, 3, 1, 2, 2,
        2, 1, 3, 5, 2, 5, 3, 2, 2, 3, 3, 3, 3, 4, 2, 4, 4, 4, 1, 1, 1, 4, 4, 3,
        1, 3, 4, 2, 1, 5, 4, 5, 1, 2, 4, 1, 2, 4, 3, 1, 3, 5, 2, 2, 2, 1, 3, 4,
        3, 3, 2, 5, 3, 2, 2, 3, 1, 1, 2, 3, 2, 2, 1, 4, 1, 3, 2, 4, 1, 5, 2, 1,
        2, 5, 1, 2, 4, 3, 3, 1, 3, 4, 4, 4, 3, 3, 4, 2, 3, 5, 4, 1, 4, 5, 3, 2,
        2, 2, 1, 1, 2, 3, 1, 2, 2, 5, 1, 3, 2, 4, 1, 5, 2, 1, 1, 4, 3, 1, 4, 5,
        4, 2, 3, 4, 3, 2, 4, 3, 4, 4, 3, 5, 4, 1, 3, 3, 1, 1, 2, 1, 1, 2, 2, 4,
        1, 3, 2, 3, 1, 4, 2, 5, 1, 5, 2, 2, 3, 1, 4, 4, 3, 2, 4, 2, 3, 4, 4, 1,
        3, 5, 4, 3, 3, 3, 4, 5, 1, 1, 2, 4, 2, 5, 1, 5, 2, 1, 1, 2, 2, 2, 1, 3,
        2, 3, 1, 4, 4, 1, 3, 1, 4, 3, 3, 3, 4, 4, 3, 2, 4, 2, 3, 5, 4, 5, 3, 4,
        2, 5, 1, 1, 2, 1, 1, 3, 1, 2, 2, 2, 1, 5, 2, 3, 2, 4, 1, 4, 3, 1, 4, 2,
        3, 2, 4, 1, 3, 4, 4, 3, 3, 5, 4, 5, 3, 3, 4, 4, 1, 1, 3, 2, 1, 3, 3, 4,
        1, 5, 3, 3, 1, 2, 3, 1, 1, 4, 3, 5, 4, 1, 2, 3, 4, 3, 2, 2, 4, 2, 2, 4,
        4, 5, 2, 1, 4, 4, 2, 5, 3, 1, 1, 1, 3, 2, 1, 3, 3, 5, 1, 2, 3, 3, 1, 4,
        3, 4, 1, 5, 2, 5, 4, 1, 2, 3, 4, 5, 2, 2, 4, 2, 2, 4, 4, 4, 2, 1, 4, 3,
        1, 1, 3, 3, 1, 5, 3, 5, 1, 3, 3, 1, 1, 2, 3, 4, 1, 4, 3, 2, 4, 1, 2, 4,
        4, 4, 2, 1, 4, 2, 2, 5, 4, 3, 2, 3, 4, 5, 2, 2, 3, 4, 1, 1, 3, 3, 1, 2,
        3, 2, 1, 5, 3, 5, 1, 3, 3, 1, 1, 4, 2, 1, 4, 1, 2, 5, 4, 3, 2, 3, 4, 2,
        2, 2, 4, 4, 2, 4, 4, 5, 1, 1, 3, 5, 1, 5, 3, 1, 1, 4, 3, 4, 1, 2, 3, 2,
        1, 3, 3, 3, 4, 1, 2, 2, 4, 3, 2, 4, 4, 2, 2, 1, 4, 5, 2, 5, 4, 4, 2, 3,
    )

    // `best.j.f4252c` one-based group/club coordinates.
    private val GROUP_4X4_L = intArrayOf(
        4, 4, 2, 2, 4, 1, 2, 4, 4, 3, 2, 1, 3, 2, 1, 1, 3, 4, 1, 2, 4, 2, 2, 3,
        3, 3, 1, 3, 3, 1, 1, 4, 2, 3, 4, 4, 2, 4, 4, 3, 1, 2, 3, 3, 1, 3, 3, 2,
        2, 1, 4, 2, 2, 2, 4, 1, 1, 1, 3, 1, 1, 4, 3, 4, 4, 4, 2, 4, 1, 2, 3, 1,
        3, 2, 1, 4, 1, 1, 4, 3, 4, 2, 2, 2, 2, 1, 4, 1, 2, 3, 3, 3, 1, 3, 3, 4,
        2, 4, 4, 2, 4, 4, 2, 1, 4, 1, 1, 2, 2, 2, 3, 2, 4, 3, 2, 3, 3, 1, 1, 3,
        3, 4, 1, 1, 3, 3, 1, 4, 2, 4, 3, 4, 1, 2, 3, 2, 1, 3, 4, 4, 2, 2, 4, 3,
        2, 3, 3, 1, 2, 1, 3, 3, 1, 1, 4, 2, 1, 4, 4, 2, 3, 1, 2, 4, 3, 2, 2, 1,
        4, 4, 1, 1, 4, 2, 1, 2, 3, 3, 2, 2, 3, 4, 2, 3, 4, 1, 1, 3, 4, 3, 1, 4,
        2, 3, 3, 2, 2, 4, 3, 3, 1, 2, 4, 4, 1, 3, 4, 3, 2, 1, 3, 4, 2, 2, 3, 1,
        1, 1, 4, 2, 1, 4, 4, 1, 3, 2, 2, 4, 4, 3, 1, 2, 1, 4, 4, 4, 3, 1, 2, 1,
        3, 4, 2, 2, 3, 3, 1, 1, 4, 2, 1, 3, 4, 1, 2, 3, 4, 4, 3, 2, 2, 4, 1, 3,
        2, 1, 1, 2, 4, 3, 3, 1, 2, 2, 1, 1, 2, 3, 1, 4, 4, 2, 3, 3, 4, 1, 3, 4,
        1, 1, 2, 4, 1, 2, 2, 3, 3, 2, 4, 2, 3, 4, 4, 4, 3, 3, 4, 3, 1, 3, 2, 2,
        1, 4, 2, 1, 3, 1, 4, 1, 2, 4, 1, 2, 3, 3, 4, 4, 3, 2, 4, 1, 3, 4, 4, 3,
        2, 2, 1, 4, 2, 3, 1, 1, 2, 1, 1, 3, 3, 1, 4, 2, 4, 3, 3, 2, 1, 4, 2, 4,
        1, 2, 2, 2, 4, 4, 3, 1, 1, 3, 2, 3, 1, 1, 2, 1, 4, 1, 3, 3, 4, 2, 3, 4,
    )
}
