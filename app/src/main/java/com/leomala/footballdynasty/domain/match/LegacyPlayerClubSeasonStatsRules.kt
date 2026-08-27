package com.leomala.footballdynasty.domain.match

/**
 * Pure structural parity for `best.o.g(code, club)` + `best.o.q0(club)` + serializable `best.e`.
 * Field names remain neutral where the persisted counter meaning is not needed by Phase 8.
 */
object LegacyPlayerClubSeasonStatsRules {
    data class Entry(
        val legacySeasonId: Int,
        val legacyClubId: Int,
        val legacyC: Int = 0,
        val legacyD: Int = 0,
        val legacyE: Int = 0,
        val legacyF: Int = 0,
        val legacyG: Int = 0,
        val legacyH: Int = 0,
        val legacyTransientI: Int = -1,
    )

    data class Result(
        val updatedEntries: List<Entry>?,
        val mutatedEntry: Entry,
        val createdEntry: Boolean,
        val retainedEntry: Boolean,
        val matchedIndex: Int?,
    )

    fun apply(
        entries: List<Entry>?,
        currentSeasonId: Int,
        clubId: Int,
        legacyCode: Int,
    ): Result {
        val index = entries?.indexOfFirst {
            it.legacyClubId == clubId && it.legacySeasonId == currentSeasonId
        }?.takeIf { it >= 0 }
        val created = index == null
        val base = if (index != null) {
            entries[index]
        } else {
            Entry(
                legacySeasonId = currentSeasonId,
                legacyClubId = clubId,
            )
        }

        val updated = when (legacyCode) {
            2 -> base.copy(legacyC = base.legacyC + 1)
            4 -> base.copy(legacyD = base.legacyD + 1)
            3 -> base.copy(
                legacyC = base.legacyC + 1,
                legacyD = base.legacyD + 1,
            )
            1 -> base.copy(legacyG = base.legacyG + 1)
            5 -> base.copy(legacyH = base.legacyH + 1)
            0 -> base.copy(legacyF = base.legacyF + 1)
            8 -> base.copy(legacyE = base.legacyE + 1)
            else -> base
        }

        val retained = entries != null
        val updatedEntries = if (entries == null) {
            null
        } else if (index == null) {
            entries + updated
        } else {
            entries.toMutableList().also { it[index] = updated }.toList()
        }

        return Result(
            updatedEntries = updatedEntries,
            mutatedEntry = updated,
            createdEntry = created,
            retainedEntry = retained,
            matchedIndex = index,
        )
    }
}
