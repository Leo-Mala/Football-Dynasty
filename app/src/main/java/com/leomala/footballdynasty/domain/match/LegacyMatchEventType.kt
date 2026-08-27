package com.leomala.footballdynasty.domain.match

/** Sporting meanings proven from `best.l`, callers and bundled drawable/string assets. */
enum class LegacyMatchEventType(val legacyCode: Int) {
    GOAL(1),
    YELLOW_CARD(2),
    SECOND_YELLOW_RED(3),
    RED_CARD(4),
    INJURY(5),
    SUBSTITUTION(6),
    MISSED_PENALTY(7),
    ;

    companion object {
        fun fromLegacyCode(code: Int): LegacyMatchEventType? =
            values().firstOrNull { it.legacyCode == code }
    }
}
