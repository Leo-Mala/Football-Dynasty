package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.LegacyImportManifestEntity
import com.leomala.footballdynasty.data.local.entity.LegacyImportStateEntity
import com.leomala.footballdynasty.foundation.error.DuplicateStableIdentityException
import com.leomala.footballdynasty.foundation.error.IntegrityMismatchException
import com.leomala.footballdynasty.migration.v1.ClubDataV1

class RoomV1DataStore(
    private val database: FootballDynastyDatabase,
) {
    suspend fun replaceImportScope(
        scope: String,
        clubs: List<ClubDataV1>,
        manifest: LegacyImportManifestEntity,
        completedState: LegacyImportStateEntity,
    ) {
        require(scope.isNotBlank()) { "Import scope must not be blank" }
        validateUniqueIdentities(clubs)

        database.withTransaction {
            // Clubs are deleted first so FK CASCADE removes their memberships.
            database.clubDao().deleteForImportScope(scope)
            database.playerDao().deleteForImportScope(scope)

            val clubEntities = clubs.map { V1RoomAdapter.clubEntity(it, scope) }
            val players = clubs.flatMap { club ->
                club.players.map { V1RoomAdapter.playerEntity(it, scope) }
            }
            val memberships = clubs.flatMap { club ->
                club.players.map(V1RoomAdapter::membershipEntity)
            }

            database.clubDao().upsertAll(clubEntities)
            database.playerDao().upsertAll(players)
            database.squadMembershipDao().upsertAll(memberships)
            database.legacyImportDao().upsertManifest(manifest)
            database.legacyImportDao().upsertState(completedState)
        }
    }

    suspend fun readImportScope(scope: String): List<ClubDataV1> = database.withTransaction {
        val clubs = database.clubDao().allForImportScope(scope)
        val players = database.playerDao().allForImportScope(scope)
        val memberships = database.squadMembershipDao().allForImportScope(scope)

        val playerById = players.associateBy { it.id }
        if (playerById.size != players.size) {
            throw IntegrityMismatchException("Duplicate player identities persisted for scope $scope")
        }

        val membershipPlayerIds = memberships.mapTo(linkedSetOf()) { it.playerId }
        val playerIds = players.mapTo(linkedSetOf()) { it.id }
        if (membershipPlayerIds != playerIds) {
            val missingMemberships = playerIds - membershipPlayerIds
            val missingPlayers = membershipPlayerIds - playerIds
            throw IntegrityMismatchException(
                "Scope $scope contains orphan rows: " +
                    "playersWithoutMembership=${missingMemberships.size}, " +
                    "membershipsWithoutPlayer=${missingPlayers.size}"
            )
        }

        val membershipsByClub = memberships.groupBy { it.clubId }
        clubs.map { club ->
            val clubMemberships = membershipsByClub[club.id].orEmpty()
            val clubPlayers = clubMemberships.map { membership ->
                playerById[membership.playerId]
                    ?: throw IntegrityMismatchException(
                        "Missing player ${membership.playerId} for club ${club.id}"
                    )
            }
            V1RoomAdapter.clubData(club, clubPlayers, clubMemberships)
        }
    }

    suspend fun counts(scope: String): ImportScopeCounts = database.withTransaction {
        ImportScopeCounts(
            clubs = database.clubDao().countForImportScope(scope),
            players = database.playerDao().countForImportScope(scope),
            seniors = database.squadMembershipDao().seniorCountForImportScope(scope),
            juniors = database.squadMembershipDao().juniorCountForImportScope(scope),
        )
    }

    suspend fun resetImportScope(scope: String) {
        require(scope.isNotBlank()) { "Import scope must not be blank" }
        database.withTransaction {
            database.clubDao().deleteForImportScope(scope)
            database.playerDao().deleteForImportScope(scope)
            database.legacyImportDao().deleteManifest(scope)
            database.legacyImportDao().deleteState(scope)
        }
    }

    private fun validateUniqueIdentities(clubs: List<ClubDataV1>) {
        val clubIds = clubs.map { it.id }
        if (clubIds.toSet().size != clubIds.size) {
            throw DuplicateStableIdentityException("Duplicate club identity in V1 import batch")
        }

        val fileRefs = clubs.map { it.sourceFileRef }
        if (fileRefs.toSet().size != fileRefs.size) {
            throw DuplicateStableIdentityException("Duplicate legacy sourceFileRef in V1 import batch")
        }

        val playerIds = clubs.flatMap { club -> club.players.map { it.id } }
        if (playerIds.toSet().size != playerIds.size) {
            throw DuplicateStableIdentityException("Duplicate player identity in V1 import batch")
        }
    }
}

data class ImportScopeCounts(
    val clubs: Int,
    val players: Int,
    val seniors: Int,
    val juniors: Int,
)
