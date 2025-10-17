package com.example.ocr

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.arabic.ArabicTextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import android.graphics.pdf.PdfRenderer

class OcrManager(private val context: Context) {

    private val TAG = "OcrManager"

    private val recognizer by lazy {
        TextRecognition.getClient(ArabicTextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private fun preprocessBitmap(original: Bitmap): Bitmap {
        val grayBitmap = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayBitmap)
        val paint = Paint()

        val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
        val contrast = 1.4f
        val brightness = -30f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )
        colorMatrix.postConcat(contrastMatrix)
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(original, 0f, 0f, paint)
        return grayBitmap
    }

    suspend fun performOcr(imageUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri))
            val preprocessed = preprocessBitmap(bitmap)
            val image = InputImage.fromBitmap(preprocessed, 0)
            val result = recognizer.process(image).await()
            result.text.trim().ifBlank { "⚠️ لم يتم العثور على أي نص في الصورة." }
        } catch (e: IOException) {
            Log.e(TAG, "فشل تحميل الصورة: ${e.message}")
            "❌ فشل تحميل الصورة: ${e.message}"
        } catch (e: Exception) {
            Log.e(TAG, "خطأ OCR: ${e.message}")
            "❌ فشل في معالجة OCR: ${e.message}"
        }
    }

    suspend fun performOcrOnPdf(pdfUri: Uri): String = withContext(Dispatchers.IO) {
        val totalText = StringBuilder()
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        try {
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            if (parcelFileDescriptor == null) return@withContext "❌ فشل فتح ملف PDF."
            pdfRenderer = PdfRenderer(parcelFileDescriptor)
            for (i in 0 until pdfRenderer.pageCount) {
                val page = pdfRenderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                val processedBitmap = preprocessBitmap(bitmap)
                val image = InputImage.fromBitmap(processedBitmap, 0)
                val result = recognizer.process(image).await()
                totalText.append("📄 صفحة ${i + 1}:\n${result.text.trim()}\n\n")
                page.close()
                bitmap.recycle()
            }
            if (totalText.isBlank()) "⚠️ لم يتم العثور على نص في ملف PDF." else totalText.toString()
        } catch (e: Exception) {
            Log.e(TAG, "PDF OCR Error: ${e.message}")
            "❌ فشل في تحليل PDF: ${e.message}"
        } finally {
            pdfRenderer?.close()
            parcelFileDescriptor?.close()
        }
    }
}