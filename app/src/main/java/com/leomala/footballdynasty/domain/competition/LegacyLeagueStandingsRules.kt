package com.leomala.footballdynasty.domain.competition

/**
 * Persistence-independent port of the league-table behavior proven in legacy `best.c0.m(...)`,
 * `best.e0.g()` and `components.f3.k`.
 *
 * `best.e0` persists points, played, wins, losses, goals-for and goals-against. Draws and goal
 * difference are derived exactly as the legacy `g()` array does. The standings comparator returns
 * equality after goals-for, so ordering must remain stable for a complete tie.
 */
object LegacyLeagueStandingsRules {
    data class Row(
        val clubId: String,
        val points: Int = 0,
        val played: Int = 0,
        val wins: Int = 0,
        val losses: Int = 0,
        val goalsFor: Int = 0,
        val goalsAgainst: Int = 0,
    ) {
        val draws: Int
            get() = played - (wins + losses)

        val goalDifference: Int
            get() = goalsFor - goalsAgainst
    }

    /** Exact per-club result mutation performed by legacy `best.c0.m(best.s, konrent.t)`. */
    fun applyResult(
        row: Row,
        goalsFor: Int,
        goalsAgainst: Int,
    ): Row {
        require(goalsFor >= 0 && goalsAgainst >= 0) { "Resolved goals must not be negative" }

        var updated = row.copy(
            played = row.played + 1,
            goalsFor = row.goalsFor + goalsFor,
            goalsAgainst = row.goalsAgainst + goalsAgainst,
        )
        updated = when {
            goalsFor > goalsAgainst -> updated.copy(
                points = updated.points + 3,
                wins = updated.wins + 1,
            )

            goalsFor < goalsAgainst -> updated.copy(
                losses = updated.losses + 1,
            )

            else -> updated.copy(points = updated.points + 1)
        }
        return updated
    }

    /** Applies one resolved match to both participating rows without creating missing clubs. */
    fun applyMatch(
        rows: List<Row>,
        homeClubId: String,
        awayClubId: String,
        homeGoals: Int,
        awayGoals: Int,
    ): List<Row> {
        require(homeClubId != awayClubId) { "Match clubs must be distinct" }
        require(rows.count { it.clubId == homeClubId } == 1) { "Home club must exist exactly once" }
        require(rows.count { it.clubId == awayClubId } == 1) { "Away club must exist exactly once" }

        return rows.map { row ->
            when (row.clubId) {
                homeClubId -> applyResult(row, homeGoals, awayGoals)
                awayClubId -> applyResult(row, awayGoals, homeGoals)
                else -> row
            }
        }
    }

    /**
     * Exact legacy comparator order: points DESC, wins DESC, goal difference DESC, goals-for DESC.
     * Kotlin/JVM `sortedWith` is stable, preserving input order when the legacy comparator returns 0.
     */
    fun rank(rows: List<Row>): List<Row> = rows.sortedWith(
        Comparator { left, right ->
            when {
                left.points != right.points -> right.points - left.points
                left.wins != right.wins -> right.wins - left.wins
                left.goalDifference != right.goalDifference ->
                    right.goalDifference - left.goalDifference

                left.goalsFor != right.goalsFor -> right.goalsFor - left.goalsFor
                else -> 0
            }
        }
    )
}
