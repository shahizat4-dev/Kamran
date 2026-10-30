package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.UnitConversions
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseExpense
import com.example.ui.theme.RoseLight
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SteelBlue
import com.example.ui.viewmodel.ConstructionViewModel

@Composable
fun ExpensesStatsScreen(viewModel: ConstructionViewModel) {
    val deliveries by viewModel.filteredDeliveries.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()

    val totalDiesel = deliveries.sumOf { it.dieselExpense }
    val totalToll = deliveries.sumOf { it.tollExpense }
    val totalDriver = deliveries.sumOf { it.driverAllowance }
    val totalUnloading = deliveries.sumOf { it.unloadingLabor }
    val totalWeigh = deliveries.sumOf { it.weighbridgeFee }
    val totalOther = deliveries.sumOf { it.otherExpense }

    val totalExpenses = summary.totalExpenses
    val totalMaterial = summary.totalMaterialCost
    val grandTotal = summary.grandTotal

    val avgCostPerTrip = if (deliveries.isNotEmpty()) grandTotal / deliveries.size else 0.0
    val avgExpensePerTrip = if (deliveries.isNotEmpty()) totalExpenses / deliveries.size else 0.0
    val avgCftPerTrip = if (deliveries.isNotEmpty()) summary.totalCft / deliveries.size else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Title Header
        Column {
            Text(
                "Site Expense & Delivery Analytics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Cost distribution, manual trip logistics, and material investment",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Grand Investment Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "TOTAL SITE INVESTMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate200,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    UnitConversions.formatCurrency(grandTotal),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar: Material vs Delivery Expenses
                val materialRatio = if (grandTotal > 0) (totalMaterial / grandTotal).toFloat() else 0.8f
                LinearProgressIndicator(
                    progress = { materialRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AmberPrimary,
                    trackColor = RoseExpense
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(10.dp).height(10.dp).background(AmberPrimary, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Material: ${UnitConversions.formatCurrency(totalMaterial)} (${(materialRatio * 100).toInt()}%)",
                            color = Slate200,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(10.dp).height(10.dp).background(RoseExpense, RoundedCornerShape(2.dp)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Expenses: ${UnitConversions.formatCurrency(totalExpenses)} (${(100 - (materialRatio * 100).toInt())}%)",
                            color = Slate200,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Trip Averages KPI Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Avg CFT
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = AmberContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Avg Load / Trip", style = MaterialTheme.typography.labelSmall, color = Slate700)
                    Text(
                        "${UnitConversions.formatNumber(avgCftPerTrip, 1)} CFT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberDark
                    )
                    Text("${UnitConversions.formatNumber(UnitConversions.cftToBrass(avgCftPerTrip), 2)} Brass", style = MaterialTheme.typography.bodySmall, color = Slate700)
                }
            }

            // Avg Cost
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Slate100)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Avg Cost / Trip", style = MaterialTheme.typography.labelSmall, color = Slate700)
                    Text(
                        UnitConversions.formatCurrency(avgCostPerTrip),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text("Exp: ${UnitConversions.formatCurrency(avgExpensePerTrip)}", style = MaterialTheme.typography.bodySmall, color = EmeraldSuccess)
                }
            }
        }

        // Manual Expenses Breakdown Section
        Text(
            "Manual Trip Logistics & Expense Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val expenseItems = listOf(
                    Triple("Diesel / Fuel", totalDiesel, SteelBlue),
                    Triple("Toll Gate Charges", totalToll, AmberPrimary),
                    Triple("Unloading / Hamali Labor", totalUnloading, EmeraldSuccess),
                    Triple("Driver Allowance / Batta", totalDriver, Color(0xFF8B5CF6)),
                    Triple("Weighbridge Fees", totalWeigh, Color(0xFFF97316)),
                    Triple("Other Site Misc", totalOther, Slate600)
                )

                expenseItems.forEach { (title, amount, color) ->
                    val fraction = if (totalExpenses > 0) (amount / totalExpenses).toFloat() else 0f
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${UnitConversions.formatCurrency(amount)} (${(fraction * 100).toInt()}%)",
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = color,
                            trackColor = Slate100
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Slate200))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Manual Trip Expenses:", fontWeight = FontWeight.Bold)
                    Text(
                        UnitConversions.formatCurrency(totalExpenses),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = RoseExpense
                    )
                }
            }
        }

        // Material-Wise Consumption Section
        Text(
            "Material-Wise Consumption & Expenditure",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        val materialGroups = deliveries.groupBy { it.materialName }
        materialGroups.forEach { (materialName, list) ->
            val matCft = list.sumOf { it.totalCft }
            val matBrass = list.sumOf { it.totalBrass }
            val matCost = list.sumOf { it.materialCost }
            val trips = list.size

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Slate100),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(materialName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("$trips Truck Trips • ${UnitConversions.formatNumber(matBrass, 2)} Brass", style = MaterialTheme.typography.bodySmall, color = Slate700)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${UnitConversions.formatNumber(matCft, 1)} CFT",
                            fontWeight = FontWeight.Bold,
                            color = AmberDark,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(UnitConversions.formatCurrency(matCost), style = MaterialTheme.typography.bodySmall, color = Slate900, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
