package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Exact field/RNG projection of the annual `best.t.e(false, p, target)` path before storage. */
object LegacyProceduralMaterializationRules {
    const val DEFAULT_STATUS: Int = 0
    const val INITIAL_DURATION_DAYS: Long = 300L

    data class TargetContext(
        val legacyR0: Boolean,
        val legacyO: Int,
        val legacyP0: Int,
        val legacyJ: Int,
        val clubLevel: Int,
        val currentYear: Int,
    )

    data class Materialized(
        val name: String,
        val age: Int,
        val country: Int,
        val position: Int,
        val status: Int,
        val side: Int,
        val cr1: Int,
        val cr2: Int,
        val star: Boolean,
        val worldTop: Boolean,
        val legacyHash: Int,
        val overall: Int,
        val marketValue: Int,
        val legacyGeneratedO: Int,
        val legacyCreatedYear: Int,
        val durationDays: Long,
    )

    fun materialize(
        random: RandomSource,
        draft: LegacyProceduralPlayerRules.Draft,
        target: TargetContext,
    ): Materialized {
        val overall = LegacyProceduralToPlayerRules.convertedLegacyN(
            random = random,
            targetR0 = target.legacyR0,
            targetO = target.legacyO,
            targetP0 = target.legacyP0,
            targetJ = target.legacyJ,
            draftLegacyN = draft.legacyN,
            draftLegacyO = draft.legacyO,
        )
        val side = if (draft.legacyG == 0) 0 else 1
        val playerCountryGroup = requireNotNull(
            LegacyCountryAssetCodes.groupForLegacyCountry(draft.legacyD)
        ) { "Unknown legacy country P${draft.legacyD}" }

        // In legacy best.t.e the player calls p()/o() before the final O0/M flag RNG block.
        // Therefore initial A0 is calculated with both flags still false and with the original
        // draft v()/hash. J1(8) can happen only afterwards and must not retroactively change A0.
        val valueBeforeFinalFlags = LegacyPlayerValueRules.calculate(
            LegacyPlayerValueRules.Input(
                countryGroup = playerCountryGroup,
                clubLevel = target.clubLevel,
                position = draft.legacyE,
                status = DEFAULT_STATUS,
                age = draft.legacyC,
                overall = overall,
                star = false,
                worldTop = false,
                legacyCreatedYear = target.currentYear,
                currentYear = target.currentYear,
                legacyHash = draft.legacyN,
            )
        )

        val flags = LegacyProceduralToPlayerRules.flags(random, draft.legacyB)
        val finalHash =
            LegacyProceduralToPlayerRules.finalLegacyNAfterFlags(draft.legacyN, flags.setO0)

        return Materialized(
            name = draft.name,
            age = valueBeforeFinalFlags.normalizedAge,
            country = draft.legacyD,
            position = draft.legacyE,
            status = DEFAULT_STATUS,
            side = side,
            cr1 = draft.legacyJ,
            cr2 = draft.legacyL,
            star = flags.setO0,
            worldTop = flags.setM,
            legacyHash = finalHash,
            overall = overall,
            marketValue = valueBeforeFinalFlags.marketValue,
            legacyGeneratedO = draft.legacyO,
            legacyCreatedYear = target.currentYear,
            durationDays = INITIAL_DURATION_DAYS,
        )
    }

    fun deterministicPlayerId(careerId: String, drawsAfterMaterialization: Long): String {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        require(drawsAfterMaterialization >= 0L) { "Draw count must not be negative" }
        return "career:$careerId:procedural:$drawsAfterMaterialization"
    }
}
