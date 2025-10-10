package com.example.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var ocrManager: OcrManager
    private lateinit var textView: TextView
    private lateinit var imageView: ImageView

    private val requestPermissionLauncher = 
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, do nothing here as we request file pick later
            } else {
                textView.text = "Storage permission denied. Cannot load Tesseract language files."
            }
        }

    private val pickFileLauncher = 
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    // For simplicity, handle image pick for now
                    // For complex file types like PDF, FileUtility will be needed
                    val bitmap = FileUtility.uriToBitmap(this, uri)
                    imageView.setImageBitmap(bitmap)
                    if (bitmap != null) {
                        textView.text = "Processing..."
                        // Run OCR on a separate thread
                        Thread {
                            val resultText = ocrManager.performOcr(bitmap)
                            runOnUiThread {
                                textView.text = resultText
                            }
                        }.start()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textViewResult)
        imageView = findViewById(R.id.imageView)
        val pickButton: Button = findViewById(R.id.buttonPickImage)

        // Request storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // Initialize OCR Manager
        ocrManager = OcrManager(this)

        // Set up the button click listener
        pickButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickFileLauncher.launch(intent)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        ocrManager.stopTesseract()
    }
}