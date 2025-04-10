package com.example.stockrequest.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.stockrequest.databinding.ActivityLoginBinding
import com.example.stockrequest.ui.viewmodels.LoginViewModel
import androidx.core.view.WindowCompat

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.loginUser(email, password)
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.signUpUser(email, password)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            binding.btnSignUp.isEnabled = !isLoading
            binding.etEmail.isEnabled = !isLoading
            binding.etPassword.isEnabled = !isLoading
        }

        viewModel.authSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Authentication successful", Toast.LENGTH_SHORT).show()
                // Finish LoginActivity and return to the previous activity (MainActivity)
                finish()
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.errorHandled() // Reset error after showing
            }
        }
    }

    // Prevent user from simply pressing back to bypass login
    override fun onBackPressed() {
        // Optional: Exit the app entirely if back is pressed on login screen
        super.onBackPressed()
        finishAffinity() // Closes all activities in the task
    }
}