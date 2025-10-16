package com.example.ocr

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions // ✅ ضروري
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException

class OcrManager(private val context: Context) {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) // ✅ الحل
    }

    private val TAG = "OcrManager"

    suspend fun performOcr(imageUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(image).await()
            val fullText = result.text.trim()

            if (fullText.isBlank()) {
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

    suspend fun performOcrOnPdf(pdfUri: Uri): String {
        return "وظيفة معالجة ملفات PDF غير مدعومة حاليًا في إعداد ML Kit هذا."
    }
}