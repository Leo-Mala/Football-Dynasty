package com.leomala.footballdynasty.domain.match

/** Exact boolean branch of legacy player `best.o.P0()`. */
object LegacyMatchPlayerP0Rules {
    fun resolve(
        legacyS: Int,
        legacyL0: Int,
        slotBasePosition: Int?,
    ): Boolean {
        if (legacyS <= 0) return true
        if (legacyS > 25) return false
        val requiredPosition = requireNotNull(slotBasePosition) {
            "Legacy slot $legacyS requires its proven Z1 base position"
        }
        if (requiredPosition == legacyL0) return false
        if ((legacyS == 10 || legacyS == 17) && legacyL0 == 1) return false
        return true
    }
}
