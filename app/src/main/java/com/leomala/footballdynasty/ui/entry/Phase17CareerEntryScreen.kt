package com.leomala.footballdynasty.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.leomala.footballdynasty.application.career.CareerCalendarCatalogStore
import com.leomala.footballdynasty.application.career.CareerCalendarCommandStore
import com.leomala.footballdynasty.application.career.CareerCompetitionCatalogStore
import com.leomala.footballdynasty.application.career.CareerEntrySummary
import com.leomala.footballdynasty.application.career.CareerFinanceCatalogStore
import com.leomala.footballdynasty.application.career.CareerJuniorCatalogStore
import com.leomala.footballdynasty.application.career.CareerSquadCatalogStore
import com.leomala.footballdynasty.application.career.CareerStadiumCatalogStore
import com.leomala.footballdynasty.domain.career.CareerState
import kotlinx.coroutines.launch

/**
 * First production-facing Phase 17 Compose path backed exclusively by the
 * certified persisted-career boundaries.
 *
 * New-career creation is intentionally not exposed here yet: the owner of the
 * career identity/RNG seed policy is still being proven. Loading an existing
 * career, reading its persisted state and moving to the next persisted scheduled
 * event are end-to-end and therefore safe to expose.
 */
@Composable
fun Phase17CareerEntryScreen(
    coordinator: CareerEntryFlowCoordinator,
    squadCatalogStore: CareerSquadCatalogStore,
    juniorCatalogStore: CareerJuniorCatalogStore,
    competitionCatalogStore: CareerCompetitionCatalogStore,
    calendarCatalogStore: CareerCalendarCatalogStore,
    calendarCommandStore: CareerCalendarCommandStore,
    stadiumCatalogStore: CareerStadiumCatalogStore,
    financeCatalogStore: CareerFinanceCatalogStore,
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
            juniorCatalogStore = juniorCatalogStore,
            competitionCatalogStore = competitionCatalogStore,
            calendarCatalogStore = calendarCatalogStore,
            calendarCommandStore = calendarCommandStore,
            stadiumCatalogStore = stadiumCatalogStore,
            financeCatalogStore = financeCatalogStore,
            onCareerChanged = { loadedCareer = it },
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
        Text(text = "Football Dynasty", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Carregar carreira",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
        )
        if (careers.isEmpty()) {
            Text(text = "Nenhuma carreira persistida encontrada.", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(careers, key = { it.careerId }) { career ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(text = career.displayName ?: career.careerId, style = MaterialTheme.typography.titleMedium)
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
            Text(text = loadError, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun CareerHomeScreen(
    career: CareerState,
    squadCatalogStore: CareerSquadCatalogStore,
    juniorCatalogStore: CareerJuniorCatalogStore,
    competitionCatalogStore: CareerCompetitionCatalogStore,
    calendarCatalogStore: CareerCalendarCatalogStore,
    calendarCommandStore: CareerCalendarCommandStore,
    stadiumCatalogStore: CareerStadiumCatalogStore,
    financeCatalogStore: CareerFinanceCatalogStore,
    onCareerChanged: (CareerState) -> Unit,
    modifier: Modifier,
) {
    var squad by remember(career.id) { mutableStateOf<CareerSquadCatalogStore.SeniorSquad?>(null) }
    var squadRequested by remember(career.id) { mutableStateOf(false) }
    var squadUnavailable by remember(career.id) { mutableStateOf(false) }
    var juniors by remember(career.id) { mutableStateOf<CareerJuniorCatalogStore.JuniorSquad?>(null) }
    var juniorsRequested by remember(career.id) { mutableStateOf(false) }
    var juniorsUnavailable by remember(career.id) { mutableStateOf(false) }
    var competitions by remember(career.id) {
        mutableStateOf<List<CareerCompetitionCatalogStore.CompetitionRow>?>(null)
    }
    var standingsRequested by remember(career.id) { mutableStateOf(false) }
    var calendar by remember(career.id) { mutableStateOf<CareerCalendarCatalogStore.CalendarSnapshot?>(null) }
    var calendarRequested by remember(career.id) { mutableStateOf(false) }
    var calendarUnavailable by remember(career.id) { mutableStateOf(false) }
    var selectedCalendarMatch by remember(career.id) {
        mutableStateOf<CareerCalendarCatalogStore.MatchRow?>(null)
    }
    var stadium by remember(career.id) { mutableStateOf<CareerStadiumCatalogStore.StadiumSnapshot?>(null) }
    var stadiumRequested by remember(career.id) { mutableStateOf(false) }
    var stadiumUnavailable by remember(career.id) { mutableStateOf(false) }
    var finances by remember(career.id) { mutableStateOf<CareerFinanceCatalogStore.FinanceSnapshot?>(null) }
    var financesRequested by remember(career.id) { mutableStateOf(false) }
    var financesUnavailable by remember(career.id) { mutableStateOf(false) }
    var nextEventInProgress by remember(career.id) { mutableStateOf(false) }
    var nextEventMessage by remember(career.id) { mutableStateOf<String?>(null) }
    var nextEventError by remember(career.id) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    if (squadRequested) {
        val loadedSquad = squad
        if (loadedSquad != null) {
            SeniorSquadScreen(
                squad = loadedSquad,
                onBack = { squadRequested = false },
                modifier = modifier,
            )
            return
        }
        if (!squadUnavailable) {
            LoadingScreen(modifier)
            return
        }
    }

    if (juniorsRequested) {
        val loadedJuniors = juniors
        if (loadedJuniors != null) {
            Phase17JuniorScreen(
                squad = loadedJuniors,
                onBack = { juniorsRequested = false },
                modifier = modifier,
            )
            return
        }
        if (!juniorsUnavailable) {
            LoadingScreen(modifier)
            return
        }
    }

    if (standingsRequested) {
        val loadedCompetitions = competitions
        if (loadedCompetitions == null) {
            LoadingScreen(modifier)
            return
        }
        CompetitionStandingsScreen(
            competitions = loadedCompetitions,
            onBack = { standingsRequested = false },
            modifier = modifier,
        )
        return
    }

    if (calendarRequested) {
        val loadedCalendar = calendar
        if (loadedCalendar != null) {
            val selectedMatch = selectedCalendarMatch
            if (selectedMatch != null) {
                CareerMatchDetailScreen(
                    match = selectedMatch,
                    onBack = { selectedCalendarMatch = null },
                    modifier = modifier,
                )
            } else {
                CareerCalendarScreen(
                    calendar = loadedCalendar,
                    onMatchSelected = { selectedCalendarMatch = it },
                    onBack = {
                        selectedCalendarMatch = null
                        calendarRequested = false
                    },
                    modifier = modifier,
                )
            }
            return
        }
        if (!calendarUnavailable) {
            LoadingScreen(modifier)
            return
        }
    }

    if (stadiumRequested) {
        val loadedStadium = stadium
        if (loadedStadium != null) {
            StadiumScreen(
                stadium = loadedStadium,
                onBack = { stadiumRequested = false },
                modifier = modifier,
            )
            return
        }
        if (!stadiumUnavailable) {
            LoadingScreen(modifier)
            return
        }
    }

    if (financesRequested) {
        val loadedFinances = finances
        if (loadedFinances != null) {
            Phase17FinanceScreen(
                finances = loadedFinances,
                onBack = { financesRequested = false },
                modifier = modifier,
            )
            return
        }
        if (!financesUnavailable) {
            LoadingScreen(modifier)
            return
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Text(text = "Central da carreira", style = MaterialTheme.typography.headlineMedium)
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Button(
                onClick = {
                    squadRequested = true
                    squadUnavailable = false
                    scope.launch {
                        squad = squadCatalogStore.loadSeniorSquad(career.id)
                        squadUnavailable = squad == null
                    }
                },
            ) {
                Text("Elenco")
            }
            Button(
                onClick = {
                    standingsRequested = true
                    scope.launch {
                        competitions = competitionCatalogStore.loadCompetitions(career.id)
                    }
                },
            ) {
                Text("Classificação")
            }
            Button(
                onClick = {
                    calendarRequested = true
                    calendarUnavailable = false
                    selectedCalendarMatch = null
                    scope.launch {
                        calendar = calendarCatalogStore.loadCalendar(career.id)
                        calendarUnavailable = calendar == null
                    }
                },
            ) {
                Text("Calendário")
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 10.dp),
        ) {
            Button(
                onClick = {
                    stadiumRequested = true
                    stadiumUnavailable = false
                    scope.launch {
                        stadium = stadiumCatalogStore.loadStadium(career.id)
                        stadiumUnavailable = stadium == null
                    }
                },
            ) {
                Text("Estádio")
            }
            Button(
                onClick = {
                    financesRequested = true
                    financesUnavailable = false
                    scope.launch {
                        finances = financeCatalogStore.loadFinances(career.id)
                        financesUnavailable = finances == null
                    }
                },
            ) {
                Text("Finanças")
            }
            Button(
                onClick = {
                    juniorsRequested = true
                    juniorsUnavailable = false
                    scope.launch {
                        juniors = juniorCatalogStore.loadJuniorSquad(career.id)
                        juniorsUnavailable = juniors == null
                    }
                },
            ) {
                Text("Juniores")
            }
        }
        Button(
            onClick = {
                nextEventInProgress = true
                nextEventMessage = null
                nextEventError = null
                scope.launch {
                    try {
                        val transition = calendarCommandStore.moveToNextScheduledEvent(career.id)
                        onCareerChanged(transition.state)
                        calendar = calendarCatalogStore.loadCalendar(career.id)
                        calendarUnavailable = calendar == null
                        nextEventMessage = if (transition.eventFound == true) {
                            "Próximo evento: dia ${transition.state.calendar.currentDayIndex + 1}."
                        } else {
                            "Nenhum próximo evento agendado."
                        }
                    } catch (_: Exception) {
                        nextEventError = "Não foi possível avançar ao próximo evento persistido."
                    } finally {
                        nextEventInProgress = false
                    }
                }
            },
            enabled = !nextEventInProgress,
            modifier = Modifier.padding(top = 10.dp),
        ) {
            Text(if (nextEventInProgress) "Avançando..." else "Próximo evento")
        }
        nextEventMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        nextEventError?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        if (squadUnavailable) {
            Text(
                text = "Elenco persistido indisponível.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        if (juniorsUnavailable) {
            Text(
                text = "Estado persistido dos juniores indisponível para esta carreira.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        if (calendarUnavailable) {
            Text(
                text = "Calendário persistido indisponível.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        if (stadiumUnavailable) {
            Text(
                text = "Estado persistido do estádio indisponível para esta carreira.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        if (financesUnavailable) {
            Text(
                text = "Estado financeiro persistido indisponível para esta carreira.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SeniorSquadScreen(
    squad: CareerSquadCatalogStore.SeniorSquad,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack) {
            Text("Voltar à central")
        }
        Text(
            text = "Elenco",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Clube: ${squad.clubId}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(squad.players, key = { it.playerId }) { player ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = player.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Posição ${player.position} • Idade ${player.age} • Força ${player.overall}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun StadiumScreen(
    stadium: CareerStadiumCatalogStore.StadiumSnapshot,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack) {
            Text("Voltar à central")
        }
        Text(
            text = "Estádio",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Clube: ${stadium.clubName}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        stadium.stadiumName?.let { stadiumName ->
            Text(
                text = "Estádio: $stadiumName",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Text(
            text = "Capacidade persistida total: ${stadium.totalCapacity}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
        stadium.sectorCapacities.forEachIndexed { index, capacity ->
            Text(
                text = "Setor ${index + 1}: $capacity",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@Composable
private fun CompetitionStandingsScreen(
    competitions: List<CareerCompetitionCatalogStore.CompetitionRow>,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack) {
            Text("Voltar à central")
        }
        Text(
            text = "Classificação",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        if (competitions.isEmpty()) {
            Text(
                text = "Nenhuma competição persistida encontrada.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp),
            )
            return
        }
        LazyColumn(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(competitions, key = { it.competitionId }) { competition ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = competition.competitionId, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "Rodada ${competition.currentRoundNumber}/${competition.totalRounds}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    competition.standings.forEachIndexed { index, standing ->
                        Text(
                            text = "${index + 1}. ${standing.clubName} — ${standing.points} pts • ${standing.played} J • ${standing.wins} V • ${standing.losses} D • ${standing.goalsFor}:${standing.goalsAgainst}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CareerCalendarScreen(
    calendar: CareerCalendarCatalogStore.CalendarSnapshot,
    onMatchSelected: (CareerCalendarCatalogStore.MatchRow) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack) {
            Text("Voltar à central")
        }
        Text(
            text = "Calendário",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Dia atual ${calendar.currentDayIndex + 1}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
        )
        if (calendar.matches.isEmpty()) {
            Text(text = "Nenhuma partida persistida encontrada.", style = MaterialTheme.typography.bodyLarge)
            return
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(calendar.matches, key = { it.matchId }) { match ->
                val presentation = match.toPhase17MatchPresentation()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = presentation.dayLabel, style = MaterialTheme.typography.titleMedium)
                    val calendarScore = presentation.scoreLabel ?: "x"
                    Text(
                        text = "${match.homeClubName} $calendarScore ${match.awayClubName}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    presentation.competitionLabels.forEach { competitionLabel ->
                        Text(text = competitionLabel, style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = { onMatchSelected(match) },
                        modifier = Modifier.padding(top = 6.dp),
                    ) {
                        Text(presentation.openActionLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun CareerMatchDetailScreen(
    match: CareerCalendarCatalogStore.MatchRow,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val presentation = match.toPhase17MatchPresentation()
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack) {
            Text("Voltar ao calendário")
        }
        Text(
            text = presentation.title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = presentation.dayLabel,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = presentation.matchupLabel,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        presentation.scoreLabel?.let { score ->
            Text(
                text = score,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Text(
            text = presentation.stateLabel,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        presentation.competitionLabels.forEach { competitionLabel ->
            Text(
                text = competitionLabel,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}
