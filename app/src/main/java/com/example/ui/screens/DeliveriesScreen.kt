package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.TruckDeliveryEntity
import com.example.model.UnitConversions
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SteelBlue
import com.example.ui.viewmodel.ConstructionViewModel
import com.example.ui.viewmodel.DateFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeliveriesScreen(
    viewModel: ConstructionViewModel,
    onOpenAddDelivery: () -> Unit
) {
    val deliveries by viewModel.filteredDeliveries.collectAsStateWithLifecycle()
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedDateFilter.collectAsStateWithLifecycle()
    val selectedMaterial by viewModel.selectedMaterialFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var deliveryToDelete by remember { mutableStateOf<TruckDeliveryEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                // KPI Header Card
                SummaryKpiBanner(
                    totalCft = summary.totalCft,
                    totalBrass = summary.totalBrass,
                    deliveryCount = summary.deliveryCount,
                    totalExpenses = summary.totalExpenses,
                    grandTotal = summary.grandTotal
                )
            }

            item {
                // Search & Filter controls
                SearchAndFilterBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    selectedFilter = selectedFilter,
                    onFilterSelect = { viewModel.setDateFilter(it) },
                    materials = materials.map { it.name },
                    selectedMaterial = selectedMaterial,
                    onMaterialSelect = { viewModel.setMaterialFilter(it) }
                )
            }

            if (deliveries.isEmpty()) {
                item {
                    EmptyDeliveriesState(onOpenAddDelivery = onOpenAddDelivery)
                }
            } else {
                items(deliveries, key = { it.id }) { delivery ->
                    DeliveryCard(
                        delivery = delivery,
                        onDelete = { deliveryToDelete = delivery }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
            }
        }

        // FAB to add new delivery
        FloatingActionButton(
            onClick = onOpenAddDelivery,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_delivery_fab"),
            containerColor = AmberPrimary,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Delivery")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Delivery", fontWeight = FontWeight.Bold)
            }
        }

        // Delete Confirmation Dialog
        deliveryToDelete?.let { del ->
            AlertDialog(
                onDismissRequest = { deliveryToDelete = null },
                title = { Text("Delete Delivery Log?") },
                text = { Text("Are you sure you want to delete delivery ${del.truckNumber} (${del.materialName})? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteDelivery(del.id)
                            deliveryToDelete = null
                        }
                    ) {
                        Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deliveryToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SummaryKpiBanner(
    totalCft: Double,
    totalBrass: Double,
    deliveryCount: Int,
    totalExpenses: Double,
    grandTotal: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AmberPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "SITE MATERIAL SUMMARY",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate200,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate800)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "$deliveryCount Trips Logged",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // CFT & Brass
                Column {
                    Text("Total Volume", style = MaterialTheme.typography.bodySmall, color = Slate200)
                    Text(
                        "${UnitConversions.formatNumber(totalCft, 1)} CFT",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AmberPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${UnitConversions.formatNumber(totalBrass, 2)} Brass",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }

                // Total Spend
                Column(horizontalAlignment = Alignment.End) {
                    Text("Grand Total Spend", style = MaterialTheme.typography.bodySmall, color = Slate200)
                    Text(
                        UnitConversions.formatCurrency(grandTotal),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Incl. ${UnitConversions.formatCurrency(totalExpenses)} Expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldSuccess
                    )
                }
            }
        }
    }
}

@Composable
fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: DateFilter,
    onFilterSelect: (DateFilter) -> Unit,
    materials: List<String>,
    selectedMaterial: String?,
    onMaterialSelect: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search truck #, challan, driver, supplier...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Slate600) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("delivery_search_field")
        )

        // Date Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DateFilter.values().forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AmberPrimary else Slate100)
                        .clickable { onFilterSelect(filter) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        filter.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else Slate700,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Material Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val allSelected = selectedMaterial == null
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (allSelected) SteelBlue else Slate100)
                    .clickable { onMaterialSelect(null) }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    "All Materials",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (allSelected) Color.White else Slate700,
                    fontWeight = if (allSelected) FontWeight.Bold else FontWeight.Normal
                )
            }

            materials.distinct().forEach { mat ->
                val isSelected = selectedMaterial == mat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) SteelBlue else Slate100)
                        .clickable { onMaterialSelect(mat) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        mat,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White else Slate700,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun DeliveryCard(
    delivery: TruckDeliveryEntity,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US)
    val dateText = dateFormat.format(Date(delivery.dateEpochMs))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Truck #, Material Badge, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate900)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            delivery.truckNumber,
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        delivery.materialName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Payment Status Badge
                val isPaid = delivery.paymentStatus.equals("PAID", ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPaid) EmeraldLight else AmberContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        delivery.paymentStatus,
                        color = if (isPaid) EmeraldSuccess else AmberDark,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                dateText + if (delivery.challanNumber.isNotEmpty()) " • Challan: ${delivery.challanNumber}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Volume & Financials Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate100)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Volume",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                    Text(
                        "${UnitConversions.formatNumber(delivery.totalCft, 1)} CFT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberDark
                    )
                    Text(
                        "${UnitConversions.formatNumber(delivery.totalBrass, 2)} Brass (${UnitConversions.formatNumber(delivery.totalCuM, 1)} m³)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Total Investment",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600
                    )
                    Text(
                        UnitConversions.formatCurrency(delivery.grandTotal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        "Mat: $${UnitConversions.formatNumber(delivery.materialCost, 0)} | Exp: +$${UnitConversions.formatNumber(delivery.totalExpenses, 0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldSuccess
                    )
                }
            }

            // Expandable details (dimensions, expenses breakdown, supplier)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Slate200)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Measurement specifics
                    if (delivery.calculationMethod == "DIMENSIONS") {
                        Text(
                            "Dimensions: ${delivery.lengthFt.toInt()}' ${delivery.lengthIn.toInt()}\" (L) × ${delivery.widthFt.toInt()}' ${delivery.widthIn.toInt()}\" (W) × ${delivery.heightFt.toInt()}' ${delivery.heightIn.toInt()}\" (H)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (delivery.calculationMethod == "WEIGHT") {
                        Text(
                            "Weighbridge: Gross ${delivery.grossWeightTons}T - Tare ${delivery.tareWeightTons}T = Net ${delivery.netWeightTons} Tons",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (delivery.ratePerCft > 0) {
                        Text(
                            "Rate: $${delivery.ratePerCft}/CFT",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )
                    }

                    if (delivery.driverName.isNotEmpty() || delivery.supplierName.isNotEmpty()) {
                        Text(
                            "Driver: ${delivery.driverName.ifEmpty { "N/A" }} | Supplier: ${delivery.supplierName.ifEmpty { "N/A" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Daily Trip Expenses Breakdown:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Diesel / Fuel: $${UnitConversions.formatNumber(delivery.dieselExpense, 0)}", style = MaterialTheme.typography.bodySmall)
                        Text("• Toll: $${UnitConversions.formatNumber(delivery.tollExpense, 0)}", style = MaterialTheme.typography.bodySmall)
                        Text("• Unloading: $${UnitConversions.formatNumber(delivery.unloadingLabor, 0)}", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Driver Batta: $${UnitConversions.formatNumber(delivery.driverAllowance, 0)}", style = MaterialTheme.typography.bodySmall)
                        Text("• Weighbridge: $${UnitConversions.formatNumber(delivery.weighbridgeFee, 0)}", style = MaterialTheme.typography.bodySmall)
                        Text("• Misc: $${UnitConversions.formatNumber(delivery.otherExpense, 0)}", style = MaterialTheme.typography.bodySmall)
                    }

                    if (delivery.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Notes: ${delivery.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate600
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Delivery", tint = Color.Red)
                        }
                    }
                }
            }

            // Expand icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Slate600,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyDeliveriesState(onOpenAddDelivery: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        colors = CardDefaults.cardColors(containerColor = Slate100),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AmberContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = AmberDark,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No Deliveries Found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "No truck delivery records match your current filter. Log a daily truck delivery to calculate volume and manual expenses.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Button(
                onClick = onOpenAddDelivery,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = AmberPrimary)
            ) {
                Text("Log First Truck Delivery")
            }
        }
    }
}
