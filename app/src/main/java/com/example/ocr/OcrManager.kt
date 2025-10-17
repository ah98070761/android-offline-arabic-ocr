package com.example.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.IOException
import android.graphics.pdf.PdfRenderer

class OcrManager(private val context: Context) {

    private val TAG = "OcrManager"

    // استخدام TextRecognizer من ML Kit مع الخيارات الافتراضية لدعم اللغات المتعددة (عربي، إنجليزي، إلخ)
    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    // معالجة الصورة لتحسين دقة التعرف على النصوص
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

    // معالجة الصور لاستخراج النصوص (عربي/إنجليزي)
    suspend fun performOcr(imageUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val bitmap = FileUtility.uriToBitmap(context, imageUri)
            if (bitmap == null) {
                Log.e(TAG, "فشل تحميل الصورة من URI: $imageUri")
                return@withContext "❌ فشل تحميل الصورة."
            }
            val preprocessed = preprocessBitmap(bitmap)
            val image = InputImage.fromBitmap(preprocessed, 0)
            val result = recognizer.process(image).await()
            val fullText = result.text.trim()
            bitmap.recycle()
            preprocessed.recycle()
            if (fullText.isBlank()) "⚠️ لم يتم العثور على أي نص في الصورة."
            else fullText
        } catch (e: IOException) {
            Log.e(TAG, "فشل تحميل الصورة: ${e.message}")
            "❌ فشل تحميل الصورة: ${e.message}"
        } catch (e: Exception) {
            Log.e(TAG, "خطأ OCR: ${e.message}")
            "❌ فشل في معالجة OCR: ${e.message}"
        }
    }

    // معالجة ملفات PDF
    suspend fun performOcrOnPdf(pdfUri: Uri): String = withContext(Dispatchers.IO) {
        val totalText = StringBuilder()
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null

        try {
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
            if (parcelFileDescriptor == null) return@withContext "❌ فشل فتح ملف PDF."

            pdfRenderer = PdfRenderer(parcelFileDescriptor)
            totalText.append("📘 بدأ تحليل ملف PDF (${pdfRenderer.pageCount} صفحة)\n\n")

            for (i in 0 until pdfRenderer.pageCount) {
                val page = pdfRenderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                val processedBitmap = preprocessBitmap(bitmap)
                val image = InputImage.fromBitmap(processedBitmap, 0)
                val result = recognizer.process(image).await()

                totalText.append("📄 صفحة ${i + 1}:\n")
                totalText.append(result.text.trim()).append("\n\n")

                page.close()
                bitmap.recycle()
                processedBitmap.recycle()
            }

            if (totalText.isBlank()) "⚠️ لم يتم العثور على نص في ملف PDF."
            else totalText.toString()

        } catch (e: Exception) {
            Log.e(TAG, "PDF OCR Error: ${e.message}")
            "❌ فشل في تحليل PDF: ${e.message}"
        } finally {
            pdfRenderer?.close()
            parcelFileDescriptor?.close()
        }
    }
}