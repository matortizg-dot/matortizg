package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {
    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_logs WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getLogsBetween(startTime: Long, endTime: Long): Flow<List<FoodLogEntity>>

    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecentLogsList(): List<FoodLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FoodLogEntity): Long

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM food_logs")
    suspend fun clearAll()
}
