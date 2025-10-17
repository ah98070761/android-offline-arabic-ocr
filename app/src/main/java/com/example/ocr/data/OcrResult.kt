package com.example.ocr.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "ocr_results")
data class OcrResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String
) : Parcelable