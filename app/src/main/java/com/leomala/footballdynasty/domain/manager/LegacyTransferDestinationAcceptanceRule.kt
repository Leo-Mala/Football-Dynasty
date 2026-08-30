package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of `best.f.x(best.o, best.c0)` from the official legacy
 * Java/SMALI corpus.
 *
 * The obfuscated scalar getters used by that method are intentionally exposed
 * here as raw codes. Their control flow is proven, but assigning modern domain
 * names to every code would exceed the available evidence.
 *
 * Legacy result codes are consumed by `ActivityProcura.t(...)` and
 * `ActivityTimes.s(...)`:
 * - 0: transfer destination passes this policy without a salary side effect;
 * - 1: destination is rejected by this policy;
 * - 2: destination passes only with the legacy salary side effect, exactly
 *   current salary multiplied by two.
 */
data class LegacyTransferDestinationAcceptanceInput(
    val destinationJCode: Int,
    val playerECode: Int,
    val sellerP0Code: Int,
    val destinationP0Code: Int,
    val destinationOCode: Int,
    val playerSalary: Int,
)

data class LegacyTransferDestinationAcceptanceResult(
    val acceptanceCode: Int,
    val requiredSalary: Int? = null,
)

object LegacyTransferDestinationAcceptanceRule {
    fun evaluate(
        input: LegacyTransferDestinationAcceptanceInput,
    ): LegacyTransferDestinationAcceptanceResult {
        if (input.destinationJCode == 0) {
            return LegacyTransferDestinationAcceptanceResult(acceptanceCode = 0)
        }

        if (input.playerECode != 0 || input.sellerP0Code < 4) {
            if (input.sellerP0Code >= 3 && input.destinationP0Code >= 3) {
                return LegacyTransferDestinationAcceptanceResult(acceptanceCode = 0)
            }
            if (input.sellerP0Code == 3 && input.destinationP0Code == 2) {
                return LegacyTransferDestinationAcceptanceResult(acceptanceCode = 0)
            }
            if (
                (input.sellerP0Code == 3 && input.destinationP0Code == 1) ||
                input.sellerP0Code <= 2
            ) {
                return LegacyTransferDestinationAcceptanceResult(acceptanceCode = 0)
            }
        } else {
            if (input.destinationP0Code >= 4 || input.destinationOCode == 1) {
                return LegacyTransferDestinationAcceptanceResult(acceptanceCode = 0)
            }
            if (input.destinationP0Code >= 3 || input.destinationOCode == 1) {
                return LegacyTransferDestinationAcceptanceResult(
                    acceptanceCode = 2,
                    requiredSalary = input.playerSalary * 2,
                )
            }
        }

        return LegacyTransferDestinationAcceptanceResult(acceptanceCode = 1)
    }
}
