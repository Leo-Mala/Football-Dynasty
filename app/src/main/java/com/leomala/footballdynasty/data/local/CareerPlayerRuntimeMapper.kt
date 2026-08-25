package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerProceduralPlayerEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.PlayerEntity
import com.leomala.footballdynasty.domain.career.LegacyCareerPlayerInitializationRules
import com.leomala.footballdynasty.domain.career.LegacyCountryAssetCodes
import com.leomala.footballdynasty.domain.career.LegacyPlayerValueRules
import com.leomala.footballdynasty.domain.career.LegacyProceduralMaterializationRules
import com.leomala.footballdynasty.foundation.random.RandomSource

/** Maps proven legacy runtime values into the career-scoped Room V3 tables. */
object CareerPlayerRuntimeMapper {
    const val MILLIS_PER_DAY: Long = 86_400_000L

    data class TargetContext(
        val clubId: String,
        val legacyR0: Boolean,
        val legacyO: Int,
        val legacyP0: Int,
        val legacyF0: Int,
        val currentYear: Int,
        val currentGameEpochMillis: Long,
        val rosterKind: String,
        val sourceOrdinal: Int,
    )

    data class CanonicalBundle(
        val runtime: CareerPlayerRuntimeEntity,
        val membership: CareerSquadMembershipEntity,
    )

    data class ProceduralBundle(
        val runtime: CareerPlayerRuntimeEntity,
        val procedural: CareerProceduralPlayerEntity,
        val membership: CareerSquadMembershipEntity,
    )

    fun canonical(
        random: RandomSource,
        careerId: String,
        player: PlayerEntity,
        target: TargetContext,
    ): CanonicalBundle {
        requireIdentity(careerId, player.id, target)
        val initialized = LegacyCareerPlayerInitializationRules.initialize(
            random = random,
            input = LegacyCareerPlayerInitializationRules.Input(
                targetR0 = target.legacyR0,
                targetO = target.legacyO,
                targetP0 = target.legacyP0,
                targetF0 = target.legacyF0,
                playerStatus = player.status,
                playerStar = player.star,
                playerWorldTop = player.worldTop,
            ),
        )
        val age = normalizeLoadedAge(player.age)
        val countryGroup = requireNotNull(LegacyCountryAssetCodes.groupForLegacyCountry(player.country)) {
            "Unknown legacy country P${player.country}"
        }
        val value = LegacyPlayerValueRules.calculate(
            LegacyPlayerValueRules.Input(
                countryGroup = countryGroup,
                clubLevel = target.legacyF0,
                position = player.position,
                status = player.status,
                age = age,
                overall = initialized.overall,
                star = player.star,
                worldTop = player.worldTop,
                legacyCreatedYear = 0,
                currentYear = target.currentYear,
                legacyHash = player.legacyHash,
            )
        )
        return CanonicalBundle(
            runtime = CareerPlayerRuntimeEntity(
                careerId = careerId,
                playerId = player.id,
                sourceType = CareerPlayerRuntimeStore.SOURCE_CANONICAL,
                stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
                age = value.normalizedAge,
                overall = initialized.overall,
                marketValue = value.marketValue,
                star = player.star,
                worldTop = player.worldTop,
                legacyHash = player.legacyHash,
                legacyGeneratedO = 0,
                legacyCreatedYear = 0,
                contractEndEpochMillis = addDays(target.currentGameEpochMillis, initialized.contractDays),
                legacyPreviousMarketValue = 0,
                legacyQ = false,
                legacyX = false,
                legacyY = false,
                legacyZ = false,
            ),
            membership = CareerSquadMembershipEntity(
                careerId = careerId,
                playerId = player.id,
                clubId = target.clubId,
                rosterKind = target.rosterKind,
                sourceOrdinal = target.sourceOrdinal,
            ),
        )
    }

    fun procedural(
        careerId: String,
        playerId: String,
        materialized: LegacyProceduralMaterializationRules.Materialized,
        target: TargetContext,
    ): ProceduralBundle {
        requireIdentity(careerId, playerId, target)
        return ProceduralBundle(
            runtime = CareerPlayerRuntimeEntity(
                careerId = careerId,
                playerId = playerId,
                sourceType = CareerPlayerRuntimeStore.SOURCE_PROCEDURAL,
                stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
                age = materialized.age,
                overall = materialized.overall,
                marketValue = materialized.marketValue,
                star = materialized.star,
                worldTop = materialized.worldTop,
                legacyHash = materialized.legacyHash,
                legacyGeneratedO = materialized.legacyGeneratedO,
                legacyCreatedYear = materialized.legacyCreatedYear,
                contractEndEpochMillis = addDays(target.currentGameEpochMillis, materialized.durationDays),
                legacyPreviousMarketValue = 0,
                legacyQ = false,
                legacyX = false,
                legacyY = false,
                legacyZ = false,
            ),
            procedural = CareerProceduralPlayerEntity(
                careerId = careerId,
                playerId = playerId,
                name = materialized.name,
                country = materialized.country,
                position = materialized.position,
                status = materialized.status,
                side = materialized.side,
                cr1 = materialized.cr1,
                cr2 = materialized.cr2,
            ),
            membership = CareerSquadMembershipEntity(
                careerId = careerId,
                playerId = playerId,
                clubId = target.clubId,
                rosterKind = target.rosterKind,
                sourceOrdinal = target.sourceOrdinal,
            ),
        )
    }

    /** Legacy `best.o.u1`: imported ages outside 16..48 are replaced with 35. */
    fun normalizeLoadedAge(age: Int): Int = if (age in 16..48) age else 35

    fun addDays(currentGameEpochMillis: Long, days: Long): Long {
        require(days >= 0L) { "Days must not be negative" }
        return Math.addExact(currentGameEpochMillis, Math.multiplyExact(days, MILLIS_PER_DAY))
    }

    private fun requireIdentity(careerId: String, playerId: String, target: TargetContext) {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        require(playerId.isNotBlank()) { "Player id must not be blank" }
        require(target.clubId.isNotBlank()) { "Club id must not be blank" }
        require(target.rosterKind.isNotBlank()) { "Roster kind must not be blank" }
        require(target.sourceOrdinal >= 0) { "Source ordinal must be non-negative" }
    }
}
