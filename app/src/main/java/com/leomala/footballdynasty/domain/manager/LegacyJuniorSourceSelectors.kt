package com.leomala.footballdynasty.domain.manager

/**
 * Read-only provenance selectors for the separately serialized legacy junior
 * collection.
 *
 * The legacy snapshot already preserves the raw numeric position/status/side
 * fields for juniors. These selectors intentionally keep those values opaque:
 * they do not promote a junior, declare senior-team eligibility, infer injury or
 * suspension, or assign a tactical role. They only return exact source
 * references in original junior collection order.
 */
fun LegacyManagedClubSourceSquads.juniorRefsByPositionCode(
    positionCode: Int,
): List<LegacySourcePlayerRef> = juniors.mapIndexedNotNull { sourceIndex, player ->
    juniorRef(sourceIndex)?.takeIf { player.position == positionCode }
}

fun LegacyManagedClubSourceSquads.juniorRefsByStatusCode(
    statusCode: Int,
): List<LegacySourcePlayerRef> = juniors.mapIndexedNotNull { sourceIndex, player ->
    juniorRef(sourceIndex)?.takeIf { player.status == statusCode }
}

fun LegacyManagedClubSourceSquads.juniorRefsBySideCode(
    sideCode: Int,
): List<LegacySourcePlayerRef> = juniors.mapIndexedNotNull { sourceIndex, player ->
    juniorRef(sourceIndex)?.takeIf { player.side == sideCode }
}
