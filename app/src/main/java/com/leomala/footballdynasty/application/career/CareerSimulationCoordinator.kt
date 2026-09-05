package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.domain.career.CareerCommand
import com.leomala.footballdynasty.domain.career.CareerFingerprint
import com.leomala.footballdynasty.domain.career.CareerSimulationEngine
import com.leomala.footballdynasty.domain.career.CareerTransition
import com.leomala.footballdynasty.domain.repository.CareerStateRepository
import com.leomala.footballdynasty.foundation.error.InvalidCareerStateException
import com.leomala.footballdynasty.foundation.error.SimulationConflictException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Serializes career mutations and keeps persistence outside the pure transition engine. */
class CareerSimulationCoordinator(
    private val repository: CareerStateRepository,
    private val engine: CareerSimulationEngine = CareerSimulationEngine(),
    private val mutationMutex: Mutex = Mutex(),
    private val logger: CareerSimulationLogger = NoOpCareerSimulationLogger,
    private val nanoTime: () -> Long = System::nanoTime,
) {
    suspend fun apply(careerId: String, command: CareerCommand): CareerTransition =
        mutationMutex.withLock {
            val started = nanoTime()
            val current = repository.findById(careerId)
                ?: throw InvalidCareerStateException("Career $careerId does not exist")
            val transition = engine.apply(current, command)
            val saved = repository.saveTransition(transition.state, command)
            val savedFingerprint = CareerFingerprint.of(saved)
            if (savedFingerprint != CareerFingerprint.of(transition.state)) {
                throw SimulationConflictException("Persisted career state diverged from transition result")
            }
            logger.transition(
                careerId = careerId,
                event = command::class.simpleName ?: "CareerCommand",
                fingerprint = savedFingerprint.take(16),
                elapsedNanos = (nanoTime() - started).coerceAtLeast(0L),
            )
            transition.copy(state = saved)
        }
}

interface CareerSimulationLogger {
    fun transition(
        careerId: String,
        event: String,
        fingerprint: String,
        elapsedNanos: Long,
    )
}

object NoOpCareerSimulationLogger : CareerSimulationLogger {
    override fun transition(
        careerId: String,
        event: String,
        fingerprint: String,
        elapsedNanos: Long,
    ) = Unit
}
