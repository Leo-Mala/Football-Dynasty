package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerProceduralPlayerEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.domain.career.CareerIntegrityValidator
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.LegacyAnnualPlayerMovementRules
import com.leomala.footballdynasty.foundation.error.CareerIntegrityException

/** Transactional persistence boundary for player state that belongs to one career/save. */
class CareerPlayerRuntimeStore(
    private val database: FootballDynastyDatabase,
    private val clockMillis: () -> Long = System::currentTimeMillis,
) {
    data class PlayerSnapshot(
        val runtime: CareerPlayerRuntimeEntity,
        val procedural: CareerProceduralPlayerEntity?,
        val membership: CareerSquadMembershipEntity?,
    )

    data class AnnualMovementPersistenceResult(
        val snapshot: PlayerSnapshot,
        val deferredSourceCode1Amount: Int?,
        val deferredTargetCode1Amount: Int?,
        val deferredSourceSpecialReferenceClear: Boolean,
        val deferredSourceE1Clear: Boolean,
    )

    suspend fun saveCanonicalRuntime(entity: CareerPlayerRuntimeEntity) {
        require(entity.sourceType == SOURCE_CANONICAL) {
            "Canonical runtime must use sourceType=$SOURCE_CANONICAL"
        }
        requireIdentity(entity.careerId, entity.playerId)
        database.careerPlayerRuntimeDao().upsertRuntime(entity)
    }

    suspend fun saveCanonicalPlayer(
        runtime: CareerPlayerRuntimeEntity,
        membership: CareerSquadMembershipEntity,
    ): PlayerSnapshot {
        require(runtime.sourceType == SOURCE_CANONICAL) {
            "Canonical runtime must use sourceType=$SOURCE_CANONICAL"
        }
        requireIdentity(runtime.careerId, runtime.playerId)
        requireSameIdentity(runtime.careerId, runtime.playerId, membership.careerId, membership.playerId)
        return database.withTransaction {
            val dao = database.careerPlayerRuntimeDao()
            dao.upsertRuntime(runtime)
            dao.upsertMembership(membership)
            PlayerSnapshot(runtime, procedural = null, membership = membership)
        }
    }

    /**
     * Commits the complete new-career canonical runtime and the RNG state that produced it in one
     * transaction. This prevents a reopen from replaying q1() draws against already-created state.
     */
    suspend fun initializeCanonicalPlayersAndCareerState(
        state: CareerState,
        bundles: List<CareerPlayerRuntimeMapper.CanonicalBundle>,
    ) {
        validateCareerStateOwner(state)
        bundles.forEach { bundle ->
            require(bundle.runtime.sourceType == SOURCE_CANONICAL)
            requireSameIdentity(
                state.id,
                bundle.runtime.playerId,
                bundle.membership.careerId,
                bundle.membership.playerId,
            )
            require(bundle.runtime.careerId == state.id) { "Runtime belongs to another career" }
        }
        database.withTransaction {
            val dao = database.careerPlayerRuntimeDao()
            bundles.forEach { bundle ->
                dao.upsertRuntime(bundle.runtime)
                dao.upsertMembership(bundle.membership)
            }
            database.careerCoreStateDao().upsert(
                CareerCoreStateRoomAdapter.entity(state, clockMillis())
            )
        }
    }

    suspend fun saveProceduralPlayer(
        runtime: CareerPlayerRuntimeEntity,
        procedural: CareerProceduralPlayerEntity,
        membership: CareerSquadMembershipEntity,
    ): PlayerSnapshot {
        validateProceduralIdentity(runtime, procedural, membership)
        return database.withTransaction {
            persistProcedural(runtime, procedural, membership)
        }
    }

    /**
     * Commits a generated player and the exact post-generation RNG snapshot in one Room
     * transaction. A crash/reopen can therefore never observe the player with the pre-generation
     * RNG state, or consume the draws without the player row.
     */
    suspend fun saveProceduralPlayerAndCareerState(
        state: CareerState,
        runtime: CareerPlayerRuntimeEntity,
        procedural: CareerProceduralPlayerEntity,
        membership: CareerSquadMembershipEntity,
    ): PlayerSnapshot {
        validateCareerStateOwner(state)
        require(runtime.careerId == state.id) { "Runtime belongs to another career" }
        validateProceduralIdentity(runtime, procedural, membership)
        return database.withTransaction {
            val snapshot = persistProcedural(runtime, procedural, membership)
            database.careerCoreStateDao().upsert(
                CareerCoreStateRoomAdapter.entity(state, clockMillis())
            )
            snapshot
        }
    }

    /**
     * Persists the player-local + membership effects of the proven annual
     * `T1(destination, A0(), false, false, false)` call in one transaction.
     *
     * Finance ledger writes and source-club special-reference mutations are deliberately returned
     * as deferred effects because those subsystems are not represented by the current modern schema.
     */
    suspend fun applyAnnualMovement(
        careerId: String,
        playerId: String,
        targetClubId: String,
        rosterKind: String,
        sourceOrdinal: Int,
        contractEndEpochMillis: Long,
        plan: LegacyAnnualPlayerMovementRules.AnnualT1Plan,
    ): AnnualMovementPersistenceResult {
        requireIdentity(careerId, playerId)
        require(targetClubId.isNotBlank()) { "Target club id must not be blank" }
        require(rosterKind.isNotBlank()) { "Roster kind must not be blank" }
        require(sourceOrdinal >= 0) { "Source ordinal must be non-negative" }
        require(plan.relinkToTarget && plan.addToTarget) { "Annual T1 plan must relink/add to target" }
        require(plan.resetX && plan.resetZ && plan.leaveYUnchanged) {
            "Annual T1 plan does not match the characterized player-local path"
        }

        return database.withTransaction {
            val dao = database.careerPlayerRuntimeDao()
            val current = requireNotNull(dao.findRuntime(careerId, playerId)) {
                "Missing runtime for career=$careerId player=$playerId"
            }
            val updated = current.copy(
                contractEndEpochMillis = contractEndEpochMillis,
                legacyPreviousMarketValue = current.marketValue,
                legacyQ = current.legacyQ || plan.setS1True,
                legacyX = false,
                legacyY = current.legacyY,
                legacyZ = false,
            )
            val membership = CareerSquadMembershipEntity(
                careerId = careerId,
                playerId = playerId,
                clubId = targetClubId,
                rosterKind = rosterKind,
                sourceOrdinal = sourceOrdinal,
            )
            dao.upsertRuntime(updated)
            dao.upsertMembership(membership)
            AnnualMovementPersistenceResult(
                snapshot = PlayerSnapshot(
                    runtime = updated,
                    procedural = dao.findProceduralPlayer(careerId, playerId),
                    membership = membership,
                ),
                deferredSourceCode1Amount = plan.sourceBCode1Amount,
                deferredTargetCode1Amount = plan.targetDCode1Amount,
                deferredSourceSpecialReferenceClear = plan.clearSourceSpecialReferences,
                deferredSourceE1Clear = plan.clearSourceE1,
            )
        }
    }

    suspend fun find(careerId: String, playerId: String): PlayerSnapshot? = database.withTransaction {
        val dao = database.careerPlayerRuntimeDao()
        val runtime = dao.findRuntime(careerId, playerId) ?: return@withTransaction null
        PlayerSnapshot(
            runtime = runtime,
            procedural = dao.findProceduralPlayer(careerId, playerId),
            membership = dao.findMembership(careerId, playerId),
        )
    }

    private suspend fun persistProcedural(
        runtime: CareerPlayerRuntimeEntity,
        procedural: CareerProceduralPlayerEntity,
        membership: CareerSquadMembershipEntity,
    ): PlayerSnapshot {
        val dao = database.careerPlayerRuntimeDao()
        dao.upsertRuntime(runtime)
        dao.upsertProceduralPlayer(procedural)
        dao.upsertMembership(membership)
        return PlayerSnapshot(runtime, procedural, membership)
    }

    private suspend fun validateCareerStateOwner(state: CareerState) {
        CareerIntegrityValidator.validate(state)
        if (database.careerMetadataDao().findById(state.id) == null) {
            throw CareerIntegrityException("Career metadata ${state.id} must exist before player runtime")
        }
        state.managedClub?.let { managed ->
            if (database.clubDao().findById(managed.clubId) == null) {
                throw CareerIntegrityException("Managed club ${managed.clubId} does not resolve")
            }
        }
    }

    private fun validateProceduralIdentity(
        runtime: CareerPlayerRuntimeEntity,
        procedural: CareerProceduralPlayerEntity,
        membership: CareerSquadMembershipEntity,
    ) {
        require(runtime.sourceType == SOURCE_PROCEDURAL) {
            "Procedural runtime must use sourceType=$SOURCE_PROCEDURAL"
        }
        requireIdentity(runtime.careerId, runtime.playerId)
        requireSameIdentity(runtime.careerId, runtime.playerId, procedural.careerId, procedural.playerId)
        requireSameIdentity(runtime.careerId, runtime.playerId, membership.careerId, membership.playerId)
    }

    private fun requireIdentity(careerId: String, playerId: String) {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        require(playerId.isNotBlank()) { "Player id must not be blank" }
    }

    private fun requireSameIdentity(
        careerId: String,
        playerId: String,
        otherCareerId: String,
        otherPlayerId: String,
    ) {
        require(careerId == otherCareerId && playerId == otherPlayerId) {
            "Career/player identity mismatch"
        }
    }

    companion object {
        const val SOURCE_CANONICAL = "CANONICAL"
        const val SOURCE_PROCEDURAL = "PROCEDURAL"
        const val RUNTIME_STATE_VERSION = 1
    }
}
