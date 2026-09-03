package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.domain.career.CareerCommand
import com.leomala.footballdynasty.domain.career.CareerFingerprint
import com.leomala.footballdynasty.domain.career.CareerSimulationEngine
import com.leomala.footballdynasty.domain.career.CareerTransition
import com.leomala.footballdynasty.domain.repository.CareerStateRepository
import com.leomala.footballdynasty.foundation.error.CareerIntegrityException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Serializes state-changing commands so concurrent callers cannot overwrite the same career. */
class CareerSimulationCoordinator(
    private val repository: CareerStateRepository,
    private val engine: CareerSimulationEngine = CareerSimulationEngine(),
) {
    private val mutationMutex = Mutex()

    suspend fun apply(careerId: String, command: CareerCommand): CareerTransition =
        mutationMutex.withLock {
            val current = repository.findById(careerId)
                ?: throw CareerIntegrityException("Career core state $careerId does not exist")
            val transition = engine.apply(current, command)
            val saved = repository.saveTransition(transition.state, command)
            if (CareerFingerprint.of(saved) != CareerFingerprint.of(transition.state)) {
                throw CareerIntegrityException("Persisted career core state differs from transition result")
            }
            transition.copy(state = saved)
        }
}
