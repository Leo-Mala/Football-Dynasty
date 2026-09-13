package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.data.local.CareerStadiumRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase

/**
 * Read-only Phase 17 projection of the persisted legacy four-sector stadium runtime.
 *
 * Historical careers without a materialized `career_stadium_runtime` row fail closed: the
 * aggregate source capacity cannot reconstruct stadium expansions that may already have happened.
 *
 * Persisted construction ownership is projected only when every construction row has explicit
 * modern ownership metadata. Migrated rows with unknown ownership make only the construction
 * subsection unavailable; current persisted capacities remain authoritative and readable.
 */
class CareerStadiumCatalogStore(
    private val database: FootballDynastyDatabase,
    private val runtimeStore: CareerStadiumRuntimeStore = CareerStadiumRuntimeStore(database),
) {
    data class ConstructionSnapshot(
        val sourceOrdinal: Int,
        val endTimestampMillis: Long,
        val additions: List<Int>,
    )

    data class StadiumSnapshot(
        val careerId: String,
        val clubId: String,
        val clubName: String,
        val stadiumName: String?,
        val sectorCapacities: List<Int>,
        val totalCapacity: Long,
        val constructions: List<ConstructionSnapshot>?,
    )

    suspend fun loadStadium(careerId: String): StadiumSnapshot? {
        val core = database.careerCoreStateDao().findById(careerId) ?: return null
        val clubId = core.managedClubId ?: return null
        val club = database.clubDao().findById(clubId) ?: return null
        val runtime = runtimeStore.find(careerId, clubId) ?: return null
        val capacities = runtime.capacities.toList()
        val constructionRows = database.careerManagerRuntimeDao().stadiumConstructions(careerId)
        val constructions = if (constructionRows.any { it.ownerClubId == null }) {
            null
        } else {
            constructionRows
                .filter { it.ownerClubId == clubId }
                .map { row ->
                    ConstructionSnapshot(
                        sourceOrdinal = row.sourceOrdinal,
                        endTimestampMillis = row.endTimestampMillis,
                        additions = listOf(
                            row.addition0,
                            row.addition1,
                            row.addition2,
                            row.addition3,
                        ),
                    )
                }
        }

        return StadiumSnapshot(
            careerId = careerId,
            clubId = clubId,
            clubName = club.name,
            stadiumName = club.stadium.takeIf { it.isNotBlank() },
            sectorCapacities = capacities,
            totalCapacity = capacities.sumOf { it.toLong() },
            constructions = constructions,
        )
    }
}
