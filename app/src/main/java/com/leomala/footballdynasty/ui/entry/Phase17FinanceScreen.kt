package com.leomala.footballdynasty.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leomala.footballdynasty.application.career.CareerFinanceCatalogStore
import com.leomala.footballdynasty.application.career.CareerFinanceCommandStore
import kotlinx.coroutines.launch

/**
 * Root-provided Phase 17 application boundary for the two already-characterized finance actions.
 * A missing provider keeps the screen read-only; no fallback command owner is synthesized.
 */
internal val LocalCareerFinanceCommandStore = staticCompositionLocalOf<CareerFinanceCommandStore?> { null }

/** Presentation of the already-materialized legacy finance runtime and certified finance actions. */
@Composable
internal fun Phase17FinanceScreen(
    finances: CareerFinanceCatalogStore.FinanceSnapshot,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    val commandStore = LocalCareerFinanceCommandStore.current
    var displayedFinances by remember(finances) { mutableStateOf(finances) }
    var actionInProgress by remember(finances.careerId) { mutableStateOf(false) }
    var actionMessage by remember(finances.careerId) { mutableStateOf<String?>(null) }
    var actionError by remember(finances.careerId) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun execute(
        action: suspend (CareerFinanceCommandStore) -> CareerFinanceCommandStore.FinanceCommandResult?,
        acceptedMessage: String,
        rejectedMessage: String,
    ) {
        val store = commandStore ?: return
        if (actionInProgress) return
        actionInProgress = true
        actionMessage = null
        actionError = null
        scope.launch {
            try {
                val result = action(store)
                if (result == null) {
                    actionError = "Ação financeira indisponível para o estado persistido desta carreira."
                } else {
                    displayedFinances = result.snapshot
                    actionMessage = if (result.accepted) acceptedMessage else rejectedMessage
                }
            } catch (_: Exception) {
                actionError = "Ação financeira indisponível para o estado persistido desta carreira."
            } finally {
                actionInProgress = false
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack, enabled = !actionInProgress) {
            Text("Voltar à central")
        }
        Text(
            text = "Finanças",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Clube: ${displayedFinances.clubName}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "Caixa: ${displayedFinances.cash}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Receitas do período: ${displayedFinances.totalIncome}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "Despesas do período: ${displayedFinances.totalExpense}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = "Saldo do período: ${displayedFinances.periodBalance}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = "Empréstimo pendente: ${displayedFinances.borrowed}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Encargo mensal: ${displayedFinances.monthlyBorrowingCharge}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp),
        )
        Button(
            onClick = {
                execute(
                    action = { it.borrow(displayedFinances.careerId) },
                    acceptedMessage = "Empréstimo aplicado ao estado persistido.",
                    rejectedMessage = "Empréstimo não permitido no estado financeiro atual.",
                )
            },
            enabled = commandStore != null && !actionInProgress,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Empréstimo")
        }
        Button(
            onClick = {
                execute(
                    action = { it.repay(displayedFinances.careerId) },
                    acceptedMessage = "Quitação aplicada ao estado persistido.",
                    rejectedMessage = "Quitação não permitida no estado financeiro atual.",
                )
            },
            enabled = commandStore != null && !actionInProgress,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Text("Quitação")
        }
        if (actionInProgress) {
            Text(
                text = "Processando ação financeira...",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        actionMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        actionError?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}
