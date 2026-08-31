package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.manager.LegacyLineupCommitResult

/**
 * Phase 11 -> Phase 9 handoff for the exact lineup state consumed by the certified match runtime.
 *
 * This mapper adds no player selection. It accepts only the already-characterized output of
 * ActivityEscalacao.y()/B(), preserves starter/bench order, carries each committed legacy g0 code,
 * and projects the starter s1(TRUE) write into the transient match wrapper.
 */
object CareerLineupMatchEvidenceMapper {
    fun fromLineups(
        home: LegacyLineupCommitResult<String>,
        away: LegacyLineupCommitResult<String>,
        homeSubstitutionsRemaining: Int,
        awaySubstitutionsRemaining: Int,
        homeLegacyModeFlag: Boolean,
        awayLegacyModeFlag: Boolean,
    ): CareerMatchPersistedRuntimeResolver.TransientMatchEvidence =
        CareerMatchPersistedRuntimeResolver.TransientMatchEvidence(
            home = fromLineup(
                lineup = home,
                expectedSideIndex = 0,
                substitutionsRemaining = homeSubstitutionsRemaining,
                legacyModeFlag = homeLegacyModeFlag,
            ),
            away = fromLineup(
                lineup = away,
                expectedSideIndex = 1,
                substitutionsRemaining = awaySubstitutionsRemaining,
                legacyModeFlag = awayLegacyModeFlag,
            ),
        )

    fun fromLineup(
        lineup: LegacyLineupCommitResult<String>,
        expectedSideIndex: Int,
        substitutionsRemaining: Int,
        legacyModeFlag: Boolean,
    ): CareerMatchPersistedRuntimeResolver.TransientClubEvidence {
        require(expectedSideIndex == 0 || expectedSideIndex == 1) {
            "Legacy match side must be 0 or 1: $expectedSideIndex"
        }
        require(lineup.matchLists.replaceSideLists) {
            "Characterized lineup did not replace legacy match-side lists"
        }
        require(lineup.matchLists.sideIndex == expectedSideIndex) {
            "Lineup side ${lineup.matchLists.sideIndex} diverged from expected side $expectedSideIndex"
        }
        require(lineup.matchLists.startersPrimary == lineup.clubStarters) {
            "Lineup starter propagation diverged from committed club starters"
        }
        require(lineup.matchLists.startersMirror == lineup.clubStarters) {
            "Lineup mirror starters diverged from committed club starters"
        }
        require(lineup.matchLists.bench == lineup.clubBench) {
            "Lineup bench propagation diverged from committed club bench"
        }

        val writesByPlayer = lineup.playerWrites.groupBy { it.player }
        fun playerEvidence(
            playerId: String,
            starter: Boolean,
        ): CareerMatchPersistedRuntimeResolver.TransientPlayerEvidence {
            val writes = writesByPlayer[playerId].orEmpty()
            require(writes.size == 1) {
                "Committed lineup player $playerId must have exactly one legacy g0 write"
            }
            val write = writes.single()
            if (starter) {
                require(write.starterFlagWrite == true) {
                    "Starter $playerId must carry the characterized s1(TRUE) write"
                }
            } else {
                require(write.starterFlagWrite == null) {
                    "Bench player $playerId must not receive an invented s1 write"
                }
            }
            return CareerMatchPersistedRuntimeResolver.TransientPlayerEvidence(
                playerId = playerId,
                legacyG0 = write.lineupCode,
                selectedOrUsed = write.starterFlagWrite == true,
            )
        }

        return CareerMatchPersistedRuntimeResolver.TransientClubEvidence(
            active = lineup.matchLists.startersPrimary.map { playerEvidence(it, starter = true) },
            bench = lineup.matchLists.bench.map { playerEvidence(it, starter = false) },
            substitutionsRemaining = substitutionsRemaining,
            legacyModeFlag = legacyModeFlag,
        )
    }
}
