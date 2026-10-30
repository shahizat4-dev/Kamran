package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MaterialEntity
import com.example.model.UnitConversions
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberLight
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SteelBlue
import com.example.ui.viewmodel.ConstructionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CftCalculatorScreen(
    viewModel: ConstructionViewModel,
    onLogAsDelivery: () -> Unit
) {
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    var selectedCalculatorTab by remember { mutableIntStateOf(0) } // 0: Dimensions & Conversions, 1: Weighbridge to CFT, 2: Unit Converter Matrix

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Cubic Feet (CFT) Calculator",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    "Dimension measurement & real-time construction unit conversion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Calculator Tabs
        TabRow(selectedTabIndex = selectedCalculatorTab) {
            Tab(
                selected = selectedCalculatorTab == 0,
                onClick = { selectedCalculatorTab = 0 },
                text = { Text("Dimensions & CFT") },
                icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                modifier = Modifier.testTag("tab_dimensions_cft")
            )
            Tab(
                selected = selectedCalculatorTab == 1,
                onClick = { selectedCalculatorTab = 1 },
                text = { Text("Weighbridge Slip") },
                icon = { Icon(Icons.Default.Scale, contentDescription = null) }
            )
            Tab(
                selected = selectedCalculatorTab == 2,
                onClick = { selectedCalculatorTab = 2 },
                text = { Text("Converter Matrix") },
                icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) }
            )
        }

        when (selectedCalculatorTab) {
            0 -> DimensionalCftCalculatorView(
                materials = materials,
                onLogAsDelivery = onLogAsDelivery
            )
            1 -> WeightToCftCalculatorTab(materials = materials)
            2 -> MultiUnitConverterTab(materials = materials)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DimensionalCftCalculatorView(
    materials: List<MaterialEntity>,
    onLogAsDelivery: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Dimension Input Mode: 0 = Feet & Inches (Standard Site), 1 = Decimal Units
    var dimensionInputMode by remember { mutableIntStateOf(0) }
    var shapeType by remember { mutableIntStateOf(0) } // 0: Box/Dumper, 1: Hopper/Trapezoid, 2: Cylinder

    // Feet & Inches values
    var lengthFt by remember { mutableStateOf("16") }
    var lengthIn by remember { mutableStateOf("6") }
    var widthFt by remember { mutableStateOf("7") }
    var widthIn by remember { mutableStateOf("6") }
    var heightFt by remember { mutableStateOf("4") }
    var heightIn by remember { mutableStateOf("0") }

    // Decimal Mode values & unit selection
    var decimalLength by remember { mutableStateOf("16.5") }
    var decimalWidth by remember { mutableStateOf("7.5") }
    var decimalHeight by remember { mutableStateOf("4.0") }
    var decimalUnit by remember { mutableStateOf("Feet (ft)") } // Feet, Meters, Inches, Yards

    // Hopper shape widths
    var topWidthFt by remember { mutableStateOf("8") }
    var bottomWidthFt by remember { mutableStateOf("6") }

    // Multiplier / Quantity
    var quantity by remember { mutableIntStateOf(1) }

    // Material Selection
    var selectedMaterial by remember {
        mutableStateOf(materials.firstOrNull() ?: MaterialEntity(name = "River Sand", defaultRatePerCft = 45.0, densityKgM3 = 1600.0))
    }
    var ratePerCft by remember { mutableStateOf(selectedMaterial.defaultRatePerCft.toString()) }
    var materialDropdownExpanded by remember { mutableStateOf(false) }

    // Compute dimensions in decimal feet
    val lFt by remember {
        derivedStateOf {
            if (dimensionInputMode == 0) {
                UnitConversions.feetAndInchesToDecimalFeet(
                    lengthFt.toDoubleOrNull() ?: 0.0,
                    lengthIn.toDoubleOrNull() ?: 0.0
                )
            } else {
                val raw = decimalLength.toDoubleOrNull() ?: 0.0
                when (decimalUnit) {
                    "Meters (m)" -> raw * 3.28084
                    "Inches (in)" -> raw / 12.0
                    "Yards (yd)" -> raw * 3.0
                    else -> raw
                }
            }
        }
    }

    val wFt by remember {
        derivedStateOf {
            if (dimensionInputMode == 0) {
                UnitConversions.feetAndInchesToDecimalFeet(
                    widthFt.toDoubleOrNull() ?: 0.0,
                    widthIn.toDoubleOrNull() ?: 0.0
                )
            } else {
                val raw = decimalWidth.toDoubleOrNull() ?: 0.0
                when (decimalUnit) {
                    "Meters (m)" -> raw * 3.28084
                    "Inches (in)" -> raw / 12.0
                    "Yards (yd)" -> raw * 3.0
                    else -> raw
                }
            }
        }
    }

    val hFt by remember {
        derivedStateOf {
            if (dimensionInputMode == 0) {
                UnitConversions.feetAndInchesToDecimalFeet(
                    heightFt.toDoubleOrNull() ?: 0.0,
                    heightIn.toDoubleOrNull() ?: 0.0
                )
            } else {
                val raw = decimalHeight.toDoubleOrNull() ?: 0.0
                when (decimalUnit) {
                    "Meters (m)" -> raw * 3.28084
                    "Inches (in)" -> raw / 12.0
                    "Yards (yd)" -> raw * 3.0
                    else -> raw
                }
            }
        }
    }

    // Single item volume in CFT
    val singleCft by remember {
        derivedStateOf {
            when (shapeType) {
                0 -> UnitConversions.calculateRectangularCft(lFt, wFt, hFt)
                1 -> {
                    val tw = topWidthFt.toDoubleOrNull() ?: wFt
                    val bw = bottomWidthFt.toDoubleOrNull() ?: (wFt * 0.75)
                    UnitConversions.calculateTrapezoidCft(lFt, tw, bw, hFt)
                }
                else -> {
                    val radius = wFt / 2.0
                    UnitConversions.calculateCylinderCft(radius, lFt)
                }
            }
        }
    }

    // Total CFT with quantity multiplier
    val totalCft by remember { derivedStateOf { singleCft * quantity } }

    // Real-Time Automatic Conversions
    val brassValue by remember { derivedStateOf { UnitConversions.cftToBrass(totalCft) } }
    val cubicMetersValue by remember { derivedStateOf { UnitConversions.cftToCubicMeters(totalCft) } }
    val cubicYardsValue by remember { derivedStateOf { UnitConversions.cftToCubicYards(totalCft) } }
    val cubicInchesValue by remember { derivedStateOf { totalCft * 1728.0 } }
    val litersValue by remember { derivedStateOf { totalCft * 28.3168466 } }
    val gallonsValue by remember { derivedStateOf { totalCft * 7.48052 } }
    val weightTonsValue by remember {
        derivedStateOf {
            UnitConversions.cftToWeightMetricTons(totalCft, selectedMaterial.densityKgM3)
        }
    }
    val weightLbsValue by remember { derivedStateOf { weightTonsValue * 2204.62 } }
    val estimatedCostValue by remember {
        derivedStateOf {
            val rate = ratePerCft.toDoubleOrNull() ?: selectedMaterial.defaultRatePerCft
            totalCft * rate
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // --- 1. HERO CFT VOLUME & REAL-TIME UNIT CONVERSION BANNER ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AmberPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "REAL-TIME VOLUME & CONVERSIONS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate200,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Copy Result Button
                    IconButton(
                        onClick = {
                            val summaryText = buildString {
                                append("Construction Volume Calculation:\n")
                                append("Volume: ${UnitConversions.formatNumber(totalCft, 2)} CFT\n")
                                append("Brass: ${UnitConversions.formatNumber(brassValue, 3)} Brass\n")
                                append("Cubic Meters: ${UnitConversions.formatNumber(cubicMetersValue, 3)} m³\n")
                                append("Cubic Yards: ${UnitConversions.formatNumber(cubicYardsValue, 2)} yd³\n")
                                append("Est. Weight: ${UnitConversions.formatNumber(weightTonsValue, 2)} Tons (${selectedMaterial.name})\n")
                                append("Est. Cost: ${UnitConversions.formatCurrency(estimatedCostValue)}")
                            }
                            clipboardManager.setText(AnnotatedString(summaryText))
                            Toast.makeText(context, "Copied volume & conversions to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp).testTag("copy_cft_result_btn")
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = AmberPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Primary CFT Display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "${UnitConversions.formatNumber(totalCft, 2)} CFT",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = AmberPrimary,
                            fontSize = 34.sp
                        )
                        if (quantity > 1) {
                            Text(
                                "(${UnitConversions.formatNumber(singleCft, 2)} CFT × $quantity items)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate200
                            )
                        }
                    }

                    // Primary Brass Badge (1 Brass = 100 CFT)
                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AmberDark)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                "${UnitConversions.formatNumber(brassValue, 2)} Brass",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "1 Brass = 100 Cu Ft",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate200
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Slate700))
                Spacer(modifier = Modifier.height(12.dp))

                // Real-Time Conversion Grid Cards
                Text(
                    "Automatic Real-Time Unit Conversions:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate200,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Row 1: m³ | yd³ | in³
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConversionMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Cubic Meters",
                        value = "${UnitConversions.formatNumber(cubicMetersValue, 3)} m³",
                        accentColor = SteelBlue
                    )
                    ConversionMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Cubic Yards",
                        value = "${UnitConversions.formatNumber(cubicYardsValue, 2)} yd³",
                        accentColor = AmberLight
                    )
                    ConversionMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Cubic Inches",
                        value = "${UnitConversions.formatNumber(cubicInchesValue, 0)} in³",
                        accentColor = Color(0xFFA855F7)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: Liters | Gallons | Est. Weight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConversionMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Liters",
                        value = "${UnitConversions.formatNumber(litersValue, 1)} L",
                        accentColor = Color(0xFF06B6D4)
                    )
                    ConversionMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "US Gallons",
                        value = "${UnitConversions.formatNumber(gallonsValue, 1)} gal",
                        accentColor = Color(0xFF14B8A6)
                    )
                    ConversionMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Est. Weight",
                        value = "${UnitConversions.formatNumber(weightTonsValue, 2)} Tons",
                        accentColor = EmeraldSuccess
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cost Summary Line
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate800)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Estimated Material Value (${selectedMaterial.name}):",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate200
                    )
                    Text(
                        UnitConversions.formatCurrency(estimatedCostValue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberPrimary
                    )
                }
            }
        }

        // --- 2. INTERACTIVE 3D PROJECTION CANVAS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "3D Volume Geometry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    // Shape Selector Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Box / Dumper", "Hopper", "Cylinder").forEachIndexed { idx, shape ->
                            val isSelected = shapeType == idx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) AmberPrimary else Slate100)
                                    .clickable { shapeType = idx }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    shape,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else Slate700,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3D Canvas Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate900)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasW = size.width
                        val canvasH = size.height

                        // Dynamic scale based on L, W, H ratios
                        val maxDim = maxOf(lFt, wFt, hFt, 1.0)
                        val normL = ((lFt / maxDim) * 0.45).toFloat().coerceIn(0.2f, 0.55f)
                        val normW = ((wFt / maxDim) * 0.35).toFloat().coerceIn(0.15f, 0.4f)
                        val normH = ((hFt / maxDim) * 0.4).toFloat().coerceIn(0.15f, 0.45f)

                        val boxW = canvasW * normL
                        val boxH = canvasH * normH
                        val left = (canvasW - boxW) / 2f - 20f
                        val top = (canvasH - boxH) / 2f + 12f

                        val depth = (canvasW * normW * 0.4f).coerceIn(16f, 40f)

                        val strokeColor = Color(0xFFF59E0B)
                        val frontFill = Color(0x33F59E0B)
                        val topFill = Color(0x55F59E0B)
                        val rightFill = Color(0x44F59E0B)

                        // Front Face
                        drawRect(
                            color = frontFill,
                            topLeft = Offset(left, top),
                            size = Size(boxW, boxH)
                        )
                        drawRect(
                            color = strokeColor,
                            topLeft = Offset(left, top),
                            size = Size(boxW, boxH),
                            style = Stroke(width = 2.5f)
                        )

                        // Top Face (3D)
                        val topPath = Path().apply {
                            moveTo(left, top)
                            lineTo(left + depth, top - depth)
                            lineTo(left + boxW + depth, top - depth)
                            lineTo(left + boxW, top)
                            close()
                        }
                        drawPath(topPath, topFill)
                        drawPath(topPath, strokeColor, style = Stroke(width = 2f))

                        // Right Face (3D)
                        val rightPath = Path().apply {
                            moveTo(left + boxW, top)
                            lineTo(left + boxW + depth, top - depth)
                            lineTo(left + boxW + depth, top + boxH - depth)
                            lineTo(left + boxW, top + boxH)
                            close()
                        }
                        drawPath(rightPath, rightFill)
                        drawPath(rightPath, strokeColor, style = Stroke(width = 2f))
                    }

                    // Dimension Badges on Canvas
                    Text(
                        "Length: ${UnitConversions.formatNumber(lFt, 1)} ft",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
                    )
                    Text(
                        "H: ${UnitConversions.formatNumber(hFt, 1)} ft",
                        color = AmberLight,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 12.dp)
                    )
                    Text(
                        "W: ${UnitConversions.formatNumber(wFt, 1)} ft",
                        color = Color(0xFF38BDF8),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 12.dp)
                    )
                }
            }
        }

        // --- 3. COMMON CONSTRUCTION PRESETS ---
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                "Quick Construction Presets (1-Tap):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Slate700
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PresetChip("10-Wheeler (18' × 7'6\" × 4'6\")") {
                    dimensionInputMode = 0
                    lengthFt = "18"; lengthIn = "0"
                    widthFt = "7"; widthIn = "6"
                    heightFt = "4"; heightIn = "6"
                }
                PresetChip("6-Wheeler (14' × 7'0\" × 3'6\")") {
                    dimensionInputMode = 0
                    lengthFt = "14"; lengthIn = "0"
                    widthFt = "7"; widthIn = "0"
                    heightFt = "3"; heightIn = "6"
                }
                PresetChip("Tractor Trolley (10' × 6' × 2')") {
                    dimensionInputMode = 0
                    lengthFt = "10"; lengthIn = "0"
                    widthFt = "6"; widthIn = "0"
                    heightFt = "2"; heightIn = "0"
                }
                PresetChip("Slab (20' × 15' × 6\")") {
                    dimensionInputMode = 0
                    lengthFt = "20"; lengthIn = "0"
                    widthFt = "15"; widthIn = "0"
                    heightFt = "0"; heightIn = "6"
                }
                PresetChip("Pillar (1' × 1' × 10')") {
                    dimensionInputMode = 0
                    lengthFt = "1"; lengthIn = "0"
                    widthFt = "1"; widthIn = "0"
                    heightFt = "10"; heightIn = "0"
                }
            }
        }

        // --- 4. DIMENSION INPUT FIELDS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Header with Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Dimension Inputs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SteelBlue
                    )

                    // Mode Toggle: Ft+In vs Decimal
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate100)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (dimensionInputMode == 0) AmberPrimary else Color.Transparent)
                                .clickable { dimensionInputMode = 0 }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "Feet & Inches",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (dimensionInputMode == 0) Color.White else Slate700,
                                fontWeight = if (dimensionInputMode == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (dimensionInputMode == 1) AmberPrimary else Color.Transparent)
                                .clickable { dimensionInputMode = 1 }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "Decimal Units",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (dimensionInputMode == 1) Color.White else Slate700,
                                fontWeight = if (dimensionInputMode == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                if (dimensionInputMode == 0) {
                    // --- FEET & INCHES INPUT FIELDS WITH STEPPERS ---
                    // Length Row
                    DimensionFtInRow(
                        label = "Length (L)",
                        feet = lengthFt,
                        inches = lengthIn,
                        onFeetChange = { lengthFt = it },
                        onInchesChange = { lengthIn = it },
                        onFeetStep = { delta ->
                            val current = lengthFt.toIntOrNull() ?: 0
                            lengthFt = maxOf(0, current + delta).toString()
                        },
                        onInchesStep = { delta ->
                            val current = lengthIn.toIntOrNull() ?: 0
                            lengthIn = maxOf(0, current + delta).toString()
                        },
                        tagPrefix = "length"
                    )

                    // Width Row
                    DimensionFtInRow(
                        label = "Width (W)",
                        feet = widthFt,
                        inches = widthIn,
                        onFeetChange = { widthFt = it },
                        onInchesChange = { widthIn = it },
                        onFeetStep = { delta ->
                            val current = widthFt.toIntOrNull() ?: 0
                            widthFt = maxOf(0, current + delta).toString()
                        },
                        onInchesStep = { delta ->
                            val current = widthIn.toIntOrNull() ?: 0
                            widthIn = maxOf(0, current + delta).toString()
                        },
                        tagPrefix = "width"
                    )

                    // Height Row
                    DimensionFtInRow(
                        label = "Height (H)",
                        feet = heightFt,
                        inches = heightIn,
                        onFeetChange = { heightFt = it },
                        onInchesChange = { heightIn = it },
                        onFeetStep = { delta ->
                            val current = heightFt.toIntOrNull() ?: 0
                            heightFt = maxOf(0, current + delta).toString()
                        },
                        onInchesStep = { delta ->
                            val current = heightIn.toIntOrNull() ?: 0
                            heightIn = maxOf(0, current + delta).toString()
                        },
                        tagPrefix = "height"
                    )

                } else {
                    // --- DECIMAL UNITS INPUT FIELDS ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dimension Unit:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        listOf("Feet (ft)", "Meters (m)", "Inches (in)", "Yards (yd)").forEach { u ->
                            val isSel = decimalUnit == u
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) SteelBlue else Slate100)
                                    .clickable { decimalUnit = u }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    u.take(6),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSel) Color.White else Slate700
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = decimalLength,
                        onValueChange = { decimalLength = it },
                        label = { Text("Length in $decimalUnit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_decimal_length")
                    )

                    OutlinedTextField(
                        value = decimalWidth,
                        onValueChange = { decimalWidth = it },
                        label = { Text("Width in $decimalUnit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_decimal_width")
                    )

                    OutlinedTextField(
                        value = decimalHeight,
                        onValueChange = { decimalHeight = it },
                        label = { Text("Height / Depth in $decimalUnit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_decimal_height")
                    )
                }

                // Quantity Multiplier Stepper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate100)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Quantity / Multiplier:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("e.g. 5 identical trucks or 10 concrete beams", style = MaterialTheme.typography.bodySmall, color = Slate600)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Slate800)
                        }

                        Text(
                            "$quantity",
                            modifier = Modifier.padding(horizontal = 14.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )

                        IconButton(
                            onClick = { quantity++ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Slate800)
                        }
                    }
                }
            }
        }

        // --- 5. MATERIAL & RATE SELECTION FOR DENSITY & VALUE ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Material Density & Valuation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                ExposedDropdownMenuBox(
                    expanded = materialDropdownExpanded,
                    onExpandedChange = { materialDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "${selectedMaterial.name} (${selectedMaterial.densityKgM3.toInt()} kg/m³)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Construction Material") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = materialDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
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
                                            "${mat.category} • Density: ${mat.densityKgM3.toInt()} kg/m³ • $${mat.defaultRatePerCft}/CFT",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate600
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
                        value = ratePerCft,
                        onValueChange = { ratePerCft = it },
                        label = { Text("Rate per CFT ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = "${selectedMaterial.densityKgM3.toInt()}",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Density (kg/m³)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 6. ACTION BUTTON: LOG AS DAILY TRUCK DELIVERY ---
        Button(
            onClick = onLogAsDelivery,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("log_cft_as_delivery_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
        ) {
            Icon(Icons.Default.LocalShipping, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Log ${UnitConversions.formatNumber(totalCft, 1)} CFT into Daily Delivery Slip",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun PresetChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AmberContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = AmberDark,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DimensionFtInRow(
    label: String,
    feet: String,
    inches: String,
    onFeetChange: (String) -> Unit,
    onInchesChange: (String) -> Unit,
    onFeetStep: (Int) -> Unit,
    onInchesStep: (Int) -> Unit,
    tagPrefix: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = Slate800)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Feet Input
            OutlinedTextField(
                value = feet,
                onValueChange = onFeetChange,
                label = { Text("Feet (ft)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("${tagPrefix}_feet_input")
            )

            // Feet Quick Steppers
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate100)
                        .clickable { onFeetStep(-1) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("-1'", fontWeight = FontWeight.Bold, color = Slate700, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate100)
                        .clickable { onFeetStep(1) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+1'", fontWeight = FontWeight.Bold, color = Slate700, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Inches Input
            OutlinedTextField(
                value = inches,
                onValueChange = onInchesChange,
                label = { Text("Inches (in)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("${tagPrefix}_inches_input")
            )

            // Inches Quick Steppers
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate100)
                        .clickable { onInchesStep(-1) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("-1\"", fontWeight = FontWeight.Bold, color = Slate700, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate100)
                        .clickable { onInchesStep(1) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+1\"", fontWeight = FontWeight.Bold, color = Slate700, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ConversionMiniCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accentColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Slate800)
            .border(1.dp, Slate700, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Slate200, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun WeightToCftCalculatorTab(materials: List<MaterialEntity>) {
    var grossTons by remember { mutableStateOf("32.5") }
    var tareTons by remember { mutableStateOf("11.5") }
    var selectedMaterial by remember {
        mutableStateOf(materials.firstOrNull() ?: MaterialEntity(name = "River Sand", densityKgM3 = 1600.0))
    }
    var customDensity by remember { mutableStateOf(selectedMaterial.densityKgM3.toString()) }

    val netTons by remember {
        derivedStateOf {
            val g = grossTons.toDoubleOrNull() ?: 0.0
            val t = tareTons.toDoubleOrNull() ?: 0.0
            maxOf(0.0, g - t)
        }
    }

    val cft by remember {
        derivedStateOf {
            val d = customDensity.toDoubleOrNull() ?: selectedMaterial.densityKgM3
            UnitConversions.weightTonsToCft(netTons, d)
        }
    }

    val brass by remember { derivedStateOf { UnitConversions.cftToBrass(cft) } }
    val cubicMeters by remember { derivedStateOf { UnitConversions.cftToCubicMeters(cft) } }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AmberContainer),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "CONVERTED VOLUME FROM WEIGHBRIDGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate700,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${UnitConversions.formatNumber(cft, 2)} CFT",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AmberDark,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "= ${UnitConversions.formatNumber(brass, 2)} Brass  |  ${UnitConversions.formatNumber(cubicMeters, 2)} m³",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    "Calculated for Net Weight ${UnitConversions.formatNumber(netTons, 2)} Metric Tons at ${customDensity} kg/m³ density.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700
                )
            }
        }

        Text("Weighbridge Slip Weight (Metric Tons)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

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
                label = { Text("Tare / Empty (Tons)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate100)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Net Material Weight:", fontWeight = FontWeight.Medium)
                Text(
                    "${UnitConversions.formatNumber(netTons, 2)} Metric Tons (${UnitConversions.formatNumber(netTons * 2204.62, 0)} lbs)",
                    fontWeight = FontWeight.Bold,
                    color = SteelBlue
                )
            }
        }

        OutlinedTextField(
            value = customDensity,
            onValueChange = { customDensity = it },
            label = { Text("Material Density (kg/m³)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            "Standard Bulk Densities:\n• River Sand: ~1600 kg/m³\n• M-Sand: ~1650 kg/m³\n• 20mm Aggregates: ~1520 kg/m³\n• Concrete Ready-Mix: ~2400 kg/m³\n• Crushed Stone: ~1550 kg/m³",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MultiUnitConverterTab(materials: List<MaterialEntity>) {
    var inputValue by remember { mutableStateOf("100") }
    var selectedUnit by remember { mutableStateOf("CFT") }

    val inputNum = inputValue.toDoubleOrNull() ?: 0.0

    // Compute base CFT
    val baseCft = when (selectedUnit) {
        "CFT" -> inputNum
        "Brass" -> UnitConversions.brassToCft(inputNum)
        "Cubic Meter (m³)" -> UnitConversions.cubicMetersToCft(inputNum)
        "Cubic Yard (yd³)" -> UnitConversions.cubicYardsToCft(inputNum)
        "Liters" -> inputNum / 28.3168466
        "US Gallons" -> inputNum / 7.48052
        else -> inputNum
    }

    val conversionUnits = listOf(
        Pair("Cubic Feet (CFT)", UnitConversions.formatNumber(baseCft, 2)),
        Pair("Brass (100 CFT)", UnitConversions.formatNumber(UnitConversions.cftToBrass(baseCft), 3)),
        Pair("Cubic Meters (m³)", UnitConversions.formatNumber(UnitConversions.cftToCubicMeters(baseCft), 3)),
        Pair("Cubic Yards (yd³)", UnitConversions.formatNumber(UnitConversions.cftToCubicYards(baseCft), 3)),
        Pair("Cubic Inches (in³)", UnitConversions.formatNumber(baseCft * 1728.0, 0)),
        Pair("Liters (L)", UnitConversions.formatNumber(baseCft * 28.3168466, 1)),
        Pair("US Gallons", UnitConversions.formatNumber(baseCft * 7.48052, 1)),
        Pair("Metric Tons (Sand @1600 kg/m³)", UnitConversions.formatNumber(UnitConversions.cftToWeightMetricTons(baseCft, 1600.0), 2)),
        Pair("Metric Tons (Stone @1520 kg/m³)", UnitConversions.formatNumber(UnitConversions.cftToWeightMetricTons(baseCft, 1520.0), 2))
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                label = { Text("Input Value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )

            var unitMenuExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = selectedUnit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unit") },
                    modifier = Modifier.fillMaxWidth().clickable { unitMenuExpanded = true }
                )
                androidx.compose.material3.DropdownMenu(
                    expanded = unitMenuExpanded,
                    onDismissRequest = { unitMenuExpanded = false }
                ) {
                    listOf("CFT", "Brass", "Cubic Meter (m³)", "Cubic Yard (yd³)", "Liters", "US Gallons").forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u) },
                            onClick = {
                                selectedUnit = u
                                unitMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Text("Automatic Unit Conversions Matrix", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate100),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                conversionUnits.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyMedium, color = Slate700)
                        Text(
                            value,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(0.8.dp).background(Slate200))
                }
            }
        }
    }
}
