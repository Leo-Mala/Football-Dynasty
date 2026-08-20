package com.leomala.footballdynasty.domain.model

enum class RosterKind {
    SENIOR,
    JUNIOR,
}

data class Player(
    val id: String,
    val clubId: String,
    val rosterKind: RosterKind,
    val name: String,
    val age: Int,
    val country: Int,
    val position: Int,
    val status: Int,
    val side: Int,
    val cr1: Int,
    val cr2: Int,
    val star: Boolean,
    val worldTop: Boolean,
)

data class Club(
    val id: String,
    val sourceFileRef: String,
    val name: String,
    val country: Int,
    val state: Int,
    val level: Int,
    val stadium: String,
    val capacity: Int,
    val reputation: Int,
    val players: List<Player>,
)

data class Competition(
    val id: String,
    val name: String?,
    val divisionName: String?,
    val division: Int?,
    val participantClubIds: List<String>,
)

data class Match(
    val id: String,
    val homeClubId: String?,
    val awayClubId: String?,
    val homeGoals: Int?,
    val awayGoals: Int?,
)

data class Career(
    val id: String,
    val displayName: String?,
)
