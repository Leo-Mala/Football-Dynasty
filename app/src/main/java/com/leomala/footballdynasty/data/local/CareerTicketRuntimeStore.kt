package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerClubTicketRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerManagerTicketRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerMatchConstructionSourceEntity
import com.leomala.footballdynasty.domain.manager.LegacyCoachRawHRule
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
        val desired = managersInWorldOrder.map {
            CareerManagerTicketRuntimeEntity(careerId, it.sourceOrdinal, it.legacyManagerId, it.rawH)
        }
        val materializedCoachState = database.careerCoachRuntimeDao().coachRuntimeForCareer(careerId)
        if (materializedCoachState.isNotEmpty()) {
            require(dao.managerStates(careerId) == desired) {
                "Cannot rematerialize ordered manager parents after V11 coach state exists"
            }
            return@withTransaction
        }
        dao.deleteManagerStates(careerId)
        dao.upsertManagerStates(desired)
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

    /**
     * Persists the independently characterized `best.f0.e(c0)` employment reset for H.
     *
     * This method deliberately models only the H slice already present in V9. Other employment
     * effects remain owned by their characterized manager transition boundary and are not invented
     * here. Duplicate manager ids preserve `best.b.b1(id)` first-match ArrayList semantics.
     */
    suspend fun applyCoachEmployment(careerId: String, legacyManagerId: Int): Int =
        mutateFirstCoachRawH(careerId, legacyManagerId) {
            LegacyCoachRawHRule.afterEmployment()
        }

    /**
     * Persists only the independently characterized annual `best.b.s()` H mutation.
     *
     * Duplicate legacy manager ids are intentionally supported: `best.b.b1(id)` observes the first
     * manager in world ArrayList order, so only that first matching V9 row is mutated here.
     */
    suspend fun applyCoachAnnualRecovery(careerId: String, legacyManagerId: Int): Int =
        mutateFirstCoachRawH(careerId, legacyManagerId) { rawH ->
            LegacyCoachRawHRule.afterAnnualRecovery(rawH)
        }

    /**
     * Persists only the proven `ActivityMainTeam.F()` floor quirk. The caller must provide the
     * already-characterized legacy floor flag; opening/refreshing a modern screen is not invented.
     */
    suspend fun applyCoachMainTeamRefresh(
        careerId: String,
        legacyManagerId: Int,
        legacyFloorEnabled: Boolean,
    ): Int = mutateFirstCoachRawH(careerId, legacyManagerId) { rawH ->
        LegacyCoachRawHRule.afterMainTeamRefresh(rawH, legacyFloorEnabled)
    }

    suspend fun findMatchConstructionSource(careerId: String, matchId: String): LegacyMatchConstructionSource? =
        dao.findMatchConstructionSource(careerId, matchId)?.let {
            LegacyMatchConstructionSource.valueOf(it.sourceCode)
        }

    private suspend fun mutateFirstCoachRawH(
        careerId: String,
        legacyManagerId: Int,
        transform: (Int) -> Int,
    ): Int = database.withTransaction {
        require(legacyManagerId != LegacyManagerIdentityRule.clubStoredManagerId(null)) {
            "Absent legacy manager id cannot carry mutable H"
        }
        val managers = dao.managerStates(careerId)
        val manager = requireNotNull(managers.firstOrNull { it.legacyManagerId == legacyManagerId }) {
            "Missing materialized manager id $legacyManagerId for career $careerId"
        }
        val after = transform(manager.rawH)
        dao.upsertManagerStates(listOf(manager.copy(rawH = after)))
        after
    }
}
