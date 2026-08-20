package com.leomala.footballdynasty.domain.repository

import com.leomala.footballdynasty.domain.career.CareerState

interface CareerStateRepository {
    suspend fun save(state: CareerState): CareerState
    suspend fun findById(id: String): CareerState?
}
