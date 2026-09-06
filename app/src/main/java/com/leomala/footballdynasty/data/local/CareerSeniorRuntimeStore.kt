package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.domain.career.CareerIntegrityValidator
import com.leomala.footballdynasty.domain.career.CareerRandomState
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.LegacyAnnualSeniorProgressionRules
import com.leomala.footballdynasty.domain.career.LegacySeniorPayrollScalarRules
import com.leomala.footballdynasty.foundation.random.StatefulJavaRandomSource
import com.leomala.footballdynasty.foundation.random.StatefulRandomSnapshot

/** Atomic result of one persisted legacy `best.o.e()` call. */
data class CareerSeniorProgressionResult(
    val runtime: com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity,
    val stateAfter: CareerState,
)

/**
 * Durable Phase 15 boundary for serialized senior fields `best.o.M/N/n`.
 *
 * Annual progression restores the career's stateful java.util.Random, composes the already-certified
 * `best.o.e()->s()/t()` rules, and commits player M/N/overall plus the advanced RNG snapshot in one
 * Room transaction. A V14 row with unknown M/N fails closed instead of inventing historical state.
 */
class CareerSeniorRuntimeStore(
    private val database: FootballDynastyDatabase,
    private val clockMillis: () -> Long = System::currentTimeMillis,
) {
    suspend fun applyAnnualProgression(
        expectedBefore: CareerState,
        playerId: String,
        club: LegacyAnnualSeniorProgressionRules.ClubState,
        legacyD0: Int,
        legacyM: Int,
    ): CareerSeniorProgressionResult = database.withTransaction {
        CareerIntegrityValidator.validate(expectedBefore)
        require(playerId.isNotBlank()) { "Player id must not be blank" }

        val coreDao = database.careerCoreStateDao()
        val persistedCore = requireNotNull(coreDao.findById(expectedBefore.id)) {
            "Missing persisted career core state ${expectedBefore.id}"
        }
        require(CareerCoreStateRoomAdapter.state(persistedCore) == expectedBefore) {
            "Stale career core state for senior progression ${expectedBefore.id}"
        }

        val playerDao = database.careerPlayerRuntimeDao()
        val current = requireNotNull(playerDao.findRuntime(expectedBefore.id, playerId)) {
            "Missing senior runtime for career=${expectedBefore.id} player=$playerId"
        }
        val currentM = requireNotNull(current.legacyAnnualM) {
            "Legacy best.o.M is unknown for migrated V14 player $playerId"
        }
        val currentN = requireNotNull(current.legacyAnnualN) {
            "Legacy best.o.N is unknown for migrated V14 player $playerId"
        }

        val random = StatefulJavaRandomSource.restore(
            StatefulRandomSnapshot(
                initialSeed = expectedBefore.random.initialSeed,
                internalState = expectedBefore.random.internalState,
                draws = expectedBefore.random.draws,
            )
        )
        val progressed = LegacyAnnualSeniorProgressionRules.apply(
            club = club,
            player = LegacyAnnualSeniorProgressionRules.PlayerState(
                age = current.age,
                overall = current.overall,
                legacyN = currentN,
                legacyMFlag = currentM,
                legacyD0 = legacyD0,
                legacyM = legacyM,
                legacyStar = current.star,
                legacyWorldTop = current.worldTop,
            ),
            random = random,
        )
        val updated = current.copy(
            overall = progressed.overall,
            legacyAnnualM = progressed.legacyMFlag,
            legacyAnnualN = progressed.legacyN,
        )
        playerDao.upsertRuntime(updated)

        val snapshot = random.snapshot()
        val stateAfter = expectedBefore.copy(
            random = CareerRandomState(
                initialSeed = snapshot.initialSeed,
                internalState = snapshot.internalState,
                draws = snapshot.draws,
            )
        )
        CareerIntegrityValidator.validate(stateAfter)
        coreDao.upsert(CareerCoreStateRoomAdapter.entity(stateAfter, clockMillis()))

        CareerSeniorProgressionResult(updated, stateAfter)
    }

    /**
     * Exact persisted writer for `best.o.o()` -> `best.o.n`.
     *
     * Position is resolved from the same canonical/procedural source that created the runtime; age,
     * overall and c/d are read from the save itself. Only the still-obfuscated club/global inputs are
     * supplied by the caller. No cached salary is reconstructed during migration.
     */
    suspend fun recomputeRawPayroll(
        careerId: String,
        playerId: String,
        club: LegacySeniorPayrollScalarRules.ClubState,
        legacyGlobalV1: Boolean,
    ): Int = database.withTransaction {
        require(careerId.isNotBlank() && playerId.isNotBlank())
        val dao = database.careerPlayerRuntimeDao()
        val current = requireNotNull(dao.findRuntime(careerId, playerId)) {
            "Missing senior runtime for career=$careerId player=$playerId"
        }
        val legacyG = when (current.sourceType) {
            CareerPlayerRuntimeStore.SOURCE_CANONICAL -> requireNotNull(
                database.playerDao().findById(playerId)
            ) { "Missing canonical player $playerId" }.position

            CareerPlayerRuntimeStore.SOURCE_PROCEDURAL -> requireNotNull(
                dao.findProceduralPlayer(careerId, playerId)
            ) { "Missing procedural player $playerId" }.position

            else -> error("Unknown player runtime sourceType=${current.sourceType}")
        }
        val raw = LegacySeniorPayrollScalarRules.calculate(
            club = club,
            player = LegacySeniorPayrollScalarRules.PlayerState(
                legacyG = legacyG,
                legacyJ = current.overall,
                legacyE = current.age,
                legacyC = current.star,
                legacyD = current.worldTop,
            ),
            legacyGlobalV1 = legacyGlobalV1,
        )
        dao.upsertRuntime(current.copy(legacyRawPayrollN = raw))
        raw
    }

    suspend fun requireRawPayroll(careerId: String, playerId: String): Int {
        val runtime = requireNotNull(database.careerPlayerRuntimeDao().findRuntime(careerId, playerId)) {
            "Missing senior runtime for career=$careerId player=$playerId"
        }
        return requireNotNull(runtime.legacyRawPayrollN) {
            "Legacy best.o.n is unknown for career=$careerId player=$playerId"
        }
    }
}
