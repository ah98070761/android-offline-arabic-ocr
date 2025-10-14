package com.example.ocr

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ocr.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val ocrManager = OcrManager()
    private var imageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.imageView.setImageURI(it)
            binding.textViewResult.setText(R.string.image_to_ocr)
            binding.textViewResult.visibility = View.VISIBLE
        }
    }
    
    private val captureImageLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            binding.imageView.setImageBitmap(it)
            imageUri = null 
            Toast.makeText(this, "تم التقاط الصورة، يرجى اختيار صورة من المعرض لـ OCR الفعلي.", Toast.LENGTH_LONG).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        OcrManager.init(applicationContext)

        binding.buttonPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*") 
        }

        binding.buttonCaptureImage.setOnClickListener {
            captureImageLauncher.launch(null) 
        }

        binding.buttonPerformOcr.setOnClickListener {
            performOcrProcess()
        }

        binding.textViewResult.visibility = View.GONE
    }

    private fun performOcrProcess() {
        val currentUri = imageUri
        if (currentUri == null) {
            Toast.makeText(this, "يرجى اختيار صورة أولاً لإجراء OCR.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.buttonPerformOcr.isEnabled = false
        binding.textViewResult.text = "جاري معالجة النص... يرجى الانتظار."
        binding.textViewResult.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    ocrManager.performOcr(currentUri)
                }
                
                binding.textViewResult.text = result

            } catch (e: Exception) {
                binding.textViewResult.text = "فشل في معالجة OCR: ${e.message}"
            } finally {
                binding.buttonPerformOcr.isEnabled = true
            }
        }
    }
}