package com.example.stockrequest.data.repository
import com.example.stockrequest.data.models.StockRequest
import com.example.stockrequest.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository class that handles all stock request data operations.
 * In a real application, this would interface with a backend API.
 * For now, we'll simulate network operations with local data.
 */
class StockRequestRepository {

    // Singleton instance of the API service
    private val apiService: ApiService by lazy { ApiService() }

    // In-memory cache of stock requests (simulating a database)
    private val stockRequests = mutableListOf<StockRequest>()

    /**
     * Retrieves all stock requests for the current user.
     * In a real application, this would make a network request.
     */
    suspend fun getStockRequests(): List<StockRequest> = withContext(Dispatchers.IO) {
        // Simulate network delay
        delay(1000)
        return@withContext stockRequests
    }

    /**
     * Submits a new stock request.
     * In a real application, this would make a network request.
     */
    suspend fun submitStockRequest(stockRequest: StockRequest): StockRequest = withContext(Dispatchers.IO) {
        // Simulate network delay
        delay(1500)

        // Generate a unique ID for the request
        stockRequest.id = UUID.randomUUID().toString()

        // Add to our in-memory cache
        stockRequests.add(stockRequest)

        // In a real application, we would use the ApiService to submit to a backend
        // apiService.submitStockRequest(stockRequest)

        return@withContext stockRequest
    }

    /**
     * Gets a stock request by ID.
     */
    suspend fun getStockRequestById(id: String): StockRequest? = withContext(Dispatchers.IO) {
        // Simulate network delay
        delay(500)

        return@withContext stockRequests.find { it.id == id }
    }

    /**
     * Updates the status of a stock request.
     */
    suspend fun updateStockRequestStatus(id: String, newStatus: String): Boolean = withContext(Dispatchers.IO) {
        // Simulate network delay
        delay(1000)

        val request = stockRequests.find { it.id == id }
        request?.let {
            it.status = newStatus
            return@withContext true
        }

        return@withContext false
    }
}