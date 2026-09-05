package com.leomala.footballdynasty.domain.career

/**
 * Pure control-flow projection of reachable legacy `best.b.p()`.
 *
 * The official SMALI preserves two complete sweeps in this exact order:
 * 1. every player from the global senior-player list receives `best.o.e()`;
 * 2. only after that sweep, every junior draft receives `best.p.b()` club by club.
 *
 * This rule freezes ordering only. It deliberately does not implement `best.o.e()` while its
 * durable `M/N` state and implicit RNG branch remain an open Phase 15 boundary. Junior draft
 * progression is already implemented and certified separately by Phase 15.1.
 */
object LegacyAnnualPlayerProgressionSweepRules {
    sealed interface Action {
        data class ProgressSeniorPlayer(
            val playerIndex: Int,
        ) : Action

        data class ProgressJuniorDraft(
            val clubIndex: Int,
            val juniorIndex: Int,
        ) : Action
    }

    data class Input(
        val seniorPlayerCount: Int,
        val juniorDraftCountsByClub: List<Int>,
    ) {
        init {
            require(seniorPlayerCount >= 0)
            require(juniorDraftCountsByClub.all { it >= 0 })
        }
    }

    fun plan(input: Input): List<Action> = buildList {
        repeat(input.seniorPlayerCount) { playerIndex ->
            add(Action.ProgressSeniorPlayer(playerIndex = playerIndex))
        }

        input.juniorDraftCountsByClub.forEachIndexed { clubIndex, juniorCount ->
            repeat(juniorCount) { juniorIndex ->
                add(
                    Action.ProgressJuniorDraft(
                        clubIndex = clubIndex,
                        juniorIndex = juniorIndex,
                    ),
                )
            }
        }
    }
}
