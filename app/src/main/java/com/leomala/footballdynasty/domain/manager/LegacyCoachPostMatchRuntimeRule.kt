package com.leomala.footballdynasty.domain.manager

/** Exact side seen by legacy `best.f0.i(best.s)` after comparing only `f0.A()` with the match. */
enum class LegacyCoachAdjustmentSide {
    HOME,
    AWAY,
    NONE,
}

data class LegacyCoachLeagueStandingInput(
    val position: Int,
    val tableSize: Int,
    val relegationCount: Int,
)

data class LegacyCoachStandingProjection(
    val band: Int,
    val missing: Boolean,
)

/** Literal reconstruction of the three-int projection returned by `best.c0.I(best.k0)`. */
object LegacyCoachStandingProjectionRule {
    fun resolve(
        rawCompetitionType: Int,
        isLegacyLeagueCompetition: Boolean,
        input: LegacyCoachLeagueStandingInput?,
    ): LegacyCoachStandingProjection {
        if (!isLegacyLeagueCompetition || rawCompetitionType !in setOf(1, 3) || input == null) {
            return LegacyCoachStandingProjection(band = 0, missing = true)
        }
        if (input.position == -1) {
            return LegacyCoachStandingProjection(band = 0, missing = true)
        }

        val topBandLimit = if (rawCompetitionType == 3) 2 else 4
        val lowerBandWidth = if (input.tableSize < 20) 2 else 4
        val band = when {
            input.position == 1 -> 1
            input.position > input.tableSize - input.relegationCount -> 5
            input.position <= topBandLimit -> 2
            input.position > input.tableSize - (input.relegationCount + lowerBandWidth) -> 4
            else -> 3
        }
        return LegacyCoachStandingProjection(band = band, missing = false)
    }
}

data class LegacyCoachPostMatchAdjustmentState(
    val rawG: Int,
    val rawH: Int,
)

data class LegacyCoachPostMatchAdjustmentContext(
    val rawCompetitionType: Int,
    val managerSide: LegacyCoachAdjustmentSide,
    val homeGoals: Int,
    val awayGoals: Int,
    val homeStrength: Int,
    val awayStrength: Int,
    val isLegacyLeagueCompetition: Boolean,
    val managerStanding: LegacyCoachLeagueStandingInput?,
    val managerIsUserControlled: Boolean,
    val currentClubCash: Long?,
)

data class LegacyCoachPostMatchAdjustmentResult(
    val state: LegacyCoachPostMatchAdjustmentState,
    val applied: Boolean,
    val effectiveManagerGoals: Int,
    val effectiveOpponentGoals: Int,
    val standingProjection: LegacyCoachStandingProjection,
    val gDeltasInOrder: List<Int>,
)

/**
 * Reachable `best.f0.i(best.s)` projection, including the exact G-before-H ordering.
 *
 * The normal caller `best.s.f()` reaches this method only for competition E values
 * 1/2/3/4/5/6/8. `NONE` is not normalized away: it is the literal branch taken when the manager
 * reached through `club.y0()` is attached through alternative `f0.l` rather than `f0.A()`.
 */
object LegacyCoachPostMatchAdjustmentRule {
    val callerCompetitionTypes: Set<Int> = linkedSetOf(1, 2, 3, 4, 5, 6, 8)

    private enum class Outcome { DRAW, WIN, LOSS }

    fun apply(
        before: LegacyCoachPostMatchAdjustmentState,
        context: LegacyCoachPostMatchAdjustmentContext,
    ): LegacyCoachPostMatchAdjustmentResult {
        if (context.rawCompetitionType !in callerCompetitionTypes) {
            return LegacyCoachPostMatchAdjustmentResult(
                state = before,
                applied = false,
                effectiveManagerGoals = 0,
                effectiveOpponentGoals = 0,
                standingProjection = LegacyCoachStandingProjection(band = 0, missing = true),
                gDeltasInOrder = emptyList(),
            )
        }

        val managerIsAway = context.managerSide == LegacyCoachAdjustmentSide.AWAY
        val managerGoals: Int
        val opponentGoals: Int
        val strengthDifference: Int
        val standingProjection: LegacyCoachStandingProjection

        when (context.managerSide) {
            LegacyCoachAdjustmentSide.HOME -> {
                managerGoals = context.homeGoals
                opponentGoals = context.awayGoals
                strengthDifference = context.homeStrength - context.awayStrength
                standingProjection = LegacyCoachStandingProjectionRule.resolve(
                    rawCompetitionType = context.rawCompetitionType,
                    isLegacyLeagueCompetition = context.isLegacyLeagueCompetition,
                    input = context.managerStanding,
                )
            }
            LegacyCoachAdjustmentSide.AWAY -> {
                managerGoals = context.awayGoals
                opponentGoals = context.homeGoals
                strengthDifference = context.awayStrength - context.homeStrength
                standingProjection = LegacyCoachStandingProjectionRule.resolve(
                    rawCompetitionType = context.rawCompetitionType,
                    isLegacyLeagueCompetition = context.isLegacyLeagueCompetition,
                    input = context.managerStanding,
                )
            }
            LegacyCoachAdjustmentSide.NONE -> {
                managerGoals = 0
                opponentGoals = 0
                strengthDifference = 0
                // `i` initializes its local array as {0,0,0} before either A()==side branch.
                standingProjection = LegacyCoachStandingProjection(band = 0, missing = false)
            }
        }

        val outcome = when {
            managerGoals > opponentGoals -> Outcome.WIN
            managerGoals < opponentGoals -> Outcome.LOSS
            else -> Outcome.DRAW
        }
        var rawG = before.rawG
        val gDeltas = mutableListOf<Int>()
        fun writeG(delta: Int) {
            rawG = addClamped(rawG, delta)
            gDeltas += delta
        }

        // First G family in `i`: result + relative raw club strength + home/away.
        when (outcome) {
            Outcome.DRAW -> {
                if (strengthDifference >= 0 && !managerIsAway) {
                    writeG(-1)
                } else if (strengthDifference < 0) {
                    writeG(1)
                }
            }
            Outcome.WIN -> Unit
            Outcome.LOSS -> {
                if (strengthDifference >= 0) {
                    writeG(if (managerIsAway) -1 else -3)
                }
            }
        }

        if (!standingProjection.missing) {
            if (context.managerIsUserControlled) {
                val debtPenalty = when (context.rawCompetitionType) {
                    1 -> -10
                    3, 4, 6 -> -5
                    else -> 0
                }
                if (debtPenalty != 0) {
                    val cash = checkNotNull(context.currentClubCash) {
                        "Legacy best.f0.i dereferences A().N() for this user-manager branch"
                    }
                    if (cash < 0L) writeG(debtPenalty)
                }
            }

            val band = if (context.rawCompetitionType == 1 || context.rawCompetitionType == 3) {
                standingProjection.band
            } else {
                // Literal `v5 = 2` branch for non-1/3 competition types.
                2
            }
            val matrixDelta = matrixDelta(
                rawCompetitionType = context.rawCompetitionType,
                outcome = outcome,
                managerIsAway = managerIsAway,
                band = band,
            )
            writeG(matrixDelta)
        }

        // H writes occur only after every G write above. Reuse the already-certified exact H rule.
        val rawH = LegacyCoachRawHRule.afterMatch(
            rawH = before.rawH,
            rawCompetitionType = context.rawCompetitionType,
            managerGoals = managerGoals,
            opponentGoals = opponentGoals,
            managerIsAway = managerIsAway,
        )

        // Final cross-field dependency in `i`: low post-match H causes another G write.
        if (rawH < LegacyCoachRawHRule.MAIN_TEAM_FLOOR) {
            writeG(-5)
        }

        return LegacyCoachPostMatchAdjustmentResult(
            state = LegacyCoachPostMatchAdjustmentState(
                rawG = rawG.coerceIn(0, 100),
                rawH = rawH.coerceIn(0, 100),
            ),
            applied = true,
            effectiveManagerGoals = managerGoals,
            effectiveOpponentGoals = opponentGoals,
            standingProjection = standingProjection,
            gDeltasInOrder = gDeltas.toList(),
        )
    }

    private fun matrixDelta(
        rawCompetitionType: Int,
        outcome: Outcome,
        managerIsAway: Boolean,
        band: Int,
    ): Int {
        val drawHome: IntArray
        val drawAway: IntArray
        val winHome: IntArray
        val winAway: IntArray
        val lossHome: IntArray
        val lossAway: IntArray

        when (rawCompetitionType) {
            1 -> {
                winHome = intArrayOf(0, 5, 4, 1, 1, 0)
                winAway = intArrayOf(0, 7, 6, 2, 2, 1)
                drawHome = intArrayOf(0, 2, 1, -1, -1, -3)
                drawAway = intArrayOf(0, 2, 1, 0, -1, -2)
                lossHome = intArrayOf(0, -2, -2, -3, -5, -7)
                lossAway = intArrayOf(0, -1, -1, -2, -4, -5)
            }
            3 -> {
                winHome = intArrayOf(0, 2, 2, 1, 1, 1)
                winAway = intArrayOf(0, 3, 2, 2, 2, 1)
                drawHome = intArrayOf(0, 1, 1, 0, 0, -1)
                drawAway = intArrayOf(0, 1, 1, 1, 0, -1)
                lossHome = intArrayOf(0, -2, -2, -3, -3, -5)
                lossAway = intArrayOf(0, -1, -1, -2, -2, -3)
            }
            4, 6 -> {
                // Structurally present in SMALI. With a real A()==match side, c0.I marks these
                // competitions missing; this matrix is reachable through the literal NONE path.
                winHome = intArrayOf(0, 5, 4, 3, 3, 3)
                winAway = intArrayOf(0, 7, 6, 4, 4, 5)
                drawHome = intArrayOf(0, 2, 1, 0, -1, -3)
                drawAway = intArrayOf(0, 2, 1, 0, 0, -2)
                lossHome = intArrayOf(0, -2, -2, -3, -3, -5)
                lossAway = intArrayOf(0, -1, -1, -2, -2, -3)
            }
            else -> return 0
        }

        return when (outcome) {
            Outcome.DRAW -> if (managerIsAway) drawAway[band] else drawHome[band]
            Outcome.WIN -> if (managerIsAway) winAway[band] else winHome[band]
            Outcome.LOSS -> if (managerIsAway) lossAway[band] else lossHome[band]
        }
    }

    private fun addClamped(value: Int, delta: Int): Int = (value + delta).coerceIn(0, 100)
}

data class LegacyCoachAssociatedClub(
    val clubId: String,
    val legacyClubId: Int,
)

data class LegacyCoachMatchAssociation(
    val club: LegacyCoachAssociatedClub,
    val side: LegacyCoachAdjustmentSide,
)

/** Exact A() first, then alternative `l`, resolution order in `best.f0.j(best.s)`. */
object LegacyCoachMatchAssociationRule {
    fun resolve(
        currentClub: LegacyCoachAssociatedClub?,
        alternativeClub: LegacyCoachAssociatedClub?,
        homeClubId: String,
        awayClubId: String,
    ): LegacyCoachMatchAssociation? = when {
        currentClub?.clubId == homeClubId -> LegacyCoachMatchAssociation(currentClub, LegacyCoachAdjustmentSide.HOME)
        currentClub?.clubId == awayClubId -> LegacyCoachMatchAssociation(currentClub, LegacyCoachAdjustmentSide.AWAY)
        alternativeClub?.clubId == homeClubId -> LegacyCoachMatchAssociation(alternativeClub, LegacyCoachAdjustmentSide.HOME)
        alternativeClub?.clubId == awayClubId -> LegacyCoachMatchAssociation(alternativeClub, LegacyCoachAdjustmentSide.AWAY)
        else -> null
    }
}

data class LegacyCoachSeasonClubRecord(
    val seasonId: Int,
    val legacyClubId: Int,
    val rawMatches: Int = 0,
    val rawWins: Int = 0,
    val rawLosses: Int = 0,
    val rawPoints: Int = 0,
    val rawOtherCount: Int = 0,
)

data class LegacyCoachPostMatchStatisticsState(
    val rawD: Int,
    val rawE: Int,
    val rawF: Int,
    val rawO: Int,
    val records: List<LegacyCoachSeasonClubRecord>,
)

data class LegacyCoachPostMatchStatisticsContext(
    val seasonId: Int,
    val currentClub: LegacyCoachAssociatedClub?,
    val alternativeClub: LegacyCoachAssociatedClub?,
    val homeClubId: String,
    val awayClubId: String,
    val homeGoals: Int,
    val awayGoals: Int,
    val rawCompetitionType: Int?,
    /** `konrent.t.x0()` only; null means the legacy competition is not that concrete class. */
    val leagueCompetitionSubtype: Int?,
)

data class LegacyCoachPostMatchStatisticsResult(
    val state: LegacyCoachPostMatchStatisticsState,
    val association: LegacyCoachMatchAssociation,
    val pointsAwarded: Int,
    val updatedRecordIndex: Int,
)

/** Literal mutation/order projection of `best.f0.j(best.s)`. */
object LegacyCoachPostMatchStatisticsRule {
    fun apply(
        before: LegacyCoachPostMatchStatisticsState,
        context: LegacyCoachPostMatchStatisticsContext,
    ): LegacyCoachPostMatchStatisticsResult {
        val association = checkNotNull(
            LegacyCoachMatchAssociationRule.resolve(
                currentClub = context.currentClub,
                alternativeClub = context.alternativeClub,
                homeClubId = context.homeClubId,
                awayClubId = context.awayClubId,
            ),
        ) {
            "Legacy best.f0.j cannot materialize q(c0) without current or alternative match club"
        }

        val managerIsAway = association.side == LegacyCoachAdjustmentSide.AWAY
        val managerGoals = if (managerIsAway) context.awayGoals else context.homeGoals
        val opponentGoals = if (managerIsAway) context.homeGoals else context.awayGoals
        val competitionType = context.rawCompetitionType ?: 0
        val subtypeForType1 = if (competitionType == 1) context.leagueCompetitionSubtype ?: 0 else 0
        val subtypeForType3 = if (competitionType == 3) context.leagueCompetitionSubtype ?: 0 else 0

        val records = before.records.toMutableList()
        var recordIndex = records.indexOfFirst {
            it.legacyClubId == association.club.legacyClubId && it.seasonId == context.seasonId
        }
        if (recordIndex < 0) {
            records += LegacyCoachSeasonClubRecord(
                seasonId = context.seasonId,
                legacyClubId = association.club.legacyClubId,
            )
            recordIndex = records.lastIndex
        }

        var record = records[recordIndex].copy(rawMatches = records[recordIndex].rawMatches + 1)
        var rawD = before.rawD + 1
        var rawE = before.rawE
        var rawF = before.rawF
        var rawO = before.rawO
        var points = 0

        when {
            managerGoals > opponentGoals -> {
                record = record.copy(rawWins = record.rawWins + 1)
                rawE += 1
                points = winPoints(
                    competitionType = competitionType,
                    subtypeForType1 = subtypeForType1,
                    subtypeForType3 = subtypeForType3,
                    managerIsAway = managerIsAway,
                )
            }
            managerGoals < opponentGoals -> {
                record = record.copy(rawLosses = record.rawLosses + 1)
                rawF += 1
            }
            else -> {
                points = drawPoints(
                    competitionType = competitionType,
                    subtypeForType1 = subtypeForType1,
                    managerIsAway = managerIsAway,
                )
            }
        }

        if (points > 0) {
            record = record.copy(rawPoints = record.rawPoints + points)
            rawO += points
        }
        records[recordIndex] = record

        return LegacyCoachPostMatchStatisticsResult(
            state = LegacyCoachPostMatchStatisticsState(
                rawD = rawD,
                rawE = rawE,
                rawF = rawF,
                rawO = rawO,
                records = records.toList(),
            ),
            association = association,
            pointsAwarded = points,
            updatedRecordIndex = recordIndex,
        )
    }

    private fun winPoints(
        competitionType: Int,
        subtypeForType1: Int,
        subtypeForType3: Int,
        managerIsAway: Boolean,
    ): Int {
        var points = when (competitionType) {
            1 -> when (subtypeForType1) {
                1 -> 4
                2 -> 3
                3 -> 2
                else -> 1
            }
            2 -> 3
            3 -> if (subtypeForType3 == 1) 2 else 1
            4 -> 5
            5 -> 7
            6 -> 4
            7 -> 5
            8 -> 4
            else -> 0
        }
        if (managerIsAway && competitionType != 7 && competitionType != 5) points += 1
        return points
    }

    private fun drawPoints(
        competitionType: Int,
        subtypeForType1: Int,
        managerIsAway: Boolean,
    ): Int {
        val awayFallback = if (managerIsAway && competitionType != 7 && competitionType != 5) 1 else 0
        return when (competitionType) {
            1 -> when (subtypeForType1) {
                1 -> 2
                2 -> 1
                else -> awayFallback
            }
            2 -> 1
            4, 5, 6, 7, 8 -> 2
            else -> awayFallback
        }
    }
}
