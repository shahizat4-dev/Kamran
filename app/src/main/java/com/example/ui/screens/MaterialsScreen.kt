package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MaterialEntity
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.theme.SteelBlue
import com.example.ui.viewmodel.ConstructionViewModel

@Composable
fun MaterialsScreen(viewModel: ConstructionViewModel) {
    val materials by viewModel.materials.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var materialToEdit by remember { mutableStateOf<MaterialEntity?>(null) }
    var materialToDelete by remember { mutableStateOf<MaterialEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Column {
                    Text(
                        "Construction Materials & Rates",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Standard material catalog, bulk densities, and default unit prices",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(materials, key = { it.id }) { mat ->
                MaterialCard(
                    material = mat,
                    onEdit = { materialToEdit = mat },
                    onDelete = { materialToDelete = mat }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_material_fab"),
            containerColor = AmberPrimary,
            contentColor = Color.White
        ) {
            Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = "Add Material")
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Material", fontWeight = FontWeight.Bold)
            }
        }

        // Add / Edit Material Dialog
        if (showAddDialog || materialToEdit != null) {
            val isEditing = materialToEdit != null
            var name by remember { mutableStateOf(materialToEdit?.name ?: "") }
            var category by remember { mutableStateOf(materialToEdit?.category ?: "Aggregates") }
            var density by remember { mutableStateOf(materialToEdit?.densityKgM3?.toString() ?: "1600") }
            var rate by remember { mutableStateOf(materialToEdit?.defaultRatePerCft?.toString() ?: "45.0") }

            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    materialToEdit = null
                },
                title = { Text(if (isEditing) "Edit Material" else "Add Construction Material") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Material Name") },
                            placeholder = { Text("e.g. River Sand, Fly Ash") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_material_name")
                        )
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            placeholder = { Text("e.g. Fine Aggregates, Binder") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = density,
                            onValueChange = { density = it },
                            label = { Text("Density (kg/m³)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_material_density")
                        )
                        OutlinedTextField(
                            value = rate,
                            onValueChange = { rate = it },
                            label = { Text("Default Rate per CFT ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_material_rate")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val entity = MaterialEntity(
                                    id = materialToEdit?.id ?: 0L,
                                    name = name.trim(),
                                    category = category.trim(),
                                    densityKgM3 = density.toDoubleOrNull() ?: 1600.0,
                                    defaultRatePerCft = rate.toDoubleOrNull() ?: 45.0
                                )
                                viewModel.saveMaterial(entity)
                                showAddDialog = false
                                materialToEdit = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                    ) {
                        Text(if (isEditing) "Update" else "Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddDialog = false
                        materialToEdit = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Confirm Dialog
        materialToDelete?.let { mat ->
            AlertDialog(
                onDismissRequest = { materialToDelete = null },
                title = { Text("Delete Material?") },
                text = { Text("Are you sure you want to delete ${mat.name}? Any historical deliveries will still keep this name.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteMaterial(mat)
                        materialToDelete = null
                    }) {
                        Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { materialToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun MaterialCard(
    material: MaterialEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmberContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = AmberDark)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(material.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "${material.category} • Density: ${material.densityKgM3.toInt()} kg/m³",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "$${material.defaultRatePerCft}/CFT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmberDark
                    )
                    Text(
                        "$${(material.defaultRatePerCft * 100).toInt()}/Brass",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate700
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Material", tint = SteelBlue)
                }

                if (!material.isPredefined) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Material", tint = Color.Red)
                    }
                }
            }
        }
    }
}
