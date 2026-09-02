package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerStadiumConstructionEntity
import com.leomala.footballdynasty.data.local.entity.CareerStadiumRuntimeEntity
import com.leomala.footballdynasty.domain.manager.LegacyStadiumConstructionRecord
import com.leomala.footballdynasty.domain.manager.LegacyStadiumConstructionRule
import com.leomala.footballdynasty.domain.manager.LegacyStadiumConstructionSweep

/**
 * Phase 13 completion boundary for `best.b.e4()`.
 *
 * The legacy sweep decides only which ordered records are complete and which positive additions
 * apply. Ownership is not inferred from the opaque legacy stadium code. New V10 records carry the
 * club context captured when construction starts; migrated V9 records have no owner and fail closed
 * before any deletion or capacity mutation.
 */
class CareerStadiumConstructionCompletionStore(
    private val database: FootballDynastyDatabase,
) {
    private val dao = database.careerManagerRuntimeDao()

    suspend fun sweepAndApply(
        careerId: String,
        currentTimestampMillis: Long,
    ): LegacyStadiumConstructionSweep = database.withTransaction {
        val entities = dao.stadiumConstructions(careerId)
        val records = entities.map { it.toDomain() }
        val sweep = LegacyStadiumConstructionRule.sweepCompleted(records, currentTimestampMillis)
        if (sweep.completed.isEmpty()) return@withTransaction sweep

        val completedIndexes = sweep.completed.mapTo(mutableSetOf()) { it.recordIndex }
        val stadiumStates = linkedMapOf<String, CareerStadiumRuntimeEntity>()

        // Resolve every required owner/state first. Any migrated unknown ownership fails before
        // touching capacities or the ordered construction list.
        sweep.completed.forEach { completed ->
            val entity = entities[completed.recordIndex]
            val ownerClubId = requireNotNull(entity.ownerClubId) {
                "Missing persisted owner for completed stadium construction ${entity.sourceOrdinal}"
            }
            if (ownerClubId !in stadiumStates) {
                stadiumStates[ownerClubId] = requireNotNull(dao.findStadiumRuntime(careerId, ownerClubId)) {
                    "Missing materialized stadium runtime $careerId/$ownerClubId"
                }
            }
        }

        // Apply in the exact source-order produced by best.b.e4(). Multiple completed records for
        // one club accumulate sequentially in the same transaction.
        sweep.completed.forEach { completed ->
            val entity = entities[completed.recordIndex]
            val ownerClubId = requireNotNull(entity.ownerClubId)
            val current = requireNotNull(stadiumStates[ownerClubId])
            val add = completed.positiveAdditionsByCategory
            require(add.size == 4)
            stadiumStates[ownerClubId] = current.copy(
                sector0Capacity = current.sector0Capacity + add[0],
                sector1Capacity = current.sector1Capacity + add[1],
                sector2Capacity = current.sector2Capacity + add[2],
                sector3Capacity = current.sector3Capacity + add[3],
            )
        }
        stadiumStates.values.forEach { dao.upsertStadiumRuntime(it) }

        // Legacy removes completed records only after scanning the source-ordered list. Preserve
        // modern ownership metadata on every remaining record while compacting source ordinals.
        val remaining = entities.filterIndexed { index, _ -> index !in completedIndexes }
        dao.deleteStadiumConstructions(careerId)
        dao.upsertStadiumConstructions(
            remaining.mapIndexed { index, entity -> entity.copy(sourceOrdinal = index) }
        )
        sweep
    }

    private fun CareerStadiumConstructionEntity.toDomain() = LegacyStadiumConstructionRecord(
        stadiumCode = stadiumCode,
        endTimestampMillis = endTimestampMillis,
        additions = listOf(addition0, addition1, addition2, addition3),
    )
}
