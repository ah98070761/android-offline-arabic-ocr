package com.example.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.IOException

/**
 * Utility class for common file-related operations.
 */
object FileUtility {

    /**
     * Converts a given [Uri] to a [Bitmap].
     * This is useful for loading images selected from the gallery or captured by the camera.
     *
     * @param context The application context.
     * @param imageUri The URI of the image to convert.
     * @return The decoded Bitmap, or null if an error occurs.
     */
    fun uriToBitmap(context: Context, imageUri: Uri): Bitmap? {
        return try {
            // Use ContentResolver to open an input stream from the URI
            context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                // Decode the input stream into a Bitmap
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: SecurityException) {
            e.printStackTrace()
            // This might happen if permission to read the URI is denied or not granted
            null
        }
    }

    // You can add more file utility methods here as needed,
    // e.g., for saving bitmaps, getting file paths, etc.
}