package com.example.stockrequest.ui.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.example.stockrequest.R
import com.example.stockrequest.databinding.ActivityNewRequestBinding
import com.example.stockrequest.ui.viewmodels.NewRequestViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat

class NewRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewRequestBinding
    private val viewModel: NewRequestViewModel by viewModels()

    private var currentPhotoPath: String = ""
    private var photoUri: Uri? = null

    // Color Picker for Wanted and Not Wanted sarees
    private val wantedColors = mutableListOf<Int>() // RGB color values
    private val notWantedColors = mutableListOf<Int>()

    // Request code for camera permission
    private val CAMERA_PERMISSION_REQUEST = 100

    // Predefined colors
    private val predefinedColors = intArrayOf(
        "#6B372B".toColorInt(), // Coffee Brown
        "#5F6D7A".toColorInt(), // Mid Grey
        "#E1AD01".toColorInt(), // Mustard Yellow
        "#FCF99C".toColorInt(), // Lemon Yellow
        "#B21807".toColorInt(), // Tomato Red
        "#FAF0E6".toColorInt(), // Soft Cream
        "#006D5B".toColorInt(), // Emerald Green
        "#93DC5C".toColorInt(), // Parrot Green
        "#90D1C8".toColorInt(), // Ocean Bluish Green
        "#800080".toColorInt(), // Purple
        "#FFBF99".toColorInt(), // Peach
        "#E77D22".toColorInt(), // Pepper Orange
        "#F699CD".toColorInt(), // Light Pink
        "#4169E1".toColorInt(), // Light Blue
        "#E0218A".toColorInt(), // Barbie Pink
        "#051650".toColorInt()  // Navy Blue
    )

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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityNewRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.new_stock_request)

        setupListeners()
        observeViewModel()
        setupColorPickers()
    }

    private fun setupColorPickers() {
        binding.btnAddColorWanted.setOnClickListener {
            showPredefinedColorOptions(true)
        }

        binding.btnAddColorNotWanted.setOnClickListener {
            showPredefinedColorOptions(false)
        }
    }

    private fun showPredefinedColorOptions(isWanted: Boolean) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_color_grid)
        dialog.setTitle(if (isWanted) R.string.pick_wanted_color else R.string.pick_not_wanted_color)

        val gridLayout = dialog.findViewById<GridLayout>(R.id.colorGrid)

        for (i in predefinedColors.indices) {
            val colorView = View(this).apply {
                setBackgroundColor(predefinedColors[i])
                val size = resources.getDimensionPixelSize(R.dimen.color_view_size)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                    setMargins(8, 8, 8, 8)
                }

                setOnClickListener {
                    if (isWanted) {
                        wantedColors.add(predefinedColors[i])
                        addColorChip(predefinedColors[i], binding.containerColorsWanted, true)
                    } else {
                        notWantedColors.add(predefinedColors[i])
                        addColorChip(predefinedColors[i], binding.containerColorsNotWanted, false)
                    }
                    dialog.dismiss()
                }
            }
            gridLayout.addView(colorView)
        }

        dialog.show()
    }

    private fun addColorChip(colorInt: Int, container: LinearLayout, isWanted: Boolean) {
        val chip = Chip(this).apply {
            chipBackgroundColor = ColorStateList.valueOf(colorInt)
            isCloseIconVisible = true
            closeIconTint = ColorStateList.valueOf(getContrastColor(colorInt))
            chipMinHeight = resources.getDimensionPixelSize(R.dimen.color_chip_size).toFloat()
            chipIconSize = resources.getDimensionPixelSize(R.dimen.color_chip_size).toFloat()

            layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.color_chip_size),
                resources.getDimensionPixelSize(R.dimen.color_chip_size)
            ).apply {
                setMargins(4, 0, 4, 0)
                gravity = Gravity.CENTER_VERTICAL
            }

            setOnCloseIconClickListener {
                if (isWanted) {
                    wantedColors.remove(colorInt)
                } else {
                    notWantedColors.remove(colorInt)
                }
                container.removeView(this)
            }
        }

        // Add the chip before the add button
        container.addView(chip, container.childCount - 1)
    }

    // Helper to get a contrasting color for the close icon
    private fun getContrastColor(color: Int): Int {
        val luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return if (luminance > 0.5) Color.BLACK else Color.WHITE
    }

    private fun setupListeners() {
        // Button to take a photo
        binding.btnAddPhoto.setOnClickListener {
            Log.d("NewRequestActivity", "Take Photo button clicked")
            if (checkCameraPermission()) {
                Log.d("NewRequestActivity", "Camera permission granted")
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
        if (wantedColors.isEmpty()) {
            Snackbar.make(binding.root, "Please select at least one color wanted", Snackbar.LENGTH_SHORT).show()
            isValid = false
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
        val colorsWanted = wantedColors.joinToString(",") { "#" + Integer.toHexString(it).substring(2) }
        val colorsNotWanted = notWantedColors.joinToString(",") { "#" + Integer.toHexString(it).substring(2) }
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
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            Log.e("NewRequestActivity", "Error creating image file", ex)
            Snackbar.make(binding.root, "Could not create image file", Snackbar.LENGTH_SHORT).show()
            null
        }

        photoFile?.also { file ->
            try {
                photoUri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    file
                )

                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                // Add URI permissions
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

                takePictureLauncher.launch(cameraIntent)
            } catch (ex: Exception) {
                Log.e("NewRequestActivity", "Error setting up camera intent", ex)
                Snackbar.make(binding.root, "Could not launch camera ${ex.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = filesDir // Use internal storage directory

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