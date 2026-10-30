package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = "Aggregates",
    val densityKgM3: Double = 1600.0,
    val defaultRatePerCft: Double = 45.0,
    val unit: String = "CFT",
    val isPredefined: Boolean = false
)
