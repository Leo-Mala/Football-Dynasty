package com.leomala.footballdynasty.application.career

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase

/** Read-only Phase 17 projection of persisted competitions and their stable-ranked standings. */
class CareerCompetitionCatalogStore(
    private val database: FootballDynastyDatabase,
) {
    data class CompetitionRow(
        val competitionId: String,
        val legacyCompetitionType: Int,
        val legacyFormatCode: Int,
        val currentRoundNumber: Int,
        val totalRounds: Int,
        val standings: List<StandingRow>,
    )

    data class StandingRow(
        val clubId: String,
        val clubName: String,
        val stableOrdinal: Int,
        val points: Int,
        val played: Int,
        val wins: Int,
        val losses: Int,
        val goalsFor: Int,
        val goalsAgainst: Int,
    )

    suspend fun loadCompetitions(careerId: String): List<CompetitionRow> = database.withTransaction {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        if (database.careerCoreStateDao().findById(careerId) == null) {
            return@withTransaction emptyList()
        }

        val competitionDao = database.careerCompetitionDao()
        competitionDao.competitionsForCareer(careerId).map { competition ->
            val standings = competitionDao.standings(careerId, competition.competitionId).map { standing ->
                val club = requireNotNull(database.clubDao().findById(standing.clubId)) {
                    "Missing club ${standing.clubId} for career=$careerId competition=${competition.competitionId}"
                }
                StandingRow(
                    clubId = standing.clubId,
                    clubName = club.name,
                    stableOrdinal = standing.stableOrdinal,
                    points = standing.points,
                    played = standing.played,
                    wins = standing.wins,
                    losses = standing.losses,
                    goalsFor = standing.goalsFor,
                    goalsAgainst = standing.goalsAgainst,
                )
            }
            CompetitionRow(
                competitionId = competition.competitionId,
                legacyCompetitionType = competition.legacyCompetitionType,
                legacyFormatCode = competition.legacyFormatCode,
                currentRoundNumber = competition.currentRoundNumber,
                totalRounds = competition.totalRounds,
                standings = standings,
            )
        }
    }
}
