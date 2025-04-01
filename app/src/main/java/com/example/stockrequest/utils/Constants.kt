package com.example.stockrequest.utils

/**
 * Class containing app-wide constants.
 */
object Constants {
    // Request status colors
    const val STATUS_SUBMITTED = "Submitted"
    const val STATUS_PROCESSING = "Processing"
    const val STATUS_ORDERED = "Ordered"
    const val STATUS_COMPLETED = "Completed"
    const val STATUS_ON_HOLD = "On Hold"

    // Intent extras
    const val EXTRA_REQUEST_ID = "extra_request_id"

    // Shared preferences
    const val PREF_NAME = "stock_request_prefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_STORE_ID = "store_id"

    // API endpoints (for future use)
    const val BASE_URL = "https://api.example.com/"
    const val ENDPOINT_REQUESTS = "requests"
    const val ENDPOINT_UPLOAD = "upload"

    // Image specifications
    const val MAX_IMAGE_WIDTH = 1024
    const val MAX_IMAGE_HEIGHT = 1024
    const val IMAGE_COMPRESSION_QUALITY = 80

    // File provider authority
    const val FILE_PROVIDER_AUTHORITY = "com.example.stockrequest.fileprovider"

    // Notification channels
    const val CHANNEL_REQUESTS = "requests_channel"
    const val CHANNEL_UPDATES = "updates_channel"
}