package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.manager.LegacyLineupFormationTables
import com.leomala.footballdynasty.domain.manager.LegacyPlayerSubroleCodeRule
import com.leomala.footballdynasty.domain.match.LegacyMatchN2CounterRules
import com.leomala.footballdynasty.domain.match.LegacyMatchPlayerP0Rules
import com.leomala.footballdynasty.domain.match.LegacyMatchPlayerRatingRules
import com.leomala.footballdynasty.domain.match.LegacyMatchRatingMetricRuntimeRules
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime

/**
 * Proven persisted/transient owner mapping for the inputs of legacy `best.o.n(best.s,int,int)`.
 *
 * This mapper performs no rating RNG draw and creates no persistence. The separate implicit
 * `new java.util.Random()` used by every legacy player-rating call remains a caller-owned
 * compatibility boundary.
 */
object CareerMatchPlayerRatingInputMapper {
    fun map(
        state: LegacyMatchTransientRuntime.State<
            CareerMatchPersistedRuntimeResolver.PersistedClubRoster,
            CareerMatchPersistedRuntimeResolver.PersistedPlayer,
        >,
        metricState: LegacyMatchRatingMetricRuntimeRules.State,
        player: LegacyMatchTransientRuntime.Player<CareerMatchPersistedRuntimeResolver.PersistedPlayer>,
        side: Int,
    ): LegacyMatchPlayerRatingRules.Input {
        require(side == 0 || side == 1) { "Legacy match side must be 0 or 1: $side" }
        val persisted = player.value
        val facts = persisted.facts
        require(facts.position in 0..4) {
            "Unproven legacy position ${facts.position} for rating player ${persisted.playerId}"
        }

        val legacyS = player.legacyG0
        val slotBasePosition = if (legacyS in 1..25) {
            LegacyLineupFormationTables.slotRequirements[legacyS][0]
        } else {
            null
        }
        val p0 = LegacyMatchPlayerP0Rules.resolve(
            legacyS = legacyS,
            legacyL0 = player.legacyL0,
            slotBasePosition = slotBasePosition,
        )
        val metrics = LegacyMatchRatingMetricRuntimeRules.ratingInputs(metricState, side)
        val score = state.score()
        val currentGoals = if (side == 0) score.legacyE else score.legacyF
        val opponentGoals = if (side == 0) score.legacyF else score.legacyE

        val participationMarker = LegacyMatchPlayerRatingRules.resolveParticipationMarker(
            state.events.map { event ->
                LegacyMatchPlayerRatingRules.ParticipationEvent(
                    primaryIsPlayer = event.primaryPlayer === player,
                    secondaryIsPlayer = event.secondaryPlayer === player,
                    legacyPeriod = event.legacyPeriod,
                    legacyMinute = event.legacyMinute,
                )
            },
        )

        return LegacyMatchPlayerRatingRules.Input(
            legacyPlayerJ = persisted.overall,
            legacyP0Flag = p0,
            // Both reads are `best.o.g0()` / field S at this point in the bytecode.
            legacyG0 = legacyS,
            legacyS = legacyS,
            legacyGCategory = facts.position,
            legacyF = LegacyPlayerSubroleCodeRule.resolve(
                positionCode = facts.position,
                cr1 = facts.cr1,
                cr2 = facts.cr2,
            ),
            legacyGCode = facts.cr1,
            currentGoals = currentGoals,
            opponentGoals = opponentGoals,
            currentLegacyE = metrics.currentLegacyE,
            opponentLegacyE = metrics.opponentLegacyE,
            currentLegacyQ0 = metrics.currentLegacyQ0,
            opponentLegacyQ0 = metrics.opponentLegacyQ0,
            opponentLegacyW = metrics.opponentLegacyW,
            opponentLegacyY = metrics.opponentLegacyY,
            star = persisted.star,
            worldTop = persisted.worldTop,
            n2 = LegacyMatchN2CounterRules.snapshot(player.legacyN2),
            participationMarker = participationMarker,
        )
    }
}
