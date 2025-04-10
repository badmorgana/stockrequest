package com.example.stockrequest.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.stockrequest.databinding.ActivityMainBinding
import com.example.stockrequest.ui.adapters.RequestListAdapter
import com.example.stockrequest.ui.viewmodels.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import android.util.Log      // For logging messages
import android.widget.Toast
import androidx.core.view.WindowCompat // For top UI bar changes


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: RequestListAdapter
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        firebaseAuth = FirebaseAuth.getInstance()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        checkAuthenticationState()
    }

    private fun checkAuthenticationState() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            // User is NOT signed in, launch LoginActivity

            val intent = Intent(this, LoginActivity::class.java)
            // Make LoginActivity the new task and clear previous activities
            // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            // Finish MainActivity so the user can't press 'back' to get here without logging in
            // finish()
        } else {
            // User IS signed in
            // Now that we know user is logged in, load the requests
            // (This replaces the load in onResume)
            viewModel.loadRequests()
        }
    }

    private fun setupRecyclerView() {
        adapter = RequestListAdapter { stockRequest -> // stockRequest object from the list
            val firebaseId = stockRequest.firebaseId // Get the String Firebase ID
            if (!firebaseId.isNullOrEmpty()) { // Check if it exists
                // Navigate to request details screen using Firebase ID
                val intent = Intent(this, RequestDetailActivity::class.java).apply {
                    // Use a new constant name for clarity (Optional but recommended)
                    putExtra(RequestDetailActivity.EXTRA_FIREBASE_REQUEST_ID, firebaseId) // Pass the String ID
                    // putExtra(RequestDetailActivity.EXTRA_REQUEST_ID, stockRequest.id.toLong()) // REMOVE OR COMMENT OUT OLD LINE
                }
                startActivity(intent)
            } else {
                // Handle cases where firebaseId might be missing in local data (e.g., older records)
                Log.e("MainActivity/Adapter", "Firebase ID is null or empty for Room ID ${stockRequest.id}. Cannot open details.")
                Toast.makeText(this, "Error opening details (missing Firebase ID)", Toast.LENGTH_SHORT).show()
            }
        }

        binding.rvRequests.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupListeners() {
        binding.fabNewRequest.setOnClickListener {
            val intent = Intent(this, NewRequestActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        viewModel.stockRequests.observe(this) { requests ->
            if (requests.isNullOrEmpty()) {
                binding.rvRequests.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
            } else {
                binding.rvRequests.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.GONE
                adapter.submitList(requests)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // Could add a progress indicator here if needed
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                // Display error message
                // Could use a Snackbar here
            }
        }
    }
}