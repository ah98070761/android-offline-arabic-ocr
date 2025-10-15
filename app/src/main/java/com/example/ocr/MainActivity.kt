package com.example.ocr

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
// تأكد من وجود هذا الاستيراد:
import com.example.ocr.databinding.ActivityMainBinding 
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    // 1. تعريف كائن View Binding
    private lateinit var binding: ActivityMainBinding
    private val ocrManager = OcrManager()
    private var imageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            // 2. استخدام binding.imageView بدلاً من imageView
            binding.imageView.setImageURI(it)
            // 3. استخدام binding.textViewResult بدلاً من textViewResult
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
        
        // 4. تهيئة View Binding وتعيين واجهة المستخدم
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        OcrManager.init(applicationContext)

        // 5. استخدام binding.buttonName لجميع الأزرار
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

        // 6. استخدام binding داخل الدالة
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