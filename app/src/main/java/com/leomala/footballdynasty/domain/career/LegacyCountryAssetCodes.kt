package com.leomala.footballdynasty.domain.career

/**
 * Country code/group lookup recovered from legacy `best.y` P0..P220.
 *
 * Codes select `names/<CODE>.txt` / `surnames/<CODE>.txt`. Groups reproduce `best.y.g()` and are
 * used by legacy player valuation. Both arrays come exclusively from the official 2026/27 corpus.
 */
object LegacyCountryAssetCodes {
    private val codes = arrayOf(
        "AFG", "AFS", "ALB", "ALE", "AND", "AGO", "AIA", "ATG", "CUR", "ARS", "ALG", "ARG", "ARM", "ARU", "AUS", "AUT", "AZE", "BAH", "BHR", "BAN", "BAR", "BEL", "BLZ", "BEN", "BER", "BIE", "BOL", "BOS", "BOT", "BRA", "BRU", "BUL", "BKF", "BUR", "BUT", "CAV", "CAM", "CMJ", "CAN", "CAT", "CAZ", "CHA", "CHI", "CHN", "CPR", "TML", "COL", "CNG", "CRN", "CRS", "COM", "CSR", "CRO", "CUB", "DIN", "DJI", "DOM", "EGI", "ELS", "EMI", "EQU", "ERI", "ESC", "ELQ", "ESV", "ESP", "EST", "ETI", "EUA", "FIJ", "FIN", "FIL", "FRA", "GAB", "GAM", "GAN", "GEO", "GRA", "GRE", "GUA", "GUN", "GUI", "GNB", "GNE", "HAI", "HOL", "HON", "HKG", "HUN", "IEM", "ICA", "ICO", "IFA", "ISA", "IVB", "IND", "IDO", "ING", "IRA", "IRQ", "IRL", "IRN", "ISL", "ISR", "ITA", "MON", "JAM", "JAP", "JOR", "QUE", "KOS", "KUW", "LAO", "LES", "LET", "LBN", "LIB", "LRI", "LIE", "LIT", "LUX", "MAC", "MCD", "MAD", "MAL", "MWI", "MLD", "MLI", "MTA", "MAR", "MAU", "MEX", "MIA", "MOC", "MOL", "MNC", "MGL", "NAM", "NEP", "NIC", "NIR", "NIG", "NOR", "NOZ", "OMA", "PGA", "PAL", "PAN", "PNG", "PAQ", "PAR", "PER", "POL", "PRI", "POR", "QUI", "RCA", "RDG", "RDO", "RTC", "ROM", "RUA", "RUS", "SAM", "SAN", "STL", "SCN", "STP", "SVG", "SEN", "SLE", "SER", "SEY", "SIN", "SIR", "SOM", "SRI", "SUA", "SUD", "SUE", "SUI", "SUR", "TAD", "TAI", "TTI", "TAW", "TAN", "TGO", "TON", "TRT", "TUN", "TCM", "TUR", "UCR", "UGA", "URU", "UZB", "VAN", "VEN", "VIE", "ZAM", "ZIM", "ICM", "MIC", "IMA", "IMR", "NAU", "PLU", "KIR", "SUS", "TUV", "IVA", "MST", "ITC", "SME", "NCA", "GIB", "GDA", "GMA", "MTI", "GFR"
    )

    private val groups = intArrayOf(
        3, 2, 0, 0, 0, 2, 4, 4, 4, 3, 2, 1, 0, 4, 3, 0, 0, 4, 3, 3, 4, 0, 4, 2, 4, 0, 1, 0, 2, 1, 3, 0, 2, 2, 3, 2, 2, 3, 4, 3, 0, 2, 1, 3, 0, 3, 1, 2, 3, 3, 2, 4, 0, 4, 0, 2, 4, 2, 4, 3, 1, 2, 0, 0, 0, 0, 0, 2, 4, 5, 0, 3, 0, 2, 2, 2, 0, 4, 0, 4, 4, 2, 2, 2, 4, 0, 4, 3, 0, 3, 4, 5, 0, 5, 4, 3, 3, 0, 3, 3, 0, 0, 0, 0, 0, 0, 4, 3, 3, 2, 0, 3, 3, 2, 0, 3, 2, 2, 0, 0, 0, 3, 0, 2, 3, 2, 3, 2, 0, 2, 2, 4, 3, 2, 0, 0, 3, 2, 3, 4, 2, 2, 0, 5, 3, 0, 3, 4, 5, 3, 1, 1, 0, 4, 0, 3, 2, 2, 4, 0, 0, 2, 0, 5, 0, 4, 4, 2, 4, 2, 2, 0, 2, 3, 3, 2, 3, 2, 2, 0, 0, 4, 3, 3, 5, 3, 2, 2, 5, 4, 2, 3, 0, 0, 2, 1, 3, 5, 1, 3, 2, 2, 2, 5, 5, 2, 5, 5, 5, 2, 5, 4, 4, 4, 5, 5, 0, 4, 5, 4, 4
    )

    init {
        check(codes.size == 221 && groups.size == 221) { "Legacy country lookup must cover P0..P220" }
    }

    val count: Int get() = codes.size

    fun codeForLegacyCountry(index: Int): String? = codes.getOrNull(index)

    fun groupForLegacyCountry(index: Int): Int? = groups.getOrNull(index)
}
