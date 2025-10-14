package com.example.ocr

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
// تأكد أن هذا السطر موجود ويعمل!
import com.example.ocr.databinding.ActivityMainBinding 
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    // 1. تعريف View Binding
    private lateinit var binding: ActivityMainBinding
    private val ocrManager = OcrManager()
    private var imageUri: Uri? = null

    // 2. مُسجل النشاط لاختيار صورة
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.imageView.setImageURI(it)
            // الوصول الصحيح: binding.textViewResult
            binding.textViewResult.setText(R.string.image_to_ocr) 
            binding.textViewResult.visibility = View.VISIBLE
        }
    }
    
    // 3. مُسجل النشاط لالتقاط صورة
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
        
        // تهيئة OcrManager
        OcrManager.init(applicationContext)

        // 4. ربط وظائف الأزرار باستخدام binding.buttonName
        binding.buttonPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*") 
        }

        binding.buttonCaptureImage.setOnClickListener {
            captureImageLauncher.launch(null) 
        }

        binding.buttonPerformOcr.setOnClickListener {
            performOcrProcess()
        }

        // إخفاء نتيجة OCR في البداية
        binding.textViewResult.visibility = View.GONE
    }

    // 5. وظيفة إجراء OCR
    private fun performOcrProcess() {
        val currentUri = imageUri
        if (currentUri == null) {
            Toast.makeText(this, "يرجى اختيار صورة أولاً لإجراء OCR.", Toast.LENGTH_SHORT).show()
            return
        }

        // تعطيل الأزرار وعرض رسالة التحميل
        // الوصول الصحيح: binding.buttonPerformOcr
        binding.buttonPerformOcr.isEnabled = false
        // الوصول الصحيح: binding.textViewResult
        binding.textViewResult.setText("جاري معالجة النص... يرجى الانتظار.")
        binding.textViewResult.visibility = View.VISIBLE

        // تشغيل عملية OCR في Coroutine (Dispatcher.IO)
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    ocrManager.performOcr(currentUri)
                }
                
                // تحديث الواجهة بنتيجة OCR
                binding.textViewResult.setText(result)

            } catch (e: Exception) {
                binding.textViewResult.setText("فشل في معالجة OCR: ${e.message}")
            } finally {
                // تفعيل الأزرار مرة أخرى
                binding.buttonPerformOcr.isEnabled = true
            }
        }
    }
}