package com.leomala.footballdynasty.application.career

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.CareerCoreStateRoomAdapter
import com.leomala.footballdynasty.data.local.CareerPlayerRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.domain.manager.LegacyPlayerSubroleCodeRule

/**
 * Read-only Phase 17 boundary for the persisted inputs already proven to feed
 * the legacy lineup runtime.
 *
 * This store deliberately does not classify availability, choose a formation,
 * restore saved tactics or execute a match. Those decisions still depend on
 * separately-owned legacy inputs whose persisted producers are not proven yet.
 */
class CareerLineupInputCatalogStore(
    private val database: FootballDynastyDatabase,
) {
    data class LineupInputs(
        val careerId: String,
        val clubId: String,
        val players: List<PlayerInput>,
    )

    data class PlayerInput(
        val playerId: String,
        val name: String,
        val positionCode: Int,
        val sideCode: Int,
        val subroleCode: Int,
        val skill: Int,
        val energy: Int,
        val star: Boolean,
        val sourceOrdinal: Int,
    )

    suspend fun loadManagedClubLineupInputs(careerId: String): LineupInputs? =
        database.withTransaction {
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

                val staticInput = when (runtime.sourceType) {
                    CareerPlayerRuntimeStore.SOURCE_CANONICAL -> {
                        val canonical = requireNotNull(database.playerDao().findById(runtime.playerId)) {
                            "Missing canonical player ${runtime.playerId}"
                        }
                        StaticInput(
                            name = canonical.name,
                            positionCode = canonical.position,
                            sideCode = canonical.side,
                            cr1 = canonical.cr1,
                            cr2 = canonical.cr2,
                        )
                    }

                    CareerPlayerRuntimeStore.SOURCE_PROCEDURAL -> {
                        val procedural = requireNotNull(
                            playerRuntimeDao.findProceduralPlayer(careerId, runtime.playerId)
                        ) {
                            "Missing procedural player career=$careerId player=${runtime.playerId}"
                        }
                        StaticInput(
                            name = procedural.name,
                            positionCode = procedural.position,
                            sideCode = procedural.side,
                            cr1 = procedural.cr1,
                            cr2 = procedural.cr2,
                        )
                    }

                    else -> error("Unknown player runtime sourceType=${runtime.sourceType}")
                }

                PlayerInput(
                    playerId = runtime.playerId,
                    name = staticInput.name,
                    positionCode = staticInput.positionCode,
                    sideCode = staticInput.sideCode,
                    subroleCode = LegacyPlayerSubroleCodeRule.resolve(
                        positionCode = staticInput.positionCode,
                        cr1 = staticInput.cr1,
                        cr2 = staticInput.cr2,
                    ),
                    // CareerMatchPersistedRuntimeResolver hydrates the certified
                    // legacy match runtime from the career-specific overall value.
                    skill = runtime.overall,
                    energy = runtime.energy,
                    star = runtime.star,
                    sourceOrdinal = membership.sourceOrdinal,
                )
            }

            LineupInputs(
                careerId = careerId,
                clubId = clubId,
                players = players,
            )
        }

    private data class StaticInput(
        val name: String,
        val positionCode: Int,
        val sideCode: Int,
        val cr1: Int,
        val cr2: Int,
    )

    private companion object {
        const val ROSTER_SENIOR = "SENIOR"
    }
}
