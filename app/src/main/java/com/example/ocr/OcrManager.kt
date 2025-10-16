package com.example.ocr

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions // استيراد الخيارات العامة والافتراضية
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await // لاستخدام .await() مع مهام ML Kit
import kotlinx.coroutines.withContext
import java.io.IOException

// 💡 الآن OcrManager يقبل السياق (Context) في الباني
class OcrManager(private val context: Context) {

    // ✅ التصحيح: استخدام الخيارات الافتراضية TextRecognizerOptions.DEFAULT_OPTIONS
    // هذه الخيارات تدعم النص اللاتيني والعديد من النصوص غير اللاتينية (مثل العربية) عبر خدمات Google Play.
    private val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )
    private val TAG = "OcrManager"

    // تم حذف init{} و ArabicTextRecognizerOptions، وحل مشكلة عدم العثور عليها

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
            // ملاحظة: قد تحتاج خدمات Google Play إلى تنزيل النموذج أولاً.
            "فشل في معالجة OCR: ${e.message}"
        }
    }

    // ملاحظة حول PDF:
    // ML Kit لا يدعم معالجة ملفات PDF مباشرةً. إذا كنت بحاجة لدعم PDF، 
    // يجب عليك استخدام PdfRenderer لتحويل كل صفحة إلى صورة (Bitmap)، 
    // ثم استدعاء performOcr(bitmap) لكل صورة.
    suspend fun performOcrOnPdf(pdfUri: Uri): String {
        // يمكنك هنا تضمين كود معالجة PDF باستخدام PdfRenderer ثم استدعاء performOcr(Bitmap)
        return "وظيفة معالجة ملفات PDF غير مدعومة حاليًا في إعداد ML Kit هذا."
    }
}