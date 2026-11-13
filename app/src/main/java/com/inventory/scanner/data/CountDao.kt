package com.inventory.scanner.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CountDao {
    @Insert
    suspend fun insert(c: CountEntity)

    @Query("SELECT * FROM counts ORDER BY timestamp DESC")
    fun getAll(): Flow<List<CountEntity>>

    @Query("SELECT * FROM counts")
    suspend fun allForExport(): List<CountEntity>

    @Query("DELETE FROM counts")
    suspend fun clearAll()
}
