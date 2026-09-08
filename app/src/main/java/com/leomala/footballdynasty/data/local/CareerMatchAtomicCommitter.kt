package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.domain.career.CareerMatchRuntimeResult
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState

/**
 * Optional finance mutation produced while resolving a match with the career RNG.
 *
 * The expected-before state makes this boundary fail closed if another manager operation changed
 * the club between calculation and commit. The enclosing transaction guarantees that match score,
 * career/RNG state, player effects and finance either all commit or all roll back together.
 */
data class CareerMatchFinanceUpdate(
    val clubId: String,
    val expectedBefore: LegacyFinanceRuntimeState,
    val after: LegacyFinanceRuntimeState,
)

/** One already-calculated legacy coach post-match mutation. */
data class CareerMatchCoachUpdate(
    val resolvedClubId: String,
    val expectedBefore: CareerCoachRuntimeState,
    val after: CareerCoachRuntimeState,
)

class CareerMatchAtomicCommitter(
    private val database: FootballDynastyDatabase,
    clockMillis: () -> Long = System::currentTimeMillis,
) {
    private val matchStore = CareerMatchStore(database, clockMillis)
    private val managerStore = CareerManagerRuntimeStore(database)
    private val coachStore = CareerCoachRuntimeStore(database)
    private val competitionPlayerRatingStore = CareerCompetitionPlayerRatingStore(database)
    private val legacyDurabilityStore = CareerLegacyDurabilityStore(database)

    suspend fun commit(
        result: CareerMatchRuntimeResult,
        playerRuntimeUpdates: List<CareerMatchPlayerRuntimeUpdate> = emptyList(),
        playerClubSeasonStatUpdates: List<CareerMatchPlayerClubSeasonStatUpdate> = emptyList(),
        financeUpdate: CareerMatchFinanceUpdate? = null,
        coachUpdatesInLegacyOrder: List<CareerMatchCoachUpdate> = emptyList(),
        competitionPlayerRatingMutationsInLegacyOrder: List<CareerCompetitionPlayerRatingMutation> = emptyList(),
        playerMatchRatingHistoryMutationsInLegacyOrder: List<CareerPlayerMatchRatingHistoryMutation> = emptyList(),
        tieBreakMutation: CareerMatchTieBreakMutation? = null,
        roundSnapshotStaging: CareerRoundSnapshotStaging? = null,
    ): CareerMatchCommitOutcome = database.withTransaction {
        val coachSideOrder = coachUpdatesInLegacyOrder.map { update ->
            when (update.resolvedClubId) {
                result.match.homeClubId -> 0
                result.match.awayClubId -> 1
                else -> throw IllegalArgumentException(
                    "Coach update club ${update.resolvedClubId} does not belong to resolved match ${result.match.id}"
                )
            }
        }
        require(coachSideOrder.zipWithNext().all { (previous, next) -> previous < next }) {
            "Coach updates must preserve legacy home-then-away order without duplicate match sides"
        }

        // In best.o.n(), components.s2 is appended before the gated k0.a(player) write. Keep that
        // proven ordering, while all effects still share one Room transaction.
        if (playerMatchRatingHistoryMutationsInLegacyOrder.isNotEmpty() || tieBreakMutation != null) {
            legacyDurabilityStore.persistMatchEvidenceInCurrentTransaction(
                careerId = result.state.id,
                matchId = result.match.id,
                playerRatingsInLegacyOrder = playerMatchRatingHistoryMutationsInLegacyOrder,
                tieBreakMutation = tieBreakMutation,
            )
        }
        competitionPlayerRatingStore.applyForMatchInCurrentTransaction(
            careerId = result.state.id,
            matchId = result.match.id,
            mutationsInLegacyOrder = competitionPlayerRatingMutationsInLegacyOrder,
        )
        val matchOutcome = matchStore.commitMatch(
            result = result,
            playerRuntimeUpdates = playerRuntimeUpdates,
            playerClubSeasonStatUpdates = playerClubSeasonStatUpdates,
            roundSnapshotStaging = roundSnapshotStaging,
        )
        financeUpdate?.let { update ->
            require(update.clubId == result.match.homeClubId || update.clubId == result.match.awayClubId) {
                "Finance update club ${update.clubId} does not belong to resolved match ${result.match.id}"
            }
            managerStore.commitFinanceState(
                careerId = result.state.id,
                clubId = update.clubId,
                expectedBefore = update.expectedBefore,
                after = update.after,
            )
        }
        coachUpdatesInLegacyOrder.forEach { update ->
            coachStore.commitPostMatch(
                careerId = result.state.id,
                expectedBefore = update.expectedBefore,
                after = update.after,
            )
        }
        matchOutcome
    }
}
