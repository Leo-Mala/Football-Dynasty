package com.leomala.footballdynasty.migration.v1

/** Stable version marker for the first modern migration contract. */
const val DATA_SCHEMA_V1: Int = 1

enum class RosterKindV1 {
    SENIOR,
    JUNIOR,
}

data class PlayerDataV1(
    val schemaVersion: Int = DATA_SCHEMA_V1,
    val id: String,
    val sourceClubId: String,
    val rosterKind: RosterKindV1,
    val sourceOrdinal: Int,
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
    val legacyAid: Int,
    val legacySid: Int,
    val legacyTid: Int,
    val legacyHash: Int,
)

data class ClubDataV1(
    val schemaVersion: Int = DATA_SCHEMA_V1,
    val id: String,
    val sourceFileRef: String,
    val name: String,
    val country: Int,
    val state: Int,
    val level: Int,
    val stadium: String,
    val capacity: Int,
    val reputation: Int,
    val primaryColor: String,
    val secondaryColor: String,
    val coach: String,
    val coachCountry: Int,
    val baseColor: Int,
    val legacyAid: Int,
    val legacySid: Int,
    val legacyTid: Int,
    val legacyVid: Int,
    val legacyId: Int,
    val legacyValid: Boolean,
    val players: List<PlayerDataV1>,
)

/**
 * Versioned contract reserved for competition migration once a characterized
 * legacy competition source is connected. Nullability means "not proven yet",
 * never a fabricated default.
 */
data class CompetitionDataV1(
    val schemaVersion: Int = DATA_SCHEMA_V1,
    val id: String,
    val legacyType: String,
    val name: String?,
    val divisionName: String?,
    val division: Int?,
    val participantClubIds: List<String>,
)

/** Versioned match contract. No legacy match is fabricated in Phase 3. */
data class MatchDataV1(
    val schemaVersion: Int = DATA_SCHEMA_V1,
    val id: String,
    val homeClubId: String?,
    val awayClubId: String?,
    val homeGoals: Int?,
    val awayGoals: Int?,
    val legacyFingerprint: String?,
)

/**
 * Initial modern career envelope. Full legacy-career payload remains blocked
 * until a real .ai21 + .s21/.s121 fixture is characterized.
 */
data class CareerDataV1(
    val schemaVersion: Int = DATA_SCHEMA_V1,
    val id: String,
    val displayName: String?,
    val legacyMetadataFingerprint: String?,
    val legacyCareerFingerprint: String?,
)
