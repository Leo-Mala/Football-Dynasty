package com.leomala.footballdynasty.domain.manager

/**
 * Optional exact-match filters over raw numeric fields already preserved from the
 * serialized legacy player record.
 *
 * These values intentionally remain opaque. Supplying a code means only
 * "serialized field equals this exact integer"; it does not assign football
 * position, availability, injury/suspension state, tactical side, lineup role or
 * any other gameplay meaning.
 */
data class LegacyRawSourcePlayerCodes(
    val positionCode: Int? = null,
    val statusCode: Int? = null,
    val sideCode: Int? = null,
)

/**
 * Returns senior references satisfying every raw code supplied in [codes].
 *
 * Source-file identity, SENIOR collection membership and original serialized order
 * are preserved by constructing references only through [seniorRef]. An omitted
 * code does not filter that field. No junior fallback or semantic interpretation is
 * performed.
 */
fun LegacyManagedClubSourceSquads.seniorRefsMatchingRawCodes(
    codes: LegacyRawSourcePlayerCodes,
): List<LegacySourcePlayerRef> = senior.mapIndexedNotNull { sourceIndex, player ->
    seniorRef(sourceIndex)?.takeIf {
        (codes.positionCode == null || player.position == codes.positionCode) &&
            (codes.statusCode == null || player.status == codes.statusCode) &&
            (codes.sideCode == null || player.side == codes.sideCode)
    }
}

/**
 * Junior counterpart of [seniorRefsMatchingRawCodes]. The separately serialized
 * JUNIOR collection stays isolated and in source order; this function does not
 * imply promotion or senior-team eligibility.
 */
fun LegacyManagedClubSourceSquads.juniorRefsMatchingRawCodes(
    codes: LegacyRawSourcePlayerCodes,
): List<LegacySourcePlayerRef> = juniors.mapIndexedNotNull { sourceIndex, player ->
    juniorRef(sourceIndex)?.takeIf {
        (codes.positionCode == null || player.position == codes.positionCode) &&
            (codes.statusCode == null || player.status == codes.statusCode) &&
            (codes.sideCode == null || player.side == codes.sideCode)
    }
}
