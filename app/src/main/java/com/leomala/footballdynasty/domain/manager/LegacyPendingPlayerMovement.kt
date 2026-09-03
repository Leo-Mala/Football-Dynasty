package com.leomala.footballdynasty.domain.manager

/**
 * Neutral projection of the three pending-movement fields proven on legacy `a.p`.
 *
 * This intentionally does not decide whether a pending movement is valid, accepted,
 * affordable, active, expired, a purchase, a sale, or a loan. Sentinel values remain
 * opaque until their control-flow semantics are recovered from Java/SMALI.
 *
 * The domain type accepts only raw scalar values so the modern domain stays independent
 * from the legacy compatibility implementation layer.
 */
data class LegacyPendingPlayerMovement(
    val clubCode: Int,
    val valueCode: Int,
    val loanFlag: Boolean,
) {
    companion object {
        fun fromRaw(
            pendSaleClub: Int,
            pendSaleValue: Int,
            pendIsLoan: Boolean,
        ): LegacyPendingPlayerMovement = LegacyPendingPlayerMovement(
            clubCode = pendSaleClub,
            valueCode = pendSaleValue,
            loanFlag = pendIsLoan,
        )
    }
}
