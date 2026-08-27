package com.leomala.footballdynasty.domain.competition

/**
 * Pure composition of the proven legacy league sequence in `konrent.t`:
 * fixture rounds are generated first; `U` starts at 1; each completed round applies every result,
 * sorts the table, increments `U`, and the league is finished once `U` is greater than total rounds.
 *
 * Date assignment, persistence and non-league competition formats remain separate boundaries.
 */
object LegacyLeagueCompetitionRuntime {
    data class ResolvedFixture(
        val homeClubId: String,
        val awayClubId: String,
        val homeGoals: Int,
        val awayGoals: Int,
    )

    data class State(
        val rounds: List<List<LegacyLeagueFixtureRules.Fixture>>,
        val standings: List<LegacyLeagueStandingsRules.Row>,
        val currentRoundNumber: Int = 1,
    ) {
        val finished: Boolean
            get() = currentRoundNumber > rounds.size

        val totalRounds: Int
            get() = rounds.size

        fun currentPlayableFixtures(): List<LegacyLeagueFixtureRules.Fixture> =
            if (finished) {
                emptyList()
            } else {
                rounds[currentRoundNumber - 1].filter {
                    it.homeClubId != null && it.awayClubId != null
                }
            }
    }

    fun create(
        clubIds: List<String>,
        legacyCycleCode: Int,
    ): State = State(
        rounds = LegacyLeagueFixtureRules.generate(clubIds, legacyCycleCode),
        standings = clubIds.map { LegacyLeagueStandingsRules.Row(it) },
    )

    /** Mirrors the `d0()` table-update/sort/round-increment order for one completed league round. */
    fun completeCurrentRound(
        state: State,
        results: List<ResolvedFixture>,
    ): State {
        require(!state.finished) { "Competition is already finished" }
        val expected = state.currentPlayableFixtures()
        require(results.size == expected.size) {
            "Resolved round must contain exactly ${expected.size} playable fixtures"
        }

        var rows = state.standings
        expected.zip(results).forEachIndexed { index, (fixture, result) ->
            val expectedHome = requireNotNull(fixture.homeClubId)
            val expectedAway = requireNotNull(fixture.awayClubId)
            require(result.homeClubId == expectedHome && result.awayClubId == expectedAway) {
                "Resolved fixture $index does not match the scheduled legacy pairing"
            }
            rows = LegacyLeagueStandingsRules.applyMatch(
                rows = rows,
                homeClubId = result.homeClubId,
                awayClubId = result.awayClubId,
                homeGoals = result.homeGoals,
                awayGoals = result.awayGoals,
            )
        }

        return state.copy(
            standings = LegacyLeagueStandingsRules.rank(rows),
            currentRoundNumber = state.currentRoundNumber + 1,
        )
    }
}
