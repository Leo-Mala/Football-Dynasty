package com.leomala.footballdynasty.domain.repository

import com.leomala.footballdynasty.domain.model.Career

interface CareerRepository {
    suspend fun save(career: Career): Career
    suspend fun findById(id: String): Career?
    suspend fun all(): List<Career>
}
