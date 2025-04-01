package com.example.stockrequest.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockrequest.data.models.StockRequest
import com.example.stockrequest.data.repository.StockRequestRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = StockRequestRepository()

    // LiveData for stock requests
    private val _stockRequests = MutableLiveData<List<StockRequest>>()
    val stockRequests: LiveData<List<StockRequest>> = _stockRequests

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadRequests()
    }

    /**
     * Loads all stock requests from the repository.
     */
    fun loadRequests() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val requests = repository.getStockRequests()
                _stockRequests.value = requests
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading requests"
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