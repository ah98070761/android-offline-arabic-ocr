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

    // مسار لغة Tesseract، يجب أن ينتهي بـ /
    private lateinit var datapath: String
    // اللغة التي سيتم استخدامها (العربية والإنجليزية)
    private val lang = "ara+eng" 

    // تهيئة API Tesseract
    private val tessBaseAPI = TessBaseAPI()

    companion object {
        private const val TAG = "OcrManager"
        private lateinit var appContext: Context

        fun init(context: Context) {
            appContext = context
        }
    }

    init {
        // يتم تهيئة مسار البيانات (Tesseract data directory)
        datapath = appContext.filesDir.absolutePath + "/tesseract/"
        copyTessData()
        
        // يجب أن تكون تهيئة API في نفس الـ Thread، ونتأكد أن المسار موجود.
        val dataDir = File(datapath)
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            Log.e(TAG, "Could not create directory $datapath")
        }

        try {
            // تهيئة Tesseract مع اللغات المطلوبة
            if (!tessBaseAPI.init(datapath, lang)) {
                Log.e(TAG, "Initialization of Tesseract failed.")
            } else {
                Log.d(TAG, "Tesseract initialized successfully.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Tesseract: ${e.message}")
        }
    }
    
    // **********************************************
    //  وظيفة النسخ: نسخ ملفات اللغة إلى مسار التخزين الداخلي للتطبيق
    // **********************************************
    private fun copyTessData() {
        val assetManager = appContext.assets
        val tessDataDir = File(datapath, "tessdata")
        
        if (!tessDataDir.exists() && !tessDataDir.mkdirs()) {
             Log.e(TAG, "Could not create tessdata directory")
             return
        }

        // قائمة بملفات اللغة التي يجب نسخها
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
                    Log.e(TAG, "Failed to copy $filename: ${e.message}")
                }
            }
        }
    }

    // **********************************************
    //  وظيفة OCR للصور
    // **********************************************
    suspend fun performOcr(imageUri: Uri): String = withContext(Dispatchers.IO) {
        if (!tessBaseAPI.isInitialized) {
            return@withContext "خطأ: لم يتم تهيئة محرك OCR بنجاح."
        }

        try {
            val bitmap = MediaStore.Images.Media.getBitmap(appContext.contentResolver, imageUri)
            
            // التأكد من أن الصورة هي RGB_565 أو ARGB_8888 (تنسيقات مدعومة)
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

    // **********************************************
    //  وظيفة OCR لملفات PDF
    // **********************************************
    suspend fun performOcrOnPdf(pdfUri: Uri): String = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return@withContext "خطأ: ميزة قراءة PDF تتطلب Android 5.0 (API 21) أو أعلى."
        }
        if (!tessBaseAPI.isInitialized) {
            return@withContext "خطأ: لم يتم تهيئة محرك OCR بنجاح."
        }

        val fullOcrResult = StringBuilder()
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        
        try {
            pfd = appContext.contentResolver.openFileDescriptor(pdfUri, "r")
            renderer = PdfRenderer(pfd!!)
            
            val pageCount = renderer.pageCount
            
            for (i in 0 until pageCount) {
                renderer.openPage(i).use { page ->
                    // إنشاء Bitmap لصفحة PDF (دقة 2X لتحسين نتائج OCR)
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