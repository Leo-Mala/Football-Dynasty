package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.domain.career.CareerIntegrityValidator
import com.leomala.footballdynasty.domain.career.CareerRandomState
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.foundation.random.RandomSource
import com.leomala.footballdynasty.foundation.random.StatefulJavaRandomSource
import com.leomala.footballdynasty.foundation.random.StatefulRandomSnapshot

data class CareerManagerProgressionRandomResult<T>(
    val value: T,
    val stateAfter: CareerState,
)

/**
 * Atomic persisted RNG boundary for manager/career progression outside the match engine.
 *
 * `best.n.m()` and replacement-manager candidate selection already consume the explicit
 * [RandomSource]. This store restores that source from the certified career core state and commits
 * only its advanced snapshot. Callers may perform V9/V11 manager writes inside [run]; Room nests
 * those writes in this transaction, so a failure rolls RNG and manager state back together.
 *
 * The operation may not mutate `career_core_state` itself. That keeps this seam narrow and prevents
 * an RNG-only progression step from silently overwriting calendar/season/managed-club changes.
 */
class CareerManagerProgressionRandomStore(
    private val database: FootballDynastyDatabase,
    private val clockMillis: () -> Long = System::currentTimeMillis,
) {
    suspend fun <T> run(
        expectedBefore: CareerState,
        operation: suspend (RandomSource) -> T,
    ): CareerManagerProgressionRandomResult<T> = database.withTransaction {
        CareerIntegrityValidator.validate(expectedBefore)
        val coreDao = database.careerCoreStateDao()
        val current = requireNotNull(coreDao.findById(expectedBefore.id)) {
            "Missing persisted career core state ${expectedBefore.id}"
        }
        require(CareerCoreStateRoomAdapter.state(current) == expectedBefore) {
            "Stale career core state for manager progression ${expectedBefore.id}"
        }

        val random = StatefulJavaRandomSource.restore(
            StatefulRandomSnapshot(
                initialSeed = expectedBefore.random.initialSeed,
                internalState = expectedBefore.random.internalState,
                draws = expectedBefore.random.draws,
            )
        )
        val value = operation(random)

        val afterOperation = requireNotNull(coreDao.findById(expectedBefore.id)) {
            "Career core state disappeared during manager progression ${expectedBefore.id}"
        }
        require(CareerCoreStateRoomAdapter.state(afterOperation) == expectedBefore) {
            "Manager progression operation must not mutate career core state directly"
        }

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

        CareerManagerProgressionRandomResult(
            value = value,
            stateAfter = stateAfter,
        )
    }
}
