package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.career.LegacyCompetitionRoundThresholdRules
import com.leomala.footballdynasty.domain.competition.LegacyCompetitionSnapshotRules

/**
 * Caller-owned annual boundary for serialized `best.k0.c(index)` -> `k0.h/i` followed by `k0.K()`.
 *
 * The store never reconstructs V16-missing values. A competition with retained k0.g rows requires
 * V17 stable ordinals plus the exact `nGrupos` and competition index or the transition fails closed.
 */
class CareerAnnualCompetitionSnapshotStore(
    private val database: FootballDynastyDatabase,
) {
    internal suspend fun snapshotAndResetInCurrentTransaction(
        careerId: String,
        legacySeasonIndex: Int,
    ) {
        require(careerId.isNotBlank())
        require(legacySeasonIndex >= 0) { "Legacy season index must not be negative" }
        val competitionDao = database.careerCompetitionDao()
        val durability = CareerLegacyDurabilityStore(database)

        competitionDao.competitionsForCareer(careerId).forEach { competition ->
            val ratings = competitionDao.playerRatings(careerId, competition.competitionId)
            if (ratings.isEmpty()) return@forEach

            val competitionIndex = requireNotNull(competition.legacyCompetitionIndex) {
                "Competition ${competition.competitionId} has k0.g rows but no proven legacy index"
            }
            val groupCount = requireNotNull(competition.legacyGroupCountA0) {
                "Competition ${competition.competitionId} has k0.g rows but no proven LoadLigaOptions.nGrupos"
            }
            require(ratings.all { it.legacyStableOrdinal != null }) {
                "Competition ${competition.competitionId} contains pre-V17 k0.g rows with unknown insertion order"
            }
            val participantCount = competitionDao.standings(careerId, competition.competitionId).size
            require(participantCount > 0) {
                "Competition ${competition.competitionId} has k0.g rows but no persisted participants"
            }
            val threshold = LegacyCompetitionRoundThresholdRules.resolve(
                participantCount = participantCount,
                legacyGroupCountA0 = groupCount,
                legacyRoundU = competition.currentRoundNumber,
            )
            val snapshot = LegacyCompetitionSnapshotRules.annual(
                candidates = ratings.map { rating ->
                    LegacyCompetitionSnapshotRules.AnnualCandidate(
                        playerId = rating.playerId,
                        clubIdAtSnapshot = database.careerPlayerRuntimeDao()
                            .findMembership(careerId, rating.playerId)?.clubId,
                        legacyAverage = rating.legacyAverageRating,
                        legacyCount = rating.legacyRatingCount,
                        legacySelector = rating.legacyCategory,
                        stableOrdinal = requireNotNull(rating.legacyStableOrdinal),
                    )
                },
                threshold = threshold,
                legacyYear = legacySeasonIndex,
                legacyCompetitionType = competition.legacyCompetitionType,
                competitionIndex = competitionIndex,
            )
            durability.persistCompetitionSnapshotInCurrentTransaction(
                careerId = careerId,
                competitionId = competition.competitionId,
                snapshotKind = CareerLegacyDurabilityStore.SNAPSHOT_ANNUAL,
                snapshot = snapshot,
            )
            if (snapshot.markTopPlayerStar) {
                val topPlayerId = requireNotNull(snapshot.topPlayerId)
                val runtime = requireNotNull(database.careerPlayerRuntimeDao().findRuntime(careerId, topPlayerId)) {
                    "Annual top player $topPlayerId has no persisted runtime"
                }
                database.careerPlayerRuntimeDao().upsertRuntime(runtime.copy(star = true))
            }

            // Exact legacy lifecycle: serialized h/i survive; only k0.g is cleared after c(index).
            competitionDao.clearPlayerRatings(careerId, competition.competitionId)
        }

        // `best.o.Y0()` clears serialized per-match `best.o.V/components.s2` at annual reset.
        database.careerLegacyDurabilityDao().clearPlayerMatchRatingHistory(careerId)
    }
}
