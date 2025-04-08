package com.example.stockrequest.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.stockrequest.data.database.StockRequestDatabase
import com.example.stockrequest.data.firebase.FirebaseManager
import com.example.stockrequest.data.models.StockRequest
import com.example.stockrequest.data.repository.StockRequestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = StockRequestDatabase.getDatabase(application)
    private val repository: StockRequestRepository
    private val firebaseManager = FirebaseManager()

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
        val dao = database.stockRequestDao()
        repository = StockRequestRepository(dao)
    }

    /**
     * Loads all stock requests from Firebase.
     */
    fun loadRequests() {
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch requests from Firebase
                val requests = firebaseManager.getAllStockRequests()
                _stockRequests.postValue(requests)
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