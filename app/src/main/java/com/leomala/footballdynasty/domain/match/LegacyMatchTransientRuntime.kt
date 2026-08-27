package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Mutable, persistence-independent compatibility runtime for the proven legacy match mutations.
 *
 * The wrappers intentionally keep reference identity (they are regular classes, not data classes)
 * because the legacy `best.o` / `best.c0` objects do not override equals and ArrayList.remove therefore
 * follows object identity for the match objects used here.
 */
object LegacyMatchTransientRuntime {
    class Player<T>(
        val value: T,
        var legacyG0: Int,
        val legacyL0: Int,
        val legacyF0: Int,
        val legacyR: Int,
        val age: Int,
        var energy: Int,
        var skill: Int,
        var legacyYellowCount: Int = 0,
        var legacyStatM: Int = 0,
        var legacyStatN: Int = 0,
        var selectedOrUsed: Boolean = false,
        var clubSeasonStats: List<LegacyPlayerClubSeasonStatsRules.Entry>? = emptyList(),
    ) {
        var lastInjuryResult: LegacyMatchInjuryRules.Result? = null
            internal set
    }

    class Club<TClub, TPlayer>(
        val value: TClub,
        val legacyClubId: Int,
        val active: MutableList<Player<TPlayer>>,
        val bench: MutableList<Player<TPlayer>>,
        val used: MutableList<Player<TPlayer>> = mutableListOf(),
        var substitutionsRemaining: Int,
        var legacyModeFlag: Boolean = false,
    )

    class State<TClub, TPlayer>(
        val currentSeasonId: Int,
        val home: Club<TClub, TPlayer>,
        val away: Club<TClub, TPlayer>,
    ) {
        val events: MutableList<LegacyMatchEventRecord<Club<TClub, TPlayer>, Player<TPlayer>>> =
            mutableListOf()

        fun score(): LegacyMatchScoreRules.Score = LegacyMatchScoreRules.rebuildFromEvents(
            events = events,
            legacyEClub = home,
            legacyFClub = away,
        )
    }

    data class ApplyResult<TClub, TPlayer>(
        val event: LegacyMatchEventRecord<Club<TClub, TPlayer>, Player<TPlayer>>,
        val substitutionApplied: Boolean,
        val injuryResult: LegacyMatchInjuryRules.Result?,
    )

    fun <TClub, TPlayer> applyEvent(
        state: State<TClub, TPlayer>,
        legacyType: Int,
        legacySubtype: Int,
        eventClub: Club<TClub, TPlayer>?,
        originalPrimary: Player<TPlayer>?,
        secondaryPlayer: Player<TPlayer>? = null,
        legacyPeriod: Int,
        legacyMinute: Int,
        random: RandomSource,
    ): ApplyResult<TClub, TPlayer> {
        val resolvedSide = when {
            eventClub === state.home -> 0
            eventClub === state.away -> 1
            else -> 0
        }
        val resolvedClub = if (resolvedSide == 0) state.home else state.away

        val application = LegacyMatchEventApplicationRules.resolve(
            legacyType = legacyType,
            legacySubtype = legacySubtype,
            homeClub = state.home,
            awayClub = state.away,
            eventClub = eventClub,
            originalPrimary = originalPrimary,
            secondaryPlayer = secondaryPlayer,
            originalPrimaryPositionIndex = originalPrimary?.legacyG0 ?: -1,
            legacyPeriod = legacyPeriod,
            legacyMinute = legacyMinute,
            substitutionsRemainingForResolvedSide = resolvedClub.substitutionsRemaining,
            legacyClubModeFlag = eventClub?.legacyModeFlag ?: false,
            selectLegacyVFromOppositeActive = {
                val opposite = when {
                    eventClub === state.home -> state.away.active
                    eventClub === state.away -> state.home.active
                    else -> emptyList()
                }
                LegacyMatchPlayerSelectionRules.selectV(
                    opposite.map { player ->
                        LegacyMatchPlayerSelectionRules.Candidate(
                            value = player,
                            legacyPositionIndex = player.legacyG0,
                        )
                    },
                    random,
                )?.value
            },
        )

        var injuryResult: LegacyMatchInjuryRules.Result? = null
        var substitutionApplied = false

        LegacyMatchEventApplicationRules.execute(
            result = application,
            appendEvent = { state.events += it },
            applyLegacyPlayerStatM = {
                originalPrimary?.legacyStatM = (originalPrimary?.legacyStatM ?: 0) + 1
            },
            applyLegacyPlayerStatN = {
                originalPrimary?.legacyStatN = (originalPrimary?.legacyStatN ?: 0) + 1
            },
            applyInjuryToOriginalPrimary = {
                val player = checkNotNull(originalPrimary) {
                    "Legacy injury application requires the original primary player"
                }
                val club = checkNotNull(eventClub) {
                    "Legacy injury application requires its event club"
                }
                val injury = LegacyMatchInjuryRules.resolve(
                    age = player.age,
                    energy = player.energy,
                    skill = player.skill,
                    random = random,
                )
                player.skill = injury.updatedSkill
                player.lastInjuryResult = injury
                injuryResult = injury

                val stats = LegacyPlayerClubSeasonStatsRules.apply(
                    entries = player.clubSeasonStats,
                    currentSeasonId = state.currentSeasonId,
                    clubId = club.legacyClubId,
                    legacyCode = 5,
                )
                player.clubSeasonStats = stats.updatedEntries
            },
            removeOriginalPrimaryFromActive = {
                if (originalPrimary != null) {
                    when {
                        eventClub === state.home -> state.home.active.remove(originalPrimary)
                        eventClub === state.away -> state.away.active.remove(originalPrimary)
                    }
                }
            },
            requestSubstitution = { request ->
                substitutionApplied = applySubstitution(
                    state = state,
                    request = request,
                    random = random,
                )
            },
        )

        return ApplyResult(
            event = application.event,
            substitutionApplied = substitutionApplied,
            injuryResult = injuryResult,
        )
    }

    fun <TClub, TPlayer> applyLegacyC(
        state: State<TClub, TPlayer>,
        club: Club<TClub, TPlayer>,
        player: Player<TPlayer>,
        legacyPeriod: Int,
        legacyMinute: Int,
        random: RandomSource,
    ): ApplyResult<TClub, TPlayer> {
        val discipline = LegacyMatchDisciplinaryRules.applyLegacyC(player.legacyYellowCount)
        player.legacyYellowCount = discipline.updatedLegacyCount
        return applyEvent(
            state = state,
            legacyType = discipline.eventType.legacyCode,
            legacySubtype = -1,
            eventClub = club,
            originalPrimary = player,
            legacyPeriod = legacyPeriod,
            legacyMinute = legacyMinute,
            random = random,
        )
    }

    fun <TClub, TPlayer> applyLegacyD(
        state: State<TClub, TPlayer>,
        club: Club<TClub, TPlayer>,
        player: Player<TPlayer>,
        legacyPeriod: Int,
        legacyMinute: Int,
        random: RandomSource,
    ): ApplyResult<TClub, TPlayer> = applyEvent(
        state = state,
        legacyType = LegacyMatchDisciplinaryRules.legacyDType().legacyCode,
        legacySubtype = -1,
        eventClub = club,
        originalPrimary = player,
        legacyPeriod = legacyPeriod,
        legacyMinute = legacyMinute,
        random = random,
    )

    private fun <TClub, TPlayer> applySubstitution(
        state: State<TClub, TPlayer>,
        request: LegacyMatchEventApplicationRules.SubstitutionRequest<Player<TPlayer>>,
        random: RandomSource,
    ): Boolean {
        val original = request.originalPlayer ?: return false
        val club = when (request.side) {
            0 -> state.home
            1 -> state.away
            else -> return false
        }

        fun wrap(player: Player<TPlayer>) = LegacyMatchSubstitutionRules.Player(
            value = player,
            legacyG0 = player.legacyG0,
            legacyL0 = player.legacyL0,
            legacyF0 = player.legacyF0,
            legacyR = player.legacyR,
        )

        val selection = LegacyMatchSubstitutionRules.resolve(
            original = wrap(original),
            active = club.active.map(::wrap),
            bench = club.bench.map(::wrap),
            automaticOutgoing = request.automaticOutgoing,
            enforceLegacyL0Compatibility = request.enforceLegacyL0Compatibility,
            random = random,
        ) ?: return false

        val outgoing = selection.outgoing.value
        val incoming = selection.incoming.value

        club.substitutionsRemaining -= 1
        incoming.legacyG0 = outgoing.legacyG0
        if (original.legacyG0 > 0) {
            incoming.legacyG0 = original.legacyG0
        }
        club.active.remove(outgoing)
        club.active.add(incoming)
        club.used.add(incoming)
        incoming.selectedOrUsed = true
        club.bench.remove(incoming)

        state.events += LegacyMatchEventRecord(
            legacyClub = club,
            legacyType = LegacyMatchEventType.SUBSTITUTION.legacyCode,
            legacySubtype = -1,
            legacyMinute = request.legacyMinute,
            legacyPeriod = request.legacyPeriod,
            primaryPlayer = outgoing,
            secondaryPlayer = incoming,
            legacySide = request.side,
        )
        return true
    }
}
