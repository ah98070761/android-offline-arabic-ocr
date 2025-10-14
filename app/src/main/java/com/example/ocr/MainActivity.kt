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
            binding.textViewResult.setText(R.string.image_to_ocr) // استخدام binding.
            binding.textViewResult.visibility = View.VISIBLE // استخدام binding.
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
        
        // الخطوة الأهم: تهيئة View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        OcrManager.init(applicationContext)

        // جميع مراجع الأزرار تستخدم binding.
        binding.buttonPickImage.setOnClickListener { // استخدام binding.
            pickImageLauncher.launch("image/*") 
        }

        binding.buttonCaptureImage.setOnClickListener { // استخدام binding.
            captureImageLauncher.launch(null) 
        }

        binding.buttonPerformOcr.setOnClickListener { // استخدام binding.
            performOcrProcess()
        }

        binding.textViewResult.visibility = View.GONE // استخدام binding.
    }

    private fun performOcrProcess() {
        val currentUri = imageUri
        if (currentUri == null) {
            Toast.makeText(this, "يرجى اختيار صورة أولاً لإجراء OCR.", Toast.LENGTH_SHORT).show()
            return
        }

        // جميع المراجع تستخدم binding.
        binding.buttonPerformOcr.isEnabled = false // استخدام binding.
        binding.textViewResult.text = "جاري معالجة النص... يرجى الانتظار." // استخدام binding.
        binding.textViewResult.visibility = View.VISIBLE // استخدام binding.

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    ocrManager.performOcr(currentUri)
                }
                
                binding.textViewResult.text = result // استخدام binding.

            } catch (e: Exception) {
                binding.textViewResult.text = "فشل في معالجة OCR: ${e.message}"
            } finally {
                binding.buttonPerformOcr.isEnabled = true // استخدام binding.
            }
        }
    }
}