package com.example.ocr

import android.content.Context
import android.net.Uri
import android.util.Log
// دعم PDF
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
// ML Kit
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.arabic.ArabicTextRecognizerOptions // استيراد خيارات اللغة العربية
// Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await 
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * فئة لإدارة عمليات التعرف على النصوص (OCR) باستخدام ML Kit.
 * تدعم التعرف على النصوص باللغة العربية ومعالجة ملفات PDF.
 */
class OcrManager(private val context: Context) {

    // تهيئة مُعرّف النصوص باستخدام خيارات اللغة العربية.
    // يتم استخدام "by lazy" لضمان التهيئة الآمنة وتفادي أخطاء مثل "p0".
    private val recognizer by lazy {
        TextRecognition.getClient(ArabicTextRecognizerOptions.Builder().build()) 
    }

    private val TAG = "OcrManager"

    /**
     * إجراء OCR على صورة واحدة.
     */
    suspend fun performOcr(imageUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(image).await()
            val fullText = result.text.trim()

            if (fullText.isBlank()) {
                "لم يتم العثور على أي نص في الصورة."
            } else {
                fullText // إرجاع النص الخام ليتم حفظه
            }

        } catch (e: IOException) {
            Log.e(TAG, "Failed to load image: ${e.message}")
            "فشل في تحميل الصورة: ${e.message}"
        } catch (e: Exception) {
            Log.e(TAG, "ML Kit OCR error: ${e.message}")
            "فشل في معالجة OCR: ${e.message}"
        }
    }

    /**
     * إجراء OCR على ملف PDF عن طريق معالجة كل صفحة كصورة.
     */
    suspend fun performOcrOnPdf(pdfUri: Uri): String = withContext(Dispatchers.IO) {
        val totalText = StringBuilder()
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null

        try {
            // فتح الملف بوصف للملف
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            if (parcelFileDescriptor == null) return@withContext "فشل فتح ملف PDF."
            
            pdfRenderer = PdfRenderer(parcelFileDescriptor)
            
            totalText.append("--- تم بدء معالجة PDF (عدد الصفحات: ${pdfRenderer.pageCount}) ---\n\n")

            for (i in 0 until pdfRenderer.pageCount) {
                val page = pdfRenderer.openPage(i)
                
                // إنشاء صورة Bitmap
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                
                // تحويل الـ Bitmap إلى InputImage وإجراء OCR
                val image = InputImage.fromBitmap(bitmap, 0)
                val result = recognizer.process(image).await()
                
                totalText.append("--- صفحة ${i + 1} ---\n")
                totalText.append(result.text.trim()).append("\n\n")

                page.close()
                bitmap.recycle()
            }
            
            if (totalText.isBlank()) {
                "تمت معالجة ملف PDF بالكامل ولكن لم يتم العثور على أي نص."
            } else {
                totalText.toString() // إرجاع النص المجمع ليتم حفظه
            }

        } catch (e: Exception) {
            Log.e(TAG, "PDF OCR error: ${e.message}")
            "فشل في معالجة OCR لملف PDF: ${e.message}"
        } finally {
            // ضمان إغلاق الموارد
            pdfRenderer?.close()
            parcelFileDescriptor?.close()
        }
    }
}
