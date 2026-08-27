package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Direct structural parity for the reachable legacy player injury routine `best.o.m(c0)`. */
object LegacyMatchInjuryRules {
    data class Result(
        val durationDays: Int,
        val updatedSkill: Int,
        val shouldSetInjuryUntil: Boolean,
        val eventType: LegacyMatchEventType = LegacyMatchEventType.INJURY,
    )

    fun resolve(
        age: Int,
        energy: Int,
        skill: Int,
        random: RandomSource,
    ): Result {
        // Legacy constructs three independent Random instances, but the observable control-flow
        // contract is three ordered draws with these exact bounds. The bound-20 draw is consumed
        // even for ages where its value is never used.
        val baseDays = random.nextInt(14)
        val olderAgeDays = random.nextInt(20) + 5

        val energyModifier = when {
            energy < 10 -> 5
            energy < 50 -> 1
            else -> 0
        }

        var duration = when {
            age <= 20 -> baseDays
            age <= 25 -> energyModifier + baseDays + 1
            age <= 30 -> energyModifier + baseDays + 2
            age <= 35 -> energyModifier + baseDays + 3
            age <= 40 -> energyModifier + baseDays + olderAgeDays
            age <= 45 -> energyModifier + baseDays + olderAgeDays
            else -> energyModifier + baseDays + 10 + olderAgeDays
        }

        var updatedSkill = skill
        if (age >= 35) {
            updatedSkill -= 5
            // Preserve the legacy strict-negative clamp: exactly zero remains zero.
            if (updatedSkill < 0) {
                updatedSkill = 1
            }
        }

        when (random.nextInt(100)) {
            1 -> duration += 70
            in 0..3 -> duration += 40
            in 4..9 -> duration += 20
        }

        return Result(
            durationDays = duration,
            updatedSkill = updatedSkill,
            shouldSetInjuryUntil = duration > 0,
        )
    }
}
