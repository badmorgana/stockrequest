package com.example.stockrequest.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Utility class for image-related operations.
 */
object ImageUtils {

    /**
     * Compresses and saves a bitmap to a file.
     *
     * @param bitmap The bitmap to compress
     * @param file The file to save to
     * @param quality Compression quality (0-100)
     * @return True if successful, false otherwise
     */
    fun saveBitmapToFile(bitmap: Bitmap, file: File, quality: Int = 80): Boolean {
        return try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Compresses a bitmap to a byte array.
     *
     * @param bitmap The bitmap to compress
     * @param quality Compression quality (0-100)
     * @return Compressed byte array
     */
    fun compressBitmapToByteArray(bitmap: Bitmap, quality: Int = 80): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }

    /**
     * Creates a resized bitmap from a URI.
     *
     * @param context The application context
     * @param uri The URI of the image
     * @param maxWidth Maximum width of the resulting bitmap
     * @param maxHeight Maximum height of the resulting bitmap
     * @return The resized bitmap or null if unable to load
     */
    fun getResizedBitmap(context: Context, uri: Uri, maxWidth: Int = 1024, maxHeight: Int = 1024): Bitmap? {
        try {
            // Get the dimensions of the image
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            // Calculate the scale
            var scale = 1
            if (options.outWidth > maxWidth || options.outHeight > maxHeight) {
                val widthScale = options.outWidth.toFloat() / maxWidth
                val heightScale = options.outHeight.toFloat() / maxHeight
                scale = Math.max(widthScale, heightScale).toInt()
            }

            // Decode with the calculated scale
            val finalOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }

            return context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, finalOptions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Creates a temporary image file in the app's cache directory.
     *
     * @param context The application context
     * @param prefix File name prefix
     * @return The created file
     */
    fun createTempImageFile(context: Context, prefix: String = "IMG_"): File {
        val storageDir = context.cacheDir
        return File.createTempFile(
            prefix,
            ".jpg",
            storageDir
        )
    }

    /**
     * Reads a bitmap from a file path.
     *
     * @param filePath The path to the image file
     * @return The bitmap or null if unable to load
     */
    fun getBitmapFromFilePath(filePath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(filePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Reads a bitmap from an input stream.
     *
     * @param inputStream The input stream
     * @return The bitmap or null if unable to load
     */
    fun getBitmapFromInputStream(inputStream: InputStream): Bitmap? {
        return try {
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}