package com.leomala.footballdynasty.data.repository

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.V1RoomAdapter
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.repository.ClubRepository
import com.leomala.footballdynasty.migration.v1.V1DomainAdapter

class RoomClubRepository(
    private val database: FootballDynastyDatabase,
) : ClubRepository {
    override suspend fun findById(id: String): Club? = database.withTransaction {
        database.clubDao().findById(id)?.let { loadClub(it) }
    }

    override suspend fun findBySourceFileRef(sourceFileRef: String): Club? = database.withTransaction {
        database.clubDao().findBySourceFileRef(sourceFileRef)?.let { loadClub(it) }
    }

    private suspend fun loadClub(entity: ClubEntity): Club {
        val players = database.playerDao().playersForClub(entity.id)
        val memberships = database.squadMembershipDao().membershipsForClub(entity.id)
        return V1DomainAdapter.club(
            V1RoomAdapter.clubData(entity, players, memberships)
        )
    }
}
