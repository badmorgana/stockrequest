package com.example.stockrequest.data.firebase

import android.net.Uri
import android.util.Log
import com.example.stockrequest.data.models.StockRequest // Ensure this import points to your StockRequest model
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseManager {
    private val TAG = "FirebaseManager"

    // Firebase instances
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Collection references
    private val requestsCollection = db.collection("stock_requests")
    private val storageRef = storage.reference.child("images") // Store images under an 'images' path

    /**
     * Uploads an image to Firebase Storage.
     * @param imageUri The URI of the image to upload.
     * @return The download URL (String) of the uploaded image.
     */
    suspend fun uploadImage(imageUri: Uri): String {
        // Generate a unique file name for the image
        val fileName = "${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(fileName)

        try {
            // Upload the file and wait for completion
            imageRef.putFile(imageUri).await()
            // Get the download URL and wait for completion
            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d(TAG, "Image uploaded successfully: $downloadUrl")
            return downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image: ${e.message}", e)
            throw e // Re-throw the exception to be handled by the caller
        }
    }

    /**
     * Saves or updates a stock request in Firestore.
     * If request.firebaseId is null or empty, a new document is created.
     * Otherwise, the existing document with that ID is updated.
     * @param request The StockRequest object to save or update.
     * @return The Firestore document ID (String) of the saved/updated request.
     */
    suspend fun saveStockRequest(request: StockRequest): String {
        try {
            val documentRef = if (request.firebaseId.isNullOrEmpty()) {
                // Create new request - Firestore generates the ID
                Log.d(TAG, "Creating new stock request in Firestore.")
                requestsCollection.document()
            } else {
                // Update existing request using the provided firebaseId
                Log.d(TAG, "Updating stock request in Firestore with ID: ${request.firebaseId}")
                requestsCollection.document(request.firebaseId!!) // Use !! because we checked isNullOrEmpty
            }

            // Prepare data for Firestore (excluding the ID fields)
            val requestData = hashMapOf(
                "itemName" to request.itemName,
                "photoUrl" to request.photoUrl,
                "colorsWanted" to request.colorsWanted,
                "colorsNotWanted" to request.colorsNotWanted,
                "quantityNeeded" to request.quantityNeeded,
                "daysNeeded" to request.daysNeeded,
                "dateSubmitted" to request.dateSubmitted,
                "status" to request.status.name // Store enum name as String
            )

            // Set the data in Firestore (set overwrites, use update for partial changes if needed)
            documentRef.set(requestData).await()
            Log.d(TAG, "Stock request saved/updated successfully with ID: ${documentRef.id}")
            return documentRef.id // Return the actual Firestore document ID
        } catch (e: Exception) {
            Log.e(TAG, "Error saving stock request: ${e.message}", e)
            throw e
        }
    }

    /**
     * Gets all stock requests from Firestore.
     * @return List<StockRequest> containing all requests.
     */
    suspend fun getAllStockRequests(): List<StockRequest> {
        try {
            Log.d(TAG, "Fetching all stock requests from Firestore.")
            val querySnapshot = requestsCollection.get().await()
            Log.d(TAG, "Fetched ${querySnapshot.size()} documents.")

            return querySnapshot.documents.mapNotNull { document ->
                try {
                    val firebaseDocId = document.id // Get the Firestore document ID (String)
                    val data = document.data ?: return@mapNotNull null // Skip if data is null

                    // Map Firestore data to StockRequest object
                    StockRequest(
                        // *** Important Assumption ***: StockRequest has 'firebaseId: String?'
                        firebaseId = firebaseDocId,
                        // Keep the original Long ID generation *only if* needed for Room compatibility.
                        // Ideally, Room would adapt or use the firebaseId as well.
                        id = firebaseDocId.hashCode().toLong(), // Still potentially problematic if Room relies on this being stable/unique separate from firebaseId
                        itemName = data["itemName"] as? String ?: "",
                        photoUrl = data["photoUrl"] as? String ?: "",
                        colorsWanted = data["colorsWanted"] as? String ?: "",
                        colorsNotWanted = data["colorsNotWanted"] as? String ?: "",
                        quantityNeeded = (data["quantityNeeded"] as? Number)?.toInt() ?: 0,
                        daysNeeded = (data["daysNeeded"] as? Number)?.toInt() ?: 0,
                        dateSubmitted = (data["dateSubmitted"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        // Safely convert status string to enum
                        status = try {
                            StockRequest.Status.valueOf((data["status"] as? String) ?: StockRequest.Status.SUBMITTED.name)
                        } catch (e: IllegalArgumentException) {
                            Log.w(TAG, "Invalid status found in document ${firebaseDocId}: ${data["status"]}. Defaulting to SUBMITTED.")
                            StockRequest.Status.SUBMITTED
                        }
                    )
                } catch (e: Exception) {
                    // Log errors during individual document parsing
                    Log.e(TAG, "Error parsing document ${document.id}: ${e.message}", e)
                    null // Return null for this document if parsing fails
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all stock requests: ${e.message}", e)
            throw e // Re-throw to be handled by the caller
        }
    }

    /**
     * Gets a single stock request by its Firestore document ID.
     * @param documentId The Firestore document ID (String) of the request to get.
     * @return The StockRequest object or null if not found or error occurred.
     */
    suspend fun getStockRequestById(documentId: String): StockRequest? {
        if (documentId.isEmpty()) {
            Log.w(TAG, "getStockRequestById called with empty documentId.")
            return null
        }
        try {
            Log.d(TAG, "Fetching stock request by ID: $documentId")
            val documentSnapshot = requestsCollection.document(documentId).get().await()

            if (!documentSnapshot.exists()) {
                Log.w(TAG, "Stock request with ID $documentId not found.")
                return null
            }

            val data = documentSnapshot.data
            if (data == null) {
                Log.w(TAG, "Stock request data is null for ID $documentId.")
                return null
            }

            Log.d(TAG, "Successfully fetched stock request data for ID: $documentId")
            // Map data to StockRequest object
            return StockRequest(
                // *** Important Assumption ***: StockRequest has 'firebaseId: String?'
                firebaseId = documentId,
                // Keep the original Long ID generation *only if* needed for Room compatibility.
                id = documentId.hashCode().toLong(), // Same caveat as in getAllStockRequests
                itemName = data["itemName"] as? String ?: "",
                photoUrl = data["photoUrl"] as? String ?: "",
                colorsWanted = data["colorsWanted"] as? String ?: "",
                colorsNotWanted = data["colorsNotWanted"] as? String ?: "",
                quantityNeeded = (data["quantityNeeded"] as? Number)?.toInt() ?: 0,
                daysNeeded = (data["daysNeeded"] as? Number)?.toInt() ?: 0,
                dateSubmitted = (data["dateSubmitted"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                status = try {
                    StockRequest.Status.valueOf((data["status"] as? String) ?: StockRequest.Status.SUBMITTED.name)
                } catch (e: IllegalArgumentException) {
                    Log.w(TAG, "Invalid status found in document ${documentId}: ${data["status"]}. Defaulting to SUBMITTED.")
                    StockRequest.Status.SUBMITTED
                }
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error fetching stock request with ID $documentId: ${e.message}", e)
            return null // Return null on error
        }
    }

    /**
     * Updates the status of a specific stock request in Firestore.
     * @param documentId The Firestore document ID (String) of the request to update.
     * @param newStatus The new Status enum value.
     * @return True if the update was successful, false otherwise.
     */
    suspend fun updateRequestStatus(documentId: String, newStatus: StockRequest.Status): Boolean {
        if (documentId.isEmpty()) {
            Log.w(TAG, "updateRequestStatus called with empty documentId.")
            return false
        }
        try {
            Log.d(TAG, "Updating status for request ID $documentId to ${newStatus.name}")
            // Update only the 'status' field
            requestsCollection.document(documentId).update("status", newStatus.name).await()
            Log.d(TAG, "Successfully updated status for request ID $documentId")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating status for request ID $documentId: ${e.message}", e)
            return false // Return false on error
        }
    }

    /**
     * Deletes a stock request from Firestore.
     * @param documentId The Firestore document ID (String) of the request to delete.
     * @return True if deletion was successful, false otherwise.
     */
    suspend fun deleteStockRequest(documentId: String): Boolean {
        if (documentId.isEmpty()) {
            Log.w(TAG, "deleteStockRequest called with empty documentId.")
            return false
        }
        try {
            Log.d(TAG, "Deleting stock request with ID: $documentId")
            requestsCollection.document(documentId).delete().await()
            Log.d(TAG, "Successfully deleted stock request with ID: $documentId")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting stock request with ID $documentId: ${e.message}", e)
            return false
        }
    }
}