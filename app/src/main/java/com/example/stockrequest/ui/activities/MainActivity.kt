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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: RequestListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Refresh the request list when returning to this screen
        viewModel.loadRequests()
    }

    private fun setupRecyclerView() {
        adapter = RequestListAdapter { stockRequest ->
            // Navigate to request details screen
            val intent = Intent(this, RequestDetailActivity::class.java).apply {
                putExtra(RequestDetailActivity.EXTRA_REQUEST_ID, stockRequest.id.toLong())
            }
            startActivity(intent)
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