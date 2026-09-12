package com.leomala.footballdynasty.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leomala.footballdynasty.application.career.CareerJuniorCatalogStore

/** Read-only presentation of persisted pre-promotion junior drafts for the managed club. */
@Composable
internal fun Phase17JuniorScreen(
    squad: CareerJuniorCatalogStore.JuniorSquad,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack) {
            Text("Voltar à central")
        }
        Text(
            text = "Juniores",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Clube: ${squad.clubId}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
        )
        if (squad.juniors.isEmpty()) {
            Text(
                text = "Nenhum júnior persistido encontrado.",
                style = MaterialTheme.typography.bodyLarge,
            )
            return
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(squad.juniors, key = { it.sourceOrdinal }) { junior ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = junior.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Posição ${junior.position} • Idade ${junior.age}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
