package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.UnitConversions
import com.example.pdf.PdfReportGenerator
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SteelBlue
import com.example.ui.viewmodel.ConstructionViewModel
import com.example.ui.viewmodel.DateFilter
import java.io.File

@Composable
fun ReportsScreen(viewModel: ConstructionViewModel) {
    val context = LocalContext.current
    val deliveries by viewModel.filteredDeliveries.collectAsStateWithLifecycle()
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedDateFilter.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingPdf.collectAsStateWithLifecycle()
    val lastGeneratedPdf by viewModel.lastGeneratedPdf.collectAsStateWithLifecycle()

    var projectName by remember { mutableStateOf("Grand Heights Tower B") }
    var siteLocation by remember { mutableStateOf("Gate 3 - Bulk Material Yard") }
    var supervisorName by remember { mutableStateOf("Eng. Arthur Vance") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Project Manager PDF Reports",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Export printable daily material delivery & expense summaries",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Project Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "1. Report Header Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SteelBlue
                )

                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Project / Contract Name") },
                    placeholder = { Text("e.g. City Metro Phase 2") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("report_project_name")
                )

                OutlinedTextField(
                    value = siteLocation,
                    onValueChange = { siteLocation = it },
                    label = { Text("Site / Unloading Yard Location") },
                    placeholder = { Text("e.g. South Pier Foundation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("report_site_location")
                )

                OutlinedTextField(
                    value = supervisorName,
                    onValueChange = { supervisorName = it },
                    label = { Text("Site Supervisor / Prepared By") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Scope Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "2. Select Date Scope for Report",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AmberDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DateFilter.values().forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AmberPrimary else Slate100)
                                .clickable { viewModel.setDateFilter(filter) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                filter.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else Slate700,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Report Preview Metrics Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "REPORT CONTENT PREVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate200,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate800)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "${deliveries.size} Truck Loads",
                            color = AmberContainer,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Volume", style = MaterialTheme.typography.bodySmall, color = Slate200)
                        Text(
                            "${UnitConversions.formatNumber(summary.totalCft, 1)} CFT",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AmberPrimary
                        )
                        Text(
                            "(${UnitConversions.formatNumber(summary.totalBrass, 2)} Brass)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate200
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Grand Total Cost", style = MaterialTheme.typography.bodySmall, color = Slate200)
                        Text(
                            UnitConversions.formatCurrency(summary.grandTotal),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Expenses: ${UnitConversions.formatCurrency(summary.totalExpenses)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldSuccess
                        )
                    }
                }
            }
        }

        // Action Buttons
        Button(
            onClick = {
                viewModel.generatePdfReport(
                    projectName = projectName,
                    siteLocation = siteLocation,
                    onSuccess = { file ->
                        Toast.makeText(context, "PDF generated successfully!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("generate_pdf_button"),
            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
            enabled = !isGenerating && deliveries.isNotEmpty()
        ) {
            if (isGenerating) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating Document...")
            } else {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Official PDF Report", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Generated PDF Ready Card
        lastGeneratedPdf?.let { file ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, EmeraldSuccess, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = EmeraldLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("PDF Ready to Export", fontWeight = FontWeight.Bold, color = Slate900)
                            Text(
                                "${file.name} (${file.length() / 1024} KB)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate700
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val shareIntent = PdfReportGenerator.createShareIntent(context, file)
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share PDF Report"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_pdf_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share PDF")
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    val viewIntent = PdfReportGenerator.createViewIntent(context, file)
                                    context.startActivity(viewIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No PDF viewer app found. Use Share instead.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open PDF")
                        }
                    }
                }
            }
        }

        // Specifications info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate100),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Official Construction Report Specifications:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("• Includes Executive KPI Cards (Total CFT, Brass, Trucks, Spend)", style = MaterialTheme.typography.bodySmall, color = Slate700)
                Text("• Itemized Daily Delivery Log with Dimensions, Rates & Challan #s", style = MaterialTheme.typography.bodySmall, color = Slate700)
                Text("• Material-wise Consumption Table & Manual Expense Breakdown (Diesel, Toll, Labor)", style = MaterialTheme.typography.bodySmall, color = Slate700)
                Text("• Formal Site Supervisor & Project Manager Sign-Off Section", style = MaterialTheme.typography.bodySmall, color = Slate700)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
