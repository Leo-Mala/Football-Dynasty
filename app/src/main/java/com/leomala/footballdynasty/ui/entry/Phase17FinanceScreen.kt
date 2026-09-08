package com.leomala.footballdynasty.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leomala.footballdynasty.application.career.CareerFinanceCatalogStore

/** Read-only view of the already-materialized legacy finance runtime. */
@Composable
internal fun Phase17FinanceScreen(
    finances: CareerFinanceCatalogStore.FinanceSnapshot,
    onBack: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack) {
            Text("Voltar à central")
        }
        Text(
            text = "Finanças",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Clube: ${finances.clubName}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "Caixa: ${finances.cash}",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Receitas do período: ${finances.totalIncome}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = "Despesas do período: ${finances.totalExpense}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = "Saldo do período: ${finances.periodBalance}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = "Empréstimo pendente: ${finances.borrowed}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Encargo mensal: ${finances.monthlyBorrowingCharge}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
