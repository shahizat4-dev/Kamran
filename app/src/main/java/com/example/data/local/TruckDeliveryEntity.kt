package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deliveries")
data class TruckDeliveryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochMs: Long = System.currentTimeMillis(),
    val truckNumber: String,
    val driverName: String = "",
    val driverPhone: String = "",
    val challanNumber: String = "",
    val supplierName: String = "",
    val projectName: String = "Site Project A",
    val siteLocation: String = "",
    val materialName: String,
    val calculationMethod: String = "DIMENSIONS", // DIMENSIONS, WEIGHT, DIRECT

    // Dimension Inputs
    val lengthFt: Double = 0.0,
    val lengthIn: Double = 0.0,
    val widthFt: Double = 0.0,
    val widthIn: Double = 0.0,
    val heightFt: Double = 0.0,
    val heightIn: Double = 0.0,

    // Weight Inputs (if applicable)
    val grossWeightTons: Double = 0.0,
    val tareWeightTons: Double = 0.0,
    val netWeightTons: Double = 0.0,

    // Calculated Volumes
    val totalCft: Double,
    val totalBrass: Double,
    val totalCuM: Double,
    val ratePerCft: Double,
    val materialCost: Double,

    // Manual Expenses for the delivery
    val dieselExpense: Double = 0.0,
    val tollExpense: Double = 0.0,
    val driverAllowance: Double = 0.0,
    val unloadingLabor: Double = 0.0,
    val weighbridgeFee: Double = 0.0,
    val otherExpense: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val grandTotal: Double = 0.0,

    val paymentStatus: String = "PAID", // PAID, PENDING, PARTIAL
    val notes: String = ""
)
