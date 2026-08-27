package com.leomala.footballdynasty.domain.match

/**
 * Pure operation-plan parity for reachable legacy `best.s.a(...)`.
 *
 * The legacy method first materializes and appends `best.l`, then applies player/stat mutations,
 * then handles dismissal/injury roster removal and optional `p1(...)` routing. This object preserves
 * that order without mutating a modern match model directly.
 */
object LegacyMatchEventApplicationRules {
    enum class Operation {
        APPEND_EVENT,
        LEGACY_PLAYER_STAT_M,
        LEGACY_PLAYER_STAT_N,
        APPLY_INJURY_TO_ORIGINAL_PRIMARY,
        REMOVE_ORIGINAL_PRIMARY_FROM_ACTIVE,
        REQUEST_SUBSTITUTION,
    }

    data class SubstitutionRequest<TPlayer>(
        val side: Int,
        val originalPlayer: TPlayer?,
        val legacyPeriod: Int,
        val legacyMinute: Int,
        val automaticOutgoing: Boolean,
        val enforceLegacyL0Compatibility: Boolean,
    )

    data class Result<TClub, TPlayer>(
        val event: LegacyMatchEventRecord<TClub, TPlayer>,
        val operations: List<Operation>,
        val substitutionRequest: SubstitutionRequest<TPlayer>?,
        val recognizedClubSide: Boolean,
        val eventPrimaryWasReplacedByOppositeSelection: Boolean,
    )

    fun <TClub : Any, TPlayer> resolve(
        legacyType: Int,
        legacySubtype: Int,
        homeClub: TClub,
        awayClub: TClub,
        eventClub: TClub?,
        originalPrimary: TPlayer?,
        secondaryPlayer: TPlayer?,
        originalPrimaryPositionIndex: Int,
        legacyPeriod: Int,
        legacyMinute: Int,
        substitutionsRemainingForResolvedSide: Int,
        legacyClubModeFlag: Boolean,
        selectLegacyVFromOppositeActive: () -> TPlayer? = { null },
    ): Result<TClub, TPlayer> {
        val recognizedClubSide: Boolean
        val side = when {
            eventClub === homeClub -> {
                recognizedClubSide = true
                0
            }
            eventClub === awayClub -> {
                recognizedClubSide = true
                1
            }
            else -> {
                recognizedClubSide = false
                // Legacy constructor still receives the default side 0 when the club is neither e nor f.
                0
            }
        }

        var eventPrimary = originalPrimary
        var resolvedSubtype = legacySubtype
        var replacedPrimary = false
        if (legacySubtype == 2) {
            val selected = selectLegacyVFromOppositeActive()
            if (selected != null) {
                eventPrimary = selected
                resolvedSubtype = 2
                replacedPrimary = true
            } else {
                // Legacy falls back only the event subtype. Later effects still target originalPrimary.
                resolvedSubtype = 1
            }
        }

        val event = LegacyMatchEventRecord(
            legacyClub = eventClub,
            legacyType = legacyType,
            legacySubtype = resolvedSubtype,
            legacyMinute = legacyMinute,
            legacyPeriod = legacyPeriod,
            primaryPlayer = eventPrimary,
            secondaryPlayer = secondaryPlayer,
            legacySide = side,
        )

        val operations = mutableListOf(Operation.APPEND_EVENT)
        var substitutionRequest: SubstitutionRequest<TPlayer>? = null

        when (legacyType) {
            2 -> operations += Operation.LEGACY_PLAYER_STAT_M
            4 -> operations += Operation.LEGACY_PLAYER_STAT_N
            3 -> {
                operations += Operation.LEGACY_PLAYER_STAT_M
                operations += Operation.LEGACY_PLAYER_STAT_N
            }
        }

        when (legacyType) {
            3, 4 -> {
                if (recognizedClubSide) {
                    operations += Operation.REMOVE_ORIGINAL_PRIMARY_FROM_ACTIVE
                    if (
                        originalPrimaryPositionIndex <= 13 &&
                        !legacyClubModeFlag &&
                        substitutionsRemainingForResolvedSide > 0
                    ) {
                        operations += Operation.REQUEST_SUBSTITUTION
                        substitutionRequest = SubstitutionRequest(
                            side = side,
                            originalPlayer = originalPrimary,
                            legacyPeriod = legacyPeriod,
                            legacyMinute = legacyMinute,
                            automaticOutgoing = true,
                            enforceLegacyL0Compatibility = false,
                        )
                    }
                }
            }

            5 -> {
                if (originalPrimary != null) {
                    operations += Operation.APPLY_INJURY_TO_ORIGINAL_PRIMARY
                }
                if (recognizedClubSide && !legacyClubModeFlag) {
                    operations += Operation.REMOVE_ORIGINAL_PRIMARY_FROM_ACTIVE
                }
                if (
                    eventClub != null &&
                    substitutionsRemainingForResolvedSide > 0 &&
                    !legacyClubModeFlag
                ) {
                    operations += Operation.REQUEST_SUBSTITUTION
                    substitutionRequest = SubstitutionRequest(
                        side = side,
                        originalPlayer = originalPrimary,
                        legacyPeriod = legacyPeriod,
                        legacyMinute = legacyMinute,
                        automaticOutgoing = false,
                        enforceLegacyL0Compatibility = true,
                    )
                }
            }
        }

        return Result(
            event = event,
            operations = operations.toList(),
            substitutionRequest = substitutionRequest,
            recognizedClubSide = recognizedClubSide,
            eventPrimaryWasReplacedByOppositeSelection = replacedPrimary,
        )
    }

    fun <TClub, TPlayer> execute(
        result: Result<TClub, TPlayer>,
        appendEvent: (LegacyMatchEventRecord<TClub, TPlayer>) -> Unit,
        applyLegacyPlayerStatM: () -> Unit,
        applyLegacyPlayerStatN: () -> Unit,
        applyInjuryToOriginalPrimary: () -> Unit,
        removeOriginalPrimaryFromActive: () -> Unit,
        requestSubstitution: (SubstitutionRequest<TPlayer>) -> Unit,
    ) {
        for (operation in result.operations) {
            when (operation) {
                Operation.APPEND_EVENT -> appendEvent(result.event)
                Operation.LEGACY_PLAYER_STAT_M -> applyLegacyPlayerStatM()
                Operation.LEGACY_PLAYER_STAT_N -> applyLegacyPlayerStatN()
                Operation.APPLY_INJURY_TO_ORIGINAL_PRIMARY -> applyInjuryToOriginalPrimary()
                Operation.REMOVE_ORIGINAL_PRIMARY_FROM_ACTIVE -> removeOriginalPrimaryFromActive()
                Operation.REQUEST_SUBSTITUTION -> requestSubstitution(
                    checkNotNull(result.substitutionRequest) {
                        "Legacy substitution operation requires its recovered request"
                    },
                )
            }
        }
    }
}
