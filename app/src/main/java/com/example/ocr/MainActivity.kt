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
            binding.imageView.setImageResource(R.drawable.ic_pdf) // عرض أيقونة PDF بدلاً من الصورة
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
        
        OcrManager.init(applicationContext)

        // تعديل مستمع النقر لزر اختيار الصور
        binding.btnSelectImage.setOnClickListener {
            pickImageLauncher.launch("image/*") 
        }

        binding.btnCaptureImage.setOnClickListener {
            captureImageLauncher.launch(null) 
        }
        
        // 3. إضافة مستمع لزر اختيار ملف PDF (سنفترض وجود زر جديد في XML)
        // **ملاحظة:** إذا لم يكن لديك زر لاختيار PDF، استخدم نفس زر اختيار الصور مؤقتاً
        binding.btnPerformOcr.setOnClickListener {
            performOcrProcess()
        }
        
        // 4. إضافة دالة للتعامل مع اختيار PDF (إذا لم يكن لديك زر مخصص، تجاهل هذا السطر)
        // إذا كان لديك زر لـ PDF، يجب إضافته في activity_main.xml
        // binding.btnSelectPdf.setOnClickListener { pickPdfLauncher.launch("application/pdf") }

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
                // 5. التفريق بين ملف PDF والصورة بناءً على نوع URI
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