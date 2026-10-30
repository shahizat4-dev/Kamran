package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.AiResultState
import com.example.ai.ParsedDeliverySlip
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

@Composable
fun AiAssistantScreen(
    viewModel: ConstructionViewModel,
    onApplyParsedDelivery: (ParsedDeliverySlip) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Material Estimator, 1: Slip Parser, 2: PM Site Audit

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Title Header with Gemini Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Gemini Construction AI",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AmberContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "Gemini 3.8 Flash",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    "AI material estimator, slip parser & logistics audit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Sub-tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Estimator") },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Slip Parser") },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Site Audit") },
                icon = { Icon(Icons.Default.Psychology, contentDescription = null) }
            )
        }

        when (selectedTab) {
            0 -> AiEstimatorView(viewModel = viewModel)
            1 -> AiChallanParserView(
                viewModel = viewModel,
                onApply = onApplyParsedDelivery
            )
            2 -> AiSiteAuditView(viewModel = viewModel)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun AiEstimatorView(viewModel: ConstructionViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val estimatorState by viewModel.aiEstimatorState.collectAsStateWithLifecycle()

    var promptInput by remember {
        mutableStateOf("I'm casting a 1,800 sq ft concrete roof slab with 6 inches thickness (M20 mix ratio 1:1.5:3). How many CFT and Brass of sand, coarse aggregates, and cement bags do I need, and how many 10-wheeler truck trips is that?")
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Project Material Estimation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Quick Preset Prompts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate100)
                    .clickable {
                        promptInput = "Casting a 2,500 sq ft foundation slab 8 inches thick using M25 concrete. Calculate total CFT, sand, 20mm aggregates, and truckloads."
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Foundation Slab 2,500 sq ft", style = MaterialTheme.typography.labelSmall, color = Slate800)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate100)
                    .clickable {
                        promptInput = "Road construction: 600 ft long, 24 ft wide, 6 inches crushed stone aggregate sub-base. How many CFT, Brass, and metric tons do I need?"
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Road Sub-base 600 ft", style = MaterialTheme.typography.labelSmall, color = Slate800)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate100)
                    .clickable {
                        promptInput = "Brick masonry wall 80 ft long, 10 ft high, 9 inches thick. Calculate brick count, mortar volume in CFT, and sand required."
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Brick Wall 80' × 10'", style = MaterialTheme.typography.labelSmall, color = Slate800)
            }
        }

        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            label = { Text("Describe project dimensions & requirements") },
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth().testTag("ai_estimator_prompt_input")
        )

        val isLoading = estimatorState is AiResultState.Loading

        Button(
            onClick = { viewModel.estimateProjectMaterials(promptInput) },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("run_ai_estimator_btn"),
            enabled = !isLoading && promptInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calculating with Gemini...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calculate Quantities with Gemini 3.8 Flash", fontWeight = FontWeight.Bold)
            }
        }

        // Response Display
        when (val state = estimatorState) {
            is AiResultState.Loading -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate100)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = AmberPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Gemini is analyzing dimensions and estimating dry mix volumes...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            is AiResultState.Streaming -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gemini Quantity Breakdown:", fontWeight = FontWeight.Bold, color = Slate900)
                            CircularProgressIndicator(color = AmberPrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(state.partialText, style = MaterialTheme.typography.bodyMedium, color = Slate800)
                    }
                }
            }
            is AiResultState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Gemini Quantity Breakdown:", fontWeight = FontWeight.Bold, color = Slate900)
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(state.data))
                                    Toast.makeText(context, "Copied estimate to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = SteelBlue)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.data, style = MaterialTheme.typography.bodyMedium, color = Slate800)
                    }
                }
            }
            is AiResultState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E6))
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE11D48))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(state.message, color = Color(0xFF9F1239), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            AiResultState.Idle -> {}
        }
    }
}

@Composable
fun AiChallanParserView(
    viewModel: ConstructionViewModel,
    onApply: (ParsedDeliverySlip) -> Unit
) {
    val parserState by viewModel.aiParserState.collectAsStateWithLifecycle()
    var slipRawText by remember {
        mutableStateOf("Truck TRK-9021 from Apex Quarry. River Sand delivery. Dumper bed dimensions: 17 ft 6 in length, 7 ft 6 in width, 4 ft 3 in height. Rate 52/cft. Paid fuel 140 and toll 35.")
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "AI Delivery Challan & Slip Parser",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Paste unstructured delivery notes or handwritten challan text. Gemini structured schema extracts truck #, dimensions, CFT rates, and manual trip expenses.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = slipRawText,
            onValueChange = { slipRawText = it },
            label = { Text("Raw Delivery Note / Challan Text") },
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth().testTag("ai_slip_raw_input")
        )

        val isLoading = parserState is AiResultState.Loading

        Button(
            onClick = { viewModel.parseChallanText(slipRawText) },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("run_ai_slip_parser_btn"),
            enabled = !isLoading && slipRawText.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = SteelBlue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Extracting with Gemini...")
            } else {
                Icon(Icons.Default.ReceiptLong, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Extract Delivery Fields with Gemini", fontWeight = FontWeight.Bold)
            }
        }

        when (val state = parserState) {
            is AiResultState.Success -> {
                val slip = state.data
                val cft = UnitConversions.calculateRectangularCft(
                    UnitConversions.feetAndInchesToDecimalFeet(slip.lengthFt, slip.lengthIn),
                    UnitConversions.feetAndInchesToDecimalFeet(slip.widthFt, slip.widthIn),
                    UnitConversions.feetAndInchesToDecimalFeet(slip.heightFt, slip.heightIn)
                )

                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, EmeraldSuccess, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = EmeraldLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Extracted Delivery Information:", fontWeight = FontWeight.Bold, color = Slate900)
                        }
                        Text("• Truck #: ${slip.truckNumber.ifEmpty { "Detected" }}", fontWeight = FontWeight.Medium)
                        Text("• Material: ${slip.materialName}", fontWeight = FontWeight.Medium)
                        Text("• Dimensions: ${slip.lengthFt.toInt()}' ${slip.lengthIn.toInt()}\" (L) × ${slip.widthFt.toInt()}' ${slip.widthIn.toInt()}\" (W) × ${slip.heightFt.toInt()}' ${slip.heightIn.toInt()}\" (H)")
                        Text("• Computed Volume: ${UnitConversions.formatNumber(cft, 1)} CFT (${UnitConversions.formatNumber(UnitConversions.cftToBrass(cft), 2)} Brass)", fontWeight = FontWeight.Bold, color = AmberDark)
                        Text("• Rate: $${slip.ratePerCft}/CFT | Diesel: $${slip.dieselExpense} | Toll: $${slip.tollExpense}")

                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                onApply(slip)
                                viewModel.resetAiParserState()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open in Delivery Form with Extracted Data")
                        }
                    }
                }
            }
            is AiResultState.Error -> {
                Text(state.message, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }
            else -> {}
        }
    }
}

@Composable
fun AiSiteAuditView(viewModel: ConstructionViewModel) {
    val auditState by viewModel.aiAuditState.collectAsStateWithLifecycle()
    val deliveries by viewModel.rawDeliveries.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Project Manager AI Site Logistics Audit",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Gemini 3.8 Flash audits your logged deliveries, material consumption, diesel expenses, and toll fees to detect cost anomalies and site bottlenecks.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val isLoading = auditState is AiResultState.Loading

        Button(
            onClick = { viewModel.generateSiteAudit() },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("run_ai_site_audit_btn"),
            enabled = !isLoading && deliveries.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating Audit...")
            } else {
                Icon(Icons.Default.Psychology, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate AI Logistics Audit (${deliveries.size} trips)", fontWeight = FontWeight.Bold)
            }
        }

        when (val state = auditState) {
            is AiResultState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Executive Audit Briefing:", fontWeight = FontWeight.Bold, color = Slate900, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.data, style = MaterialTheme.typography.bodyMedium, color = Slate800)
                    }
                }
            }
            is AiResultState.Error -> {
                Text(state.message, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }
            else -> {}
        }
    }
}
