package com.leomala.footballdynasty.application.career

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.CareerCoreStateRoomAdapter
import com.leomala.footballdynasty.data.local.CareerPlayerRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase

/**
 * Read-only Phase 17 contract projection for the managed club's persisted senior squad.
 *
 * Salary remains a raw legacy code because the recovered source does not prove a currency label.
 * Missing V7 commercial materialization is represented as null instead of synthesizing a value.
 */
class CareerContractCatalogStore(
    private val database: FootballDynastyDatabase,
) {
    data class ContractList(
        val careerId: String,
        val clubId: String,
        val rows: List<ContractRow>,
    )

    data class ContractRow(
        val playerId: String,
        val name: String,
        val position: Int,
        val sourceOrdinal: Int,
        val contractEndEpochMillis: Long,
        val salaryCode: Int?,
    )

    suspend fun loadContracts(careerId: String): ContractList? = database.withTransaction {
        require(careerId.isNotBlank()) { "Career id must not be blank" }

        val core = database.careerCoreStateDao().findById(careerId)
            ?: return@withTransaction null
        val state = CareerCoreStateRoomAdapter.state(core)
        val clubId = state.managedClub?.clubId ?: return@withTransaction null
        if (database.clubDao().findById(clubId) == null) {
            return@withTransaction null
        }

        val playerDao = database.careerPlayerRuntimeDao()
        val managerDao = database.careerManagerRuntimeDao()
        val memberships = playerDao.membershipsForClub(
            careerId = careerId,
            clubId = clubId,
            rosterKind = ROSTER_SENIOR,
        )

        val rows = memberships.map { membership ->
            val runtime = requireNotNull(playerDao.findRuntime(careerId, membership.playerId)) {
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
                        playerDao.findProceduralPlayer(careerId, runtime.playerId)
                    ) {
                        "Missing procedural player career=$careerId player=${runtime.playerId}"
                    }
                    procedural.name to procedural.position
                }

                else -> error("Unknown player runtime sourceType=${runtime.sourceType}")
            }
            val commercial = managerDao.findPlayerCommercial(careerId, runtime.playerId)
            ContractRow(
                playerId = runtime.playerId,
                name = identity.first,
                position = identity.second,
                sourceOrdinal = membership.sourceOrdinal,
                contractEndEpochMillis = runtime.contractEndEpochMillis,
                salaryCode = commercial?.salario,
            )
        }

        ContractList(
            careerId = careerId,
            clubId = clubId,
            rows = rows,
        )
    }

    private companion object {
        const val ROSTER_SENIOR = "SENIOR"
    }
}
