package com.leomala.footballdynasty.domain.model

data class LegacyPlayerSnapshot(
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

data class LegacyTeamSnapshot(
    val name: String,
    val fileRef: String,
    val country: Int,
    val state: Int,
    val level: Int,
    val stadium: String,
    val capacity: Int,
    val reputation: Int,
    val players: List<LegacyPlayerSnapshot>,
    val juniors: List<LegacyPlayerSnapshot>,
)

data class LegacySaveMetadataSnapshot(
    val a: Int?,
    val n: String?,
    val tc: String?,
    val i: String?,
    val path: String?,
)
