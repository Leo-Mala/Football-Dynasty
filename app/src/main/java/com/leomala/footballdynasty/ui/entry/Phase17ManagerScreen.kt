package com.leomala.footballdynasty.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leomala.footballdynasty.application.career.CareerContractCatalogStore
import com.leomala.footballdynasty.application.career.CareerManagerCatalogStore
import kotlinx.coroutines.launch

/**
 * Phase 17 presentation of the already-persisted manager runtime.
 *
 * Raw legacy fields stay explicitly raw because their user-facing labels have not been promoted
 * from corpus evidence. This screen performs no employment, dismissal or progression mutation.
 */
@Composable
fun Phase17ManagerScreen(
    manager: CareerManagerCatalogStore.ManagerSnapshot,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contractCatalogStore = LocalCareerContractCatalogStore.current
    var contracts by remember(manager.careerId) {
        mutableStateOf<CareerContractCatalogStore.ContractList?>(null)
    }
    var contractsRequested by remember(manager.careerId) { mutableStateOf(false) }
    var contractsLoading by remember(manager.careerId) { mutableStateOf(false) }
    var contractsError by remember(manager.careerId) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    if (contractsRequested) {
        val loadedContracts = contracts
        if (loadedContracts != null) {
            Phase17ContractScreen(
                contracts = loadedContracts,
                onBack = { contractsRequested = false },
                modifier = modifier,
            )
            return
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack) {
            Text("Voltar à central")
        }
        Text(
            text = "Treinador",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Clube: ${manager.clubName}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(
            onClick = {
                val store = contractCatalogStore ?: return@Button
                if (contractsLoading) return@Button
                contractsLoading = true
                contractsError = null
                scope.launch {
                    try {
                        val loaded = store.loadContracts(manager.careerId)
                        if (loaded == null) {
                            contractsError = "Contratos persistidos indisponíveis para esta carreira."
                        } else {
                            contracts = loaded
                            contractsRequested = true
                        }
                    } catch (_: Exception) {
                        contractsError = "Contratos persistidos indisponíveis para esta carreira."
                    } finally {
                        contractsLoading = false
                    }
                }
            },
            enabled = contractCatalogStore != null && !contractsLoading,
            modifier = Modifier.padding(top = 10.dp),
        ) {
            Text(if (contractsLoading) "Carregando contratos..." else "Contratos do elenco")
        }
        contractsError?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Text(
            text = "Identificador legado: ${manager.legacyManagerId}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = if (manager.isUserControlled) "Controle: usuário" else "Controle: não usuário",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = "Ordem persistida: ${manager.sourceOrdinal}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = "Campos legados persistidos",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "G=${manager.rawG} • H=${manager.rawH} • D=${manager.rawD} • E=${manager.rawE} • F=${manager.rawF} • O=${manager.rawO} • M=${manager.rawM}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = "Registros persistidos de temporada/clube: ${manager.records.size}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )
        if (manager.records.isEmpty()) {
            Text(
                text = "Nenhum registro persistido disponível.",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(manager.records) { index, record ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Registro ${index + 1}",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = "Temporada legado=${record.seasonId} • Clube legado=${record.legacyClubId}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "Valores brutos: partidas=${record.rawMatches} • vitórias=${record.rawWins} • derrotas=${record.rawLosses} • pontos=${record.rawPoints} • outro=${record.rawOtherCount}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
