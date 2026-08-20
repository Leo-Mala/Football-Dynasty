package com.leomala.footballdynasty.domain.model

/**
 * Compatibility-only representation of a serialized legacy player.
 *
 * The `legacy*` fields are intentionally opaque. They are preserved byte-level
 * source metadata and must not be reinterpreted or normalized during migration.
 */
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
    val legacyAid: Int = 0,
    val legacySid: Int = 0,
    val legacyTid: Int = 0,
    val legacyHash: Int = 0,
)

/**
 * Compatibility-only representation of a serialized legacy team.
 *
 * This is not the modern domain model. Fields whose semantics are still only
 * partially understood are carried forward unchanged so migration never
 * invents or discards source information.
 */
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
    val primaryColor: String = "",
    val secondaryColor: String = "",
    val coach: String = "",
    val coachCountry: Int = 0,
    val baseColor: Int = 0,
    val legacyAid: Int = 0,
    val legacySid: Int = 0,
    val legacyTid: Int = 0,
    val legacyVid: Int = 0,
    val legacyId: Int = 0,
    val legacyValid: Boolean = false,
)

data class LegacySaveMetadataSnapshot(
    val a: Int?,
    val n: String?,
    val tc: String?,
    val i: String?,
    val path: String?,
)
