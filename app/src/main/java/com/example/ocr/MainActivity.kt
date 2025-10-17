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
import androidx.core.app.ActivityCompat
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
    private val ocrManager = OcrManager(this)
    private var urisToProcess: List<Uri>? = null
    private lateinit var db: AppDatabase
    private lateinit var mAdView : AdView

    private val REQUEST_PERMISSION_CODE = 1001

    private val pickContentLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
            uris?.let {
                urisToProcess = it
                binding.imageView.setImageURI(it.firstOrNull())
                binding.tvOcrResult.text = "تم اختيار ${it.size} عنصر للمعالجة. اضغط 'تنفيذ'."
                binding.tvOcrResult.visibility = View.VISIBLE
                binding.btnPerformOcr.text = "تنفيذ OCR على (${it.size}) عنصر"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1️⃣ طلب الأذونات وقت التشغيل
        checkAndRequestPermissions()

        // 2️⃣ تهيئة قاعدة البيانات
        db = AppDatabase.getDatabase(applicationContext)

        // 3️⃣ تهيئة AdMob
        MobileAds.initialize(this) {}
        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        // 4️⃣ مستمعي الأزرار
        binding.btnSelectImage.setOnClickListener {
            pickContentLauncher.launch("image/*|application/pdf")
        }
        binding.btnPerformOcr.setOnClickListener { performOcrProcess() }
        binding.btnDeleteAll.setOnClickListener { deleteAllResults() }

        // 5️⃣ مراقبة البيانات المحفوظة
        lifecycleScope.launch {
            db.ocrResultDao().getAllResults().collect { results ->
                binding.tvSavedResultsInfo.text = "النتائج المحفوظة حاليًا: ${results.size} عنصر."
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT <= 32) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        }
        permissions.add(Manifest.permission.CAMERA)

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), REQUEST_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "❌ يجب منح الأذونات للوصول للصور والكاميرا.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun performOcrProcess() {
        val currentUris = urisToProcess
        if (currentUris.isNullOrEmpty()) {
            Toast.makeText(this, "يرجى اختيار محتوى أولاً لإجراء OCR.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnPerformOcr.isEnabled = false
        binding.tvOcrResult.text = "جاري معالجة ${currentUris.size} عنصر... قد يستغرق الأمر بعض الوقت."

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

                if (resultText.isNotBlank() && !resultText.startsWith("فشل")) {
                    db.ocrResultDao().insert(OcrResult(text = resultText))
                    combinedResult.append("✅ تم استخراج وحفظ العنصر ${index + 1} بنجاح.\n")
                    successCount++
                } else {
                    combinedResult.append("❌ فشل استخراج العنصر ${index + 1} أو كان فارغاً.\n")
                }

                combinedResult.append("--- النص المستخرج ---\n")
                combinedResult.append(resultText.take(200)).append("...\n\n")
            }

            binding.tvOcrResult.text =
                "تمت المعالجة بنجاح! تم حفظ ${successCount} عنصر.\n\n" + combinedResult.toString()
            binding.btnPerformOcr.isEnabled = true
            Toast.makeText(this@MainActivity, "تمت معالجة وحفظ ${successCount} عنصر بنجاح.", Toast.LENGTH_LONG).show()
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