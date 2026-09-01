package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerClubTicketRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerManagerTicketRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerMatchConstructionSourceEntity
import com.leomala.footballdynasty.domain.manager.LegacyManagerIdentityRule
import com.leomala.footballdynasty.domain.manager.LegacyMatchConstructionSource

data class CareerClubTicketRuntimeState(val rawDivisionCode: Int, val legacyManagerId: Int)

data class CareerManagerTicketRuntimeState(
    val sourceOrdinal: Int,
    val legacyManagerId: Int,
    val rawH: Int,
)

class CareerTicketRuntimeStore(private val database: FootballDynastyDatabase) {
    private val dao = database.careerTicketRuntimeDao()

    suspend fun materializeClubState(
        careerId: String,
        clubId: String,
        state: CareerClubTicketRuntimeState,
    ) = database.withTransaction {
        requireNotNull(database.careerMetadataDao().findById(careerId)) { "Missing career $careerId" }
        requireNotNull(database.clubDao().findById(clubId)) { "Missing club $clubId" }
        dao.upsertClubState(
            CareerClubTicketRuntimeEntity(careerId, clubId, state.rawDivisionCode, state.legacyManagerId)
        )
    }

    suspend fun materializeManagers(
        careerId: String,
        managersInWorldOrder: List<CareerManagerTicketRuntimeState>,
    ) = database.withTransaction {
        requireNotNull(database.careerMetadataDao().findById(careerId)) { "Missing career $careerId" }
        require(managersInWorldOrder.map { it.sourceOrdinal } == managersInWorldOrder.indices.toList()) {
            "Manager source ordinals must be contiguous legacy ArrayList order"
        }
        dao.deleteManagerStates(careerId)
        dao.upsertManagerStates(
            managersInWorldOrder.map {
                CareerManagerTicketRuntimeEntity(careerId, it.sourceOrdinal, it.legacyManagerId, it.rawH)
            }
        )
    }

    suspend fun materializeMatchConstructionSource(
        careerId: String,
        matchId: String,
        source: LegacyMatchConstructionSource,
    ) = database.withTransaction {
        requireNotNull(database.careerScheduledMatchDao().findById(careerId, matchId)) {
            "Missing scheduled match $careerId/$matchId"
        }
        dao.upsertMatchConstructionSource(CareerMatchConstructionSourceEntity(careerId, matchId, source.name))
    }

    suspend fun findClubState(careerId: String, clubId: String): CareerClubTicketRuntimeState? =
        dao.findClubState(careerId, clubId)?.let {
            CareerClubTicketRuntimeState(it.rawDivisionCode, it.legacyManagerId)
        }

    suspend fun managersInWorldOrder(careerId: String): List<CareerManagerTicketRuntimeState> =
        dao.managerStates(careerId).map {
            CareerManagerTicketRuntimeState(it.sourceOrdinal, it.legacyManagerId, it.rawH)
        }

    suspend fun resolveCoachRawH(careerId: String, legacyManagerId: Int): Int? {
        if (legacyManagerId == LegacyManagerIdentityRule.clubStoredManagerId(null)) return null
        val manager = managersInWorldOrder(careerId).firstOrNull { it.legacyManagerId == legacyManagerId }
        return requireNotNull(manager) {
            "Missing materialized manager id $legacyManagerId for career $careerId"
        }.rawH
    }

    suspend fun findMatchConstructionSource(careerId: String, matchId: String): LegacyMatchConstructionSource? =
        dao.findMatchConstructionSource(careerId, matchId)?.let {
            LegacyMatchConstructionSource.valueOf(it.sourceCode)
        }
}
