package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.MaterialEntity
import com.example.data.local.TruckDeliveryEntity
import com.example.model.UnitConversions
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SteelBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeliveryDialog(
    materials: List<MaterialEntity>,
    initialSlip: com.example.ai.ParsedDeliverySlip? = null,
    onDismiss: () -> Unit,
    onSave: (TruckDeliveryEntity) -> Unit
) {
    var truckNumber by remember { mutableStateOf(initialSlip?.truckNumber.orEmpty()) }
    var challanNumber by remember { mutableStateOf(initialSlip?.challanNumber.orEmpty()) }
    var driverName by remember { mutableStateOf("") }
    var driverPhone by remember { mutableStateOf("") }
    var supplierName by remember { mutableStateOf("") }
    var siteLocation by remember { mutableStateOf("") }

    var selectedMaterial by remember {
        val match = if (initialSlip != null && initialSlip.materialName.isNotBlank()) {
            materials.find { it.name.contains(initialSlip.materialName, ignoreCase = true) }
        } else null
        mutableStateOf(match ?: materials.firstOrNull() ?: MaterialEntity(name = "River Sand", defaultRatePerCft = 45.0))
    }
    var materialDropdownExpanded by remember { mutableStateOf(false) }

    var calcMethodTab by remember { mutableIntStateOf(0) } // 0: Dimensions, 1: Weight, 2: Direct

    // Dimensions (Ft & In)
    var lengthFt by remember { mutableStateOf(if (initialSlip != null && initialSlip.lengthFt > 0) initialSlip.lengthFt.toInt().toString() else "16") }
    var lengthIn by remember { mutableStateOf(if (initialSlip != null) initialSlip.lengthIn.toInt().toString() else "0") }
    var widthFt by remember { mutableStateOf(if (initialSlip != null && initialSlip.widthFt > 0) initialSlip.widthFt.toInt().toString() else "7") }
    var widthIn by remember { mutableStateOf(if (initialSlip != null) initialSlip.widthIn.toInt().toString() else "6") }
    var heightFt by remember { mutableStateOf(if (initialSlip != null && initialSlip.heightFt > 0) initialSlip.heightFt.toInt().toString() else "4") }
    var heightIn by remember { mutableStateOf(if (initialSlip != null) initialSlip.heightIn.toInt().toString() else "0") }

    // Weight
    var grossTons by remember { mutableStateOf("30.0") }
    var tareTons by remember { mutableStateOf("12.0") }

    // Direct
    var directCft by remember { mutableStateOf("450.0") }

    // Rate
    var ratePerCft by remember {
        mutableStateOf(if (initialSlip != null && initialSlip.ratePerCft > 0) initialSlip.ratePerCft.toString() else selectedMaterial.defaultRatePerCft.toString())
    }

    // Manual Expenses
    var dieselExpense by remember { mutableStateOf(if (initialSlip != null && initialSlip.dieselExpense > 0) initialSlip.dieselExpense.toInt().toString() else "100") }
    var tollExpense by remember { mutableStateOf(if (initialSlip != null && initialSlip.tollExpense > 0) initialSlip.tollExpense.toInt().toString() else "30") }
    var driverAllowance by remember { mutableStateOf(if (initialSlip != null && initialSlip.driverAllowance > 0) initialSlip.driverAllowance.toInt().toString() else "40") }
    var unloadingLabor by remember { mutableStateOf(if (initialSlip != null && initialSlip.unloadingLabor > 0) initialSlip.unloadingLabor.toInt().toString() else "50") }
    var weighbridgeFee by remember { mutableStateOf("15") }
    var otherExpense by remember { mutableStateOf("0") }

    var paymentStatus by remember { mutableStateOf("PAID") }
    var notes by remember { mutableStateOf(initialSlip?.notes.orEmpty()) }

    // Live calculations
    val calculatedCft by remember {
        derivedStateOf {
            when (calcMethodTab) {
                0 -> {
                    val l = UnitConversions.feetAndInchesToDecimalFeet(
                        lengthFt.toDoubleOrNull() ?: 0.0,
                        lengthIn.toDoubleOrNull() ?: 0.0
                    )
                    val w = UnitConversions.feetAndInchesToDecimalFeet(
                        widthFt.toDoubleOrNull() ?: 0.0,
                        widthIn.toDoubleOrNull() ?: 0.0
                    )
                    val h = UnitConversions.feetAndInchesToDecimalFeet(
                        heightFt.toDoubleOrNull() ?: 0.0,
                        heightIn.toDoubleOrNull() ?: 0.0
                    )
                    UnitConversions.calculateRectangularCft(l, w, h)
                }
                1 -> {
                    val gross = grossTons.toDoubleOrNull() ?: 0.0
                    val tare = tareTons.toDoubleOrNull() ?: 0.0
                    val net = maxOf(0.0, gross - tare)
                    UnitConversions.weightTonsToCft(net, selectedMaterial.densityKgM3)
                }
                else -> {
                    directCft.toDoubleOrNull() ?: 0.0
                }
            }
        }
    }

    val calculatedBrass by remember {
        derivedStateOf { UnitConversions.cftToBrass(calculatedCft) }
    }

    val calculatedCuM by remember {
        derivedStateOf { UnitConversions.cftToCubicMeters(calculatedCft) }
    }

    val materialCost by remember {
        derivedStateOf {
            val rate = ratePerCft.toDoubleOrNull() ?: 0.0
            calculatedCft * rate
        }
    }

    val totalExpenses by remember {
        derivedStateOf {
            val diesel = dieselExpense.toDoubleOrNull() ?: 0.0
            val toll = tollExpense.toDoubleOrNull() ?: 0.0
            val batta = driverAllowance.toDoubleOrNull() ?: 0.0
            val unloading = unloadingLabor.toDoubleOrNull() ?: 0.0
            val weigh = weighbridgeFee.toDoubleOrNull() ?: 0.0
            val other = otherExpense.toDoubleOrNull() ?: 0.0
            diesel + toll + batta + unloading + weigh + other
        }
    }

    val grandTotal by remember {
        derivedStateOf { materialCost + totalExpenses }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AmberContainer)
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = AmberDark
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Log Daily Truck Delivery",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Enter dimensions & manual trip expenses",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_delivery_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // --- SECTION 1: Material & Truck Info ---
                    Text(
                        "1. Material & Vehicle Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = SteelBlue,
                        fontWeight = FontWeight.Bold
                    )

                    // Material Selector
                    ExposedDropdownMenuBox(
                        expanded = materialDropdownExpanded,
                        onExpandedChange = { materialDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedMaterial.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Material") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = materialDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_material_input")
                        )
                        ExposedDropdownMenu(
                            expanded = materialDropdownExpanded,
                            onDismissRequest = { materialDropdownExpanded = false }
                        ) {
                            materials.forEach { mat ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(mat.name, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "${mat.category} • Default: $${mat.defaultRatePerCft}/CFT • ${mat.densityKgM3.toInt()} kg/m³",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedMaterial = mat
                                        ratePerCft = mat.defaultRatePerCft.toString()
                                        materialDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = truckNumber,
                            onValueChange = { truckNumber = it },
                            label = { Text("Truck / Dumper #") },
                            placeholder = { Text("e.g. TRK-4819") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_truck_number")
                        )
                        OutlinedTextField(
                            value = challanNumber,
                            onValueChange = { challanNumber = it },
                            label = { Text("Challan / Slip #") },
                            placeholder = { Text("e.g. CH-2041") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_challan_number")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = driverName,
                            onValueChange = { driverName = it },
                            label = { Text("Driver Name") },
                            placeholder = { Text("e.g. John Doe") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_driver_name")
                        )
                        OutlinedTextField(
                            value = supplierName,
                            onValueChange = { supplierName = it },
                            label = { Text("Supplier / Quarry") },
                            placeholder = { Text("e.g. Apex Stone") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_supplier_name")
                        )
                    }

                    // --- SECTION 2: Volume & Dimensions Calculation ---
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "2. Volume Measurement (Cubic Feet)",
                        style = MaterialTheme.typography.titleMedium,
                        color = AmberDark,
                        fontWeight = FontWeight.Bold
                    )

                    TabRow(selectedTabIndex = calcMethodTab) {
                        Tab(
                            selected = calcMethodTab == 0,
                            onClick = { calcMethodTab = 0 },
                            text = { Text("Dimensions") }
                        )
                        Tab(
                            selected = calcMethodTab == 1,
                            onClick = { calcMethodTab = 1 },
                            text = { Text("Weighbridge") }
                        )
                        Tab(
                            selected = calcMethodTab == 2,
                            onClick = { calcMethodTab = 2 },
                            text = { Text("Direct CFT") }
                        )
                    }

                    when (calcMethodTab) {
                        0 -> {
                            // Dimensions: Length, Width, Height in Feet & Inches
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "Truck Bed Dimensions (Feet & Inches):",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Length
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Length:", modifier = Modifier.width(60.dp), fontWeight = FontWeight.Medium)
                                    OutlinedTextField(
                                        value = lengthFt,
                                        onValueChange = { lengthFt = it },
                                        label = { Text("Feet (ft)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("input_length_ft")
                                    )
                                    OutlinedTextField(
                                        value = lengthIn,
                                        onValueChange = { lengthIn = it },
                                        label = { Text("Inches (in)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("input_length_in")
                                    )
                                }

                                // Width
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Width:", modifier = Modifier.width(60.dp), fontWeight = FontWeight.Medium)
                                    OutlinedTextField(
                                        value = widthFt,
                                        onValueChange = { widthFt = it },
                                        label = { Text("Feet (ft)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("input_width_ft")
                                    )
                                    OutlinedTextField(
                                        value = widthIn,
                                        onValueChange = { widthIn = it },
                                        label = { Text("Inches (in)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("input_width_in")
                                    )
                                }

                                // Height
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Height:", modifier = Modifier.width(60.dp), fontWeight = FontWeight.Medium)
                                    OutlinedTextField(
                                        value = heightFt,
                                        onValueChange = { heightFt = it },
                                        label = { Text("Feet (ft)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("input_height_ft")
                                    )
                                    OutlinedTextField(
                                        value = heightIn,
                                        onValueChange = { heightIn = it },
                                        label = { Text("Inches (in)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("input_height_in")
                                    )
                                }
                            }
                        }
                        1 -> {
                            // Weight: Gross, Tare -> Net -> CFT
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = grossTons,
                                        onValueChange = { grossTons = it },
                                        label = { Text("Gross Weight (Tons)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = tareTons,
                                        onValueChange = { tareTons = it },
                                        label = { Text("Tare Weight (Tons)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                val gross = grossTons.toDoubleOrNull() ?: 0.0
                                val tare = tareTons.toDoubleOrNull() ?: 0.0
                                val net = maxOf(0.0, gross - tare)
                                Text(
                                    "Net Weight: ${UnitConversions.formatNumber(net, 2)} Tons (Density: ${selectedMaterial.densityKgM3.toInt()} kg/m³)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SteelBlue
                                )
                            }
                        }
                        2 -> {
                            OutlinedTextField(
                                value = directCft,
                                onValueChange = { directCft = it },
                                label = { Text("Enter Total Volume in Cubic Feet (CFT)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Volume Result Banner
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AmberContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Calculated Volume:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Slate700
                                )
                                Text(
                                    "${UnitConversions.formatNumber(calculatedCft, 2)} CFT",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberDark
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "= ${UnitConversions.formatNumber(calculatedBrass, 2)} Brass",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    "= ${UnitConversions.formatNumber(calculatedCuM, 2)} m³",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate700
                                )
                            }
                        }
                    }

                    // Rate & Material Cost
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = ratePerCft,
                            onValueChange = { ratePerCft = it },
                            label = { Text("Rate per CFT ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_rate_per_cft")
                        )

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Slate100)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "Material Cost",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate700
                                )
                                Text(
                                    UnitConversions.formatCurrency(materialCost),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }
                        }
                    }

                    // --- SECTION 3: Manual Trip & Delivery Expenses ---
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "3. Daily Truck Delivery Expenses (Manual)",
                        style = MaterialTheme.typography.titleMedium,
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Track fuel, toll, driver, unloading, and site handling costs for this trip:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = dieselExpense,
                            onValueChange = { dieselExpense = it },
                            label = { Text("Diesel / Fuel ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_diesel_expense")
                        )
                        OutlinedTextField(
                            value = tollExpense,
                            onValueChange = { tollExpense = it },
                            label = { Text("Toll Charges ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_toll_expense")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = driverAllowance,
                            onValueChange = { driverAllowance = it },
                            label = { Text("Driver Batta ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unloadingLabor,
                            onValueChange = { unloadingLabor = it },
                            label = { Text("Unloading Labor ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weighbridgeFee,
                            onValueChange = { weighbridgeFee = it },
                            label = { Text("Weighbridge Fee ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = otherExpense,
                            onValueChange = { otherExpense = it },
                            label = { Text("Other Misc ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Grand Total Summary Box
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate200, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Slate900)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Material Cost:", color = Slate200, style = MaterialTheme.typography.bodyMedium)
                                Text(UnitConversions.formatCurrency(materialCost), color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Trip Expenses:", color = Slate200, style = MaterialTheme.typography.bodyMedium)
                                Text("+ ${UnitConversions.formatCurrency(totalExpenses)}", color = EmeraldSuccess, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Slate700)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Grand Total Investment:", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    UnitConversions.formatCurrency(grandTotal),
                                    color = AmberPrimary,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Payment Status & Notes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status:", fontWeight = FontWeight.Medium)
                        listOf("PAID", "PENDING", "PARTIAL").forEach { st ->
                            val isSelected = paymentStatus == st
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AmberPrimary else Slate100)
                                    .clickable { paymentStatus = st }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    st,
                                    color = if (isSelected) Color.White else Slate700,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Site Notes / Tower Location") },
                        placeholder = { Text("e.g. Unloaded at Tower C excavation site") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val delivery = TruckDeliveryEntity(
                                truckNumber = truckNumber.ifBlank { "TRK-${System.currentTimeMillis() % 10000}" },
                                challanNumber = challanNumber,
                                driverName = driverName,
                                driverPhone = driverPhone,
                                supplierName = supplierName,
                                materialName = selectedMaterial.name,
                                calculationMethod = when (calcMethodTab) {
                                    0 -> "DIMENSIONS"
                                    1 -> "WEIGHT"
                                    else -> "DIRECT"
                                },
                                lengthFt = lengthFt.toDoubleOrNull() ?: 0.0,
                                lengthIn = lengthIn.toDoubleOrNull() ?: 0.0,
                                widthFt = widthFt.toDoubleOrNull() ?: 0.0,
                                widthIn = widthIn.toDoubleOrNull() ?: 0.0,
                                heightFt = heightFt.toDoubleOrNull() ?: 0.0,
                                heightIn = heightIn.toDoubleOrNull() ?: 0.0,
                                grossWeightTons = grossTons.toDoubleOrNull() ?: 0.0,
                                tareWeightTons = tareTons.toDoubleOrNull() ?: 0.0,
                                netWeightTons = maxOf(0.0, (grossTons.toDoubleOrNull() ?: 0.0) - (tareTons.toDoubleOrNull() ?: 0.0)),
                                totalCft = calculatedCft,
                                totalBrass = calculatedBrass,
                                totalCuM = calculatedCuM,
                                ratePerCft = ratePerCft.toDoubleOrNull() ?: 0.0,
                                materialCost = materialCost,
                                dieselExpense = dieselExpense.toDoubleOrNull() ?: 0.0,
                                tollExpense = tollExpense.toDoubleOrNull() ?: 0.0,
                                driverAllowance = driverAllowance.toDoubleOrNull() ?: 0.0,
                                unloadingLabor = unloadingLabor.toDoubleOrNull() ?: 0.0,
                                weighbridgeFee = weighbridgeFee.toDoubleOrNull() ?: 0.0,
                                otherExpense = otherExpense.toDoubleOrNull() ?: 0.0,
                                totalExpenses = totalExpenses,
                                grandTotal = grandTotal,
                                paymentStatus = paymentStatus,
                                notes = notes
                            )
                            onSave(delivery)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_delivery_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                    ) {
                        Text("Save Delivery Log", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
