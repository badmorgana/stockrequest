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

class RequestDetailViewModel (application: Application): AndroidViewModel(application) {

    private val database = StockRequestDatabase.getDatabase(application)
    private val stockRequestDao = database.stockRequestDao()
    private val repository = StockRequestRepository(stockRequestDao)

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
     * Loads the details of a specific stock request.
     *
     * @param requestId The ID of the request to load
     */
    fun loadRequestDetails(requestId: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val request = repository.getRequestById(requestId)
                if (request != null) {
                    _stockRequest.value = request
                } else {
                    _error.value = "Request not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading request details"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates the status of the current request.
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
                // Update the request (this method doesn't return a value)
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