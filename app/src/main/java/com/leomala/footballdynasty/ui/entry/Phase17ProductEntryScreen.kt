package com.leomala.footballdynasty.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Product entry router for Phase 17.
 *
 * No automatic career-id or RNG-seed policy is introduced. A new career is created only from
 * values explicitly entered by the user plus a club selected from the canonical persisted list.
 * Existing-career rendering remains delegated to the already-certified persisted flow.
 */
@Composable
fun Phase17ProductEntryScreen(
    coordinator: CareerEntryFlowCoordinator,
    existingCareerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    var route by remember { mutableStateOf(ProductEntryRoute.CHOICE) }

    when (route) {
        ProductEntryRoute.CHOICE -> Column(
            modifier = modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Football Dynasty", style = MaterialTheme.typography.headlineMedium)
            Button(
                onClick = { route = ProductEntryRoute.LOAD },
                modifier = Modifier.padding(top = 18.dp),
            ) {
                Text("Carregar carreira")
            }
            Button(
                onClick = { route = ProductEntryRoute.CREATE },
                enabled = coordinator.careerCreationAvailable,
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Nova carreira")
            }
        }

        ProductEntryRoute.LOAD -> existingCareerContent()

        ProductEntryRoute.CREATE -> ExplicitCareerCreationScreen(
            coordinator = coordinator,
            onBack = { route = ProductEntryRoute.CHOICE },
            onCreated = { route = ProductEntryRoute.LOAD },
            modifier = modifier,
        )
    }
}

@Composable
private fun ExplicitCareerCreationScreen(
    coordinator: CareerEntryFlowCoordinator,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier,
) {
    var selectionState by remember { mutableStateOf<CareerEntryUiState?>(null) }
    var careerId by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var seedText by remember { mutableStateOf("") }
    var creating by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(coordinator) {
        selectionState = coordinator.openClubSelection()
    }

    val state = selectionState
    if (state == null) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val seed = seedText.toLongOrNull()
    val ready = careerId.isNotBlank() && seed != null && state.selectedClubId != null && !creating

    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack, enabled = !creating) { Text("Voltar") }
        Text(
            text = "Nova carreira",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Identidade e seed são explícitos; nenhum valor automático é inventado.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp),
        )
        OutlinedTextField(
            value = careerId,
            onValueChange = { careerId = it; error = null },
            label = { Text("ID da carreira") },
            enabled = !creating,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it; error = null },
            label = { Text("Nome da carreira (opcional)") },
            enabled = !creating,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        OutlinedTextField(
            value = seedText,
            onValueChange = { seedText = it; error = null },
            label = { Text("Seed numérica") },
            supportingText = {
                if (seedText.isNotEmpty() && seed == null) Text("Informe um número inteiro válido.")
            },
            enabled = !creating,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        )
        Text(
            text = "Escolha o clube",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 14.dp, bottom = 8.dp),
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(state.clubs, key = { it.id }) { club ->
                Button(
                    onClick = {
                        selectionState = coordinator.selectClub(state, club.id)
                        error = null
                    },
                    enabled = !creating,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val selected = state.selectedClubId == club.id
                    Text(if (selected) "✓ ${club.name}" else club.name)
                }
            }
        }
        error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Button(
            onClick = {
                val explicitSeed = requireNotNull(seed)
                creating = true
                error = null
                scope.launch {
                    runCatching {
                        coordinator.createCareerFromExplicitInputs(
                            state = state,
                            careerId = careerId,
                            displayName = displayName.takeIf { it.isNotBlank() },
                            seed = explicitSeed,
                        )
                    }.onSuccess {
                        creating = false
                        onCreated()
                    }.onFailure { failure ->
                        creating = false
                        error = failure.message ?: "Não foi possível criar a carreira."
                    }
                }
            },
            enabled = ready,
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        ) {
            Text(if (creating) "Criando..." else "Criar carreira")
        }
    }
}

private enum class ProductEntryRoute {
    CHOICE,
    LOAD,
    CREATE,
}
