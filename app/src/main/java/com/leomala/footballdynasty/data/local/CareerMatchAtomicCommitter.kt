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

/**
 * One already-calculated legacy coach post-match mutation.
 *
 * [resolvedClubId] is the match side whose `c0.y0()` resolved this manager. The list supplied to
 * [CareerMatchAtomicCommitter.commit] must retain the proven `best.s.f()` order: home first, then
 * away, omitting a side only when the legacy manager lookup produced no manager. The same manager
 * source ordinal may therefore legitimately appear twice when corrupt/source state points both
 * clubs at the same first ArrayList entry; callers must chain the second expected-before state from
 * the first after-state instead of deduplicating it.
 */
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

    suspend fun commit(
        result: CareerMatchRuntimeResult,
        playerRuntimeUpdates: List<CareerMatchPlayerRuntimeUpdate> = emptyList(),
        playerClubSeasonStatUpdates: List<CareerMatchPlayerClubSeasonStatUpdate> = emptyList(),
        financeUpdate: CareerMatchFinanceUpdate? = null,
        coachUpdatesInLegacyOrder: List<CareerMatchCoachUpdate> = emptyList(),
    ) = database.withTransaction {
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

        matchStore.commitMatch(
            result = result,
            playerRuntimeUpdates = playerRuntimeUpdates,
            playerClubSeasonStatUpdates = playerClubSeasonStatUpdates,
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
    }
}
