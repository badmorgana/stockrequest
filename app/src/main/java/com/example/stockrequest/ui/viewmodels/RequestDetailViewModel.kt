package com.example.stockrequest.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockrequest.data.models.StockRequest
import com.example.stockrequest.data.repository.StockRequestRepository
import kotlinx.coroutines.launch

class RequestDetailViewModel : ViewModel() {

    private val repository = StockRequestRepository()

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
                val request = repository.getStockRequestById(requestId)
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
    fun updateRequestStatus(newStatus: String) {
        val currentRequestId = _stockRequest.value?.id ?: return

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val success = repository.updateStockRequestStatus(currentRequestId, newStatus)
                if (success) {
                    // Reload the request to get the updated data
                    loadRequestDetails(currentRequestId)
                } else {
                    _error.value = "Failed to update status"
                }
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