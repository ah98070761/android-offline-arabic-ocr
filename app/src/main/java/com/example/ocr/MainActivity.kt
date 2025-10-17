package com.example.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.ocr.databinding.ActivityMainBinding
import com.example.ocr.data.AppDatabase
import com.example.ocr.data.OcrResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val ocrManager by lazy { OcrManager(this) }
    private var urisToProcess: List<Uri>? = null
    private lateinit var db: AppDatabase
    private val TAG = "MainActivity"

    private val pickContentLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri>? ->
        uris?.let {
            urisToProcess = it
            binding.imageView.setImageURI(it.firstOrNull())
            binding.tvOcrResult.text = "تم اختيار ${it.size} عنصر للمعالجة. اضغط 'تنفيذ'."
            binding.tvOcrResult.visibility = View.VISIBLE
            binding.btnPerformOcr.text = "تنفيذ OCR على (${it.size}) عنصر"
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (!granted) {
            Toast.makeText(this, "الأذونات مطلوبة لاختيار الصور أو ملفات PDF.", Toast.LENGTH_LONG).show()
            // إذا لم تُمنح الأذونات، يمكنك إما إغلاق التطبيق أو تعطيل بعض الوظائف
        } else {
            Toast.makeText(this, "تم منح الأذونات بنجاح.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // تهيئة قاعدة البيانات
            db = AppDatabase.getDatabase(applicationContext)

            // تعطيل AdMob مؤقتًا لاختبار التعطل
            /*
            MobileAds.initialize(this) {}
            mAdView = binding.adView
            mAdView.loadAd(AdRequest.Builder().build())
            */

            // التحقق من الأذونات
            checkPermissions()

            binding.btnSelectImage.setOnClickListener {
                if (hasPermissions()) {
                    pickContentLauncher.launch("image/*|application/pdf")
                } else {
                    Toast.makeText(this, "يرجى منح الأذونات أولاً.", Toast.LENGTH_SHORT).show()
                    checkPermissions()
                }
            }

            binding.btnPerformOcr.setOnClickListener {
                performOcrProcess()
            }

            binding.btnDeleteAll.setOnClickListener {
                deleteAllResults()
            }

            lifecycleScope.launch {
                try {
                    db.ocrResultDao().getAllResults().collect { results ->
                        binding.tvSavedResultsInfo.text = "النتائج المحفوظة حاليًا: ${results.size} عنصر."
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "خطأ في جلب النتائج المحفوظة: ${e.message}")
                    binding.tvSavedResultsInfo.text = "خطأ في جلب النتائج المحفوظة."
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في onCreate: ${e.message}", e)
            Toast.makeText(this, "خطأ في تهيئة التطبيق: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun hasPermissions(): Boolean {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }
        return permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }
        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun performOcrProcess() {
        val currentUris = urisToProcess
        if (currentUris.isNullOrEmpty()) {
            Toast.makeText(this, "يرجى اختيار محتوى أولاً.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnPerformOcr.isEnabled = false
        binding.tvOcrResult.text = "جاري معالجة ${currentUris.size} عنصر..."

        lifecycleScope.launch {
            try {
                val combinedResult = StringBuilder()
                var successCount = 0

                for ((index, uri) in currentUris.withIndex()) {
                    binding.tvOcrResult.text = "جاري معالجة العنصر ${index + 1} من ${currentUris.size}..."

                    val resultText = withContext(Dispatchers.IO) {
                        val mimeType = contentResolver.getType(uri)
                        if (mimeType == "application/pdf") {
                            ocrManager.performOcrOnPdf(uri)
                        } else {
                            ocrManager.performOcr(uri)
                        }
                    }

                    if (resultText.isNotBlank() && !resultText.startsWith("❌")) {
                        db.ocrResultDao().insert(OcrResult(text = resultText))
                        combinedResult.append("✅ تم استخراج وحفظ العنصر ${index + 1} بنجاح.\n")
                        successCount++
                    } else {
                        combinedResult.append("❌ فشل استخراج العنصر ${index + 1}.\n")
                    }

                    combinedResult.append("--- النص المستخرج ---\n")
                    combinedResult.append(resultText.take(200)).append("...\n\n")
                }

                binding.tvOcrResult.text = "تمت المعالجة! تم حفظ ${successCount} عنصر.\n\n" + combinedResult.toString()
                binding.btnPerformOcr.isEnabled = true
                Toast.makeText(this@MainActivity, "تمت معالجة وحفظ ${successCount} عنصر.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "خطأ في معالجة OCR: ${e.message}", e)
                binding.tvOcrResult.text = "خطأ في معالجة OCR: ${e.message}"
                binding.btnPerformOcr.isEnabled = true
                Toast.makeText(this@MainActivity, "خطأ في معالجة OCR: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteAllResults() {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db.ocrResultDao().deleteAll()
                }
                Toast.makeText(this@MainActivity, "تم حذف جميع النصوص المحفوظة.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "خطأ في حذف النتائج: ${e.message}", e)
                Toast.makeText(this@MainActivity, "خطأ في حذف النتائج: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}