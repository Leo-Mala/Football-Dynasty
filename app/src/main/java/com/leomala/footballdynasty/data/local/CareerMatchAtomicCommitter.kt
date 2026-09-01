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

class CareerMatchAtomicCommitter(
    private val database: FootballDynastyDatabase,
    clockMillis: () -> Long = System::currentTimeMillis,
) {
    private val matchStore = CareerMatchStore(database, clockMillis)
    private val managerStore = CareerManagerRuntimeStore(database)

    suspend fun commit(
        result: CareerMatchRuntimeResult,
        playerRuntimeUpdates: List<CareerMatchPlayerRuntimeUpdate> = emptyList(),
        playerClubSeasonStatUpdates: List<CareerMatchPlayerClubSeasonStatUpdate> = emptyList(),
        financeUpdate: CareerMatchFinanceUpdate? = null,
    ) = database.withTransaction {
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
    }
}
