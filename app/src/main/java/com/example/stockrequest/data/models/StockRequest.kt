package com.example.stockrequest.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "stock_requests")
data class StockRequest(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    val itemName: String,
    val photoUrl: String,
    val colorsWanted: String,
    val colorsNotWanted: String = "",
    val quantityNeeded: Int,
    val daysNeeded: Int,
    val dateSubmitted: Long = System.currentTimeMillis(),
    var status: Status = Status.SUBMITTED
) {
    enum class Status {
        SUBMITTED, PROCESSING, ORDERED, COMPLETED, ON_HOLD
    }

    // Companion object for creating a default instance or generating unique identifiers
    companion object {
        fun createDefault() = StockRequest(
            itemName = "",
            photoUrl = "",
            colorsWanted = "",
            quantityNeeded = 0,
            daysNeeded = 0
        )
    }
}