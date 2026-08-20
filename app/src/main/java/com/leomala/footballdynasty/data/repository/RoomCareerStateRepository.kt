package com.leomala.footballdynasty.data.repository

import com.leomala.footballdynasty.data.local.CareerCoreStateRoomAdapter
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.domain.career.CareerIntegrityValidator
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.repository.CareerStateRepository
import com.leomala.footballdynasty.foundation.error.CareerIntegrityException

class RoomCareerStateRepository(
    private val database: FootballDynastyDatabase,
    private val clockMillis: () -> Long = System::currentTimeMillis,
) : CareerStateRepository {
    override suspend fun save(state: CareerState): CareerState {
        CareerIntegrityValidator.validate(state)
        if (database.careerMetadataDao().findById(state.id) == null) {
            throw CareerIntegrityException("Career metadata ${state.id} must exist before core state")
        }
        state.managedClub?.let { managed ->
            if (database.clubDao().findById(managed.clubId) == null) {
                throw CareerIntegrityException("Managed club ${managed.clubId} does not resolve")
            }
        }
        database.careerCoreStateDao().upsert(
            CareerCoreStateRoomAdapter.entity(state, clockMillis())
        )
        return requireNotNull(findById(state.id))
    }

    override suspend fun findById(id: String): CareerState? =
        database.careerCoreStateDao().findById(id)?.let { entity ->
            CareerCoreStateRoomAdapter.state(entity).also(CareerIntegrityValidator::validate)
        }
}
