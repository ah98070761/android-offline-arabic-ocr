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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val ocrManager by lazy { OcrManager(this) }
    private var urisToProcess: List<Uri>? = null
    private lateinit var db: AppDatabase
    private lateinit var mAdView: AdView

    // اختيار محتوى متعدد (صور + PDF)
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

    // طلب الأذونات
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (!granted) {
            Toast.makeText(this, "الأذونات مطلوبة لتشغيل التطبيق.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(applicationContext)

        MobileAds.initialize(this) {}
        mAdView = binding.adView
        mAdView.loadAd(AdRequest.Builder().build())

        checkPermissions()

        binding.btnSelectImage.setOnClickListener {
            pickContentLauncher.launch("image/*|application/pdf")
        }

        binding.btnPerformOcr.setOnClickListener {
            performOcrProcess()
        }

        binding.btnDeleteAll.setOnClickListener {
            deleteAllResults()
        }

        lifecycleScope.launch {
            db.ocrResultDao().getAllResults().collect { results ->
                binding.tvSavedResultsInfo.text = "النتائج المحفوظة حاليًا: ${results.size} عنصر."
            }
        }
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
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
        }
    }

    private fun deleteAllResults() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.ocrResultDao().deleteAll()
            }
            Toast.makeText(this@MainActivity, "تم حذف جميع النصوص المحفوظة.", Toast.LENGTH_SHORT).show()
        }
    }
}