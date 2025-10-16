package com.example.ocr

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.arabic.ArabicTextRecognizerOptions // استيراد خيارات اللغة العربية
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await // لاستخدام .await() مع مهام ML Kit
import kotlinx.coroutines.withContext
import java.io.IOException

// 💡 الآن OcrManager يقبل السياق (Context) في الباني
class OcrManager(private val context: Context) {

    // ✅ تهيئة ML Kit لاستخدام نموذج اللغة العربية
    private val recognizer = TextRecognition.getClient(
        ArabicTextRecognizerOptions.Builder().build()
    )
    private val TAG = "OcrManager"

    // لا حاجة لـ init{} أو Companion Object لتحميل المكتبات أو نسخ ملفات اللغة.
    
    // ❌ إزالة دالة copyTessData()
    // ❌ إزالة دالة performOcrOnPdf() (لأن ML Kit لا يدعمها مباشرة)
    
    suspend fun performOcr(imageUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            // 1. إنشاء InputImage من URI باستخدام سياق التطبيق
            val image = InputImage.fromFilePath(context, imageUri)
            
            // 2. معالجة الصورة باستخدام ML Kit وانتظار النتيجة
            val result = recognizer.process(image).await() 
            
            val fullText = result.text.trim()

            if (fullText.isNullOrBlank()) {
                "لم يتم العثور على أي نص في الصورة."
            } else {
                "تمت عملية القراءة الضوئية بنجاح باستخدام ML Kit!\n--- نتيجة القراءة ---\n$fullText"
            }

        } catch (e: IOException) {
            Log.e(TAG, "Failed to load image: ${e.message}")
            "فشل في تحميل الصورة: ${e.message}"
        } catch (e: Exception) {
            Log.e(TAG, "ML Kit OCR error: ${e.message}")
            "فشل في معالجة OCR: ${e.message}"
        }
    }

    // ملاحظة حول PDF:
    // ML Kit لا يدعم معالجة ملفات PDF مباشرةً. إذا كنت بحاجة لدعم PDF، 
    // يجب عليك استخدام PdfRenderer لتحويل كل صفحة إلى صورة (Bitmap)، 
    // ثم استدعاء performOcr(bitmap) لكل صورة.
    suspend fun performOcrOnPdf(pdfUri: Uri): String {
        return "وظيفة معالجة ملفات PDF غير مدعومة حاليًا في إعداد ML Kit هذا."
    }
}