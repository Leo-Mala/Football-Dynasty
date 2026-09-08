package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.data.local.CareerStadiumRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase

/**
 * Read-only Phase 17 projection of the persisted legacy four-sector stadium runtime.
 *
 * Historical careers without a materialized `career_stadium_runtime` row fail closed: the
 * aggregate source capacity cannot reconstruct stadium expansions that may already have happened.
 */
class CareerStadiumCatalogStore(
    private val database: FootballDynastyDatabase,
    private val runtimeStore: CareerStadiumRuntimeStore = CareerStadiumRuntimeStore(database),
) {
    data class StadiumSnapshot(
        val careerId: String,
        val clubId: String,
        val clubName: String,
        val stadiumName: String?,
        val sectorCapacities: List<Int>,
        val totalCapacity: Long,
    )

    suspend fun loadStadium(careerId: String): StadiumSnapshot? {
        val core = database.careerCoreStateDao().findById(careerId) ?: return null
        val clubId = core.managedClubId ?: return null
        val club = database.clubDao().findById(clubId) ?: return null
        val runtime = runtimeStore.find(careerId, clubId) ?: return null
        val capacities = runtime.capacities.toList()

        return StadiumSnapshot(
            careerId = careerId,
            clubId = clubId,
            clubName = club.name,
            stadiumName = club.stadium.takeIf { it.isNotBlank() },
            sectorCapacities = capacities,
            totalCapacity = capacities.sumOf { it.toLong() },
        )
    }
}
