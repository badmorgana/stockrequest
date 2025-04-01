package com.example.stockrequest.data.models

import java.util.Date
import java.io.Serializable

data class StockRequest(
    var id: String = "",
    val itemName: String = "",
    var photoUrl: String = "",
    val colorsWanted: String = "",
    val colorsNotWanted: String = "",
    val quantityNeeded: Int = 0,
    val daysNeeded: Int = 0,
    val dateSubmitted: Date = Date(),
    var status: String = Status.SUBMITTED.displayName,
    var storeId: String = "",
    var submitterId: String = ""
) : Serializable {

    // Enum for request status
    enum class Status(val displayName: String) {
        SUBMITTED("Submitted"),
        PROCESSING("Processing"),
        ORDERED("Ordered"),
        COMPLETED("Completed"),
        ON_HOLD("On Hold")
    }

    fun isUrgent(): Boolean {
        return daysNeeded <= 3
    }
}