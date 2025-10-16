package com.example.ocr

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ocr.databinding.ActivityMainBinding
import com.example.ocr.data.AppDatabase
import com.example.ocr.data.OcrResult
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds // استيراد AdMob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * النشاط الرئيسي للتحكم في واجهة المستخدم، اختيار المحتوى، تشغيل OCR، وحفظ النتائج.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val ocrManager = OcrManager(this)
    private var urisToProcess: List<Uri>? = null // لحفظ URIs المتعددة (صور أو PDF)
    private lateinit var db: AppDatabase
    private lateinit var mAdView : AdView // إعلان AdMob

    // Activity Launcher لاختيار صور/ملفات PDF متعددة (GetMultipleContents)
    private val pickContentLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
        uris?.let {
            urisToProcess = it
            binding.imageView.setImageURI(it.firstOrNull()) // عرض أول عنصر فقط
            binding.tvOcrResult.text = "تم اختيار ${it.size} عنصر للمعالجة. اضغط 'تنفيذ'."
            binding.tvOcrResult.visibility = View.VISIBLE
            binding.btnPerformOcr.text = "تنفيذ OCR على (${it.size}) عنصر"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. تهيئة قاعدة بيانات Room
        db = AppDatabase.getDatabase(applicationContext)

        // 2. تهيئة AdMob SDK وعرض الإعلان
        MobileAds.initialize(this) {}
        mAdView = binding.adView 
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        
        // 3. مستمعي النقر
        binding.btnSelectImage.setOnClickListener {
            // السماح باختيار ملفات صور (*/*) و PDF (application/pdf)
            pickContentLauncher.launch("image/*|application/pdf")
        }

        binding.btnPerformOcr.setOnClickListener {
            performOcrProcess()
        }
        
        binding.btnDeleteAll.setOnClickListener {
            deleteAllResults()
        }
        
        // 4. مراقبة البيانات المحفوظة وعرض عددها
        lifecycleScope.launch {
            db.ocrResultDao().getAllResults().collect { results ->
                binding.tvSavedResultsInfo.text = "النتائج المحفوظة حاليًا: ${results.size} عنصر."
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
                
                // حفظ كل نتيجة فوراً إذا لم تكن نص خطأ
                if (resultText.isNotBlank() && !resultText.startsWith("فشل")) {
                    db.ocrResultDao().insert(OcrResult(text = resultText))
                    combinedResult.append("✅ تم استخراج وحفظ العنصر ${index + 1} بنجاح.\n")
                    successCount++
                } else {
                    combinedResult.append("❌ فشل استخراج العنصر ${index + 1} أو كان فارغاً.\n")
                }
                
                combinedResult.append("--- النص المستخرج ---\n")
                combinedResult.append(resultText.take(200)).append("...\n\n") // عرض جزء صغير

            }

            // عرض الملخص النهائي
            binding.tvOcrResult.text = "تمت المعالجة بنجاح! تم حفظ ${successCount} عنصر.\n\n" + combinedResult.toString()
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
