package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.legacy.compatibility.LegacyCareerPlayerCommercialSnapshot

/**
 * Neutral projection of the three pending-movement fields proven on legacy `a.p`.
 *
 * This intentionally does not decide whether a pending movement is valid, accepted,
 * affordable, active, expired, a purchase, a sale, or a loan. Sentinel values remain
 * opaque until their control-flow semantics are recovered from Java/SMALI.
 */
data class LegacyPendingPlayerMovement(
    val clubCode: Int,
    val valueCode: Int,
    val loanFlag: Boolean,
) {
    companion object {
        fun from(snapshot: LegacyCareerPlayerCommercialSnapshot): LegacyPendingPlayerMovement =
            LegacyPendingPlayerMovement(
                clubCode = snapshot.pendSaleClub,
                valueCode = snapshot.pendSaleValue,
                loanFlag = snapshot.pendIsLoan,
            )
    }
}
