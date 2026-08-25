package com.leomala.footballdynasty.domain.career

/** Exact integer/rounding projection of legacy `best.o.p()` for the persisted `A0()` value. */
object LegacyPlayerValueRules {
    data class Input(
        val countryGroup: Int,
        val clubLevel: Int,
        val position: Int,
        val status: Int,
        val age: Int,
        val overall: Int,
        val star: Boolean,
        val worldTop: Boolean,
        val legacyCreatedYear: Int,
        val currentYear: Int,
        val legacyHash: Int,
    )

    data class Result(
        val normalizedAge: Int,
        val marketValue: Int,
    )

    fun calculate(input: Input): Result {
        var factor = when {
            input.clubLevel >= 21 -> 750
            input.clubLevel >= 20 -> 600
            input.clubLevel >= 18 -> 500
            input.clubLevel >= 12 -> 400
            else -> 366
        }

        if (input.star) {
            factor = when {
                input.clubLevel >= 22 && input.countryGroup == 0 -> factor * 3
                input.clubLevel >= 21 && input.countryGroup == 0 -> factor * 2
                else -> Math.round(factor.toDouble() * 1.7).toInt()
            }
        }
        if (input.worldTop) {
            factor = Math.round(factor.toDouble() * 1.6).toInt()
        }
        if (input.position == 4) {
            factor = Math.round(factor.toDouble() * 1.3).toInt()
        }
        if (input.status == 1) {
            factor = Math.round(factor.toDouble() + (factor.toDouble() * 0.2)).toInt()
        }

        val age = input.age.coerceAtLeast(16)
        val ageAdjustment = when {
            age < 20 -> (32 - age) * 27
            age <= 25 -> (32 - age) * 22
            age < 32 -> (32 - age) * 15
            age < 34 -> (34 - age) * 10
            else -> -((age - 34) * 50)
        }
        val adjustedFactor = (factor + ageAdjustment).takeIf { it > 0 } ?: 60
        val doubledOverall = input.overall * 2
        var marketValue = doubledOverall * doubledOverall * adjustedFactor

        if (input.legacyCreatedYear > 0 && input.currentYear == input.legacyCreatedYear) {
            marketValue =
                Math.round(marketValue.toDouble() * 0.03).toInt() * input.legacyHash
        }

        return Result(normalizedAge = age, marketValue = marketValue)
    }
}
