package com.example.data.local

import kotlinx.coroutines.flow.Flow

class ConstructionRepository(private val dao: ConstructionDao) {

    val allMaterials: Flow<List<MaterialEntity>> = dao.getAllMaterials()
    val allDeliveries: Flow<List<TruckDeliveryEntity>> = dao.getAllDeliveries()

    fun getDeliveriesByDateRange(startEpochMs: Long, endEpochMs: Long): Flow<List<TruckDeliveryEntity>> {
        return dao.getDeliveriesByDateRange(startEpochMs, endEpochMs)
    }

    fun getDeliveriesByMaterial(materialName: String): Flow<List<TruckDeliveryEntity>> {
        return dao.getDeliveriesByMaterial(materialName)
    }

    suspend fun getDeliveryById(id: Long): TruckDeliveryEntity? {
        return dao.getDeliveryById(id)
    }

    suspend fun insertDelivery(delivery: TruckDeliveryEntity): Long {
        return dao.insertDelivery(delivery)
    }

    suspend fun updateDelivery(delivery: TruckDeliveryEntity) {
        dao.updateDelivery(delivery)
    }

    suspend fun deleteDelivery(delivery: TruckDeliveryEntity) {
        dao.deleteDelivery(delivery)
    }

    suspend fun deleteDeliveryById(id: Long) {
        dao.deleteDeliveryById(id)
    }

    suspend fun insertMaterial(material: MaterialEntity): Long {
        return dao.insertMaterial(material)
    }

    suspend fun updateMaterial(material: MaterialEntity) {
        dao.updateMaterial(material)
    }

    suspend fun deleteMaterial(material: MaterialEntity) {
        dao.deleteMaterial(material)
    }
}
