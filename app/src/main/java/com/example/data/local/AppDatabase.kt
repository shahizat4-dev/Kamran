package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [MaterialEntity::class, TruckDeliveryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun constructionDao(): ConstructionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "construction_cft_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database.constructionDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: ConstructionDao) {
            val defaultMaterials = listOf(
                MaterialEntity(name = "River Sand", category = "Fine Aggregates", densityKgM3 = 1600.0, defaultRatePerCft = 52.0, isPredefined = true),
                MaterialEntity(name = "M-Sand (Manufactured)", category = "Fine Aggregates", densityKgM3 = 1650.0, defaultRatePerCft = 42.0, isPredefined = true),
                MaterialEntity(name = "Coarse Aggregate 20mm", category = "Coarse Aggregates", densityKgM3 = 1520.0, defaultRatePerCft = 38.0, isPredefined = true),
                MaterialEntity(name = "Coarse Aggregate 10mm", category = "Coarse Aggregates", densityKgM3 = 1500.0, defaultRatePerCft = 40.0, isPredefined = true),
                MaterialEntity(name = "Crushed Stone 40mm", category = "Ballast / Road", densityKgM3 = 1550.0, defaultRatePerCft = 34.0, isPredefined = true),
                MaterialEntity(name = "Red Soil / Murrum", category = "Earth Filling", densityKgM3 = 1700.0, defaultRatePerCft = 18.0, isPredefined = true),
                MaterialEntity(name = "Ready Mix Concrete M25", category = "Concrete", densityKgM3 = 2400.0, defaultRatePerCft = 135.0, isPredefined = true),
                MaterialEntity(name = "Topsoil / Garden Earth", category = "Landscaping", densityKgM3 = 1250.0, defaultRatePerCft = 22.0, isPredefined = true)
            )
            dao.insertMaterials(defaultMaterials)

            // Seed realistic delivery entries so project managers can immediately test PDF exports & stats
            val now = System.currentTimeMillis()
            val dayMs = 86_400_000L

            val sampleDeliveries = listOf(
                TruckDeliveryEntity(
                    dateEpochMs = now - (dayMs * 0) - 7200000L,
                    truckNumber = "TRK-8812",
                    driverName = "Carlos Mendez",
                    driverPhone = "+1 555-0192",
                    challanNumber = "CH-4491",
                    supplierName = "Apex Quarry Co.",
                    projectName = "Grand Heights Tower B",
                    siteLocation = "East Gate Unloading Bay",
                    materialName = "River Sand",
                    calculationMethod = "DIMENSIONS",
                    lengthFt = 16.0, lengthIn = 6.0,
                    widthFt = 7.0, widthIn = 6.0,
                    heightFt = 4.0, heightIn = 0.0,
                    totalCft = 495.0,
                    totalBrass = 4.95,
                    totalCuM = 14.02,
                    ratePerCft = 52.0,
                    materialCost = 25740.0,
                    dieselExpense = 120.0,
                    tollExpense = 35.0,
                    driverAllowance = 40.0,
                    unloadingLabor = 60.0,
                    weighbridgeFee = 15.0,
                    otherExpense = 0.0,
                    totalExpenses = 270.0,
                    grandTotal = 26010.0,
                    paymentStatus = "PAID",
                    notes = "Approved clean river sand for slab casting"
                ),
                TruckDeliveryEntity(
                    dateEpochMs = now - (dayMs * 1) - 14400000L,
                    truckNumber = "DMP-3401",
                    driverName = "Marcus Ray",
                    driverPhone = "+1 555-0381",
                    challanNumber = "CH-4482",
                    supplierName = "Blue Ridge Stone",
                    projectName = "Grand Heights Tower B",
                    siteLocation = "Basement Pillar Area",
                    materialName = "Coarse Aggregate 20mm",
                    calculationMethod = "DIMENSIONS",
                    lengthFt = 18.0, lengthIn = 0.0,
                    widthFt = 7.0, widthIn = 6.0,
                    heightFt = 4.0, heightIn = 6.0,
                    totalCft = 607.5,
                    totalBrass = 6.08,
                    totalCuM = 17.20,
                    ratePerCft = 38.0,
                    materialCost = 23085.0,
                    dieselExpense = 145.0,
                    tollExpense = 45.0,
                    driverAllowance = 50.0,
                    unloadingLabor = 80.0,
                    weighbridgeFee = 15.0,
                    otherExpense = 20.0,
                    totalExpenses = 355.0,
                    grandTotal = 23440.0,
                    paymentStatus = "PAID",
                    notes = "Aggregate quality certificate verified"
                ),
                TruckDeliveryEntity(
                    dateEpochMs = now - (dayMs * 2) - 18000000L,
                    truckNumber = "TRK-5520",
                    driverName = "David Kim",
                    driverPhone = "+1 555-0819",
                    challanNumber = "CH-4467",
                    supplierName = "Apex Quarry Co.",
                    projectName = "Grand Heights Tower B",
                    siteLocation = "Main Access Road",
                    materialName = "Crushed Stone 40mm",
                    calculationMethod = "WEIGHT",
                    grossWeightTons = 32.5,
                    tareWeightTons = 12.0,
                    netWeightTons = 20.5,
                    totalCft = 467.1,
                    totalBrass = 4.67,
                    totalCuM = 13.23,
                    ratePerCft = 34.0,
                    materialCost = 15881.4,
                    dieselExpense = 110.0,
                    tollExpense = 30.0,
                    driverAllowance = 40.0,
                    unloadingLabor = 55.0,
                    weighbridgeFee = 20.0,
                    otherExpense = 0.0,
                    totalExpenses = 255.0,
                    grandTotal = 16136.4,
                    paymentStatus = "PENDING",
                    notes = "Base sub-grade layering material"
                )
            )
            dao.insertDeliveries(sampleDeliveries)
        }
    }
}
