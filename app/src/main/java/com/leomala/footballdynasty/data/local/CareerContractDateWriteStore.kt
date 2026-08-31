package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.domain.manager.LegacyContractDateWriteRule
import com.leomala.footballdynasty.domain.manager.LegacyContractWriteInvocation

/**
 * Transactional persistence seam for the recovered legacy `best.o.c(long, boolean)` contract-date write.
 *
 * The caller must provide the contract end value it observed before deciding the write. If the persisted
 * value has moved meanwhile, the transaction fails instead of silently extending from stale state.
 */
class CareerContractDateWriteStore(
    private val database: FootballDynastyDatabase,
) {
    private val playerDao = database.careerPlayerRuntimeDao()

    suspend fun commit(
        careerId: String,
        playerId: String,
        currentCareerTimestampMillis: Long,
        expectedContractEndMillis: Long,
        invocation: LegacyContractWriteInvocation,
    ): Long = database.withTransaction {
        val runtime = requireNotNull(playerDao.findRuntime(careerId, playerId)) {
            "Missing player runtime for contract write $careerId/$playerId"
        }
        require(runtime.contractEndEpochMillis == expectedContractEndMillis) {
            "Stale contract end for $careerId/$playerId: expected=$expectedContractEndMillis actual=${runtime.contractEndEpochMillis}"
        }

        val after = LegacyContractDateWriteRule.endTimestampMillis(
            currentCareerTimestampMillis = currentCareerTimestampMillis,
            currentContractEndMillis = runtime.contractEndEpochMillis,
            invocation = invocation,
        )
        playerDao.upsertRuntime(runtime.copy(contractEndEpochMillis = after))
        after
    }
}
