package com.example.stockrequest.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val TAG = "LoginViewModel"

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for authentication success
    private val _authSuccess = MutableLiveData<Boolean>()
    val authSuccess: LiveData<Boolean> = _authSuccess

    // LiveData for error messages
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loginUser(email: String, password: String) {
        if (!isValidInput(email, password)) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Attempting sign in for email: $email")
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                Log.d(TAG, "Sign in successful for email: $email")
                _authSuccess.value = true
            } catch (e: Exception) {
                Log.w(TAG, "Sign in failed", e)
                handleAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUpUser(email: String, password: String) {
        if (!isValidInput(email, password)) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Attempting sign up for email: $email")
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                Log.d(TAG, "Sign up successful for email: $email. User: ${firebaseAuth.currentUser?.uid}")
                // Automatically logged in after successful signup
                _authSuccess.value = true
            } catch (e: Exception) {
                Log.w(TAG, "Sign up failed", e)
                handleAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun isValidInput(email: String, password: String): Boolean {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _error.value = "Please enter a valid email address."
            return false
        }
        if (password.isBlank()) {
            _error.value = "Please enter a password."
            return false
        }
        // Firebase requires at least 6 characters for password during signup
        if (password.length < 6) {
            _error.value = "Password must be at least 6 characters."
            return false
        }
        return true
    }


    private fun handleAuthError(e: Exception) {
        _error.value = when (e) {
            is FirebaseAuthWeakPasswordException -> "Password is too weak. Please use at least 6 characters."
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password. Please try again." // Covers wrong password or malformed email
            is FirebaseAuthUserCollisionException -> "An account already exists with this email address."
            else -> e.localizedMessage ?: "Authentication failed. Please try again." // General error
        }
    }

    fun errorHandled() {
        _error.value = null
    }
}