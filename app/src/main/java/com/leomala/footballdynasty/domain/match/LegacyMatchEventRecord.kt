package com.leomala.footballdynasty.domain.match

/** Structural field map for legacy serializable `best.l`. */
data class LegacyMatchEventRecord<TClub, TPlayer>(
    val legacyClub: TClub? = null,
    val legacyType: Int = -1,
    val legacySubtype: Int = -1,
    val legacyMinute: Int = -1,
    val legacyPeriod: Int = -1,
    val primaryPlayer: TPlayer? = null,
    val secondaryPlayer: TPlayer? = null,
    val legacyFlagH: Boolean = false,
    val legacySide: Int = 0,
    val legacyFlagJ: Boolean = false,
) {
    companion object {
        fun <TClub, TPlayer> default(): LegacyMatchEventRecord<TClub, TPlayer> =
            LegacyMatchEventRecord()

        fun <TClub, TPlayer> forSide(legacySide: Int): LegacyMatchEventRecord<TClub, TPlayer> =
            LegacyMatchEventRecord(legacySide = legacySide)

        fun <TClub, TPlayer> marker(
            legacyType: Int,
            legacyFlagH: Boolean,
        ): LegacyMatchEventRecord<TClub, TPlayer> =
            LegacyMatchEventRecord(
                legacyType = legacyType,
                legacyFlagH = legacyFlagH,
            )
    }
}
