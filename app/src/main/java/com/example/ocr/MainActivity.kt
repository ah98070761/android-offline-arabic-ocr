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

    // تعريف كائن View Binding
    private lateinit var binding: ActivityMainBinding
    private val ocrManager = OcrManager()
    private var imageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            // ✅ تم تغيير textViewResult إلى tvOcrResult
            binding.imageView.setImageURI(it) 
            binding.tvOcrResult.setText(R.string.image_to_ocr) 
            binding.tvOcrResult.visibility = View.VISIBLE
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

        // تهيئة View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        OcrManager.init(applicationContext)

        // ✅ تم تغيير buttonPickImage إلى btnSelectImage
        binding.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*") 
        }

        binding.btnCaptureImage.setOnClickListener {
            captureImageLauncher.launch(null) 
        }

        binding.btnPerformOcr.setOnClickListener {
            performOcrProcess()
        }

        // ✅ تم تغيير textViewResult إلى tvOcrResult
        binding.tvOcrResult.visibility = View.GONE
    }

    private fun performOcrProcess() {
        val currentUri = imageUri
        if (currentUri == null) {
            Toast.makeText(this, "يرجى اختيار صورة أولاً لإجراء OCR.", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ تم تغيير buttonPerformOcr إلى btnPerformOcr
        binding.btnPerformOcr.isEnabled = false
        // ✅ تم تغيير textViewResult إلى tvOcrResult
        binding.tvOcrResult.text = "جاري معالجة النص... يرجى الانتظار."
        binding.tvOcrResult.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    ocrManager.performOcr(currentUri)
                }

                // ✅ تم تغيير textViewResult إلى tvOcrResult
                binding.tvOcrResult.text = result

            } catch (e: Exception) {
                binding.tvOcrResult.text = "فشل في معالجة OCR: ${e.message}"
            } finally {
                binding.btnPerformOcr.isEnabled = true
            }
        }
    }
}