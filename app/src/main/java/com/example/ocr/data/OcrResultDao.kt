package com.example.ocr.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OcrResultDao {
    @Insert
    suspend fun insert(result: OcrResult)

    @Query("SELECT * FROM ocr_results ORDER BY id DESC")
    fun getAllResults(): Flow<List<OcrResult>>

    @Query("DELETE FROM ocr_results")
    suspend fun deleteAll()
}