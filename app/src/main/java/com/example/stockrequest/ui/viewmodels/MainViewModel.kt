package com.example.stockrequest.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.stockrequest.data.database.StockRequestDatabase
import com.example.stockrequest.data.models.StockRequest
import com.example.stockrequest.data.repository.StockRequestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StockRequestDatabase.getDatabase(application)
    private val repository: StockRequestRepository
    private val stockRequestDao =database.stockRequestDao()
    private val stockRequestRepository = StockRequestRepository(stockRequestDao)

    // LiveData for stock requests
    val stockRequests: LiveData<List<StockRequest>>

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        val dao = database.stockRequestDao()
        repository = StockRequestRepository(dao)

        // Convert Flow to LiveData
        stockRequests = repository.allRequests.asLiveData(viewModelScope.coroutineContext)
    }

    /**
     * Loads all stock requests from the repository.
     */
    fun loadRequests() {
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Since stockRequests is now a Flow converted to LiveData,
                // we don't need to manually set its value
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Error loading requests")
            } finally {
                _isLoading.postValue(false)
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