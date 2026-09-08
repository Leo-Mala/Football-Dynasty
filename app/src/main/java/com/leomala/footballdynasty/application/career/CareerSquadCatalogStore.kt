package com.leomala.footballdynasty.application.career

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.CareerCoreStateRoomAdapter
import com.leomala.footballdynasty.data.local.CareerPlayerRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase

/** Read-only Phase 17 projection of the managed club's persisted senior squad. */
class CareerSquadCatalogStore(
    private val database: FootballDynastyDatabase,
) {
    data class SeniorSquad(
        val careerId: String,
        val clubId: String,
        val players: List<PlayerRow>,
    )

    data class PlayerRow(
        val playerId: String,
        val name: String,
        val position: Int,
        val age: Int,
        val overall: Int,
        val marketValue: Int,
        val star: Boolean,
        val worldTop: Boolean,
        val sourceOrdinal: Int,
    )

    suspend fun loadSeniorSquad(careerId: String): SeniorSquad? = database.withTransaction {
        require(careerId.isNotBlank()) { "Career id must not be blank" }

        val coreEntity = database.careerCoreStateDao().findById(careerId)
            ?: return@withTransaction null
        val state = CareerCoreStateRoomAdapter.state(coreEntity)
        val clubId = state.managedClub?.clubId ?: return@withTransaction null
        if (database.clubDao().findById(clubId) == null) {
            return@withTransaction null
        }

        val playerRuntimeDao = database.careerPlayerRuntimeDao()
        val memberships = playerRuntimeDao.membershipsForClub(
            careerId = careerId,
            clubId = clubId,
            rosterKind = ROSTER_SENIOR,
        )
        val players = memberships.map { membership ->
            val runtime = requireNotNull(
                playerRuntimeDao.findRuntime(careerId, membership.playerId)
            ) {
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
            PlayerRow(
                playerId = runtime.playerId,
                name = identity.first,
                position = identity.second,
                age = runtime.age,
                overall = runtime.overall,
                marketValue = runtime.marketValue,
                star = runtime.star,
                worldTop = runtime.worldTop,
                sourceOrdinal = membership.sourceOrdinal,
            )
        }

        SeniorSquad(
            careerId = careerId,
            clubId = clubId,
            players = players,
        )
    }

    private companion object {
        const val ROSTER_SENIOR = "SENIOR"
    }
}
