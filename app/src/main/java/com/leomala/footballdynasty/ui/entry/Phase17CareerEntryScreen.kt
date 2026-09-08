package com.leomala.footballdynasty.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.leomala.footballdynasty.application.career.CareerEntrySummary
import com.leomala.footballdynasty.application.career.CareerSquadCatalogStore
import com.leomala.footballdynasty.domain.career.CareerState
import kotlinx.coroutines.launch

/**
 * First production-facing Phase 17 Compose path backed exclusively by the
 * certified persisted-career read boundaries.
 *
 * New-career creation is intentionally not exposed here yet: the owner of the
 * career identity/RNG seed policy is still being proven. Loading an existing
 * career and reading its persisted senior squad are already end-to-end and
 * therefore safe to expose.
 */
@Composable
fun Phase17CareerEntryScreen(
    coordinator: CareerEntryFlowCoordinator,
    squadCatalogStore: CareerSquadCatalogStore,
    modifier: Modifier = Modifier,
) {
    var entryState by remember { mutableStateOf<CareerEntryUiState?>(null) }
    var loadedCareer by remember { mutableStateOf<CareerState?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(coordinator) {
        entryState = coordinator.openEntry()
    }

    val career = loadedCareer
    if (career != null) {
        CareerHomeScreen(
            career = career,
            squadCatalogStore = squadCatalogStore,
            modifier = modifier,
        )
        return
    }

    val state = entryState
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

    CareerListScreen(
        careers = state.careers,
        loadError = loadError,
        onLoad = { careerId ->
            scope.launch {
                when (val result = coordinator.openCareer(careerId)) {
                    is CareerEntryOpenResult.Loaded -> {
                        loadError = null
                        loadedCareer = result.state
                    }
                    is CareerEntryOpenResult.NotLoadable -> {
                        loadError = "Carreira indisponível para carregamento."
                    }
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun CareerListScreen(
    careers: List<CareerEntrySummary>,
    loadError: String?,
    onLoad: (String) -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Text(
            text = "Football Dynasty",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Carregar carreira",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
        )
        if (careers.isEmpty()) {
            Text(
                text = "Nenhuma carreira persistida encontrada.",
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(careers, key = { it.careerId }) { career ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = career.displayName ?: career.careerId,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        val detail = listOfNotNull(
                            career.managedClubName,
                            career.seasonYear?.let { "Temporada $it" },
                        ).joinToString(" • ")
                        if (detail.isNotEmpty()) {
                            Text(text = detail, style = MaterialTheme.typography.bodyMedium)
                        }
                        Button(
                            onClick = { onLoad(career.careerId) },
                            enabled = career.loadable,
                            modifier = Modifier.padding(top = 6.dp),
                        ) {
                            Text(if (career.loadable) "Carregar" else "Save incompleto")
                        }
                    }
                }
            }
        }
        if (loadError != null) {
            Text(
                text = loadError,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun CareerHomeScreen(
    career: CareerState,
    squadCatalogStore: CareerSquadCatalogStore,
    modifier: Modifier,
) {
    var squad by remember(career.careerId) {
        mutableStateOf<CareerSquadCatalogStore.SeniorSquad?>(null)
    }
    var squadRequested by remember(career.careerId) { mutableStateOf(false) }
    var squadUnavailable by remember(career.careerId) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (squadRequested) {
        val loadedSquad = squad
        if (loadedSquad != null) {
            SeniorSquadScreen(squad = loadedSquad, modifier = modifier)
            return
        }
        if (!squadUnavailable) {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
            return
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Text(
            text = "Central da carreira",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Temporada ${career.season.year}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Dia ${career.calendar.currentDayIndex + 1}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp),
        )
        career.managedClub?.let { managedClub ->
            Text(
                text = "Clube: ${managedClub.clubId}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        Button(
            onClick = {
                squadRequested = true
                squadUnavailable = false
                scope.launch {
                    squad = squadCatalogStore.loadSeniorSquad(career.careerId)
                    squadUnavailable = squad == null
                }
            },
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Elenco")
        }
        if (squadUnavailable) {
            Text(
                text = "Elenco persistido indisponível.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun SeniorSquadScreen(
    squad: CareerSquadCatalogStore.SeniorSquad,
    modifier: Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Text(
            text = "Elenco",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Clube: ${squad.clubId}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(squad.players, key = { it.playerId }) { player ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Posição ${player.position} • Idade ${player.age} • Força ${player.overall}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
