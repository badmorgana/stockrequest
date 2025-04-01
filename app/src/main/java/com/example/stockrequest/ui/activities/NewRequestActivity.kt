package com.example.stockrequest.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.example.stockrequest.R
import com.example.stockrequest.databinding.ActivityNewRequestBinding
import com.example.stockrequest.ui.viewmodels.NewRequestViewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NewRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewRequestBinding
    private val viewModel: NewRequestViewModel by viewModels()

    private var currentPhotoPath: String = ""
    private var photoUri: Uri? = null

    // Request code for camera permission
    private val CAMERA_PERMISSION_REQUEST = 100

    // Activity result launchers
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Photo was taken successfully
            photoUri?.let { uri ->
                loadImageIntoView(uri)
                viewModel.setPhotoUri(uri.toString())
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                loadImageIntoView(uri)
                viewModel.setPhotoUri(uri.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.new_stock_request)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        // Button to take a photo
        binding.btnAddPhoto.setOnClickListener {
            if (checkCameraPermission()) {
                dispatchTakePictureIntent()
            } else {
                requestCameraPermission()
            }
        }

        // Button to pick from gallery
        binding.btnGallery.setOnClickListener {
            val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(pickPhotoIntent)
        }

        // Submit button
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                submitRequest()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            // Could show a progress indicator here
            binding.btnSubmit.isEnabled = !isLoading
        }

        viewModel.requestSubmitted.observe(this) { isSubmitted ->
            if (isSubmitted) {
                Toast.makeText(this, "Request submitted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.errorHandled()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Check item name
        if (binding.etItemName.text.isNullOrBlank()) {
            binding.tilItemName.error = "Item name is required"
            isValid = false
        } else {
            binding.tilItemName.error = null
        }

        // Check if photo is selected
        if (photoUri == null) {
            Snackbar.make(binding.root, "Please add a reference photo", Snackbar.LENGTH_SHORT).show()
            isValid = false
        }

        // Check colors wanted
        if (binding.etColorsWanted.text.isNullOrBlank()) {
            binding.tilColorsWanted.error = "Colors wanted is required"
            isValid = false
        } else {
            binding.tilColorsWanted.error = null
        }

        // Check quantity
        if (binding.etQuantity.text.isNullOrBlank()) {
            binding.tilQuantity.error = "Quantity is required"
            isValid = false
        } else {
            binding.tilQuantity.error = null
        }

        // Check days needed
        if (binding.etDaysNeeded.text.isNullOrBlank()) {
            binding.tilDaysNeeded.error = "Days needed is required"
            isValid = false
        } else {
            binding.tilDaysNeeded.error = null
        }

        return isValid
    }

    private fun submitRequest() {
        val itemName = binding.etItemName.text.toString()
        val colorsWanted = binding.etColorsWanted.text.toString()
        val colorsNotWanted = binding.etColorsNotWanted.text.toString()
        val quantity = binding.etQuantity.text.toString().toIntOrNull() ?: 0
        val daysNeeded = binding.etDaysNeeded.text.toString().toIntOrNull() ?: 0

        viewModel.submitRequest(
            itemName,
            photoUri.toString(),
            colorsWanted,
            colorsNotWanted,
            quantity,
            daysNeeded
        )
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Snackbar.make(
                    binding.root,
                    "Camera permission is required to take photos",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Snackbar.make(binding.root, "Could not create image file", Snackbar.LENGTH_SHORT).show()
                    null
                }

                // Continue only if the File was successfully created
                photoFile?.also {
                    photoUri = FileProvider.getUriForFile(
                        this,
                        "com.example.stockrequest.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    takePictureLauncher.launch(takePictureIntent)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(null) ?: throw IOException("External storage not available")

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun loadImageIntoView(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.ivPhoto)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the back button in the action bar
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}