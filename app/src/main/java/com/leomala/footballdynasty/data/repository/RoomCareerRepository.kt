package com.leomala.footballdynasty.data.repository

import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.V1RoomAdapter
import com.leomala.footballdynasty.domain.model.Career
import com.leomala.footballdynasty.domain.repository.CareerRepository
import com.leomala.footballdynasty.migration.v1.V1DomainAdapter

class RoomCareerRepository(
    private val database: FootballDynastyDatabase,
    private val clockMillis: () -> Long = System::currentTimeMillis,
) : CareerRepository {
    override suspend fun save(career: Career): Career {
        require(career.id.isNotBlank()) { "Career id must not be blank" }
        val existing = database.careerMetadataDao().findById(career.id)
        val now = clockMillis()
        val entity = V1RoomAdapter.careerEntity(
            data = V1DomainAdapter.careerData(career),
            createdAtEpochMillis = existing?.createdAtEpochMillis ?: now,
            updatedAtEpochMillis = now,
        )
        database.careerMetadataDao().upsert(entity)
        return V1DomainAdapter.career(V1RoomAdapter.careerData(entity))
    }

    override suspend fun findById(id: String): Career? =
        database.careerMetadataDao().findById(id)?.let { entity ->
            V1DomainAdapter.career(V1RoomAdapter.careerData(entity))
        }

    override suspend fun all(): List<Career> =
        database.careerMetadataDao().all().map { entity ->
            V1DomainAdapter.career(V1RoomAdapter.careerData(entity))
        }
}
