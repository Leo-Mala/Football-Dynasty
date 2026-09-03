package com.leomala.footballdynasty.domain.repository

import com.leomala.footballdynasty.domain.career.CareerCommand
import com.leomala.footballdynasty.domain.career.CareerState

interface CareerStateRepository {
    suspend fun save(state: CareerState): CareerState

    /**
     * Persists a command result through the repository-specific transactional boundary.
     *
     * The default deliberately preserves the existing behavior for repositories that have no
     * additional command-coupled persisted state.
     */
    suspend fun saveTransition(
        state: CareerState,
        command: CareerCommand,
    ): CareerState = save(state)

    suspend fun findById(id: String): CareerState?
}
