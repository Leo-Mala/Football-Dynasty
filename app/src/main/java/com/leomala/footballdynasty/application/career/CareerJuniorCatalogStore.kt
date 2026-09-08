package com.leomala.footballdynasty.application.career

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.CareerCoreStateRoomAdapter
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase

/**
 * Read-only Phase 17 projection of the managed club's persisted pre-promotion junior drafts.
 *
 * Only semantics already proved by the characterized legacy runtime are surfaced: list order,
 * name, age (`legacyC`) and position (`legacyE`). Remaining numeric legacy fields stay opaque and
 * are intentionally not relabeled as strength, potential, value, eligibility or priority.
 */
class CareerJuniorCatalogStore(
    private val database: FootballDynastyDatabase,
) {
    data class JuniorSquad(
        val careerId: String,
        val clubId: String,
        val juniors: List<JuniorRow>,
    )

    data class JuniorRow(
        val sourceOrdinal: Int,
        val name: String,
        val age: Int,
        val position: Int,
    )

    suspend fun loadJuniorSquad(careerId: String): JuniorSquad? = database.withTransaction {
        require(careerId.isNotBlank()) { "Career id must not be blank" }

        val coreEntity = database.careerCoreStateDao().findById(careerId)
            ?: return@withTransaction null
        val state = CareerCoreStateRoomAdapter.state(coreEntity)
        val clubId = state.managedClub?.clubId ?: return@withTransaction null
        if (database.clubDao().findById(clubId) == null) {
            return@withTransaction null
        }

        val juniors = database.careerJuniorDraftDao().listForClub(careerId, clubId).map { draft ->
            JuniorRow(
                sourceOrdinal = draft.sourceOrdinal,
                name = draft.name,
                age = draft.legacyC,
                position = draft.legacyE,
            )
        }

        JuniorSquad(
            careerId = careerId,
            clubId = clubId,
            juniors = juniors,
        )
    }
}
