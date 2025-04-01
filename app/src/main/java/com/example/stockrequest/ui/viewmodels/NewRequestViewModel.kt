package com.example.stockrequest.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockrequest.data.database.StockRequestDatabase
import com.example.stockrequest.data.models.StockRequest
import com.example.stockrequest.data.repository.StockRequestRepository
import kotlinx.coroutines.launch

class NewRequestViewModel (application: Application): AndroidViewModel(application) {

    private val database = StockRequestDatabase.getDatabase(application)
    private val stockRequestDao = database.stockRequestDao()
    private val repository = StockRequestRepository(stockRequestDao)

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
        photoUrl: String,
        colorsWanted: String,
        colorsNotWanted: String,
        quantity: Int,
        daysNeeded: Int
    ) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // First upload the image
                val uploadedImageUrl = photoUrl // In a real app, you'd upload the image here

                // Create the stock request
                val stockRequest = StockRequest(
                    itemName = itemName,
                    photoUrl = uploadedImageUrl,
                    colorsWanted = colorsWanted,
                    colorsNotWanted = colorsNotWanted,
                    quantityNeeded = quantity,
                    daysNeeded = daysNeeded
                )

                // Submit the request to the repository
                repository.insert(stockRequest)

                _requestSubmitted.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Error submitting request"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun errorHandled() {
        _error.value = null
    }
}