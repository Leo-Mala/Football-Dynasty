package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionEntity
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionMatchEntity
import com.leomala.footballdynasty.data.local.entity.CareerCompetitionStandingEntity
import com.leomala.footballdynasty.domain.competition.LegacyLeagueStandingsRules

data class CareerCompetitionSnapshot(
    val careerId: String,
    val competitionId: String,
    val legacyCompetitionType: Int,
    val legacyFormatCode: Int,
    val currentRoundNumber: Int,
    val totalRounds: Int,
    val standings: List<LegacyLeagueStandingsRules.Row>,
    /** Exact serialized `LoadLigaOptions.nRebaixados`; null means the source is not proven. */
    val legacyRelegationCount: Int? = null,
) {
    val finished: Boolean
        get() = currentRoundNumber > totalRounds
}

/** Persistence boundary for the proven league subset of legacy `konrent.t` + `best.e0`. */
class CareerCompetitionStore(
    private val database: FootballDynastyDatabase,
) {
    suspend fun initializeLeague(
        careerId: String,
        competitionId: String,
        legacyCompetitionType: Int,
        legacyFormatCode: Int,
        clubIds: List<String>,
        roundMatchIds: List<List<String>>,
        legacyRelegationCount: Int? = null,
    ) {
        validateInitialization(careerId, competitionId, clubIds, roundMatchIds)
        legacyRelegationCount?.let {
            require(it >= 0) { "Legacy LoadLigaOptions.nRebaixados must not be negative" }
        }
        database.withTransaction {
            requireNotNull(database.careerMetadataDao().findById(careerId)) {
                "Career metadata $careerId must exist before competition initialization"
            }
            val dao = database.careerCompetitionDao()
            require(dao.findCompetition(careerId, competitionId) == null) {
                "Competition $competitionId is already persisted for career $careerId"
            }

            val scheduledById = database.careerScheduledMatchDao().findAll(careerId).associateBy { it.matchId }
            val links = roundMatchIds.flatMapIndexed { roundIndex, ids ->
                ids.mapIndexed { fixtureOrdinal, matchId ->
                    val scheduled = requireNotNull(scheduledById[matchId]) {
                        "Competition match $matchId is not in the persisted career schedule"
                    }
                    require(!scheduled.processed) {
                        "Competition initialization requires unresolved scheduled match $matchId"
                    }
                    require(scheduled.homeClubId in clubIds && scheduled.awayClubId in clubIds) {
                        "Competition match $matchId contains a club outside the competition"
                    }
                    CareerCompetitionMatchEntity(
                        careerId = careerId,
                        competitionId = competitionId,
                        matchId = matchId,
                        roundNumber = roundIndex + 1,
                        fixtureOrdinal = fixtureOrdinal,
                    )
                }
            }

            dao.upsertCompetition(
                CareerCompetitionEntity(
                    careerId = careerId,
                    competitionId = competitionId,
                    legacyCompetitionType = legacyCompetitionType,
                    legacyFormatCode = legacyFormatCode,
                    currentRoundNumber = 1,
                    totalRounds = roundMatchIds.size,
                    legacyRelegationCount = legacyRelegationCount,
                )
            )
            dao.upsertStandings(
                clubIds.mapIndexed { ordinal, clubId ->
                    CareerCompetitionStandingEntity(
                        careerId = careerId,
                        competitionId = competitionId,
                        clubId = clubId,
                        stableOrdinal = ordinal,
                        points = 0,
                        played = 0,
                        wins = 0,
                        losses = 0,
                        goalsFor = 0,
                        goalsAgainst = 0,
                    )
                }
            )
            dao.upsertMatches(links)
        }
    }

    suspend fun load(careerId: String, competitionId: String): CareerCompetitionSnapshot? {
        val competition = database.careerCompetitionDao().findCompetition(careerId, competitionId)
            ?: return null
        val standings = database.careerCompetitionDao().standings(careerId, competitionId)
            .map { it.toRow() }
        return competition.toSnapshot(standings)
    }

    /** Explicit repair/import boundary: fail if the current round is not fully resolved. */
    suspend fun completeCurrentRound(
        careerId: String,
        competitionId: String,
    ): CareerCompetitionSnapshot = database.withTransaction {
        val competition = requireNotNull(
            database.careerCompetitionDao().findCompetition(careerId, competitionId)
        ) { "Missing competition $competitionId for career $careerId" }
        require(competition.currentRoundNumber <= competition.totalRounds) {
            "Competition $competitionId is already finished"
        }
        advanceCurrentRoundIfResolvedInCurrentTransaction(careerId, competitionId)
            ?: throw IllegalArgumentException(
                "Competition round ${competition.currentRoundNumber} is not fully resolved"
            )
    }

    /**
     * Caller-owned transaction helper used by the match commit path. Returns null while any current
     * round fixture is unresolved; otherwise applies results, stable-sorts and increments legacy U.
     */
    internal suspend fun advanceCurrentRoundIfResolvedInCurrentTransaction(
        careerId: String,
        competitionId: String,
    ): CareerCompetitionSnapshot? {
        val dao = database.careerCompetitionDao()
        val competition = requireNotNull(dao.findCompetition(careerId, competitionId)) {
            "Missing competition $competitionId for career $careerId"
        }
        if (competition.currentRoundNumber > competition.totalRounds) return null

        val links = dao.matchesForRound(careerId, competitionId, competition.currentRoundNumber)
        require(links.isNotEmpty()) {
            "Competition round ${competition.currentRoundNumber} has no persisted matches"
        }
        val resolved = links.map { link ->
            requireNotNull(database.careerScheduledMatchDao().findById(careerId, link.matchId)) {
                "Missing scheduled competition match ${link.matchId}"
            }
        }
        if (resolved.any { !it.processed || it.homeGoals == null || it.awayGoals == null }) {
            return null
        }

        var rows = dao.standings(careerId, competitionId).map { it.toRow() }
        require(rows.isNotEmpty()) { "Competition $competitionId has no persisted standings" }
        resolved.forEach { match ->
            rows = LegacyLeagueStandingsRules.applyMatch(
                rows = rows,
                homeClubId = match.homeClubId,
                awayClubId = match.awayClubId,
                homeGoals = requireNotNull(match.homeGoals),
                awayGoals = requireNotNull(match.awayGoals),
            )
        }

        val ranked = LegacyLeagueStandingsRules.rank(rows)
        dao.upsertStandings(
            ranked.mapIndexed { ordinal, row -> row.toEntity(careerId, competitionId, ordinal) }
        )
        val advanced = competition.copy(currentRoundNumber = competition.currentRoundNumber + 1)
        dao.upsertCompetition(advanced)
        return advanced.toSnapshot(ranked)
    }

    private fun validateInitialization(
        careerId: String,
        competitionId: String,
        clubIds: List<String>,
        roundMatchIds: List<List<String>>,
    ) {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        require(competitionId.isNotBlank()) { "Competition id must not be blank" }
        require(clubIds.isNotEmpty()) { "Competition must contain clubs" }
        require(clubIds.none { it.isBlank() }) { "Competition club ids must not be blank" }
        require(clubIds.distinct().size == clubIds.size) { "Competition club ids must be unique" }
        require(roundMatchIds.isNotEmpty()) { "Competition must contain rounds" }
        val ids = roundMatchIds.flatten()
        require(ids.isNotEmpty()) { "Competition must contain scheduled matches" }
        require(ids.none { it.isBlank() }) { "Competition match ids must not be blank" }
        require(ids.distinct().size == ids.size) { "Competition match ids must be unique" }
    }

    private fun CareerCompetitionEntity.toSnapshot(
        rows: List<LegacyLeagueStandingsRules.Row>,
    ) = CareerCompetitionSnapshot(
        careerId = careerId,
        competitionId = competitionId,
        legacyCompetitionType = legacyCompetitionType,
        legacyFormatCode = legacyFormatCode,
        currentRoundNumber = currentRoundNumber,
        totalRounds = totalRounds,
        standings = rows,
        legacyRelegationCount = legacyRelegationCount,
    )

    private fun CareerCompetitionStandingEntity.toRow() = LegacyLeagueStandingsRules.Row(
        clubId = clubId,
        points = points,
        played = played,
        wins = wins,
        losses = losses,
        goalsFor = goalsFor,
        goalsAgainst = goalsAgainst,
    )

    private fun LegacyLeagueStandingsRules.Row.toEntity(
        careerId: String,
        competitionId: String,
        stableOrdinal: Int,
    ) = CareerCompetitionStandingEntity(
        careerId = careerId,
        competitionId = competitionId,
        clubId = clubId,
        stableOrdinal = stableOrdinal,
        points = points,
        played = played,
        wins = wins,
        losses = losses,
        goalsFor = goalsFor,
        goalsAgainst = goalsAgainst,
    )
}
