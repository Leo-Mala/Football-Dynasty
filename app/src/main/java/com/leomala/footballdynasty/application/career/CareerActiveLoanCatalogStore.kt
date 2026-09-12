package com.leomala.footballdynasty.application.career

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.CareerCoreStateRoomAdapter
import com.leomala.footballdynasty.data.local.CareerPlayerRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase

/**
 * Read-only Phase 17 projection of persisted active loans involving the managed club.
 *
 * Source/destination ownership and the expiry timestamp are durable V17 facts. This boundary does
 * not infer transfer fees, clauses, renewal terms or any action that is not explicitly persisted.
 */
class CareerActiveLoanCatalogStore(
    private val database: FootballDynastyDatabase,
) {
    data class ActiveLoanSnapshot(
        val careerId: String,
        val managedClubId: String,
        val loans: List<ActiveLoanRow>,
    )

    data class ActiveLoanRow(
        val playerId: String,
        val playerName: String,
        val sourceClubId: String,
        val sourceClubName: String,
        val destinationClubId: String,
        val destinationClubName: String,
        val expiresAtEpochMillis: Long,
        val managedClubIsSource: Boolean,
        val managedClubIsDestination: Boolean,
    )

    suspend fun loadManagedClubActiveLoans(careerId: String): ActiveLoanSnapshot? =
        database.withTransaction {
            require(careerId.isNotBlank()) { "Career id must not be blank" }

            val coreEntity = database.careerCoreStateDao().findById(careerId)
                ?: return@withTransaction null
            val state = CareerCoreStateRoomAdapter.state(coreEntity)
            val managedClubId = state.managedClub?.clubId ?: return@withTransaction null
            if (database.clubDao().findById(managedClubId) == null) {
                return@withTransaction null
            }

            val playerRuntimeDao = database.careerPlayerRuntimeDao()
            val loans = database.careerManagerRuntimeDao()
                .activeLoansForCareer(careerId)
                .filter { loan ->
                    loan.sourceClubId == managedClubId || loan.destinationClubId == managedClubId
                }
                .map { loan ->
                    val runtime = requireNotNull(
                        playerRuntimeDao.findRuntime(careerId, loan.playerId)
                    ) {
                        "Missing runtime for active loan career=$careerId player=${loan.playerId}"
                    }
                    val playerName = when (runtime.sourceType) {
                        CareerPlayerRuntimeStore.SOURCE_CANONICAL ->
                            requireNotNull(database.playerDao().findById(runtime.playerId)) {
                                "Missing canonical player ${runtime.playerId}"
                            }.name

                        CareerPlayerRuntimeStore.SOURCE_PROCEDURAL ->
                            requireNotNull(
                                playerRuntimeDao.findProceduralPlayer(careerId, runtime.playerId)
                            ) {
                                "Missing procedural player career=$careerId player=${runtime.playerId}"
                            }.name

                        else -> error("Unknown player runtime sourceType=${runtime.sourceType}")
                    }
                    val sourceClub = requireNotNull(database.clubDao().findById(loan.sourceClubId)) {
                        "Missing active-loan source club ${loan.sourceClubId}"
                    }
                    val destinationClub = requireNotNull(
                        database.clubDao().findById(loan.destinationClubId)
                    ) {
                        "Missing active-loan destination club ${loan.destinationClubId}"
                    }

                    ActiveLoanRow(
                        playerId = loan.playerId,
                        playerName = playerName,
                        sourceClubId = sourceClub.id,
                        sourceClubName = sourceClub.name,
                        destinationClubId = destinationClub.id,
                        destinationClubName = destinationClub.name,
                        expiresAtEpochMillis = loan.expiresAtEpochMillis,
                        managedClubIsSource = loan.sourceClubId == managedClubId,
                        managedClubIsDestination = loan.destinationClubId == managedClubId,
                    )
                }

            ActiveLoanSnapshot(
                careerId = careerId,
                managedClubId = managedClubId,
                loans = loans,
            )
        }
}
