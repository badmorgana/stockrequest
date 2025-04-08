package com.example.stockrequest.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.stockrequest.data.database.StockRequestDatabase
import com.example.stockrequest.data.firebase.FirebaseManager
import com.example.stockrequest.data.models.StockRequest
import com.example.stockrequest.data.repository.StockRequestRepository
import kotlinx.coroutines.launch

class RequestDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StockRequestDatabase.getDatabase(application)
    private val stockRequestDao = database.stockRequestDao()
    private val repository = StockRequestRepository(stockRequestDao)
    private val firebaseManager = FirebaseManager()

    // LiveData for the stock request
    private val _stockRequest = MutableLiveData<StockRequest?>()
    val stockRequest: LiveData<StockRequest?> = _stockRequest

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Loads the details of a specific stock request from Firebase.
     *
     * @param firebaseRequestId The ID of the request to load
     */
    fun loadRequestDetails(firebaseRequestId: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Try to get the request from Firebase
                Log.d("RequestDetailViewModel", "Fetching from Firebase: $firebaseRequestId")
                val request = firebaseManager.getStockRequestById(firebaseRequestId)

                if (request != null) {
                    Log.d("RequestDetailViewModel", "Found request in Firebase")
                    _stockRequest.value = request
                } else {
                    Log.w("RequestDetailViewModel", "Request not found in Firebase: $firebaseRequestId")
                    _error.value = "Request not found"
                    // If not found in Firebase, try local database
                    val localRequest = repository.getRequestById(firebaseRequestId)
                    if (localRequest != null) {
                        _stockRequest.value = localRequest
                    } else {
                        _error.value = "Request not found"
                    }
                }
            } catch (e: Exception) {
                Log.e("RequestDetailViewModel", "Error loading details for $firebaseRequestId", e)
                _error.value = e.message ?: "Error loading request details"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates the status of the current request in both Firebase and local database.
     *
     * @param newStatus The new status to set
     */
    fun updateRequestStatus(newStatus: StockRequest.Status) {
        val currentRequest = _stockRequest.value ?: return

        // Create updated request with new status
        val updatedRequest = currentRequest.copy(status = newStatus)

        _isLoading.value = true

        viewModelScope.launch {
            try {
                // Update in Firebase
                val firebaseSuccess = firebaseManager.updateRequestStatus(
                    updatedRequest.id.toString(),
                    newStatus
                )

                // Update in local database
                repository.update(updatedRequest)

                // Reload the request to get the updated data
                loadRequestDetails(updatedRequest.id.toString())
            } catch (e: Exception) {
                _error.value = e.message ?: "Error updating request status"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Resets the error message after it has been displayed.
     */
    fun errorHandled() {
        _error.value = null
    }
}