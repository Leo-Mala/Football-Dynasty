package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerClubTicketRuntimeEntity
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

enum class CareerCoachEmploymentRole {
    OUTGOING,
    INCOMING,
}

data class CareerCoachEmploymentUpdate(
    val role: CareerCoachEmploymentRole,
    val expectedBefore: CareerCoachRuntimeState,
    val after: CareerCoachRuntimeState,
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
    private val managerDao = database.careerManagerRuntimeDao()

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
        persistCoachState(careerId, after)
    }

    /**
     * Atomic persistence seam for the already-characterized employment chain
     * `best.b.G(target,outgoing,incoming) -> f0.l/e`.
     *
     * The caller supplies the in-memory states produced by that rule in exact outgoing->incoming
     * order. This seam persists fields already owned by V9/V11 plus the target club's persisted
     * `Q0()/t1()` control flag when that manager-runtime slice exists. World-history/controlled-club
     * lists and deeper helper effects remain outside this boundary until their own representation
     * is proven; a missing manager-runtime slice is not replaced with an invented default.
     */
    suspend fun commitEmploymentTransition(
        careerId: String,
        targetClubId: String,
        expectedClubState: CareerClubTicketRuntimeState,
        clubLegacyManagerIdAfter: Int,
        updatesInLegacyOrder: List<CareerCoachEmploymentUpdate>,
    ) = database.withTransaction {
        requireNotNull(database.careerMetadataDao().findById(careerId)) { "Missing career $careerId" }
        requireNotNull(database.clubDao().findById(targetClubId)) { "Missing club $targetClubId" }
        requireEmploymentOrder(updatesInLegacyOrder)

        val currentClub = requireNotNull(ticketDao.findClubState(careerId, targetClubId)) {
            "Missing materialized club manager state $careerId/$targetClubId"
        }
        require(
            CareerClubTicketRuntimeState(currentClub.rawDivisionCode, currentClub.legacyManagerId) == expectedClubState
        ) { "Stale club manager state for $careerId/$targetClubId" }

        updatesInLegacyOrder.forEach { update ->
            requireEmploymentMutation(targetClubId, update)
            validateState(update.after)
            val current = requireNotNull(find(careerId, update.expectedBefore.sourceOrdinal)) {
                "Missing materialized V11 coach state for $careerId/${update.expectedBefore.sourceOrdinal}"
            }
            require(current == update.expectedBefore) {
                "Stale coach runtime for $careerId/${update.expectedBefore.sourceOrdinal}"
            }
            persistCoachState(careerId, update.after)
        }

        val incoming = updatesInLegacyOrder.lastOrNull { it.role == CareerCoachEmploymentRole.INCOMING }
        if (incoming == null) {
            require(clubLegacyManagerIdAfter == LegacyManagerIdentityRule.clubStoredManagerId(null)) {
                "Employment transition without incoming manager must leave target club manager absent"
            }
        } else {
            require(clubLegacyManagerIdAfter == incoming.after.legacyManagerId) {
                "Target club manager id must match incoming manager"
            }
        }

        managerDao.findClubRuntime(careerId, targetClubId)?.let { managerRuntime ->
            var activeAfter = managerRuntime.active
            updatesInLegacyOrder.forEach { update ->
                when (update.role) {
                    CareerCoachEmploymentRole.OUTGOING -> {
                        if (update.expectedBefore.isUserControlled) activeAfter = false
                    }
                    CareerCoachEmploymentRole.INCOMING -> {
                        if (update.after.isUserControlled) activeAfter = true
                    }
                }
            }
            if (activeAfter != managerRuntime.active) {
                managerDao.upsertClubRuntime(managerRuntime.copy(active = activeAfter))
            }
        }

        ticketDao.upsertClubState(
            CareerClubTicketRuntimeEntity(
                careerId = careerId,
                clubId = targetClubId,
                rawDivisionCode = currentClub.rawDivisionCode,
                legacyManagerId = clubLegacyManagerIdAfter,
            )
        )
    }

    private suspend fun persistCoachState(
        careerId: String,
        after: CareerCoachRuntimeState,
    ) {
        val manager = requireNotNull(
            ticketDao.managerStates(careerId).firstOrNull { it.sourceOrdinal == after.sourceOrdinal }
        ) { "Missing ordered manager row $careerId/${after.sourceOrdinal}" }
        require(manager.legacyManagerId == after.legacyManagerId) {
            "Manager id ${manager.legacyManagerId} diverges from ${after.legacyManagerId} at ordinal ${after.sourceOrdinal}"
        }
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

    private fun requireEmploymentOrder(updates: List<CareerCoachEmploymentUpdate>) {
        require(updates.size <= 2) { "Legacy employment dispatcher has at most outgoing and incoming managers" }
        val roles = updates.map { it.role }
        require(
            roles == emptyList<CareerCoachEmploymentRole>() ||
                roles == listOf(CareerCoachEmploymentRole.OUTGOING) ||
                roles == listOf(CareerCoachEmploymentRole.INCOMING) ||
                roles == listOf(CareerCoachEmploymentRole.OUTGOING, CareerCoachEmploymentRole.INCOMING)
        ) { "Employment updates must preserve legacy outgoing -> incoming order" }
    }

    private fun requireEmploymentMutation(
        targetClubId: String,
        update: CareerCoachEmploymentUpdate,
    ) {
        val before = update.expectedBefore
        val after = update.after
        require(after.sourceOrdinal == before.sourceOrdinal)
        require(after.legacyManagerId == before.legacyManagerId)
        require(after.isUserControlled == before.isUserControlled)
        require(after.alternativeClubId == before.alternativeClubId)
        require(after.rawD == before.rawD)
        require(after.rawE == before.rawE)
        require(after.rawF == before.rawF)
        require(after.rawO == before.rawO)
        require(after.records == before.records)

        when (update.role) {
            CareerCoachEmploymentRole.OUTGOING -> {
                require(before.currentClubId == targetClubId) {
                    "Outgoing manager must belong to target club before legacy l()"
                }
                require(after.currentClubId == null) { "Outgoing manager current club must be cleared" }
                require(after.previousClubId == targetClubId) { "Outgoing previous club must capture target club" }
                require(after.rawG == before.rawG)
                require(after.rawH == before.rawH)
                require(after.rawM == before.rawM)
            }

            CareerCoachEmploymentRole.INCOMING -> {
                require(after.currentClubId == targetClubId) { "Incoming manager must join target club" }
                require(after.previousClubId == before.previousClubId)
                require(after.previousClubCountry == before.previousClubCountry)
                require(after.previousClubDivisionIndex == before.previousClubDivisionIndex)
                require(after.rawG == 100) { "Legacy f0.e sets G=100" }
                require(after.rawH == 80) { "Legacy f0.e sets H=80" }
                require(after.rawM == 0) { "Legacy f0.e sets M=0" }
            }
        }
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
