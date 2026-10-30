package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AddDeliveryDialog
import com.example.ui.screens.CftCalculatorScreen
import com.example.ui.screens.DeliveriesScreen
import com.example.ui.screens.ExpensesStatsScreen
import com.example.ui.screens.MaterialsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SteelBlue
import com.example.ui.viewmodel.ConstructionViewModel

import androidx.compose.material.icons.filled.AutoAwesome
import com.example.ui.screens.AiAssistantScreen

sealed class AppTab(val title: String, val icon: ImageVector, val tag: String) {
    object Calculator : AppTab("CFT Calc", Icons.Default.Calculate, "tab_calculator")
    object Deliveries : AppTab("Deliveries", Icons.Default.LocalShipping, "tab_deliveries")
    object AiAssistant : AppTab("Gemini AI", Icons.Default.AutoAwesome, "tab_ai")
    object Expenses : AppTab("Expenses", Icons.Default.Payments, "tab_expenses")
    object Reports : AppTab("Reports", Icons.Default.Description, "tab_reports")
}

class MainActivity : ComponentActivity() {

    private val viewModel: ConstructionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        com.example.util.configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: ConstructionViewModel) {
    var currentTab by remember { mutableIntStateOf(0) }
    var showAddDeliveryDialog by remember { mutableStateOf(false) }
    var parsedSlipForDialog by remember { mutableStateOf<com.example.ai.ParsedDeliverySlip?>(null) }
    var showMaterialsDialog by remember { mutableStateOf(false) }

    val materials by viewModel.materials.collectAsStateWithLifecycle()

    val tabs = listOf(
        AppTab.Calculator,
        AppTab.Deliveries,
        AppTab.AiAssistant,
        AppTab.Expenses,
        AppTab.Reports
    )

    // BackHandler: return to Calculator if on secondary tab
    BackHandler(enabled = currentTab != 0) {
        currentTab = 0
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AmberPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                tabs[currentTab].icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                when (currentTab) {
                                    0 -> "CFT Calculator"
                                    1 -> "Daily Deliveries"
                                    2 -> "Gemini Construction AI"
                                    3 -> "Site Expenses"
                                    else -> "PDF Reports"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                when (currentTab) {
                                    0 -> "Automatic Real-Time Unit Conversions"
                                    1 -> "Truck Delivery & Volume Logs"
                                    2 -> "Material Estimator, Slip Parser & Audit"
                                    3 -> "Manual Trip Logistics & Expenses"
                                    else -> "Project Manager PDF Document Export"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate700
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showMaterialsDialog = true },
                        modifier = Modifier.testTag("top_bar_materials_btn")
                    ) {
                        Icon(
                            Icons.Default.Layers,
                            contentDescription = "Materials Catalog",
                            tint = Slate800
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    val selected = currentTab == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = {
                            Text(
                                tab.title,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AmberDark,
                            selectedTextColor = AmberDark,
                            indicatorColor = AmberContainer,
                            unselectedIconColor = Slate700,
                            unselectedTextColor = Slate700
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> CftCalculatorScreen(
                    viewModel = viewModel,
                    onLogAsDelivery = {
                        parsedSlipForDialog = null
                        showAddDeliveryDialog = true
                    }
                )
                1 -> DeliveriesScreen(
                    viewModel = viewModel,
                    onOpenAddDelivery = {
                        parsedSlipForDialog = null
                        showAddDeliveryDialog = true
                    }
                )
                2 -> AiAssistantScreen(
                    viewModel = viewModel,
                    onApplyParsedDelivery = { slip ->
                        parsedSlipForDialog = slip
                        showAddDeliveryDialog = true
                    }
                )
                3 -> ExpensesStatsScreen(viewModel = viewModel)
                4 -> ReportsScreen(viewModel = viewModel)
            }
        }

        // Add Delivery Dialog
        if (showAddDeliveryDialog) {
            AddDeliveryDialog(
                materials = materials,
                initialSlip = parsedSlipForDialog,
                onDismiss = {
                    showAddDeliveryDialog = false
                    parsedSlipForDialog = null
                },
                onSave = { delivery ->
                    viewModel.saveDelivery(delivery)
                    showAddDeliveryDialog = false
                    parsedSlipForDialog = null
                }
            )
        }

        // Materials Dialog
        if (showMaterialsDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showMaterialsDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
            ) {
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Materials Catalog & Bulk Densities",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showMaterialsDialog = false }) {
                                Icon(androidx.compose.material.icons.Icons.Default.Layers, contentDescription = "Close")
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            MaterialsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
