package com.leomala.footballdynasty.application.career

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.CareerCoreStateRoomAdapter
import com.leomala.footballdynasty.data.local.CareerPlayerRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase

/**
 * Read-only Phase 17 projection for the legacy player-search/market surface.
 *
 * Rows are persisted senior players belonging to clubs other than the managed club. Presence in
 * this catalog does not mean that a player is transfer-listed or that an offer is currently valid.
 * Commercial actions stay outside this boundary until their complete persisted owner is proven.
 */
class CareerTransferSearchCatalogStore(
    private val database: FootballDynastyDatabase,
) {
    data class SearchCatalog(
        val careerId: String,
        val managedClubId: String,
        val rows: List<PlayerRow>,
    )

    data class PlayerRow(
        val playerId: String,
        val name: String,
        val clubId: String,
        val clubName: String,
        val position: Int,
        val age: Int,
        val overall: Int,
        val marketValue: Int,
        val sourceOrdinal: Int,
    )

    suspend fun loadOtherSeniorPlayers(careerId: String): SearchCatalog? = database.withTransaction {
        require(careerId.isNotBlank()) { "Career id must not be blank" }

        val coreEntity = database.careerCoreStateDao().findById(careerId)
            ?: return@withTransaction null
        val state = CareerCoreStateRoomAdapter.state(coreEntity)
        val managedClubId = state.managedClub?.clubId ?: return@withTransaction null

        val clubsById = database.clubDao().all().associateBy { it.id }
        if (clubsById[managedClubId] == null) {
            return@withTransaction null
        }

        val playerRuntimeDao = database.careerPlayerRuntimeDao()
        val runtimesByPlayerId = playerRuntimeDao.runtimeForCareer(careerId).associateBy { it.playerId }
        val rows = playerRuntimeDao.membershipsForCareer(careerId)
            .filter { membership ->
                membership.rosterKind == ROSTER_SENIOR && membership.clubId != managedClubId
            }
            .map { membership ->
                val runtime = requireNotNull(runtimesByPlayerId[membership.playerId]) {
                    "Missing runtime for career=$careerId player=${membership.playerId}"
                }
                val identity = when (runtime.sourceType) {
                    CareerPlayerRuntimeStore.SOURCE_CANONICAL -> {
                        val canonical = requireNotNull(database.playerDao().findById(runtime.playerId)) {
                            "Missing canonical player ${runtime.playerId}"
                        }
                        canonical.name to canonical.position
                    }

                    CareerPlayerRuntimeStore.SOURCE_PROCEDURAL -> {
                        val procedural = requireNotNull(
                            playerRuntimeDao.findProceduralPlayer(careerId, runtime.playerId)
                        ) {
                            "Missing procedural player career=$careerId player=${runtime.playerId}"
                        }
                        procedural.name to procedural.position
                    }

                    else -> error("Unknown player runtime sourceType=${runtime.sourceType}")
                }
                val club = requireNotNull(clubsById[membership.clubId]) {
                    "Missing club ${membership.clubId} for career=$careerId player=${membership.playerId}"
                }
                PlayerRow(
                    playerId = runtime.playerId,
                    name = identity.first,
                    clubId = membership.clubId,
                    clubName = club.name,
                    position = identity.second,
                    age = runtime.age,
                    overall = runtime.overall,
                    marketValue = runtime.marketValue,
                    sourceOrdinal = membership.sourceOrdinal,
                )
            }

        SearchCatalog(
            careerId = careerId,
            managedClubId = managedClubId,
            rows = rows,
        )
    }

    private companion object {
        const val ROSTER_SENIOR = "SENIOR"
    }
}
