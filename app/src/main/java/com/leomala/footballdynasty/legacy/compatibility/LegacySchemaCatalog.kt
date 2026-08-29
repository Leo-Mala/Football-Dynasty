package com.leomala.footballdynasty.legacy.compatibility

data class LegacyTypeDescriptor(
    val type: String,
    val role: String,
    val confirmedFields: Set<String>,
)

object LegacySchemaCatalog {
    val player = LegacyTypeDescriptor(
        type = "a.p",
        role = "career player",
        confirmedFields = setOf(
            "anoIn",
            "aposentado",
            "energiaBase",
            "forca",
            "pais",
            "posicao",
            "salario",
            "status",
            "rcClause",
            "rcRenewYear",
            "rcConvYear",
            "pendSaleClub",
            "pendSaleValue",
            "pendIsLoan",
        ),
    )
    val club = LegacyTypeDescriptor(
        type = "a.ac",
        role = "career club",
        confirmedFields = linkedSetOf("xp") + LegacyCareerClubCommercialFields.confirmedNames,
    )
    val match = LegacyTypeDescriptor(
        type = "a.t",
        role = "match state",
        confirmedFields = setOf("qT", "qU"),
    )
    val competition = LegacyTypeDescriptor(
        type = "d.q",
        role = "league/competition",
        confirmedFields = setOf("nomeLiga", "nomeDivisao", "divisao", "nTimes", "nRebaixados", "doisTurnos"),
    )
    val career = LegacyTypeDescriptor(
        type = "a.b",
        role = "career aggregate root",
        confirmedFields = emptySet(),
    )

    val core = listOf(player, club, match, competition, career)
}
