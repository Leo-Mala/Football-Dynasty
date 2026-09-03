package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.domain.manager.LegacyLineupMatchLists
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime

/**
 * Application-layer handoff from the characterized ActivityEscalacao.y()/B() result into the
 * already-characterized transient match runtime.
 *
 * This object adds no selection or football semantics: it only applies the exact side lists that
 * LegacyLineupCommitRule already proved. Reference identity and source order are preserved.
 */
object LegacyManagerMatchRuntimeBridge {
    data class Result<TPlayer>(
        val applied: Boolean,
        val sideIndex: Int,
        /** The second starter list written by the legacy lineup commit is retained losslessly. */
        val startersMirror: List<LegacyMatchTransientRuntime.Player<TPlayer>>,
    )

    fun <TClub, TPlayer> applyLineup(
        state: LegacyMatchTransientRuntime.State<TClub, TPlayer>,
        matchLists: LegacyLineupMatchLists<LegacyMatchTransientRuntime.Player<TPlayer>>,
    ): Result<TPlayer> {
        if (!matchLists.replaceSideLists) {
            return Result(
                applied = false,
                sideIndex = matchLists.sideIndex,
                startersMirror = matchLists.startersMirror.toList(),
            )
        }

        val club = when (matchLists.sideIndex) {
            0 -> state.home
            1 -> state.away
            else -> return Result(
                applied = false,
                sideIndex = matchLists.sideIndex,
                startersMirror = matchLists.startersMirror.toList(),
            )
        }

        club.active.clear()
        club.active.addAll(matchLists.startersPrimary)
        club.bench.clear()
        club.bench.addAll(matchLists.bench)

        return Result(
            applied = true,
            sideIndex = matchLists.sideIndex,
            startersMirror = matchLists.startersMirror.toList(),
        )
    }
}
