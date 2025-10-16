// app/src/main/java/com/example/ocr/data/OcrResultDao.kt (ملف جديد)
package com.example.ocr.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OcrResultDao {
    @Query("SELECT * FROM ocr_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<OcrResult>>

    @Insert
    suspend fun insert(result: OcrResult)

    @Delete
    suspend fun delete(result: OcrResult)
    
    @Query("DELETE FROM ocr_results")
    suspend fun deleteAll()
}