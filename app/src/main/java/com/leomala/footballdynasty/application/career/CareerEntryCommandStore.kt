package com.leomala.footballdynasty.application.career

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.repository.RoomCareerRepository
import com.leomala.footballdynasty.data.repository.RoomCareerStateRepository
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.model.Career
import com.leomala.footballdynasty.foundation.error.CareerIntegrityException

/**
 * Write boundary for the Phase 17 new-career entry flow.
 *
 * The UI supplies identity, seed and the already-selected canonical club. This
 * boundary does not synthesize any of them. Metadata and certified core state
 * are committed in the same Room transaction so a partially-created career is
 * never observable.
 */
class CareerEntryCommandStore(
    private val database: FootballDynastyDatabase,
    private val clockMillis: () -> Long = System::currentTimeMillis,
) {
    suspend fun createCareer(
        careerId: String,
        displayName: String?,
        seed: Long,
        managedClubId: String,
    ): CareerState = database.withTransaction {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        require(managedClubId.isNotBlank()) { "Managed club id must not be blank" }

        if (database.careerMetadataDao().findById(careerId) != null) {
            throw CareerIntegrityException("Career metadata $careerId already exists")
        }
        if (database.clubDao().findById(managedClubId) == null) {
            throw CareerIntegrityException("Managed club $managedClubId does not resolve")
        }

        val careerRepository = RoomCareerRepository(database, clockMillis)
        val stateRepository = RoomCareerStateRepository(database, clockMillis)

        careerRepository.save(
            Career(
                id = careerId,
                displayName = displayName,
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
            )
        )

        stateRepository.save(
            CareerStateFactory.create(
                id = careerId,
                seed = seed,
                managedClubId = managedClubId,
            )
        )
    }
}
