package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyManagerInteractionEvidenceTest {
    @Test
    fun confirmedCatalogMatchesOnlyVersionedActivityLayoutEvidence() {
        assertEquals(
            linkedSetOf(
                LegacyManagerInteractionEvidence.PLAYER_SEARCH_PROPOSAL,
                LegacyManagerInteractionEvidence.PLAYER_CONTRACT,
                LegacyManagerInteractionEvidence.PLAYER_SALE,
                LegacyManagerInteractionEvidence.PLAYER_RETIREMENT,
                LegacyManagerInteractionEvidence.TEAM_PROPOSAL,
                LegacyManagerInteractionEvidence.CAREER_CLUB_OFFER,
            ),
            LegacyManagerInteractionEvidenceCatalog.confirmed,
        )
    }

    @Test
    fun resolvesOnlyExactClassAndObservedLayoutPairs() {
        assertEquals(
            LegacyManagerInteractionEvidence.PLAYER_SEARCH_PROPOSAL,
            LegacyManagerInteractionEvidenceCatalog.fromExactSource(
                "ActivityProcura",
                "dialog_proposta",
            ),
        )
        assertEquals(
            LegacyManagerInteractionEvidence.PLAYER_CONTRACT,
            LegacyManagerInteractionEvidenceCatalog.fromExactSource(
                "DialogIgrokInfo",
                "dialog_contrato",
            ),
        )
        assertEquals(
            LegacyManagerInteractionEvidence.PLAYER_SALE,
            LegacyManagerInteractionEvidenceCatalog.fromExactSource(
                "DialogIgrokInfo",
                "dialog_venda",
            ),
        )
        assertEquals(
            LegacyManagerInteractionEvidence.PLAYER_RETIREMENT,
            LegacyManagerInteractionEvidenceCatalog.fromExactSource(
                "DialogIgrokInfo",
                "dialog_aposentar",
            ),
        )
        assertEquals(
            LegacyManagerInteractionEvidence.TEAM_PROPOSAL,
            LegacyManagerInteractionEvidenceCatalog.fromExactSource(
                "ActivityTimes",
                "dialog_proposta",
            ),
        )
        assertEquals(
            LegacyManagerInteractionEvidence.CAREER_CLUB_OFFER,
            LegacyManagerInteractionEvidenceCatalog.fromExactSource(
                "ActivityMainTeam",
                "dialog_oferta",
            ),
        )
    }

    @Test
    fun identicalLayoutNameDoesNotEraseLegacySourceClassIdentity() {
        assertTrue(
            LegacyManagerInteractionEvidenceCatalog.isConfirmedExactSource(
                "ActivityProcura",
                "dialog_proposta",
            ),
        )
        assertTrue(
            LegacyManagerInteractionEvidenceCatalog.isConfirmedExactSource(
                "ActivityTimes",
                "dialog_proposta",
            ),
        )
        assertFalse(
            LegacyManagerInteractionEvidenceCatalog.isConfirmedExactSource(
                "DialogIgrokInfo",
                "dialog_proposta",
            ),
        )
    }

    @Test
    fun rejectsPlausibleButUnprovenInteractionAliases() {
        assertNull(
            LegacyManagerInteractionEvidenceCatalog.fromExactSource(
                "ActivityProcura",
                "dialog_transferencia",
            ),
        )
        assertNull(
            LegacyManagerInteractionEvidenceCatalog.fromExactSource(
                "ActivityMainTeam",
                "dialog_proposta",
            ),
        )
        assertFalse(
            LegacyManagerInteractionEvidenceCatalog.isConfirmedExactSource(
                "DialogIgrokInfo",
                "dialog_emprestimo",
            ),
        )
    }
}
