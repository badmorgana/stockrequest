package com.example.stockrequest.ui.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log // Add Log import
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.stockrequest.data.database.StockRequestDatabase
import com.example.stockrequest.data.firebase.FirebaseManager
import com.example.stockrequest.data.models.StockRequest
import com.example.stockrequest.data.repository.StockRequestRepository
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth
import kotlinx.coroutines.launch

class NewRequestViewModel(application: Application) : AndroidViewModel(application) {

    // ... (keep existing properties: database, stockRequestDao, repository, firebaseManager, LiveData etc.) ...
    private val database = StockRequestDatabase.getDatabase(application)
    private val stockRequestDao = database.stockRequestDao()
    private val repository = StockRequestRepository(stockRequestDao)
    private val firebaseManager = FirebaseManager()
    private val firebaseAuth = FirebaseAuth.getInstance() // Get FirebaseAuth instance

    // LiveData for photo URI
    private val _photoUri = MutableLiveData<String>()
    val photoUri: LiveData<String> = _photoUri

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for submission success
    private val _requestSubmitted = MutableLiveData<Boolean>()
    val requestSubmitted: LiveData<Boolean> = _requestSubmitted

    // LiveData for error messages
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    fun setPhotoUri(uri: String) {
        _photoUri.value = uri
    }


    fun submitRequest(
        itemName: String,
        photoUrl: String, // Keep this as String as passed from Activity
        colorsWanted: String,
        colorsNotWanted: String,
        quantity: Int,
        daysNeeded: Int
    ) {
        _isLoading.value = true

        // --- START: Authentication Check ---
        if (firebaseAuth.currentUser == null) {
            Log.w("NewRequestViewModel", "User not signed in. Aborting submission.")
            _error.value = "You must be signed in to submit a request."
            _isLoading.value = false
            return // Stop execution here
        }
        // --- END: Authentication Check ---

        viewModelScope.launch {
            try {
                // User is signed in, proceed with Firebase operations
                Log.d("NewRequestViewModel", "User signed in (${firebaseAuth.currentUser?.uid}). Proceeding with submission.")
                val uri = Uri.parse(photoUrl)
                val uploadedImageUrl = firebaseManager.uploadImage(uri)

                // Create the initial request object (firebaseId is null here)
                val stockRequest = StockRequest(
                    // *** firebaseId is initially null ***
                    itemName = itemName,
                    photoUrl = uploadedImageUrl,
                    colorsWanted = colorsWanted,
                    colorsNotWanted = colorsNotWanted,
                    quantityNeeded = quantity,
                    daysNeeded = daysNeeded
                )

                // Save to Firebase Firestore FIRST to get the ID
                // Make sure saveStockRequest returns the String ID
                val generatedFirebaseId = firebaseManager.saveStockRequest(stockRequest)
                Log.d("NewRequestViewModel", "Saved to Firestore with ID: $generatedFirebaseId")

                // Update the object with the Firestore ID before saving locally
                stockRequest.firebaseId = generatedFirebaseId

                // Save locally to Room database (now includes the firebaseId)
                val localId = repository.insert(stockRequest)
                // We can still assign the Room ID if needed elsewhere, but firebaseId is crucial
                stockRequest.id = localId
                Log.d("NewRequestViewModel", "Saved to Room with ID: $localId and FirebaseID: ${stockRequest.firebaseId}")


                _requestSubmitted.value = true

            } catch (e: Exception) {
                Log.e("NewRequestViewModel", "Error during submission: ${e.message}", e)
                _error.value = "Error submitting request: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun errorHandled() {
        _error.value = null
    }
}