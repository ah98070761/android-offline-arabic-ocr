// app/src/main/java/com/example/ocr/data/OcrResult.kt (ملف جديد)
package com.example.ocr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ocr_results")
data class OcrResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)