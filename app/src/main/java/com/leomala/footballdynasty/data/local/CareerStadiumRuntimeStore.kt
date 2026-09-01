package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerStadiumRuntimeEntity

data class CareerStadiumRuntimeState(val capacities: List<Int>) {
    init {
        require(capacities.size == 4)
        require(capacities.all { it >= 0 })
    }
}

/** Fail-closed Room boundary for the four capacities consumed by legacy `best.k.b(best.s)`. */
class CareerStadiumRuntimeStore(private val database: FootballDynastyDatabase) {
    private val dao = database.careerManagerRuntimeDao()

    suspend fun materialize(
        careerId: String,
        clubId: String,
        state: CareerStadiumRuntimeState,
    ) = database.withTransaction {
        requireNotNull(database.careerMetadataDao().findById(careerId)) { "Missing career $careerId" }
        requireNotNull(database.clubDao().findById(clubId)) { "Missing club $clubId" }
        dao.upsertStadiumRuntime(state.toEntity(careerId, clubId))
    }

    suspend fun find(careerId: String, clubId: String): CareerStadiumRuntimeState? =
        dao.findStadiumRuntime(careerId, clubId)?.toDomain()

    suspend fun commit(
        careerId: String,
        clubId: String,
        expectedBefore: CareerStadiumRuntimeState,
        after: CareerStadiumRuntimeState,
    ) = database.withTransaction {
        val current = requireNotNull(dao.findStadiumRuntime(careerId, clubId)) {
            "Missing materialized stadium runtime $careerId/$clubId"
        }
        require(current.toDomain() == expectedBefore) { "Stale stadium runtime for $clubId" }
        dao.upsertStadiumRuntime(after.toEntity(careerId, clubId))
    }

    private fun CareerStadiumRuntimeState.toEntity(careerId: String, clubId: String) =
        CareerStadiumRuntimeEntity(
            careerId = careerId,
            clubId = clubId,
            sector0Capacity = capacities[0],
            sector1Capacity = capacities[1],
            sector2Capacity = capacities[2],
            sector3Capacity = capacities[3],
        )

    private fun CareerStadiumRuntimeEntity.toDomain() = CareerStadiumRuntimeState(
        listOf(sector0Capacity, sector1Capacity, sector2Capacity, sector3Capacity)
    )
}
