package com.leomala.footballdynasty.domain.career

/**
 * Pure control-flow projection of reachable legacy `best.b.F()`.
 *
 * The SMALI executes three deterministic passes:
 * 1. every tournament entry receives `k0.c(innerIndex)`;
 * 2. every global player receives `o.d1(0)`;
 * 3. for each competition, only players from `z0()[0].h()` receive `o.D0()`.
 *
 * This rule deliberately freezes ordering and the legacy first-entry quirk only. It does not
 * claim whole-runtime equivalence for `k0.c`, `o.d1` or `o.D0`; those mutations remain separate
 * Phase 15 boundaries until their modern state mappings are proven.
 */
object LegacyAnnualFResetRules {
    sealed interface Action {
        data class CallTournamentReset(
            val competitionIndex: Int,
            val entryIndex: Int,
            val argument: Int,
        ) : Action

        data class ResetGlobalPlayerCounter(
            val playerIndex: Int,
            val value: Int = 0,
        ) : Action

        data class ProgressFirstEntryPlayer(
            val competitionIndex: Int,
            val playerIndex: Int,
        ) : Action
    }

    data class Input(
        val tournamentEntryCounts: List<Int>,
        val globalPlayerCount: Int,
        val firstEntryPlayerCounts: List<Int>,
    ) {
        init {
            require(globalPlayerCount >= 0)
            require(tournamentEntryCounts.all { it >= 0 })
            require(firstEntryPlayerCounts.all { it >= 0 })
            require(firstEntryPlayerCounts.size == tournamentEntryCounts.size) {
                "firstEntryPlayerCounts must describe the same competition list"
            }
        }
    }

    fun plan(input: Input): List<Action> = buildList {
        input.tournamentEntryCounts.forEachIndexed { competitionIndex, entryCount ->
            repeat(entryCount) { entryIndex ->
                add(
                    Action.CallTournamentReset(
                        competitionIndex = competitionIndex,
                        entryIndex = entryIndex,
                        argument = entryIndex,
                    ),
                )
            }
        }

        repeat(input.globalPlayerCount) { playerIndex ->
            add(Action.ResetGlobalPlayerCounter(playerIndex = playerIndex))
        }

        input.firstEntryPlayerCounts.forEachIndexed { competitionIndex, playerCount ->
            repeat(playerCount) { playerIndex ->
                add(
                    Action.ProgressFirstEntryPlayer(
                        competitionIndex = competitionIndex,
                        playerIndex = playerIndex,
                    ),
                )
            }
        }
    }
}
