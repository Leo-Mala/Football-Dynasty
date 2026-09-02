package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerCoachRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerCoachSeasonClubRecordEntity
import com.leomala.footballdynasty.domain.manager.LegacyCoachSeasonClubRecord
import com.leomala.footballdynasty.domain.manager.LegacyManagerIdentityRule

/** Lossless V11 projection of one concrete manager ArrayList entry plus its V9 H field. */
data class CareerCoachRuntimeState(
    val sourceOrdinal: Int,
    val legacyManagerId: Int,
    val isUserControlled: Boolean,
    val currentClubId: String?,
    val alternativeClubId: String?,
    val previousClubId: String?,
    val previousClubCountry: Int?,
    val previousClubDivisionIndex: Int?,
    val rawG: Int,
    val rawH: Int,
    val rawD: Int,
    val rawE: Int,
    val rawF: Int,
    val rawO: Int,
    val rawM: Int,
    val records: List<LegacyCoachSeasonClubRecord>,
)

/**
 * Additive/fail-closed V11 manager persistence.
 *
 * No V10 row can be promoted into this state by migration because V10 does not contain G/D/E/F/o,
 * manager associations or ordered season+club records. Callers must explicitly materialize all
 * proven source/runtime values. H is checked against and written back to the pre-existing ordered
 * manager row so there is exactly one persisted H source.
 */
class CareerCoachRuntimeStore(private val database: FootballDynastyDatabase) {
    private val coachDao = database.careerCoachRuntimeDao()
    private val ticketDao = database.careerTicketRuntimeDao()

    suspend fun materialize(
        careerId: String,
        state: CareerCoachRuntimeState,
    ) = database.withTransaction {
        requireNotNull(database.careerMetadataDao().findById(careerId)) { "Missing career $careerId" }
        validateState(state)
        val manager = requireNotNull(
            ticketDao.managerStates(careerId).firstOrNull { it.sourceOrdinal == state.sourceOrdinal }
        ) { "Missing ordered manager row $careerId/${state.sourceOrdinal}" }
        require(manager.legacyManagerId == state.legacyManagerId) {
            "Manager id ${manager.legacyManagerId} diverges from ${state.legacyManagerId} at ordinal ${state.sourceOrdinal}"
        }
        require(manager.rawH == state.rawH) {
            "Materialized H ${state.rawH} diverges from certified V9 H ${manager.rawH}"
        }
        coachDao.upsertCoachRuntime(state.toEntity(careerId))
        replaceRecords(careerId, state.sourceOrdinal, state.records)
    }

    suspend fun find(
        careerId: String,
        sourceOrdinal: Int,
    ): CareerCoachRuntimeState? {
        val entity = coachDao.findCoachRuntime(careerId, sourceOrdinal) ?: return null
        val manager = requireNotNull(
            ticketDao.managerStates(careerId).firstOrNull { it.sourceOrdinal == sourceOrdinal }
        ) { "V11 coach row has no parent ordered manager $careerId/$sourceOrdinal" }
        return entity.toDomain(
            legacyManagerId = manager.legacyManagerId,
            rawH = manager.rawH,
            records = coachDao.seasonClubRecords(careerId, sourceOrdinal).map { it.toDomain() },
        )
    }

    /** `best.b.b1(id)` first-match semantics. Missing manager id skips; missing V11 state blocks. */
    suspend fun resolveFirstCoachState(
        careerId: String,
        legacyManagerId: Int,
    ): CareerCoachRuntimeState? {
        if (legacyManagerId == LegacyManagerIdentityRule.clubStoredManagerId(null)) return null
        val manager = ticketDao.managerStates(careerId).firstOrNull { it.legacyManagerId == legacyManagerId }
            ?: return null
        return requireNotNull(find(careerId, manager.sourceOrdinal)) {
            "Missing materialized V11 coach state for manager id $legacyManagerId at ${manager.sourceOrdinal}"
        }
    }

    /**
     * Stale-state guarded post-match persistence. The caller must already have applied legacy
     * `j(match) -> i(match)` in memory. Fields outside that exact post-match mutation surface are
     * required to stay byte-for-byte equivalent at this boundary.
     */
    suspend fun commitPostMatch(
        careerId: String,
        expectedBefore: CareerCoachRuntimeState,
        after: CareerCoachRuntimeState,
    ) = database.withTransaction {
        requirePostMatchStableContext(expectedBefore, after)
        validateState(after)
        val current = requireNotNull(find(careerId, expectedBefore.sourceOrdinal)) {
            "Missing materialized V11 coach state for $careerId/${expectedBefore.sourceOrdinal}"
        }
        require(current == expectedBefore) {
            "Stale coach runtime for $careerId/${expectedBefore.sourceOrdinal}"
        }
        val manager = requireNotNull(
            ticketDao.managerStates(careerId).firstOrNull { it.sourceOrdinal == expectedBefore.sourceOrdinal }
        )
        ticketDao.upsertManagerStates(listOf(manager.copy(rawH = after.rawH)))
        coachDao.upsertCoachRuntime(after.toEntity(careerId))
        replaceRecords(careerId, after.sourceOrdinal, after.records)
    }

    private suspend fun replaceRecords(
        careerId: String,
        managerSourceOrdinal: Int,
        records: List<LegacyCoachSeasonClubRecord>,
    ) {
        coachDao.deleteSeasonClubRecords(careerId, managerSourceOrdinal)
        coachDao.upsertSeasonClubRecords(
            records.mapIndexed { index, record -> record.toEntity(careerId, managerSourceOrdinal, index) }
        )
    }

    private fun validateState(state: CareerCoachRuntimeState) {
        require(state.sourceOrdinal >= 0) { "Manager source ordinal must be non-negative" }
        require(state.legacyManagerId != LegacyManagerIdentityRule.clubStoredManagerId(null)) {
            "Absent manager id cannot carry V11 coach runtime"
        }
        require(state.rawG in 0..100) { "Legacy manager G must stay clamped" }
        require(state.rawH in 0..100) { "Legacy manager H must stay clamped" }
    }

    private fun requirePostMatchStableContext(
        before: CareerCoachRuntimeState,
        after: CareerCoachRuntimeState,
    ) {
        require(after.sourceOrdinal == before.sourceOrdinal)
        require(after.legacyManagerId == before.legacyManagerId)
        require(after.isUserControlled == before.isUserControlled)
        require(after.currentClubId == before.currentClubId)
        require(after.alternativeClubId == before.alternativeClubId)
        require(after.previousClubId == before.previousClubId)
        require(after.previousClubCountry == before.previousClubCountry)
        require(after.previousClubDivisionIndex == before.previousClubDivisionIndex)
        require(after.rawM == before.rawM)
    }

    private fun CareerCoachRuntimeState.toEntity(careerId: String) = CareerCoachRuntimeEntity(
        careerId = careerId,
        managerSourceOrdinal = sourceOrdinal,
        isUserControlled = isUserControlled,
        currentClubId = currentClubId,
        alternativeClubId = alternativeClubId,
        previousClubId = previousClubId,
        previousClubCountry = previousClubCountry,
        previousClubDivisionIndex = previousClubDivisionIndex,
        rawG = rawG,
        rawD = rawD,
        rawE = rawE,
        rawF = rawF,
        rawO = rawO,
        rawM = rawM,
    )

    private fun CareerCoachRuntimeEntity.toDomain(
        legacyManagerId: Int,
        rawH: Int,
        records: List<LegacyCoachSeasonClubRecord>,
    ) = CareerCoachRuntimeState(
        sourceOrdinal = managerSourceOrdinal,
        legacyManagerId = legacyManagerId,
        isUserControlled = isUserControlled,
        currentClubId = currentClubId,
        alternativeClubId = alternativeClubId,
        previousClubId = previousClubId,
        previousClubCountry = previousClubCountry,
        previousClubDivisionIndex = previousClubDivisionIndex,
        rawG = rawG,
        rawH = rawH,
        rawD = rawD,
        rawE = rawE,
        rawF = rawF,
        rawO = rawO,
        rawM = rawM,
        records = records,
    )

    private fun LegacyCoachSeasonClubRecord.toEntity(
        careerId: String,
        managerSourceOrdinal: Int,
        sourceOrdinal: Int,
    ) = CareerCoachSeasonClubRecordEntity(
        careerId = careerId,
        managerSourceOrdinal = managerSourceOrdinal,
        sourceOrdinal = sourceOrdinal,
        legacySeasonId = seasonId,
        legacyClubId = legacyClubId,
        rawMatches = rawMatches,
        rawWins = rawWins,
        rawLosses = rawLosses,
        rawPoints = rawPoints,
        rawOtherCount = rawOtherCount,
    )

    private fun CareerCoachSeasonClubRecordEntity.toDomain() = LegacyCoachSeasonClubRecord(
        seasonId = legacySeasonId,
        legacyClubId = legacyClubId,
        rawMatches = rawMatches,
        rawWins = rawWins,
        rawLosses = rawLosses,
        rawPoints = rawPoints,
        rawOtherCount = rawOtherCount,
    )
}
