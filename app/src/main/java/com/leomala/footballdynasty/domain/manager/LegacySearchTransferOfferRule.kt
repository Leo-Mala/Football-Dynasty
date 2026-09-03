package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of `ActivityProcura.t(best.o, best.c0, int)` from the
 * official Brasfoot SMALI corpus.
 *
 * The legacy method evaluates a purchase offer after earlier UI/precondition
 * checks. Its threshold depends on the player's raw position code, the selling
 * club's count at that position, strength, age and player value. The caller's
 * `best.f.x(player, destinationClub)` result is supplied as an opaque code so
 * this rule does not invent or duplicate that separate legacy policy.
 *
 * Arithmetic deliberately follows the JVM operation order from SMALI,
 * including Int multiplication/division and Java Math.round behavior.
 */
data class LegacySearchTransferOfferInput(
    val playerPositionCode: Int,
    val sellerPositionCounts: IntArray,
    val playerStrength: Int,
    val playerAge: Int,
    val playerValue: Int,
    val offerValue: Int,
    val buyerFunds: Long,
    val destinationAcceptanceCode: Int,
)

enum class LegacySearchTransferOfferDecision(val legacyCode: Int) {
    OFFER_REFUSED(0),
    PLAYER_BOUGHT(1),
    DOES_NOT_WANT_TO_JOIN(4),
    WANTS_NEW_SALARY(6),
    COUNTER_PROPOSAL(7),
}

data class LegacySearchTransferOfferResult(
    val decision: LegacySearchTransferOfferDecision,
    val counterOfferValue: Int? = null,
)

object LegacySearchTransferOfferRule {
    private val requiredPositionDepth = intArrayOf(3, 4, 4, 5, 4, 4)
    private val onePlayerScarcityPremium = doubleArrayOf(1.5, 1.5, 1.5, 1.5, 2.0, 1.0)
    private val otherScarcityPremium = doubleArrayOf(1.0, 1.0, 1.0, 1.0, 1.5, 2.0)
    private val establishedPlayerPremium = doubleArrayOf(0.5, 0.2, 0.2, 0.2, 0.5, 1.0)
    private val weakOrOldDiscountPercent = intArrayOf(15, 20, 20, 10, 10, 2)

    fun minimumAcceptedOffer(input: LegacySearchTransferOfferInput): Int {
        val position = input.playerPositionCode
        val sellerCountAtPosition = input.sellerPositionCounts[position]
        val value = input.playerValue

        if (sellerCountAtPosition < requiredPositionDepth[position]) {
            val multiplier = if (sellerCountAtPosition == 1) {
                onePlayerScarcityPremium[position]
            } else {
                otherScarcityPremium[position]
            }
            return value + Math.round(value.toDouble() * multiplier).toInt()
        }

        if (input.playerStrength >= 30 && input.playerAge <= 35) {
            return value + Math.round(
                value.toDouble() * establishedPlayerPremium[position],
            ).toInt()
        }

        val discount = Math.round(
            ((value * weakOrOldDiscountPercent[position]) / 100).toFloat(),
        )
        return value - discount
    }

    fun evaluate(input: LegacySearchTransferOfferInput): LegacySearchTransferOfferResult {
        val minimum = minimumAcceptedOffer(input)

        if (input.offerValue >= minimum) {
            val decision = when (input.destinationAcceptanceCode) {
                1 -> LegacySearchTransferOfferDecision.DOES_NOT_WANT_TO_JOIN
                2 -> LegacySearchTransferOfferDecision.WANTS_NEW_SALARY
                else -> LegacySearchTransferOfferDecision.PLAYER_BOUGHT
            }
            return LegacySearchTransferOfferResult(decision = decision)
        }

        if (
            input.buyerFunds >= minimum.toLong() &&
            (input.destinationAcceptanceCode == 0 || input.destinationAcceptanceCode == 2)
        ) {
            return LegacySearchTransferOfferResult(
                decision = LegacySearchTransferOfferDecision.COUNTER_PROPOSAL,
                counterOfferValue = minimum,
            )
        }

        return LegacySearchTransferOfferResult(
            decision = LegacySearchTransferOfferDecision.OFFER_REFUSED,
        )
    }
}
