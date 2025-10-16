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
    
    // ✅ تهيئة OcrManager يجب أن تكون هنا.
    private val ocrManager = OcrManager(this)
    private var imageUri: Uri? = null

    // 1. Activity Launcher لاختيار الصور
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.imageView.setImageURI(it)
            binding.tvOcrResult.setText(R.string.image_to_ocr)
            binding.tvOcrResult.visibility = View.VISIBLE
            binding.btnPerformOcr.text = getString(R.string.perform_ocr_image) // تحديث النص
        }
    }

    // 2. Activity Launcher لاختيار ملفات PDF
    private val pickPdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it // نستخدم imageUri مؤقتاً لتخزين URI ملف PDF
            // ✅ تم الإصلاح: تم تغيير ic_pdf إلى ic_launcher_foreground كبديل موجود
            binding.imageView.setImageResource(R.drawable.ic_launcher_foreground) 
            binding.tvOcrResult.text = getString(R.string.pdf_ready_to_ocr)
            binding.tvOcrResult.visibility = View.VISIBLE
            binding.btnPerformOcr.text = getString(R.string.perform_ocr_pdf) // تحديث النص
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

        // ❌ التصحيح: تم حذف السطر التالي الذي كان يسبب خطأ Unresolved reference: init
        // OcrManager.init(applicationContext) 

        // تعديل مستمع النقر لزر اختيار الصور
        binding.btnSelectImage.setOnClickListener {
            // سنستخدم زر اختيار الصور لاختيار إما صور أو PDF
            pickImageLauncher.launch("image/*|application/pdf")
        }

        binding.btnCaptureImage.setOnClickListener {
            captureImageLauncher.launch(null) 
        }

        binding.btnPerformOcr.setOnClickListener {
            performOcrProcess()
        }

        binding.tvOcrResult.visibility = View.GONE
    }

    private fun performOcrProcess() {
        val currentUri = imageUri
        if (currentUri == null) {
            Toast.makeText(this, "يرجى اختيار صورة أو ملف PDF أولاً لإجراء OCR.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnPerformOcr.isEnabled = false
        binding.tvOcrResult.text = "جاري معالجة النص... يرجى الانتظار."
        binding.tvOcrResult.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // التفريق بين ملف PDF والصورة بناءً على نوع URI
                val result = withContext(Dispatchers.IO) {
                    val mimeType = contentResolver.getType(currentUri)
                    if (mimeType == "application/pdf") {
                        ocrManager.performOcrOnPdf(currentUri)
                    } else {
                        ocrManager.performOcr(currentUri)
                    }
                }

                binding.tvOcrResult.text = result

            } catch (e: Exception) {
                binding.tvOcrResult.text = "فشل في معالجة OCR: ${e.message}"
            } finally {
                binding.btnPerformOcr.isEnabled = true
            }
        }
    }
}