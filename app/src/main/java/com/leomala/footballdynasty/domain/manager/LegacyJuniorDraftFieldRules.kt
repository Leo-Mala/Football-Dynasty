package com.leomala.footballdynasty.domain.manager

/**
 * Exact non-RNG fields of legacy `best.p` that are computed during `best.p.d(...)` but were not
 * part of the older procedural-player draft model.
 *
 * SMALI is authoritative. All caller/global values that the bytecode reads are explicit inputs so
 * this rule remains pure and can be persisted/replayed without consulting mutable process globals.
 */
object LegacyJuniorDraftFieldRules {
    data class ValueContext(
        val targetF0: Int,
        val targetV0: Boolean,
        val targetO: Int,
        val globalV1: Boolean,
        val countryGroup: Int,
        val legacyB: Boolean,
        val legacyC: Int,
        val legacyE: Int,
        val legacyF: Int,
        val legacyN: Int,
    )

    data class MissingDraftFields(
        val legacyH: Int,
        val legacyI: Int,
        val developmentRemainder: Double = 0.0,
    )

    /** Exact final value assigned to `best.p.h` by `best.p.f(c0)`. */
    fun legacyH(context: ValueContext): Int {
        var base = when {
            context.targetF0 >= 21 -> 750
            context.targetF0 >= 20 -> 600
            context.targetF0 >= 18 -> 500
            context.targetF0 >= 12 -> 400
            else -> 366
        }
        if (context.legacyB) {
            base = when {
                context.targetF0 >= 22 && context.countryGroup == 0 -> base * 3
                context.targetF0 >= 21 && context.countryGroup == 0 -> base * 2
                else -> roundLegacy(base * 1.7)
            }
        }
        if (context.legacyE == 4) {
            base = roundLegacy(base * 1.3)
        }

        val age = context.legacyC.coerceAtLeast(16)
        val ageAdjustment = when {
            age < 20 -> (32 - age) * 27
            age <= 25 -> (32 - age) * 22
            age < 32 -> (32 - age) * 15
            age < 34 -> (34 - age) * 10
            else -> -(age - 34) * 50
        }
        base += ageAdjustment
        if (base <= 0) base = 60

        val squaredDoubleF = (context.legacyF * 2) * (context.legacyF * 2)
        var value = roundLegacy(squaredDoubleF.toDouble() * base.toDouble() * 0.03) * context.legacyN
        if (context.legacyN == 10) {
            value = roundLegacy(value * 1.5)
        }
        return value
    }

    /** Exact final value assigned to `best.p.i` by `best.p.e(c0)`. */
    fun legacyI(context: ValueContext): Int {
        var base = if (context.targetV0) {
            when (context.targetO) {
                1 -> 750
                2 -> 550
                3 -> 500
                4, 5 -> 450
                else -> 350
            }
        } else {
            when (context.targetO) {
                1 -> 600
                2 -> 500
                3 -> 450
                4, 5 -> 400
                else -> 350
            }
        }
        if (context.targetF0 > 20) base += 50
        base += when (context.legacyE) {
            0 -> -70
            1 -> -30
            2 -> -40
            4 -> -50
            else -> 0
        }

        val halfBase = roundLegacy(base * 0.5)
        var value = context.legacyF * 2 * halfBase
        if (context.legacyC >= 32) {
            value -= (context.legacyC - 32) * 300
        }
        if (context.legacyB) {
            value += context.legacyF * 250
        }
        value = value.coerceAtLeast(500)
        var result = roundLegacy(value * 0.1)
        if (context.globalV1) result *= 4
        return result
    }

    fun missingFields(context: ValueContext): MissingDraftFields = MissingDraftFields(
        legacyH = legacyH(context),
        legacyI = legacyI(context),
        // `best.p` constructor initializes D to zero and `best.p.d(...)` does not mutate it.
        developmentRemainder = 0.0,
    )

    /**
     * Final list-side effects selected by boolean p0 in `best.t.e(p0, p, c0)`.
     * Player-field materialization and all RNG before these effects are shared by both routes.
     */
    enum class PromotionRoute {
        MANUAL_FALSE,
        ANNUAL_TRUE,
    }

    data class PromotionListEffects(
        val removeDraftFromClubImmediately: Boolean,
        val stageDraftInLegacyL1: Boolean,
        val stageMaterializedPlayerInLegacyD0: Boolean,
        val stageMaterializedPlayerInLegacyJ1: Boolean,
    )

    fun promotionListEffects(route: PromotionRoute): PromotionListEffects = when (route) {
        PromotionRoute.MANUAL_FALSE -> PromotionListEffects(
            removeDraftFromClubImmediately = true,
            stageDraftInLegacyL1 = false,
            stageMaterializedPlayerInLegacyD0 = true,
            stageMaterializedPlayerInLegacyJ1 = false,
        )
        PromotionRoute.ANNUAL_TRUE -> PromotionListEffects(
            removeDraftFromClubImmediately = false,
            stageDraftInLegacyL1 = true,
            stageMaterializedPlayerInLegacyD0 = false,
            stageMaterializedPlayerInLegacyJ1 = true,
        )
    }

    private fun roundLegacy(value: Double): Int = java.lang.Math.round(value).toInt()
}
