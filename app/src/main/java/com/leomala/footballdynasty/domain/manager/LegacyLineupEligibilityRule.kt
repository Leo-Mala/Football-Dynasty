package com.leomala.footballdynasty.domain.manager

data class LegacyLineupEligibilityPlayer<T>(
    val runtime: LegacyLineupRuntimePlayer<T>,
    val blockedByM0: Boolean,
    val excludedByCompetitionV0: Boolean,
    val hasClub: Boolean,
    val clubActiveQ0: Boolean?,
    val contractTimestampK: Long,
)

data class LegacyLineupEligibilityResult<T>(val eligible: List<LegacyLineupRuntimePlayer<T>>, val unavailable: List<LegacyLineupRuntimePlayer<T>>)

/** ActivityEscalacao.v() + best.o.K0() + f3.w/f3.s. Opaque legacy predicates stay opaque. */
object LegacyLineupEligibilityRule {
    fun <T> isEligible(player: LegacyLineupEligibilityPlayer<T>, competitionRestrictionActive: Boolean, modeFlag: Boolean, currentCareerTime: Long): Boolean {
        if (player.blockedByM0) return false
        var eligible = true
        if (competitionRestrictionActive && player.excludedByCompetitionV0) eligible = false
        if (modeFlag || !player.hasClub || player.clubActiveQ0 == null || player.clubActiveQ0 == false || player.contractTimestampK >= currentCareerTime) return eligible
        return false
    }

    fun <T> classify(roster: List<LegacyLineupEligibilityPlayer<T>>, convertedAuxiliaryWhenMode: List<LegacyLineupRuntimePlayer<T>>, competitionRestrictionActive: Boolean, modeFlag: Boolean, currentCareerTime: Long): LegacyLineupEligibilityResult<T> {
        val eligible = mutableListOf<LegacyLineupRuntimePlayer<T>>(); val unavailable = mutableListOf<LegacyLineupRuntimePlayer<T>>()
        roster.forEach { (if (isEligible(it, competitionRestrictionActive, modeFlag, currentCareerTime)) eligible else unavailable).add(it.runtime) }
        if (modeFlag) eligible += convertedAuxiliaryWhenMode
        return LegacyLineupEligibilityResult(eligible.sortedWith(::compareEligible), unavailable.sortedWith(::compareUnavailable))
    }
    private fun <T> compareEligible(a: LegacyLineupRuntimePlayer<T>, b: LegacyLineupRuntimePlayer<T>): Int { if(a.skill>b.skill)return -1;if(a.skill<b.skill)return 1;if(a.energy>b.energy)return -1;if(a.energy<b.energy)return 1;return 0 }
    private fun <T> compareUnavailable(a: LegacyLineupRuntimePlayer<T>, b: LegacyLineupRuntimePlayer<T>): Int { if(a.positionCode>b.positionCode)return 1;if(a.positionCode<b.positionCode)return -1;if(a.subroleCode>b.subroleCode)return 1;if(a.subroleCode<b.subroleCode)return -1;if(a.skill>b.skill)return -1;if(a.skill<b.skill)return 1;if(a.star&&!b.star)return -1;if(!a.star&&b.star)return 1;return 0 }
}
