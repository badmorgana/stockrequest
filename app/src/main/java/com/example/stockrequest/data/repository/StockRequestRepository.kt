package com.example.stockrequest.data.repository

import com.example.stockrequest.data.dao.StockRequestDao
import com.example.stockrequest.data.models.StockRequest
import kotlinx.coroutines.flow.Flow

class StockRequestRepository(
    private val stockRequestDao: StockRequestDao
) {
    // Room-based methods
    val allRequests: Flow<List<StockRequest>> = stockRequestDao.getAllRequests()

    suspend fun insert(request: StockRequest): Long {
        return stockRequestDao.insert(request)
    }

    suspend fun update(request: StockRequest) {
        stockRequestDao.update(request)
    }

    fun getRequestsByStatus(status: StockRequest.Status): Flow<List<StockRequest>> {
        return stockRequestDao.getRequestsByStatus(status)
    }

    suspend fun delete(requestId: Long) {
        stockRequestDao.delete(requestId)
    }
    // Optional: method to get request by ID from Room
    suspend fun getRequestById(id: String): StockRequest? {
        return stockRequestDao.getRequestById(id.toLong())
    }
}