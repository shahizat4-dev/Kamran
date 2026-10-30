package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConstructionDao {

    // Materials
    @Query("SELECT * FROM materials ORDER BY name ASC")
    fun getAllMaterials(): Flow<List<MaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<MaterialEntity>)

    @Update
    suspend fun updateMaterial(material: MaterialEntity)

    @Delete
    suspend fun deleteMaterial(material: MaterialEntity)

    @Query("SELECT COUNT(*) FROM materials")
    suspend fun getMaterialCount(): Int

    // Deliveries
    @Query("SELECT * FROM deliveries ORDER BY dateEpochMs DESC")
    fun getAllDeliveries(): Flow<List<TruckDeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE dateEpochMs >= :startEpochMs AND dateEpochMs <= :endEpochMs ORDER BY dateEpochMs DESC")
    fun getDeliveriesByDateRange(startEpochMs: Long, endEpochMs: Long): Flow<List<TruckDeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE materialName = :materialName ORDER BY dateEpochMs DESC")
    fun getDeliveriesByMaterial(materialName: String): Flow<List<TruckDeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE id = :id LIMIT 1")
    suspend fun getDeliveryById(id: Long): TruckDeliveryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: TruckDeliveryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveries(deliveries: List<TruckDeliveryEntity>)

    @Update
    suspend fun updateDelivery(delivery: TruckDeliveryEntity)

    @Delete
    suspend fun deleteDelivery(delivery: TruckDeliveryEntity)

    @Query("DELETE FROM deliveries WHERE id = :id")
    suspend fun deleteDeliveryById(id: Long)

    @Query("SELECT COUNT(*) FROM deliveries")
    suspend fun getDeliveryCount(): Int
}
