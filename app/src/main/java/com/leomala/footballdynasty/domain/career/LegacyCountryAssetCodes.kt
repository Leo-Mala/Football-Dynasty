package com.leomala.footballdynasty.domain.career

/**
 * Country-code lookup recovered from legacy `best.y` P0..P220.
 *
 * This is structural data from the official Brasfoot 2026/27 corpus, used only to select the
 * matching `names/<CODE>.txt` and `surnames/<CODE>.txt` assets.
 */
object LegacyCountryAssetCodes {
    private val codes = arrayOf(
        "AFG", "AFS", "ALB", "ALE", "AND", "AGO", "AIA", "ATG", "CUR", "ARS", "ALG", "ARG", "ARM", "ARU", "AUS", "AUT", "AZE", "BAH", "BHR", "BAN", "BAR", "BEL", "BLZ", "BEN", "BER", "BIE", "BOL", "BOS", "BOT", "BRA", "BRU", "BUL", "BKF", "BUR", "BUT", "CAV", "CAM", "CMJ", "CAN", "CAT", "CAZ", "CHA", "CHI", "CHN", "CPR", "TML", "COL", "CNG", "CRN", "CRS", "COM", "CSR", "CRO", "CUB", "DIN", "DJI", "DOM", "EGI", "ELS", "EMI", "EQU", "ERI", "ESC", "ELQ", "ESV", "ESP", "EST", "ETI", "EUA", "FIJ", "FIN", "FIL", "FRA", "GAB", "GAM", "GAN", "GEO", "GRA", "GRE", "GUA", "GUN", "GUI", "GNB", "GNE", "HAI", "HOL", "HON", "HKG", "HUN", "IEM", "ICA", "ICO", "IFA", "ISA", "IVB", "IND", "IDO", "ING", "IRA", "IRQ", "IRL", "IRN", "ISL", "ISR", "ITA", "MON", "JAM", "JAP", "JOR", "QUE", "KOS", "KUW", "LAO", "LES", "LET", "LBN", "LIB", "LRI", "LIE", "LIT", "LUX", "MAC", "MCD", "MAD", "MAL", "MWI", "MLD", "MLI", "MTA", "MAR", "MAU", "MEX", "MIA", "MOC", "MOL", "MNC", "MGL", "NAM", "NEP", "NIC", "NIR", "NIG", "NOR", "NOZ", "OMA", "PGA", "PAL", "PAN", "PNG", "PAQ", "PAR", "PER", "POL", "PRI", "POR", "QUI", "RCA", "RDG", "RDO", "RTC", "ROM", "RUA", "RUS", "SAM", "SAN", "STL", "SCN", "STP", "SVG", "SEN", "SLE", "SER", "SEY", "SIN", "SIR", "SOM", "SRI", "SUA", "SUD", "SUE", "SUI", "SUR", "TAD", "TAI", "TTI", "TAW", "TAN", "TGO", "TON", "TRT", "TUN", "TCM", "TUR", "UCR", "UGA", "URU", "UZB", "VAN", "VEN", "VIE", "ZAM", "ZIM", "ICM", "MIC", "IMA", "IMR", "NAU", "PLU", "KIR", "SUS", "TUV", "IVA", "MST", "ITC", "SME", "NCA", "GIB", "GDA", "GMA", "MTI", "GFR"
    )

    val count: Int get() = codes.size

    fun codeForLegacyCountry(index: Int): String? = codes.getOrNull(index)
}
