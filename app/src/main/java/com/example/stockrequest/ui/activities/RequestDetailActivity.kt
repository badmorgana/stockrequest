package com.example.stockrequest.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.stockrequest.R
import com.example.stockrequest.data.models.StockRequest
import com.example.stockrequest.databinding.ActivityRequestDetailBinding
import com.example.stockrequest.ui.viewmodels.RequestDetailViewModel
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.stockrequest.utils.loadImage

class RequestDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRequestDetailBinding
    private val viewModel: RequestDetailViewModel by viewModels()

    companion object {
        const val EXTRA_REQUEST_ID = "extra_request_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.request_details)

        // Get request ID from intent
        val requestId = intent.getLongExtra(EXTRA_REQUEST_ID, -1L)
        if (requestId == -1L) {
            showToast("Error: No request ID provided")
            finish()
            return
        }

        // Load the request details
        viewModel.loadRequestDetails(requestId.toString())

        // Observe ViewModel
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.stockRequest.observe(this) { request ->
            request?.let {
                displayRequestDetails(it)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.errorHandled()
            }
        }
    }

    private fun displayRequestDetails(request: StockRequest) {
        binding.apply {
            tvItemNameValue.text = request.itemName
            tvColorsWantedValue.text = request.colorsWanted
            tvColorsNotWantedValue.text = if (request.colorsNotWanted.isNotEmpty()) {
                request.colorsNotWanted
            } else {
                "None specified"
            }
            tvQuantityValue.text = request.quantityNeeded.toString()
            tvDaysNeededValue.text = "${request.daysNeeded} ${getString(R.string.days)}"
            tvDateSubmittedValue.text = request.dateSubmitted.formatToString()

            // Set status chip
            chipStatus.text = request.status.name
            chipStatus.setChipBackgroundColorResource(getStatusColor(request.status))

            // Load image
            if (request.photoUrl.isNotEmpty()) {
                ivItemPhoto.loadImage(request.photoUrl)
            }
        }
    }

    private fun getStatusColor(status: StockRequest.Status): Int {
        return when (status) {
            StockRequest.Status.SUBMITTED -> R.color.status_submitted
            StockRequest.Status.PROCESSING -> R.color.status_processing
            StockRequest.Status.ORDERED -> R.color.status_ordered
            StockRequest.Status.COMPLETED -> R.color.status_completed
            StockRequest.Status.ON_HOLD -> R.color.status_on_hold
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Extension function for showing toast
    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    // Extension function for date formatting
    private fun Long.formatToString(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(this)
    }
}