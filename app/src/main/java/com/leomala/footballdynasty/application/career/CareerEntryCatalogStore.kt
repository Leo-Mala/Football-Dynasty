package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.data.local.CareerCoreStateRoomAdapter
import com.leomala.footballdynasty.data.local.dao.CareerCoreStateDao
import com.leomala.footballdynasty.data.local.dao.CareerMetadataDao
import com.leomala.footballdynasty.data.local.dao.ClubDao
import com.leomala.footballdynasty.domain.career.CareerState

/**
 * Read boundary used by the Phase 17 entry flow.
 *
 * It exposes only state already owned by the certified Room V17 model. No UI
 * state is persisted here and no missing career state is synthesized.
 */
class CareerEntryCatalogStore(
    private val careerMetadataDao: CareerMetadataDao,
    private val careerCoreStateDao: CareerCoreStateDao,
    private val clubDao: ClubDao,
) {
    suspend fun listCareers(): List<CareerEntrySummary> =
        careerMetadataDao.all().map { metadata ->
            val core = careerCoreStateDao.findById(metadata.id)
            val managedClub = core?.managedClubId?.let { clubDao.findById(it) }
            CareerEntrySummary(
                careerId = metadata.id,
                displayName = metadata.displayName,
                updatedAtEpochMillis = metadata.updatedAtEpochMillis,
                loadable = core != null,
                seasonNumber = core?.seasonNumber,
                seasonYear = core?.seasonYear,
                currentDayIndex = core?.currentDayIndex,
                managedClubId = core?.managedClubId,
                managedClubName = managedClub?.name,
            )
        }

    /**
     * A career is loadable only when both its metadata envelope and certified
     * core state exist. Orphan rows fail closed instead of fabricating state.
     */
    suspend fun loadCareer(careerId: String): CareerState? {
        if (careerMetadataDao.findById(careerId) == null) return null
        val core = careerCoreStateDao.findById(careerId) ?: return null
        return CareerCoreStateRoomAdapter.state(core)
    }

    /**
     * Preserves the canonical ClubDao sourceFileRef ordering used by the
     * imported corpus. Presentation may group/filter later only when that UI
     * behavior is explicitly proven.
     */
    suspend fun selectableClubs(): List<CareerSelectableClub> =
        clubDao.all().map { club ->
            CareerSelectableClub(
                id = club.id,
                sourceFileRef = club.sourceFileRef,
                name = club.name,
                country = club.country,
                state = club.state,
                level = club.level,
            )
        }
}

data class CareerEntrySummary(
    val careerId: String,
    val displayName: String?,
    val updatedAtEpochMillis: Long,
    val loadable: Boolean,
    val seasonNumber: Int?,
    val seasonYear: Int?,
    val currentDayIndex: Int?,
    val managedClubId: String?,
    val managedClubName: String?,
)

data class CareerSelectableClub(
    val id: String,
    val sourceFileRef: String,
    val name: String,
    val country: Int,
    val state: Int,
    val level: Int,
)
