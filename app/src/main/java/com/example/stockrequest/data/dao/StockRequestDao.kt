package com.example.stockrequest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.stockrequest.data.models.StockRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface StockRequestDao {
    @Insert
    suspend fun insert(request: StockRequest): Long

    @Query("SELECT * FROM stock_requests ORDER BY dateSubmitted DESC")
    fun getAllRequests(): Flow<List<StockRequest>>

    @Query("SELECT * FROM stock_requests WHERE status = :status")
    fun getRequestsByStatus(status: StockRequest.Status): Flow<List<StockRequest>>

    @Update
    suspend fun update(request: StockRequest)

    @Query("DELETE FROM stock_requests WHERE id = :requestId")
    suspend fun delete(requestId: Long)

    @Query("SELECT * FROM stock_requests WHERE id = :id")
    suspend fun getRequestById(id: Long): StockRequest?
}