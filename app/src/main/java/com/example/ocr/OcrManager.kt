package com.example.ocr

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class OcrManager {

    private lateinit var datapath: String
    private val lang = "ara+eng" 
    private val tessBaseAPI = TessBaseAPI()
    private var isTessInitialized = false 

    companion object {
        private const val TAG = "OcrManager"
        private lateinit var appContext: Context

        // 🌟 التصحيح 1: تحميل المكتبات الأصلية يدويًا قبل أي استخدام لها
        init {
            try {
                // tess-two تتطلب تحميل هذه المكتبات
                System.loadLibrary("pngt")
                System.loadLibrary("lept")
                System.loadLibrary("jpgt")
                System.loadLibrary("tess")
                Log.d(TAG, "Native libraries loaded successfully.")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "ERROR loading native Tesseract libraries: ${e.message}")
            }
        }

        fun init(context: Context) {
            appContext = context
            // 💡 ملاحظة: يجب التأكد أن دالة init يتم استدعاؤها مرة واحدة فقط (مثلاً في Application class)
        }
    }

    // 🌟 التصحيح 2: التهيئة الآن تأخذ السياق (Context)
    constructor(context: Context) {
        appContext = context // تعيين السياق في الباني
        datapath = appContext.filesDir.absolutePath + "/tesseract/"

        // 1. محاولة نسخ ملفات اللغة
        copyTessData()

        val dataDir = File(datapath)
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            Log.e(TAG, "Could not create directory $datapath")
        }

        // 2. محاولة تهيئة Tesseract
        try {
            // هذا الجزء هو الأكثر عرضة للانهيار إذا فشل نسخ الملف
            if (!tessBaseAPI.init(datapath, lang)) {
                Log.e(TAG, "Initialization of Tesseract failed. Check logs for missing files at: $datapath/tessdata/")
                isTessInitialized = false 
            } else {
                Log.d(TAG, "Tesseract initialized successfully.")
                isTessInitialized = true 
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error initializing Tesseract: ${e.message}")
            isTessInitialized = false
        }
    }

    private fun copyTessData() {
        val assetManager = appContext.assets
        val tessDataDir = File(datapath, "tessdata")

        if (!tessDataDir.exists() && !tessDataDir.mkdirs()) {
             Log.e(TAG, "Could not create tessdata directory")
             return
        }

        val languages = arrayOf("ara.traineddata", "eng.traineddata")

        languages.forEach { filename ->
            val file = File(tessDataDir, filename)
            if (!file.exists()) {
                try {
                    assetManager.open("tessdata/$filename").use { inputStream ->
                        FileOutputStream(file).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    Log.d(TAG, "Copied $filename successfully.")
                } catch (e: IOException) {
                    // إذا فشلت عملية النسخ، تحقق من أن الملفات موجودة في مجلد assets/tessdata
                    Log.e(TAG, "Failed to copy $filename: ${e.message}")
                }
            }
        }
    }

    suspend fun performOcr(imageUri: Uri): String = withContext(Dispatchers.IO) {
        if (!isTessInitialized) {
            return@withContext "خطأ: لم يتم تهيئة محرك OCR بنجاح. يرجى التأكد من وجود ملفات اللغة."
        }
        // ... (بقية الكود تبقى كما هي) ...
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(appContext.contentResolver, imageUri)
            val processedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false) 

            tessBaseAPI.setImage(processedBitmap)
            val result = tessBaseAPI.utF8Text
            tessBaseAPI.clear()
            processedBitmap.recycle()

            if (result.isNullOrBlank()) {
                "لم يتم العثور على أي نص في الصورة."
            } else {
                "تمت عملية القراءة الضوئية بنجاح!\n--- نتيجة القراءة ---\n$result"
            }

        } catch (e: Exception) {
            Log.e(TAG, "OCR error: ${e.message}")
            "فشل في معالجة OCR: ${e.message}"
        }
    }

    suspend fun performOcrOnPdf(pdfUri: Uri): String = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return@withContext "خطأ: ميزة قراءة PDF تتطلب Android 5.0 (API 21) أو أعلى."
        }
        if (!isTessInitialized) {
            return@withContext "خطأ: لم يتم تهيئة محرك OCR بنجاح. يرجى التأكد من وجود ملفات اللغة."
        }
        // ... (بقية الكود تبقى كما هي) ...
        val fullOcrResult = StringBuilder()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null

        try {
             pfd = appContext.contentResolver.openFileDescriptor(pdfUri, "r")
             renderer = PdfRenderer(pfd!!)

            val pageCount = renderer.pageCount

            for (i in 0 until pageCount) {
                renderer.openPage(i).use { page ->
                    val width = page.width * 2
                    val height = page.height * 2
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    tessBaseAPI.setImage(bitmap)
                    val pageText = tessBaseAPI.utF8Text

                    fullOcrResult.append("--- الصفحة ${i + 1} ---\n")
                    fullOcrResult.append(pageText).append("\n\n")

                    tessBaseAPI.clear()
                    bitmap.recycle()
                }
            }

            return@withContext "تمت عملية OCR على ملف PDF بنجاح!\n--- النتيجة الإجمالية ---\n${fullOcrResult.toString()}"

        } catch (e: Exception) {
            Log.e(TAG, "PDF OCR error: ${e.message}")
            return@withContext "فشل في معالجة PDF/OCR: ${e.message}"
        } finally {
            renderer?.close()
            pfd?.close()
        }
    }
}