package com.example.stockrequest.utils

import androidx.room.TypeConverter
import com.example.stockrequest.data.models.StockRequest

class StockRequestStatusConverter {
    @TypeConverter
    fun fromStatus(status: StockRequest.Status): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(statusName: String): StockRequest.Status {
        return StockRequest.Status.valueOf(statusName)
    }
}